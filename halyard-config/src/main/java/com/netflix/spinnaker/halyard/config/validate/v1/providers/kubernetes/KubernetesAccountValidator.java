/*
 * Copyright 2016 Google, Inc.
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
 */

package com.netflix.spinnaker.halyard.config.validate.v1.providers.kubernetes;

import com.netflix.spinnaker.clouddriver.kubernetes.v1.security.KubernetesConfigParser;
import com.netflix.spinnaker.halyard.core.secrets.v1.SecretSessionManager;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.node.Node;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.containers.DockerRegistryReference;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemBuilder;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.config.validate.v1.util.ValidatingFileReader;
import com.netflix.spinnaker.halyard.core.job.v1.JobExecutor;
import com.netflix.spinnaker.halyard.core.job.v1.JobRequest;
import com.netflix.spinnaker.halyard.core.job.v1.JobStatus;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskInterrupted;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.netflix.spinnaker.halyard.config.validate.v1.providers.dockerRegistry.DockerRegistryReferenceValidation.validateDockerRegistries;
import static com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity.ERROR;
import static com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity.FATAL;
import static com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity.WARNING;

import com.netflix.spinnaker.config.secrets.EncryptedSecret;

@Component
public class KubernetesAccountValidator extends Validator<KubernetesAccount> {
  @Autowired
  private SecretSessionManager secretSessionManager;

  @Override
  public void validate(ConfigProblemSetBuilder psBuilder, KubernetesAccount account) {
    DeploymentConfiguration deploymentConfiguration;

    // TODO(lwander) this is still a little messy - I should use the filters to get the necessary docker account
    Node parent = account.getParent();
    while (!(parent instanceof DeploymentConfiguration)) {
      // Note this will crash in the above check if the halconfig representation is corrupted
      // (that's ok, because it indicates a more serious error than we want to validate).
      parent = parent.getParent();
    }
    deploymentConfiguration = (DeploymentConfiguration) parent;

    validateKindConfig(psBuilder, account);

    // TODO(lwander) validate all config with clouddriver's v2 creds
    switch (account.getProviderVersion()) {
      case V1:
        final List<String> dockerRegistryNames = account.getDockerRegistries().stream().map(DockerRegistryReference::getAccountName)
            .collect(Collectors.toList());
        validateDockerRegistries(psBuilder, deploymentConfiguration, dockerRegistryNames, Provider.ProviderType.KUBERNETES);
        validateKubeconfig(psBuilder, account);
        validateOnlySpinnakerConfig(psBuilder, account);
      case V2:
        break;
      default:
        throw new IllegalStateException("Unknown provider version " + account.getProviderVersion());

    }
  }

  private void validateKindConfig(ConfigProblemSetBuilder psBuilder, KubernetesAccount account) {
    List<String> kinds = account.getKinds();
    List<String> omitKinds = account.getOmitKinds();
    List<KubernetesAccount.CustomKubernetesResource> customResources = account.getCustomResources();

    if (account.getProviderVersion() == Provider.ProviderVersion.V1) {
      if (CollectionUtils.isNotEmpty(kinds) || CollectionUtils.isNotEmpty(omitKinds) || CollectionUtils.isNotEmpty(customResources)) {
        psBuilder.addProblem(WARNING, "Kubernetes accounts at V1 do no support configuring caching behavior for kinds or custom resources.");
      }

      return;
    }

    if (CollectionUtils.isNotEmpty(kinds) && CollectionUtils.isNotEmpty(omitKinds)) {
      psBuilder.addProblem(ERROR, "At most one of \"kinds\" and \"omitKinds\" may be specified.");
    }

    if (CollectionUtils.isNotEmpty(kinds) && CollectionUtils.isNotEmpty(customResources)) {
      List<String> unmatchedKinds = customResources.stream()
          .map(KubernetesAccount.CustomKubernetesResource::getKubernetesKind)
          .filter(cr -> !kinds.contains(cr))
          .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(unmatchedKinds)) {
        psBuilder.addProblem(WARNING, "The following custom resources \"" + customResources + "\" will not be cached since they aren't listed in your existing resource kinds configuration: \"" + kinds + "\".");
      }
    }

