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

import com.netflix.spinnaker.halyard.config.model.v1.ha.HaServices;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.IgorService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.DistributedService.DeployPriority;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@EqualsAndHashCode(callSuper = true)
public class KubernetesV2IgorService extends IgorService implements KubernetesV2Service<IgorService.Igor> {
  final DeployPriority deployPriority = new DeployPriority(0);

  @Delegate
  @Autowired
  KubernetesV2ServiceDelegate serviceDelegate;

  @Override
  public boolean isEnabled(DeploymentConfiguration deploymentConfiguration) {
    return deploymentConfiguration.getProviders().getDockerRegistry().isEnabled() ||
        deploymentConfiguration.getCi().ciEnabled();
  }

  @Override
  public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    List<Profile> profiles = super.getProfiles(deploymentConfiguration, endpoints);

    if (hasServiceOverrides(deploymentConfiguration)) {
      SpinnakerRuntimeSettings serviceOverrides = new SpinnakerRuntimeSettings();
      if (endpoints.getServiceSettings(Type.CLOUDDRIVER_RO).getEnabled()) {
        serviceOverrides.setServiceSettings(Type.CLOUDDRIVER, endpoints.getServiceSettings(Type.CLOUDDRIVER_RO).withOnlyBaseUrl());
      }
      if (endpoints.getServiceSettings(Type.ECHO_SLAVE).getEnabled()) {
        serviceOverrides.setServiceSettings(Type.ECHO, endpoints.getServiceSettings(Type.ECHO_SLAVE).withOnlyBaseUrl());
      }
      String filename = "igor-overrides.yml";
      String path = Paths.get(getConfigOutputPath(), filename).toString();
      Profile profile = getSpinnakerProfileFactory().getProfile(filename, path, deploymentConfiguration, serviceOverrides);
      profiles.add(profile);
    }

    return profiles;
  }

  @Override
  public ServiceSettings defaultServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    if (hasServiceOverrides(deploymentConfiguration)) {
      return new Settings(Arrays.asList("overrides", "local"));
    }
    return new Settings();
  }

  private boolean hasServiceOverrides(DeploymentConfiguration deployment) {
    HaServices haServices = deployment.getDeploymentEnvironment().getHaServices();
    return haServices.getClouddriver().isEnabled() || haServices.getEcho().isEnabled();
  }
}
