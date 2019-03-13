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

package com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.spinnaker.config.secrets.EncryptedSecret;
import com.netflix.spinnaker.halyard.config.config.v1.ArtifactSourcesConfig;
import com.netflix.spinnaker.halyard.core.secrets.v1.SecretSessionManager;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.node.LocalFile;
import com.netflix.spinnaker.halyard.config.model.v1.node.SecretFile;
import com.netflix.spinnaker.halyard.config.model.v1.node.ValidForSpinnakerVersion;
import com.netflix.spinnaker.halyard.config.model.v1.providers.containers.ContainerAccount;
import com.netflix.spinnaker.halyard.config.model.v1.providers.dockerRegistry.DockerRegistryProvider;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity.ERROR;

@Data
@EqualsAndHashCode(callSuper = true)
public class KubernetesAccount extends ContainerAccount implements Cloneable {
  String context;
  String cluster;
  String user;
  @ValidForSpinnakerVersion(lowerBound = "1.5.0", tooLowMessage = "Spinnaker does not support configuring this behavior before that version.")
  Boolean configureImagePullSecrets;
  Boolean serviceAccount;
  int cacheThreads = 1;
  List<String> namespaces = new ArrayList<>();
  List<String> omitNamespaces = new ArrayList<>();
  @ValidForSpinnakerVersion(lowerBound = "1.7.0", tooLowMessage = "Configuring kind caching behavior is not supported yet.")
  List<String> kinds = new ArrayList<>();
  @ValidForSpinnakerVersion(lowerBound = "1.7.0", tooLowMessage = "Configuring kind caching behavior is not supported yet.")
  List<String> omitKinds = new ArrayList<>();
  @ValidForSpinnakerVersion(lowerBound = "1.6.0", tooLowMessage = "Custom kinds and resources are not supported yet.")
  List<CustomKubernetesResource> customResources = new ArrayList<>();
  @ValidForSpinnakerVersion(lowerBound = "1.8.0", tooLowMessage = "Caching policies are not supported yet.")
  List<KubernetesCachingPolicy> cachingPolicies = new ArrayList<>();

  @LocalFile @SecretFile String kubeconfigFile;
  String kubeconfigContents;
  String kubectlPath;
  Integer kubectlRequestTimeoutSeconds;
  Boolean checkPermissionsOnStartup;
  Boolean liveManifestCalls;

  // Without the annotations, these are written as `oauthServiceAccount` and `oauthScopes`, respectively.
  @JsonProperty("oAuthServiceAccount") @LocalFile @SecretFile String oAuthServiceAccount;
  @JsonProperty("oAuthScopes") List<String> oAuthScopes;
  String namingStrategy;
  String skin;
  @JsonProperty("onlySpinnakerManaged") Boolean onlySpinnakerManaged;
  Boolean debug;

  @Autowired
  private SecretSessionManager secretSessionManager;

  public boolean usesServiceAccount() {
    return serviceAccount != null && serviceAccount;
  }

  public String getKubeconfigFile() {
    if (usesServiceAccount()) {
      return null;
    }

    if (kubeconfigFile == null || kubeconfigFile.isEmpty()) {
      return System.getProperty("user.home") + "/.kube/config";
    } else {
      return kubeconfigFile;
    }
  }

  protected List<String> contextOptions(ConfigProblemSetBuilder psBuilder) {
    Config kubeconfig;
    try {
      if (EncryptedSecret.isEncryptedSecret(getKubeconfigFile())) {
        kubeconfig = KubeConfigUtils.parseConfigFromString(secretSessionManager.decrypt(getKubeconfigFile()));
      } else {
        File kubeconfigFileOpen = new File(getKubeconfigFile());
        kubeconfig = KubeConfigUtils.parseConfig(kubeconfigFileOpen);
      }
    } catch (IOException e) {
      psBuilder.addProblem(ERROR, e.getMessage());
      return null;
    }

    return kubeconfig.getContexts()
        .stream()
        .map(NamedContext::getName)
        .collect(Collectors.toList());
  }

  protected List<String> dockerRegistriesOptions(ConfigProblemSetBuilder psBuilder) {
    DeploymentConfiguration context = parentOfType(DeploymentConfiguration.class);
    DockerRegistryProvider dockerRegistryProvider = context.getProviders().getDockerRegistry();

    if (dockerRegistryProvider != null) {
      return dockerRegistryProvider
          .getAccounts()
          .stream()
          .map(Account::getName)
          .collect(Collectors.toList());
    } else {
      return null;
    }
  }

  @Override
  public void makeBootstrappingAccount(ArtifactSourcesConfig artifactSourcesConfig) {
    super.makeBootstrappingAccount(artifactSourcesConfig);

    DeploymentConfiguration deploymentConfiguration = parentOfType(DeploymentConfiguration.class);
    String location = StringUtils.isEmpty(deploymentConfiguration.getDeploymentEnvironment().getLocation()) ? "spinnaker" : deploymentConfiguration.getDeploymentEnvironment().getLocation();

    // These changes are only surfaced in the account used by the bootstrapping clouddriver,
    // the user's clouddriver will be unchanged.
    if (!namespaces.isEmpty() && !namespaces.contains(location)) {
      namespaces.add(location);
    }

    if (!omitNamespaces.isEmpty() && omitNamespaces.contains(location)) {
      omitNamespaces.remove(location);
    }
  }

  @Data
  public static class CustomKubernetesResource {
    String kubernetesKind;
    String spinnakerKind;
    boolean versioned = false;
  }

  @Data
  public static class KubernetesCachingPolicy {
    String kubernetesKind;
    int maxEntriesPerAgent;
  }
}
