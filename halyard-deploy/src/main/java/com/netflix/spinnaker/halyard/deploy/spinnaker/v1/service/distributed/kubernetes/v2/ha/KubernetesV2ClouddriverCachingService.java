package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.ha;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ClouddriverProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.KubernetesV2ClouddriverProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ClouddriverCachingService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ClouddriverService.Clouddriver;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2Service;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2ServiceDelegate;
import java.util.List;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KubernetesV2ClouddriverCachingService extends ClouddriverCachingService implements
    KubernetesV2Service<Clouddriver> {
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
