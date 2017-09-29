/*
 * Copyright 2017 Netflix, Inc.
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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.providers.dockerRegistry.DockerRegistryAccount;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.EcrTokenRefreshService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Component
@Data
public class KubernetesEcrTokenRefreshService extends EcrTokenRefreshService {
  @Delegate
  @Autowired
  KubernetesDistributedServiceDelegate distributedServiceDelegate;

  @Override
  public Settings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    Settings settings = new Settings();
    boolean enabled = false;

    for (DockerRegistryAccount account : deploymentConfiguration.getProviders().getDockerRegistry().getAccounts()) {
      if (account.isEcr()) {
        enabled = true;
        break;
      }
    }

    // Create our custom volume mount. This is where the ECR token will be stored.
    Map<String, String> volumeMounts = new HashMap<>();
    volumeMounts.put("/opt/passwords/", "ecr-pass");

    settings.setArtifactId(getArtifactId(deploymentConfiguration.getName()))
        .setVolumeMounts(volumeMounts)
        .setLocation("spinnaker")
        .setEnabled(enabled);

    return settings;
  }

  private String getArtifactId(String deploymentName) {
    return "quay.io/skuid/ecr-token-refresh:1.1.0";
  }
}
