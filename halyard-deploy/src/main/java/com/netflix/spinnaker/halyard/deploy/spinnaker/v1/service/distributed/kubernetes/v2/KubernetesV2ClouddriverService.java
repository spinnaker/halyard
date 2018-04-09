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

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ClouddriverProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.KubernetesV2ClouddriverProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ClouddriverService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KubernetesV2ClouddriverService extends ClouddriverService implements KubernetesV2Service<ClouddriverService.Clouddriver> {
  @Delegate
  @Autowired
  KubernetesV2ServiceDelegate serviceDelegate;

  @Autowired
  KubernetesV2ClouddriverProfileFactory kubernetesV2ClouddriverProfileFactory;

  protected ClouddriverProfileFactory getClouddriverProfileFactory() {
    return kubernetesV2ClouddriverProfileFactory;
  }

  @Override
  public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    List<Profile> profiles = super.getProfiles(deploymentConfiguration, endpoints);
    generateAwsProfile(deploymentConfiguration, endpoints, getRootHomeDirectory()).ifPresent(profiles::add);
    generateAwsProfile(deploymentConfiguration, endpoints, getHomeDirectory()).ifPresent(profiles::add);
    return profiles;
  }

  @Override
  public ServiceSettings defaultServiceSettings() {
    return new Settings();
  }
}
