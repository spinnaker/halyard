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

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigParser;
import com.netflix.spinnaker.halyard.config.errors.v1.HalconfigException;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.node.Node;
import com.netflix.spinnaker.halyard.config.model.v1.node.NodeFilter;
import com.netflix.spinnaker.halyard.config.model.v1.problem.Problem;
import com.netflix.spinnaker.halyard.config.model.v1.problem.ProblemBuilder;
import com.netflix.spinnaker.halyard.config.services.v1.DeploymentService;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.registry.ProfileRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;
import retrofit.RetrofitError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A profile is a specialization of an artifact by means of feature flipping in .js and .yml
 * files.
 */
abstract public class SpinnakerProfile {
  @Autowired
  HalconfigParser parser;

  @Autowired
  Yaml yamlParser;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  DeploymentService deploymentService;

  @Autowired
  ProfileRegistry profileRegistry;

  @Autowired
  ArtifactService artifactService;

  final String EDIT_WARNING =
      commentPrefix() + "WARNING\n" +
      commentPrefix() + "This file was autogenerated, and _will_ be overwritten by Halyard.\n" +
      commentPrefix() + "Any edits you make here _will_ be lost.\n";

  protected abstract String commentPrefix();

  public abstract String getProfileName();

  public abstract SpinnakerArtifact getArtifact();

  public ProfileConfig getFullConfig(String deploymentName) {
    DeploymentConfiguration deploymentConfiguration = deploymentService.getDeploymentConfiguration(deploymentName);
    ProfileConfig result = generateFullConfig(
        getBaseConfig(deploymentConfiguration),
        deploymentConfiguration);
    result.setConfigContents(EDIT_WARNING + result.getConfigContents());
    return result;
  }

  public abstract String getProfileFileName();

  /**
   * Overwrite this for components that need to specialize their config.
   *
   * @param config the base halconfig returned from the config storage.
   * @param deploymentConfiguration the deployment configuration being translated into Spinnaker config.
   * @return the fully written configuration.
   */
  protected ProfileConfig generateFullConfig(ProfileConfig config, DeploymentConfiguration deploymentConfiguration) {
    return config;
  }

  /**
   * @return the base config (typically found in a profile's ./halconfig/ directory) for
   * the version of the profile specified by the Spinnaker version in the loaded halconfig.
   */
  private ProfileConfig getBaseConfig(DeploymentConfiguration deploymentConfiguration) {
    String componentName = getProfileName();
    String profileFileName = getProfileFileName();
    try {
      String componentVersion = artifactService.getArtifactVersion(deploymentConfiguration.getName(), getArtifact());
      String componentObjectName = String.join("/", componentName, componentVersion, profileFileName);

      return new ProfileConfig()
          .setConfigContents(IOUtils.toString(profileRegistry.getObjectContents(componentObjectName)))
          .setVersion(componentVersion);
    } catch (RetrofitError | IOException e) {
      throw new HalconfigException(
          new ProblemBuilder(Problem.Severity.FATAL,
              "Unable to retrieve a profile for \"" + componentName + "\": " + e.getMessage())
              .build()
      );
    }
  }

  /**
   * @param node is the node to find dependent files in.
   * @return the list of files required by the node to function.
   */
  List<String> dependentFiles(Node node) {
    List<String> files = new ArrayList<>();

    Consumer<Node> fileFinder = n -> files.addAll(n.localFiles().stream().map(f -> {
      try {
        f.setAccessible(true);
        return (String) f.get(n);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Failed to get local files for node " + n.getNodeName(), e);
      } finally {
        f.setAccessible(false);
      }
    }).filter(Objects::nonNull).collect(Collectors.toList()));
    node.recursiveConsume(fileFinder);

    return files;
  }
}
