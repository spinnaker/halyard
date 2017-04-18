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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.google;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.providers.google.GoogleAccount;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.services.v1.GenerateService;
import com.netflix.spinnaker.halyard.deploy.services.v1.VaultService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ConfigSource;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceInterfaceFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.VaultServerService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit.RetrofitError;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Component
@Data
public class GoogleVaultServerService extends VaultServerService implements GoogleDistributedService<VaultServerService.Vault> {
  @Autowired
  private String dockerRegistry;

  @Autowired
  GoogleMonitoringDaemonService monitoringDaemonService;

  @Autowired
  ArtifactService artifactService;

  @Autowired
  ServiceInterfaceFactory serviceInterfaceFactory;

  @Autowired
  String googleImageProject;

  @Autowired
  String startupScriptPath;

  @Autowired
  VaultService vaultService;

  @Override
  public void ensureRunning(AccountDeploymentDetails<GoogleAccount> details,
      GenerateService.ResolvedConfiguration resolvedConfiguration,
      List<ConfigSource> configSources,
      boolean recreate) {
    GoogleDistributedService.super.ensureRunning(details, resolvedConfiguration, configSources, recreate);
    Vault vault = connect(details, resolvedConfiguration.getRuntimeSettings());
    getToken(details.getDeploymentName(), vault);
  }

  @Override
  public Settings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    Settings settings = new Settings();
    settings.setArtifactId(getArtifactId(deploymentConfiguration.getName()))
        .setLocation("us-central1-f")
        .setEnabled(true);
    return settings;
  }

  @Override
  public List<ConfigSource> stageProfiles(AccountDeploymentDetails<GoogleAccount> details,
      GenerateService.ResolvedConfiguration resolvedConfiguration) {
    /* vault server may not stage profiles, since it acts as our config server */
    return new ArrayList<>();
  }

  public String getArtifactId(String deploymentName) {
    return GoogleDistributedService.super.getArtifactId(deploymentName);
  }

  final DeployPriority deployPriority = new DeployPriority(10);
  final boolean requiredToBootstrap = true;
}
