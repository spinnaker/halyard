package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.ha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigDirectoryStructure;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class KubernetesV2SpringServiceWrapperFactory {
  private static final String SPRING_SERVICE_PROFILE_OUTPUT_DIRECTORY = "/opt/spinnaker/config/";

  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  ArtifactService artifactService;
  @Autowired
  Yaml yamlParser;
  @Autowired
  HalconfigDirectoryStructure halconfigDirectoryStructure;

  public <T> KubernetesV2SpringServiceWrapper<T> newSpringServiceInstance(SpringService<T> baseService, String additionalProfileName, String additionalProfileContents) {
    return new KubernetesV2SpringServiceWrapper<>(objectMapper,
        artifactService,
        yamlParser,
        halconfigDirectoryStructure,
        baseService,
        additionalProfileName,
        SPRING_SERVICE_PROFILE_OUTPUT_DIRECTORY,
        additionalProfileContents);
  }
}
