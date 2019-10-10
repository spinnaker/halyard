/*
 * Copyright 2019 Armory, Inc.
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

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.node.Plugins;
import com.netflix.spinnaker.halyard.config.model.v1.plugins.Manifest;
import com.netflix.spinnaker.halyard.config.model.v1.plugins.Plugin;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PluginProfileFactory extends StringBackedProfileFactory {

  public Profile getProfileByName(
      String configOutputPath,
      String serviceName,
      DeploymentConfiguration deploymentConfiguration) {

    String pluginFilename = "plugins.yml";
    String pluginPath = Paths.get(configOutputPath, pluginFilename).toString();

    String deploymentName = deploymentConfiguration.getName();
    String version = getArtifactService().getArtifactVersion(deploymentName, getArtifact());
    String profileName = serviceName + '-' + pluginFilename;
    Profile profile = getBaseProfile(profileName, version, pluginPath);

    final Plugins plugins = deploymentConfiguration.getPlugins();

    Map<String, Object> pluginsYaml = new HashMap<>();
    Map<String, Object> fullyRenderedYaml = new HashMap<>();

    List<Map<String, Object>> pluginMetadata =
        plugins.getPlugins().stream()
            .filter(p -> p.getEnabled())
            .filter(p -> !p.getManifestLocation().isEmpty())
            .filter(p -> p.getManifest().getResources().containsKey(serviceName))
            .map(p -> composeMetadata(p, p.getManifest(), serviceName))
            .collect(Collectors.toList());

    pluginsYaml.put("pluginConfigurations", pluginMetadata);
    pluginsYaml.put("downloadingEnabled", plugins.isDownloadingEnabled());
    fullyRenderedYaml.put("plugins", pluginsYaml);

    profile.appendContents(
        yamlToString(deploymentConfiguration.getName(), profile, fullyRenderedYaml));
    return profile;
  }

  public Map<String, Object> composeMetadata(Plugin plugin, Manifest manifest, String serviceName) {
    if (!manifest.getResources().containsKey(serviceName)) {
      return null;
    }
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("enabled", plugin.getEnabled());
    metadata.put("name", manifest.getName());
    metadata.put("jars", manifest.getResources().get(serviceName));
    return metadata;
  }

  @Override
  protected void setProfile(
      Profile profile,
      DeploymentConfiguration deploymentConfiguration,
      SpinnakerRuntimeSettings endpoints) {}

  @Override
  protected String getRawBaseProfile() {
    return "";
  }

  @Override
  public SpinnakerArtifact getArtifact() {
    return SpinnakerArtifact.SPINNAKER;
  }

  @Override
  protected String commentPrefix() {
    return "## ";
  }
}
