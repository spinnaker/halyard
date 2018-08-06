package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ClouddriverCachingProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import java.nio.file.Paths;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;

@EqualsAndHashCode(callSuper = true)
@Data
abstract public class ClouddriverCachingService extends ClouddriverService {
  @Autowired
  ClouddriverCachingProfileFactory clouddriverCachingProfileFactory;

  @Override
  public Type getType() {
    return Type.CLOUDDRIVER_CACHING;
  }

  @Override
  public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    List<Profile> profiles = super.getProfiles(deploymentConfiguration, endpoints);

    // TODO: should this be part of clouddriver repo's halconfig dir?
    String filename = "clouddriver-caching.yml";
    String path = Paths.get(getConfigOutputPath(), filename).toString();
    Profile profile = clouddriverCachingProfileFactory.getProfile(filename, path, deploymentConfiguration, endpoints);
    profile.getEnv().put("SPRING_PROFILES_ACTIVE", "caching"); // TODO: this should be done somewhere else?

    profiles.add(profile);
    return profiles;
  }
}
