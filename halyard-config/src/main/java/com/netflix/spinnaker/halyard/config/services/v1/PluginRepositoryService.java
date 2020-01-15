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

package com.netflix.spinnaker.halyard.config.services.v1;

import com.netflix.spinnaker.halyard.config.error.v1.ConfigNotFoundException;
import com.netflix.spinnaker.halyard.config.error.v1.IllegalConfigException;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.node.NodeFilter;
import com.netflix.spinnaker.halyard.config.model.v1.node.Plugins;
import com.netflix.spinnaker.halyard.config.model.v1.plugins.PluginRepository;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemBuilder;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.problem.v1.ProblemSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PluginRepositoryService {
  private final LookupService lookupService;
  private final ValidateService validateService;
  private final DeploymentService deploymentService;

  public Plugins getPlugins(String deploymentName) {
    NodeFilter filter = new NodeFilter().setDeployment(deploymentName).setPlugins();

    return lookupService.getSingularNodeOrDefault(
        filter, Plugins.class, Plugins::new, n -> setPlugins(deploymentName, n));
  }

  private void setPlugins(String deploymentName, Plugins newPlugins) {
    DeploymentConfiguration deploymentConfiguration =
        deploymentService.getDeploymentConfiguration(deploymentName);
    deploymentConfiguration.setPlugins(newPlugins);
  }

  public List<PluginRepository> getAllPluginRepositories(String deploymentName) {
    return getPlugins(deploymentName).getRepositories();
  }

  public PluginRepository getPluginRepository(String deploymentName, String repositoryId) {
    List<PluginRepository> matchingPluginRepositories =
        getPlugins(deploymentName).getRepositories().stream()
            .filter(n -> n.getId().equals(repositoryId))
            .collect(Collectors.toList());

    switch (matchingPluginRepositories.size()) {
      case 0:
        throw new ConfigNotFoundException(
            new ConfigProblemBuilder(
                    Problem.Severity.FATAL,
                    "No plugin repository with id \"" + repositoryId + "\" was found")
                .setRemediation("Create a new plugin repository with id \"" + repositoryId + "\"")
                .build());
      case 1:
        return matchingPluginRepositories.get(0);
      default:
        throw new IllegalConfigException(
            new ConfigProblemBuilder(
                    Problem.Severity.FATAL,
                    "More than one plugin repository with id \"" + repositoryId + "\" was found")
                .setRemediation(
                    "Manually delete/rename duplicate plugin repositories with id \""
                        + repositoryId
                        + "\" in your halconfig file")
                .build());
    }
  }

  public void setPluginRepository(
      String deploymentName, String pluginRepositoryId, PluginRepository newPluginRepository) {
    List<PluginRepository> pluginRepositories = getAllPluginRepositories(deploymentName);
    for (int i = 0; i < pluginRepositories.size(); i++) {
      if (pluginRepositories.get(i).getNodeName().equals(pluginRepositoryId)) {
        pluginRepositories.set(i, newPluginRepository);
        return;
      }
    }
    throw new HalException(
        new ConfigProblemBuilder(
                Problem.Severity.FATAL,
                "Plugin repository \"" + pluginRepositoryId + "\" wasn't found")
            .build());
  }

  public void deletePluginRepository(String deploymentName, String repositoryId) {
    List<PluginRepository> pluginRepositories = getAllPluginRepositories(deploymentName);
    boolean removed = pluginRepositories.removeIf(repo -> repo.getId().equals(repositoryId));

    if (!removed) {
      throw new HalException(
          new ConfigProblemBuilder(
                  Problem.Severity.FATAL, "Plugin repository \"" + repositoryId + "\" wasn't found")
              .build());
    }
  }

  public void addPluginRepository(String deploymentName, PluginRepository newPluginRepository) {
    String newPluginRepositoryId = newPluginRepository.getId();
    List<PluginRepository> pluginRepositories = getAllPluginRepositories(deploymentName);
    for (PluginRepository repo : pluginRepositories) {
      if (repo.getId().equals(newPluginRepositoryId)) {
        throw new HalException(
            new ConfigProblemBuilder(
                    Problem.Severity.FATAL,
                    "Plugin repository \"" + newPluginRepositoryId + "\" already exists")
                .build());
      }
    }
    pluginRepositories.add(newPluginRepository);
  }

  public ProblemSet validateAllPluginRepositories(String deploymentName) {
    NodeFilter filter = new NodeFilter().setDeployment(deploymentName).withAnyPluginRepository();
    return validateService.validateMatchingFilter(filter);
  }

  public ProblemSet validatePluginRepository(String deploymentName, String pluginRepositoryId) {
    NodeFilter filter =
        new NodeFilter().setDeployment(deploymentName).setPluginRepository(pluginRepositoryId);
    return validateService.validateMatchingFilter(filter);
  }
}
