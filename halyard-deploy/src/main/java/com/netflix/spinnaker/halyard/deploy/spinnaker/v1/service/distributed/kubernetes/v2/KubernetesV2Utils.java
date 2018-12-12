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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.job.v1.JobRequest;
import com.netflix.spinnaker.halyard.core.job.v1.JobStatus;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.resource.v1.JinjaJarResource;
import com.netflix.spinnaker.halyard.core.resource.v1.TemplatedResource;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskInterrupted;
import java.util.Arrays;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class KubernetesV2Utils {
  static private ObjectMapper mapper = new ObjectMapper();

  static public boolean exists(KubernetesAccount account, String manifest) {
    Map<String, Object> parsedManifest = parseManifest(manifest);
    String kind = (String) parsedManifest.get("kind");
    Map<String, Object> metadata = (Map<String, Object>) parsedManifest.getOrDefault("metadata", new HashMap<>());
    String name = (String) metadata.get("name");
    String namespace = (String) metadata.get("namespace");

    return exists(account, namespace, kind, name);
  }

  static private boolean exists(KubernetesAccount account, String namespace, String kind, String name) {
    log.info("Checking for " + kind + "/" + name);
    List<String> command = kubectlPrefix(account);

    if (StringUtils.isNotEmpty(namespace)) {
      command.add("-n");
      command.add(namespace);
    }

    command.add("get");
    command.add(kind);
    command.add(name);

    JobRequest request = new JobRequest().setTokenizedCommand(command);

    String jobId = DaemonTaskHandler.getJobExecutor().startJob(request);

    JobStatus status;
    try {
      status = DaemonTaskHandler.getJobExecutor().backoffWait(jobId);
    } catch (InterruptedException e) {
      throw new DaemonTaskInterrupted(e);
    }

    if (status.getState() != JobStatus.State.COMPLETED) {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Unterminated check for " + kind + "/" + name + " in " + namespace,
          status.getStdErr(),
          status.getStdOut()));
    }

    if (status.getResult() == JobStatus.Result.SUCCESS) {
      return true;
    } else if (status.getStdErr().contains("NotFound")) {
      return false;
    } else {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Failed check for " + kind + "/" + name + " in " + namespace,
          status.getStdErr(),
          status.getStdOut()));
    }
  }

  static public boolean isReady(KubernetesAccount account, String namespace, String service) {
    log.info("Checking readiness for " + service);
    List<String> command = kubectlPrefix(account);

    if (StringUtils.isNotEmpty(namespace)) {
      command.add("-n=" + namespace);
    }

    command.add("get");
    command.add("po");

    command.add("-l=cluster=" + service);
    command.add("-o=jsonpath='{.items[*].status.containerStatuses[*].ready}'");
    // This command returns a space-separated string of true/false values indicating whether each of
    // the pod's containers are READY.
    // e.g., if we are querying two spin-orca pods and both pods' monitoring-daemon containers are
    // READY but the orca containers are not READY, the output may be 'true false true false'.

    JobRequest request = new JobRequest().setTokenizedCommand(command);

    String jobId = DaemonTaskHandler.getJobExecutor().startJob(request);

    JobStatus status;
    try {
      status = DaemonTaskHandler.getJobExecutor().backoffWait(jobId);
    } catch (InterruptedException e) {
      throw new DaemonTaskInterrupted(e);
    }

    if (status.getState() != JobStatus.State.COMPLETED) {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Unterminated readiness check for " + service + " in " + namespace,
          status.getStdErr(),
          status.getStdOut()));
    }

    if (status.getResult() == JobStatus.Result.SUCCESS) {
      String readyStatuses = status.getStdOut();
      if (readyStatuses.isEmpty()) {
        return false;
      }
      readyStatuses = readyStatuses.substring(1, readyStatuses.length() - 1); // Strip leading and trailing single quote
      if (readyStatuses.isEmpty()) {
        return false;
      }
      return Arrays.stream(readyStatuses.split(" "))
          .allMatch(s -> s.equals("true"));
    } else {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Failed readiness check for " + service + " in " + namespace,
          status.getStdErr(),
          status.getStdOut()));
    }
  }

  static public void deleteSpinnaker(KubernetesAccount account, String namespace) {
    List<String> command = kubectlPrefix(account);
    if (StringUtils.isNotEmpty(namespace)) {
      command.add("-n=" + namespace);
    }

    command.add("delete");
    command.add("deploy,svc,secret");
    command.add("-l=app=spin");

    JobRequest request = new JobRequest().setTokenizedCommand(command);

    String jobId = DaemonTaskHandler.getJobExecutor().startJob(request);

    JobStatus status;
    try {
      status = DaemonTaskHandler.getJobExecutor().backoffWait(jobId);
    } catch (InterruptedException e) {
      throw new DaemonTaskInterrupted(e);
    }

    if (status.getState() != JobStatus.State.COMPLETED) {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Deleting spinnaker never completed in " + namespace,
          status.getStdErr(),
          status.getStdOut()));
    }

    if (status.getResult() != JobStatus.Result.SUCCESS) {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Deleting spinnaker failed in " + namespace,
          status.getStdErr(),
          status.getStdOut()));
    }
  }

  static public void delete(KubernetesAccount account, String namespace, String service) {
    List<String> command = kubectlPrefix(account);
    if (StringUtils.isNotEmpty(namespace)) {
      command.add("-n=" + namespace);
    }

    command.add("delete");
    command.add("deploy,svc,secret");
    command.add("-l=cluster=" + service);

    JobRequest request = new JobRequest().setTokenizedCommand(command);

    String jobId = DaemonTaskHandler.getJobExecutor().startJob(request);

    JobStatus status;
    try {
      status = DaemonTaskHandler.getJobExecutor().backoffWait(jobId);
    } catch (InterruptedException e) {
      throw new DaemonTaskInterrupted(e);
    }

    if (status.getState() != JobStatus.State.COMPLETED) {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Deleting service " + service + " never completed",
          status.getStdErr(),
          status.getStdOut()));
    }

    if (status.getResult() != JobStatus.Result.SUCCESS) {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Deleting service " + service + " failed",
          status.getStdErr(),
          status.getStdOut()));
    }
  }

  static public void apply(KubernetesAccount account, String manifest) {
    manifest = prettify(manifest);
    List<String> command = kubectlPrefix(account);
    command.add("apply");
    command.add("-f");
    command.add("-"); // read from stdin

    JobRequest request = new JobRequest().setTokenizedCommand(command);

    ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    String jobId = DaemonTaskHandler.getJobExecutor().startJob(request,
        System.getenv(),
        new ByteArrayInputStream(manifest.getBytes()),
        stdout,
        stderr);

    JobStatus status;
    try {
      status = DaemonTaskHandler.getJobExecutor().backoffWait(jobId);
    } catch (InterruptedException e) {
      throw new DaemonTaskInterrupted(e);
    }

    if (status.getState() != JobStatus.State.COMPLETED) {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Unterminated deployment of manifest:",
          manifest,
          stderr.toString(),
          stdout.toString()));
    }

    if (status.getResult() != JobStatus.Result.SUCCESS) {
      throw new HalException(Problem.Severity.FATAL, String.join("\n",
          "Failed to deploy manifest:",
          manifest,
          stderr.toString(),
          stdout.toString()));
    }
  }

  static public String createSecret(KubernetesAccount account, String namespace, String clusterName, String name, List<SecretMountPair> files) {
    Map<String, String> contentMap = new HashMap<>();
    for (SecretMountPair pair: files) {
      String contents;
      try {
        contents = new String(Base64.getEncoder().encode(IOUtils.toByteArray(new FileInputStream(pair.getContents()))));
      } catch (IOException e) {
        throw new HalException(Problem.Severity.FATAL, "Failed to read required config file: " + pair.getContents().getAbsolutePath() + ": " + e.getMessage(), e);
      }

      contentMap.put(pair.getName(), contents);
    }

    name = name + "-" + Math.abs(contentMap.hashCode());

    TemplatedResource secret = new JinjaJarResource("/kubernetes/manifests/secret.yml");
    Map<String, Object> bindings = new HashMap<>();

    bindings.put("files", contentMap);
    bindings.put("name", name);
    bindings.put("namespace", namespace);
    bindings.put("clusterName", clusterName);

    secret.extendBindings(bindings);

    apply(account, secret.toString());

    return name;
  }

  private static List<String> kubectlPrefix(KubernetesAccount account) {
    List<String> command = new ArrayList<>();
    command.add("kubectl");

    if (account.usesServiceAccount()) {
      return command;
    }

    String context = account.getContext();
    if (context != null && !context.isEmpty()) {
      command.add("--context");
      command.add(context);
    }

    String kubeconfig = account.getKubeconfigFile();
    if (kubeconfig != null && !kubeconfig.isEmpty()) {
      command.add("--kubeconfig");
      command.add(kubeconfig);
    }

    return command;
  }

  static List<String> kubectlPodServiceCommand(KubernetesAccount account, String namespace, String service) {
    List<String> command = kubectlPrefix(account);

    if (StringUtils.isNotEmpty(namespace)) {
      command.add("-n=" + namespace);
    }

    command.add("get");
    command.add("po");

    command.add("-l=cluster=" + service);
    command.add("-o=jsonpath='{.items[0].metadata.name}'");

    return command;
  }

  static List<String> kubectlConnectPodCommand(KubernetesAccount account, String namespace, String name, int port) {
    List<String> command = kubectlPrefix(account);

    if (StringUtils.isNotEmpty(namespace)) {
      command.add("-n=" + namespace);
    }

    command.add("port-forward");
    command.add(name);
    command.add(port + "");

    return command;
  }

  static private String prettify(String input) {
    Yaml yaml = new Yaml(new SafeConstructor());
    return yaml.dump(yaml.load(input));
  }

  static private Map<String, Object> parseManifest(String input) {
    Yaml yaml = new Yaml(new SafeConstructor());
    return mapper.convertValue(yaml.load(input), new TypeReference<Map<String, Object>>() {});
  }

  @Data
  static public class SecretMountPair {
    File contents;
    String name;

    public SecretMountPair(File inputFile) {
      this(inputFile, inputFile);
    }

    public SecretMountPair(File inputFile, File outputFile) {
      this.contents = inputFile;
      this.name = outputFile.getName();
    }
  }
}
