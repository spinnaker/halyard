/*
 * Copyright 2017 Google, Inc.
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
 */

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes;

import com.netflix.spinnaker.clouddriver.kubernetes.deploy.KubernetesUtil;
import com.netflix.spinnaker.clouddriver.kubernetes.deploy.description.servergroup.KubernetesImageDescription;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerMonitoringDaemonService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@EqualsAndHashCode(callSuper = true)
@Component
@Data
public class KubernetesMonitoringDaemonService extends SpinnakerMonitoringDaemonService {
  @Delegate
  @Autowired
  KubernetesDistributedServiceDelegate distributedServiceDelegate;

  @Override
  public Settings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    KubernetesSharedServiceSettings kubernetesSharedServiceSettings = new KubernetesSharedServiceSettings(deploymentConfiguration);
    Settings settings = new Settings();
    settings.setArtifactId(getArtifactId(deploymentConfiguration.getName()))
        .setLocation(kubernetesSharedServiceSettings.getDeployLocation())
        .setEnabled(deploymentConfiguration.getMetricStores().isEnabled());
    return settings;
  }

  private String getArtifactId(String deploymentName) {
    String artifactName = getArtifact().getName();
    String version = getArtifactService().getArtifactVersion(deploymentName, getArtifact());

    KubernetesImageDescription image = new KubernetesImageDescription(artifactName, version, getDockerRegistry(deploymentName));
    return KubernetesUtil.getImageId(image);
  }
}