    if (CollectionUtils.isNotEmpty(omitKinds) && CollectionUtils.isNotEmpty(customResources)) {
      List<String> matchedKinds = customResources.stream()
          .map(KubernetesAccount.CustomKubernetesResource::getKubernetesKind)
          .filter(omitKinds::contains)
          .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(matchedKinds)) {
        psBuilder.addProblem(WARNING, "The following custom resources \"" + customResources + "\" will not be cached since they are listed in you've omitted them in you omitKinds configuration: \"" + omitKinds + "\".");
      }
    }
  }

  private void validateOnlySpinnakerConfig(ConfigProblemSetBuilder psBuilder, KubernetesAccount account) {
    Boolean onlySpinnakerManaged = account.getOnlySpinnakerManaged();

    if (account.getProviderVersion() == Provider.ProviderVersion.V1) {
      if (onlySpinnakerManaged) {
        psBuilder.addProblem(WARNING, "Kubernetes accounts at V1 does not support configuring caching behavior for a only spinnaker managed resources.");
      }
    }
  }

  private void validateKubeconfig(ConfigProblemSetBuilder psBuilder, KubernetesAccount account) {
    io.fabric8.kubernetes.api.model.Config kubeconfig;
    String context = account.getContext();
    String cluster = account.getCluster();
    String user = account.getUser() ;
    List<String> namespaces = account.getNamespaces();
    List<String> omitNamespaces = account.getOmitNamespaces();

    // This indicates if a first pass at the config looks OK. If we don't see any serious problems, we'll do one last check
    // against the requested kubernetes cluster to ensure that we can run spinnaker.
    boolean smoketest = true;

    boolean namespacesProvided = namespaces != null && !namespaces.isEmpty();
    boolean omitNamespacesProvided = omitNamespaces != null && !omitNamespaces.isEmpty();

    if (namespacesProvided && omitNamespacesProvided) {
      psBuilder.addProblem(ERROR, "At most one of \"namespaces\" and \"omitNamespaces\" can be supplied.");
      smoketest = false;
    }

    // TODO(lwander) find a good resource / list of resources for generating kubeconfig files to link to here.
    try {
      String kubeconfigContents;
      if (EncryptedSecret.isEncryptedSecret(account.getKubeconfigFile())) {
        kubeconfigContents = secretSessionManager.decrypt(account.getKubeconfigFile());
      } else {
        kubeconfigContents = ValidatingFileReader.contents(psBuilder, account.getKubeconfigFile());
      }

      if (kubeconfigContents == null) {
        return;
      }

      kubeconfig = KubeConfigUtils.parseConfigFromString(kubeconfigContents);
    } catch (IOException e) {
      psBuilder.addProblem(ERROR, e.getMessage());
      return;
    }

    System.out.println(context);
    if (context != null && !context.isEmpty()) {
      Optional<NamedContext> namedContext = kubeconfig
          .getContexts()
          .stream()
          .filter(c -> c.getName().equals(context))
          .findFirst();

      if (!namedContext.isPresent()) {
        psBuilder.addProblem(ERROR, "Context \"" + context + "\" not found in kubeconfig \"" + account.getKubeconfigFile() + "\".", "context")
            .setRemediation("Either add this context to your kubeconfig, rely on the default context, or pick another kubeconfig file.");
        smoketest = false;
      }
    } else {
      String currentContext = kubeconfig.getCurrentContext();
      if (StringUtils.isEmpty(currentContext)) {
        psBuilder.addProblem(ERROR, "You have not specified a Kubernetes context, and your kubeconfig \"" + account.getKubeconfigFile() + "\" has no current-context.", "context")
            .setRemediation("Either specify a context in your halconfig, or set a current-context in your kubeconfig.");
        smoketest = false;
      } else {
        psBuilder.addProblem(WARNING, "You have not specified a Kubernetes context in your halconfig, Spinnaker will use \"" + currentContext + "\" instead.", "context")
            .setRemediation("We recommend explicitly setting a context in your halconfig, to ensure changes to your kubeconfig won't break your deployment.");
      }
    }

    if (smoketest) {
      Config config = KubernetesConfigParser.parse(secretSessionManager.decryptAsFile(account.getKubeconfigFile()), context, cluster, user, namespaces, false);
      try {
        KubernetesClient client = new DefaultKubernetesClient(config);

        client.namespaces().list();
      } catch (Exception e) {
        ConfigProblemBuilder pb = psBuilder.addProblem(ERROR, "Unable to communicate with your Kubernetes cluster: " + e.getMessage() + ".");

        if (e.getMessage().contains("Token may have expired")) {
          pb.setRemediation("If you downloaded these keys with gcloud, it's possible they are in the wrong format. To fix this, run \n\n"
              + "gcloud config set container/use_client_certificate true\n\ngcloud container clusters get-credentials $CLUSTERNAME");
        } else {
          pb.setRemediation("Unable to authenticate with your Kubernetes cluster. Try using kubectl to verify your credentials.");
        }
      }
    }
  }

  public void ensureKubectlExists(ConfigProblemSetBuilder p) {
    JobExecutor jobExecutor = DaemonTaskHandler.getJobExecutor();
    JobRequest request = new JobRequest()
        .setTokenizedCommand(Collections.singletonList("kubectl"))
        .setTimeoutMillis(TimeUnit.SECONDS.toMillis(10));

    JobStatus status;
    try {
      status = jobExecutor.backoffWait(jobExecutor.startJob(request));
    } catch (InterruptedException e) {
      throw new DaemonTaskInterrupted(e);
    }

    if (status.getResult() != JobStatus.Result.SUCCESS) {
      p.addProblem(FATAL, String.join(" ", "`kubectl` not installed, or can't be found by Halyard. It is needed for",
          "opening connections to your Kubernetes cluster to send commands to the Spinnaker deployment running there."))
          .setRemediation(String.join(" ", "Visit https://kubernetes.io/docs/tasks/kubectl/install/.",
              "If you've already installed kubectl via gcloud, it's possible updates to your $PATH aren't visible to Halyard. ",
              "You might have to restart Halyard for it to pick up the new $PATH."));
    }
  }
}