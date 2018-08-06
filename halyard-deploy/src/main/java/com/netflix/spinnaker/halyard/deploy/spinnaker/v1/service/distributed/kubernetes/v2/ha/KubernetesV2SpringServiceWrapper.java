package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.ha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigDirectoryStructure;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerMonitoringDaemonService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpringService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2Service;
import lombok.experimental.Delegate;
import org.yaml.snakeyaml.Yaml;

/**
 * Wrapper over KubernetesV2Service/SpringService class with additional profile.
 */
public class KubernetesV2SpringServiceWrapper<T> extends SpringServiceWrapper<T> implements KubernetesV2Service<T> {
  KubernetesV2Service<T> baseKubernetesV2Service;

  public KubernetesV2SpringServiceWrapper(ObjectMapper objectMapper, ArtifactService artifactService, Yaml yamlParser, HalconfigDirectoryStructure halconfigDirectoryStructure, SpringService<T> baseService, String additionalProfileName, String additionalProfileOutputDirectory, String additionalProfileContents) {
    super(objectMapper, artifactService, yamlParser, halconfigDirectoryStructure, baseService, additionalProfileName, additionalProfileOutputDirectory, additionalProfileContents);

    if (!KubernetesV2Service.class.isInstance(baseService)) {
      throw new RuntimeException("Wrapped service must be a KubernetesV2Services instance");
    }

    baseKubernetesV2Service = KubernetesV2Service.class.cast(baseService);
  }

  @Override
  public String getDockerRegistry(String deploymentName, SpinnakerArtifact artifact) {
    return baseKubernetesV2Service.getDockerRegistry(deploymentName, artifact);
  }

  @Override
  public ServiceSettings defaultServiceSettings() {
    return baseKubernetesV2Service.defaultServiceSettings();
  }

  @Override
  public SpinnakerMonitoringDaemonService getMonitoringDaemonService() {
    return baseKubernetesV2Service.getMonitoringDaemonService();
  }
}
