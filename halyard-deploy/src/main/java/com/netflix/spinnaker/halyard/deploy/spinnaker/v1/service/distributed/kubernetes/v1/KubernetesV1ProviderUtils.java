/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v1;

import com.netflix.spinnaker.clouddriver.kubernetes.v1.security.KubernetesConfigParser;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.job.v1.JobExecutor;
import com.netflix.spinnaker.halyard.core.job.v1.JobRequest;
import com.netflix.spinnaker.halyard.core.job.v1.JobStatus;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskInterrupted;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class KubernetesV1ProviderUtils {
  // Map from deployment name -> the port & job managing the connection.
  private static ConcurrentHashMap<String, Proxy> proxyMap = new ConcurrentHashMap<>();

  @Data
  static class Proxy {
    String jobId;
    Integer port;

    static String buildKey(String deployment) {
      return String.format("%d:%s", Thread.currentThread().getId(), deployment);
    }
  }

  static URI proxyServiceEndpoint(Proxy proxy, String namespace, String serviceName, int servicePort) {
    try {
      return new URIBuilder().setPort(proxy.getPort())
          .setHost("localhost")
          .setScheme("http")
          .setPath("/api/v1/namespaces/" + namespace + "/services/" + serviceName + ":" + servicePort + "/proxy")
          .build();
    } catch (URISyntaxException e) {
      throw new HalException(Severity.FATAL, "Malformed service details: " + e.getMessage());
    }
  }

  static Proxy openProxy(JobExecutor jobExecutor, AccountDeploymentDetails<KubernetesAccount> details) {
    KubernetesAccount account = details.getAccount();
    Proxy proxy = proxyMap.getOrDefault(Proxy.buildKey(details.getDeploymentName()), new Proxy());
    String jobId = proxy.jobId;
    if (StringUtils.isEmpty(jobId) || !jobExecutor.jobExists(jobId)) {
      DaemonTaskHandler.newStage("Connecting to the Kubernetes cluster in account \"" + account.getName() + "\"");
      List<String> command = kubectlAccountCommand(details);
      command.add("proxy");
      command.add("--port=0"); // select a random port
      JobRequest request = new JobRequest().setTokenizedCommand(command);

      proxy.jobId = jobExecutor.startJob(request);

      JobStatus status = jobExecutor.updateJob(proxy.jobId);

      while (status == null) {
        DaemonTaskHandler.safeSleep(TimeUnit.SECONDS.toMillis(2));
        status = jobExecutor.updateJob(proxy.jobId);
      }

      // This should be a long-running job.
      if (status.getState() == JobStatus.State.COMPLETED) {
        throw new HalException(Severity.FATAL,
            "Unable to establish a proxy against account " + account.getName()
                + ":\n" + status.getStdOut() + "\n" + status.getStdErr());
      }

      String connectionMessage = status.getStdOut();
      Pattern portPattern = Pattern.compile(":(\\d+)");
      Matcher matcher = portPattern.matcher(connectionMessage);
      if (matcher.find()) {
        proxy.setPort(Integer.valueOf(matcher.group(1)));
        proxyMap.put(Proxy.buildKey(details.getDeploymentName()), proxy);
        DaemonTaskHandler.message("Connected to kubernetes cluster for account "
            + account.getName() + " on port " + proxy.getPort());
        DaemonTaskHandler.message("View the kube ui on http://localhost:" + proxy.getPort() + "/ui/");
      } else {
        throw new HalException(Severity.FATAL,
            "Could not parse connection information from:\n"
                + connectionMessage + "(" + status.getStdErr() + ")");
      }
    }

    return proxy;
  }

  static KubernetesClient getClient(AccountDeploymentDetails<KubernetesAccount> details) {
    KubernetesAccount account = details.getAccount();
    Config config = KubernetesConfigParser.parse(account.getKubeconfigFile(),
        account.getContext(),
        account.getCluster(),
        account.getUser(),
        account.getNamespaces(),
        account.usesServiceAccount());

    return new DefaultKubernetesClient(config);
  }

  static void resize(AccountDeploymentDetails<KubernetesAccount> details, String namespace, String replicaSetName, int targetSize) {
    KubernetesClient client = getClient(details);
    client.extensions().replicaSets().inNamespace(namespace).withName(replicaSetName).scale(targetSize);
  }

  static void upsertSecret(AccountDeploymentDetails<KubernetesAccount> details, Set<Pair<File, String>> files, String secretName, String namespace) {
    KubernetesClient client = getClient(details);

    if (client.secrets().inNamespace(namespace).withName(secretName).get() != null) {
      client.secrets().inNamespace(namespace).withName(secretName).delete();
    }

    Map<String, String> secretContents = new HashMap<>();

    files.forEach(pair -> {
      try {
        File file = pair.getLeft();
        String name = pair.getRight();
        String data = new String(Base64.getEncoder().encode(IOUtils.toByteArray(new FileInputStream(file))));

        secretContents.putIfAbsent(name, data);
      } catch (IOException e) {
        throw new HalException(Severity.ERROR, "Unable to read contents of \"" + pair.getLeft() + "\": " + e);
      }
    });

    SecretBuilder secretBuilder = new SecretBuilder();
    secretBuilder = secretBuilder.withNewMetadata()
        .withName(secretName)
        .withNamespace(namespace)
        .endMetadata()
        .withData(secretContents);

    client.secrets().inNamespace(namespace).create(secretBuilder.build());
  }

  static void createNamespace(AccountDeploymentDetails<KubernetesAccount> details, String namespace) {
    KubernetesClient client = getClient(details);
    if (client.namespaces().withName(namespace).get() == null) {
      client.namespaces().create(new NamespaceBuilder()
          .withNewMetadata()
          .withName(namespace)
          .endMetadata()
          .build());
    }
  }

  static void deleteReplicaSet(AccountDeploymentDetails<KubernetesAccount> details, String namespace, String name) {
    getClient(details).extensions().replicaSets().inNamespace(namespace).withName(name).delete();
  }

  private static String halSecret(String name, Integer version, String component) {
    return String.join("-", "hal", name, version + "", component);
  }

  static String componentSecret(String name, Integer version) {
    return halSecret(name, version, "profiles");
  }

  static String componentMonitoring(String name, Integer version) {
    return halSecret(name, version, "monitoring");
  }

  static String componentRegistry(String name, Integer version) {
    return halSecret(name, version, "registry");
  }

  static String componentDependencies(String name, Integer version) {
    return halSecret(name, version, "dependencies");
  }

  static List<String> kubectlPortForwardCommand(AccountDeploymentDetails<KubernetesAccount> details, String namespace, String instance, int targetPort, int localPort) {
    List<String> command =  kubectlAccountCommand(details);
    command.add("--namespace");
    command.add(namespace);

    command.add("port-forward");
    command.add(instance);

    command.add(localPort + ":" + targetPort);
    return command;
  }

  static void kubectlDeleteNamespaceCommand(JobExecutor jobExecutor, AccountDeploymentDetails<KubernetesAccount> details, String namespace) {
    List<String> command = kubectlAccountCommand(details);
    command.add("delete");
    command.add("namespace");
    command.add(namespace);
    JobRequest request = new JobRequest().setTokenizedCommand(command);

    try {
      jobExecutor.backoffWait(jobExecutor.startJob(request));
    } catch (InterruptedException e) {
      throw new DaemonTaskInterrupted(e);
    }
  }

  static void storeInstanceLogs(JobExecutor jobExecutor, AccountDeploymentDetails<KubernetesAccount> details, String namespace, String instanceName, String containerName, File outputFile) {
    List<String> command = kubectlAccountCommand(details);
    command.add("--namespace");
    command.add(namespace);
    command.add("logs");
    command.add(instanceName);
    command.add(containerName);

    JobRequest request = new JobRequest().setTokenizedCommand(command);

    JobStatus status;
    try {
      status = jobExecutor.backoffWait(jobExecutor.startJob(request));
    } catch (InterruptedException e) {
      throw new DaemonTaskInterrupted(e);
    }

    try {
      IOUtils.write(status.getStdOut().getBytes(), new FileOutputStream(new File(outputFile, containerName)));
    } catch (IOException e) {
      throw new HalException(Severity.FATAL, "Unable to store logs: " + e.getMessage(), e);
    }
  }

  private static List<String> kubectlAccountCommand(AccountDeploymentDetails<KubernetesAccount> details) {
    KubernetesAccount account = details.getAccount();
    List<String> command = new ArrayList<>();
    command.add("kubectl");

    String context = account.getContext();
    if (context != null && !context.isEmpty()) {
      command.add("--context");
      command.add(context);
    }

    String cluster = account.getCluster();
    if (cluster != null && !cluster.isEmpty()) {
      command.add("--cluster");
      command.add(cluster);
    }

    String user = account.getUser();
    if (user != null && !user.isEmpty()) {
      command.add("--user");
      command.add(user);
    }

    String kubeconfig = account.getKubeconfigFile();
    if (kubeconfig != null && !kubeconfig.isEmpty()) {
      command.add("--kubeconfig");
      command.add(kubeconfig);
    }

    return command;
  }
}
