package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.ha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigDirectoryStructure;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.CustomProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.SpinnakerProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.StringBackedProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpringService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.yaml.snakeyaml.Yaml;

/**
 * A SpinnakerServiceWrapper is used to add an additional profile to an existing SpinnakerService instance.
 */
public abstract class SpringServiceWrapper<T> extends SpinnakerService<T> {
  SpringService<T> baseService;

  String additionalProfileName;
  String additionalProfileOutputDirectory;
  String additionalProfileContents;

  public SpringServiceWrapper(ObjectMapper objectMapper, ArtifactService artifactService, Yaml yamlParser, HalconfigDirectoryStructure halconfigDirectoryStructure, SpringService baseService, String additionalProfileName, String additionalProfileOutputDirectory, String additionalProfileContents) {
    super(objectMapper, artifactService, yamlParser, halconfigDirectoryStructure);
    this.baseService = baseService;
    this.additionalProfileName = additionalProfileName;
    this.additionalProfileOutputDirectory = additionalProfileOutputDirectory;
    this.additionalProfileContents = additionalProfileContents;
  }

  @Override
  public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    List<Profile> profiles = baseService.getProfiles(deploymentConfiguration, endpoints);

    ArtifactService artifactService = getArtifactService();
    SpinnakerArtifact artifact = getArtifact();
    ProfileFactory profileFactory = new StringBackedProfileFactory() {
      @Override
      protected void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration,
          SpinnakerRuntimeSettings endpoints) {
        profile.appendContents(profile.getBaseContents());
      }

      @Override
      protected ArtifactService getArtifactService() {
        return artifactService;
      }

      @Override
      public SpinnakerArtifact getArtifact() {
        return artifact;
      }

      @Override
      protected String commentPrefix() {
        return "## ";
      }

      @Override
      protected String getRawBaseProfile() {
        return additionalProfileContents;
      }
    };

    String profileFileName = getCanonicalName() + "-" + additionalProfileName + ".yml";
    Profile additionalProfile = profileFactory.getProfile(profileFileName, Paths.get(additionalProfileOutputDirectory, profileFileName).toString(), deploymentConfiguration, endpoints);
    additionalProfile.getEnv().put("SPRING_PROFILES_ACTIVE", additionalProfileName); // TODO: this should be done somewhere else?
    profiles.add(additionalProfile);

    return profiles;
  }

  @Override
  public Type getType() {
    return baseService.getType();
  }

  @Override
  public Class<T> getEndpointClass() {
    return baseService.getEndpointClass();
  }

  @Override
  public Optional<String> customProfileOutputPath(String profileName) {
    return baseService.customProfileOutputPath(profileName);
  }

  @Override
  public SpinnakerArtifact getArtifact() {
    return baseService.getArtifact();
  }

  @Override
  public ServiceSettings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    return baseService.buildServiceSettings(deploymentConfiguration);
  }
}
