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
 */

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.google;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.KayentaService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.SidecarService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Component
public class GoogleKayentaService extends KayentaService implements GoogleDistributedService<KayentaService.Kayenta> {
  final DeployPriority deployPriority = new DeployPriority(4);
  final boolean requiredToBootstrap = false;

  @Delegate
  @Autowired
  GoogleDistributedServiceDelegate googleDistributedServiceDelegate;

  @Override
  public List<SidecarService> getSidecars(SpinnakerRuntimeSettings runtimeSettings) {
    List<SidecarService> result = GoogleDistributedService.super.getSidecars(runtimeSettings);
    result.add(getConsulClientService());
    result.add(getVaultClientService());
    return result;
  }

  @Override
  public Settings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    Settings settings = new Settings();
    settings.setArtifactId(getArtifactId(deploymentConfiguration.getName()))
        .setAddress(buildAddress())
        .setLocation("us-central1-f")
        .setEnabled(deploymentConfiguration.getCanary().isEnabled());
    return settings;
  }
}
