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

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.Front50Service;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.HasServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.DistributedLogCollector;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.KubernetesSharedServiceSettings;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@EqualsAndHashCode(callSuper = true)
@Component
@Data
public class KubernetesV1Front50Service extends Front50Service
    implements KubernetesV1DistributedService<Front50Service.Front50> {
  @Delegate @Autowired KubernetesV1DistributedServiceDelegate distributedServiceDelegate;

  @Delegate(excludes = HasServiceSettings.class)
  public DistributedLogCollector getLogCollector() {
    return getLogCollectorFactory().build(this);
  }

  @Override
  public List<Profile> getProfiles(
      DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    List<Profile> profiles = super.getProfiles(deploymentConfiguration, endpoints);
    generateAwsProfile(deploymentConfiguration, endpoints, getRootHomeDirectory())
        .ifPresent(profiles::add);
    generateAwsProfile(deploymentConfiguration, endpoints, getHomeDirectory())
        .ifPresent(profiles::add);
    return profiles;
  }

  @Override
  public Settings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    KubernetesSharedServiceSettings kubernetesSharedServiceSettings =
        new KubernetesSharedServiceSettings(deploymentConfiguration);
    Settings settings = new Settings();
    String location = kubernetesSharedServiceSettings.getDeployLocation();
    settings
        .setAddress(buildAddress(location))
        .setArtifactId(getArtifactId(deploymentConfiguration))
        .setLocation(location)
        .setEnabled(true);
    return settings;
  }

  final DeployPriority deployPriority = new DeployPriority(4);
  final boolean requiredToBootstrap = false;
}
