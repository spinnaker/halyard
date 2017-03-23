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

package com.netflix.spinnaker.halyard.deploy.services.v1;

import com.netflix.spinnaker.halyard.config.config.v1.HalconfigDirectoryStructure;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemBuilder;
import com.netflix.spinnaker.halyard.config.services.v1.DeploymentService;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import com.netflix.spinnaker.halyard.deploy.config.v1.ConfigParser;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.ServiceProviderFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerServiceProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Component
@Slf4j
public class GenerateService {
  @Autowired
  private String spinnakerStagingPath;

  @Autowired
  private DeploymentService deploymentService;

  @Autowired
  private ServiceProviderFactory serviceProviderFactory;

  @Autowired
  private String halconfigPath;

  @Autowired
  private HalconfigDirectoryStructure halconfigDirectoryStructure;

  @Autowired
  private List<SpinnakerService> spinnakerServices = new ArrayList<>();

  @Autowired
  private ConfigParser configParser;

  /**
   * Generate config for a given deployment.
   *
   * This involves a few steps:
   *
   *   1. Clear out old config generated in a prior run.
   *   2. Generate configuration using the halconfig as the source of truth, while collecting files needed by
   *      the deployment.
   *   3. Copy custom profiles from the specified deployment over to the new deployment.
   *
   * @param deploymentName is the deployment whose config to generate
   * @return a mapping from components to the profile's required local files.
   */
  public ResolvedConfiguration generateConfig(String deploymentName) {
    DaemonTaskHandler.newStage("Generating all Spinnaker profile files and endpoints");
    log.info("Generating config from \"" + halconfigPath + "\" with deploymentName \"" + deploymentName + "\"");
    File spinnakerStaging = new File(spinnakerStagingPath);
    DeploymentConfiguration deploymentConfiguration = deploymentService.getDeploymentConfiguration(deploymentName);

    DaemonTaskHandler.log("Building service endpoints");
    SpinnakerServiceProvider serviceProvider = serviceProviderFactory.create(deploymentConfiguration);
    SpinnakerRuntimeSettings endpoints = serviceProvider.buildEndpoints(deploymentConfiguration);

    // Step 1.
    try {
      FileUtils.deleteDirectory(spinnakerStaging);
    } catch (IOException e) {
      throw new HalException(
          new ConfigProblemBuilder(Severity.FATAL, "Unable to clear old spinnaker config: " + e.getMessage() + ".").build());
    }

    if (!spinnakerStaging.mkdirs()) {
      throw new HalException(
          new ConfigProblemBuilder(Severity.FATAL, "Unable to create new spinnaker config directory \"" + spinnakerStagingPath + "\".").build());
    }

    // Step 2.
    Map<SpinnakerService.Type, Map<String, Profile>> serviceProfiles = new HashMap<>();
    Path stagingPath;
    for (SpinnakerService service : serviceProvider.getServices()) {
      ServiceSettings settings = endpoints.getServiceSettings(service);
      if (settings != null && !settings.isEnabled()) {
        continue;
      }

      List<Profile> profiles = service.getProfiles(deploymentConfiguration, endpoints);

      String pluralized = profiles.size() == 1 ? "" : "s";
      DaemonTaskHandler.log("Generated " + profiles.size() + " profile" + pluralized + " for " + service.getCannonicalName());
      for (Profile profile : profiles) {
        stagingPath = Paths.get(profile.getStagedFile(spinnakerStagingPath));
        log.info("Writing " + profile.getName() + " profile to " + stagingPath + " with " + profile.getRequiredFiles().size() + " required files");

        configParser.atomicWrite(stagingPath, profile.getContents());
      }

      Map<String, Profile> profileMap = new HashMap<>();
      for (Profile profile : profiles) {
        profileMap.put(profile.getName(), profile);
      }

      serviceProfiles.put(service.getType(), profileMap);
    }

    // Step 3.
    Path userProfilePath = halconfigDirectoryStructure.getUserProfilePath(deploymentName);

    if (Files.isDirectory(userProfilePath)) {
      DaemonTaskHandler.newStage("Copying user-provided profiles");
      File[] files = userProfilePath.toFile().listFiles();
      if (files == null) {
        files = new File[0];
      }

      Arrays.stream(files).forEach(f -> {
        try {
          DaemonTaskHandler.log("Copying existing profile " + f.getName());
          Files.copy(f.toPath(), Paths.get(spinnakerStaging.toString(), f.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          throw new HalException(
              new ConfigProblemBuilder(Severity.FATAL, "Unable to copy profile \"" + f.getName() + "\": " + e.getMessage() + ".").build()
          );
        }
      });
    }

    // Step 4.
    ResolvedConfiguration result = new ResolvedConfiguration()
        .setServiceProfiles(serviceProfiles)
        .setRuntimeSettings(endpoints);

    return result;
  }

  @Data
  public static class ResolvedConfiguration {
    private Map<SpinnakerService.Type, Map<String, Profile>> serviceProfiles = new HashMap<>();
    SpinnakerRuntimeSettings runtimeSettings;

    public ServiceSettings getServiceSettings(SpinnakerService service) {
      return runtimeSettings.getServiceSettings(service);
    }
  }
}
