package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import org.springframework.stereotype.Component;

@Component
public class ClouddriverRwProfileFactory extends StringBackedProfileFactory {
  @Override
  public SpinnakerArtifact getArtifact() {
    return SpinnakerArtifact.CLOUDDRIVER;
  }

  @Override
  protected String commentPrefix() {
    return "## ";
  }

  @Override
  protected void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    profile.appendContents("\n"
        + "server:\n"
        + "  port: ${services.clouddriverRw.port:7002}\n"
        + "  address: ${services.clouddriverRw.host:localhost}\n"
        + "redis:\n"
        + "  connection: ${services.redisForClouddriver.baseUrl:redis://localhost:6379}\n"
        + "caching:\n"
        + "  redis:\n"
        + "    hashingEnabled: false\n"
        + "  writeEnabled: false");

    profile.appendContents(profile.getBaseContents());
  }

  @Override
  protected String getRawBaseProfile() {
    return "";
  }
}
