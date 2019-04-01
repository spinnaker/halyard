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
 */

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile;

import com.netflix.spinnaker.halyard.config.model.v1.node.Artifacts;
import com.netflix.spinnaker.halyard.config.model.v1.node.Cis;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.node.Providers;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IgorProfileFactory extends SpringProfileFactory {
  @Override
  public SpinnakerArtifact getArtifact() {
    return SpinnakerArtifact.IGOR;
  }

  @Override
  public String getMinimumSecretDecryptionVersion(String deploymentName) {
    return "1.0.2";
  }

  @Override
  public void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    super.setProfile(profile, deploymentConfiguration, endpoints);
    Providers providers = deploymentConfiguration.getProviders();
    if (providers.getDockerRegistry().isEnabled()) {
      profile.appendContents("dockerRegistry.enabled: true");
    }

    Cis cis = deploymentConfiguration.getCi();
    List<String> files = backupRequiredFiles(cis, deploymentConfiguration.getName());

    Artifacts artifacts = deploymentConfiguration.getArtifacts();
    if (artifacts != null) {
      files.addAll(backupRequiredFiles(artifacts, deploymentConfiguration.getName()));
      profile.appendContents(yamlToString(deploymentConfiguration.getName(), profile, new ArtifactWrapper(artifacts)));
    }

    profile.appendContents(yamlToString(deploymentConfiguration.getName(), profile, cis))
        .appendContents(profile.getBaseContents())
        .setRequiredFiles(files);
  }

  @Data
  private static class ArtifactWrapper {
    private Artifacts artifacts;

    ArtifactWrapper(Artifacts artifacts) {
      this.artifacts = artifacts;
    }
  }
}
