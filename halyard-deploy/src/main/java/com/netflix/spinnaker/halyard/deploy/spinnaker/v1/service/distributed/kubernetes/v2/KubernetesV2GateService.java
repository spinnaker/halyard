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
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.GateService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpringService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.DeployPriority;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.KubernetesSharedServiceSettings;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@EqualsAndHashCode(callSuper = true)
public class KubernetesV2GateService extends GateService implements KubernetesV2Service<GateService.Gate> {
  final DeployPriority deployPriority = new DeployPriority(0);

  @Delegate
  @Autowired
  KubernetesV2ServiceDelegate serviceDelegate;

  @Override
  public ServiceSettings defaultServiceSettings() {
    return new Settings();
  }

  @Override
  public Settings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    KubernetesSharedServiceSettings kubernetesSharedServiceSettings = new KubernetesSharedServiceSettings(deploymentConfiguration);
    Settings settings = new Settings(deploymentConfiguration.getSecurity().getApiSecurity());
    settings.setArtifactId(getArtifactId(deploymentConfiguration.getName()))
        .setLocation(kubernetesSharedServiceSettings.getDeployLocation())
        .setEnabled(true);
    return settings;
  }

  public static class Builder extends SpringService.Builder<KubernetesV2GateService,Builder> {
    KubernetesV2GateService source;

    public Builder(KubernetesV2GateService source) {
      super(source.getArtifact(), source.getArtifactService());
      this.source = source;
    }

    @Override
    public KubernetesV2GateService build() {
      Type type = Type.GATE.withTypeNameSuffix(typeNameSuffix);
      KubernetesV2GateService service = new KubernetesV2GateService() {
        @Override
        public Type getType() {
          return type;
        }

        @Override
        public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration,
            SpinnakerRuntimeSettings endpoints) {
          List<Profile> profiles = super.getProfiles(deploymentConfiguration, endpoints);
          profiles.addAll(generateExtraProfiles(deploymentConfiguration, endpoints));
          return profiles;
        }

        @Override
        public Settings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
          KubernetesSharedServiceSettings kubernetesSharedServiceSettings = new KubernetesSharedServiceSettings(deploymentConfiguration);
          Settings settings = new Settings(deploymentConfiguration.getSecurity().getApiSecurity());
          settings.setArtifactId(getArtifactId(deploymentConfiguration.getName()))
              .setLocation(kubernetesSharedServiceSettings.getDeployLocation())
              .setEnabled(true);
          activateExtraProfiles(settings);
          return settings;
        }
      };

      service.copyProperties(source);
      return service;
    }
  }
}
