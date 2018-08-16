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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.SpinnakerProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.StringBackedProfileFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Component
@EqualsAndHashCode(callSuper = true)
abstract public class SpringService<T> extends SpinnakerService<T> {
  private static final String SPRING_CONFIG_OUTPUT_PATH = "/opt/spinnaker/config/";

  protected String getConfigOutputPath() {
    return SPRING_CONFIG_OUTPUT_PATH;
  }

  @Autowired
  SpinnakerProfileFactory spinnakerProfileFactory;

  @Override
  public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    String filename = "spinnaker.yml";
    String path = Paths.get(getConfigOutputPath(), filename).toString();
    List<Profile> result = new ArrayList<>();
    result.add(spinnakerProfileFactory.getProfile(filename, path, deploymentConfiguration, endpoints));
    return result;
  }

  @Override
  protected Optional<String> customProfileOutputPath(String profileName) {
    if (profileName.equals(getCanonicalName() + ".yml") || profileName.startsWith(getCanonicalName() + "-") || profileName.startsWith("spinnaker")) {
      return Optional.of(Paths.get(getConfigOutputPath(), profileName).toString());
    } else {
      return Optional.empty();
    }
  }

  protected List<String> springProfiles = new ArrayList<>();

  public static abstract class Builder<S extends SpringService,B extends Builder<S,B>> extends SpinnakerService.Builder<S,B> {
    private final SpinnakerArtifact artifact;
    private final ArtifactService artifactService;

    protected final Map<String, String> extraSpringProfiles = new HashMap<>();

    public Builder(SpinnakerArtifact artifact, ArtifactService artifactService) {
      this.artifact = artifact;
      this.artifactService = artifactService;
    }

    public B addProfile(String profileName, String profileContents) {
      extraSpringProfiles.put(profileName, profileContents);
      return (B) this;
    }

    protected void activateExtraProfiles(SpringServiceSettings settings) {
      List<String> profilesToActivate = new ArrayList<>();
      profilesToActivate.add(fullSpringProfileName("local"));
      profilesToActivate.add(fullSpringProfileName("test"));
      profilesToActivate.addAll(extraSpringProfiles.keySet());
      settings.addProfiles(profilesToActivate);
    }

    protected List<Profile> generateExtraProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
      return extraSpringProfiles.keySet().stream()
          .map(springProfileName -> {
            String extraProfileFileName = artifact.getName() + "-" + springProfileName + ".yml";
            ProfileFactory extraProfileFactory = new StringBackedProfileFactory() {
              @Override
              protected void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
                profile.appendContents(profile.getBaseContents());
              }

              @Override
              protected ArtifactService getArtifactService() { return artifactService; }

              @Override
              public SpinnakerArtifact getArtifact() { return artifact; }

              @Override
              protected String commentPrefix() { return "## "; }

              @Override
              protected String getRawBaseProfile() { return extraSpringProfiles.get(springProfileName); }
            };

            return extraProfileFactory.getProfile(
                extraProfileFileName,
                Paths.get(SPRING_CONFIG_OUTPUT_PATH, extraProfileFileName).toString(),
                deploymentConfiguration,
                endpoints);
          }).collect(Collectors.toList());
    }

    /**
     * E.g.,
     * If canonical name is "clouddriver" and profileName is "test", full profile name should be "test".
     * If canonical name is "clouddriver-ro" and profileName is "test", full profile name should be "ro-test".
     */
    private String fullSpringProfileName(String springProfileName) {
      if (StringUtils.isBlank(typeNameSuffix)) {
        return springProfileName;
      }
      return typeNameSuffix + "-" + springProfileName;
    }
  }
}
