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

package com.netflix.spinnaker.halyard.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigDirectoryStructure;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigParser;
import com.netflix.spinnaker.halyard.config.model.v1.node.Halconfig;
import com.netflix.spinnaker.halyard.config.model.v1.node.MetricStore;
import com.netflix.spinnaker.halyard.config.model.v1.node.MetricStores;
import com.netflix.spinnaker.halyard.config.services.v1.MetricStoresService;
import com.netflix.spinnaker.halyard.core.DaemonResponse.UpdateRequestBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.ProblemSet;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTask;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import com.netflix.spinnaker.halyard.models.v1.ValidationSettings;
import com.netflix.spinnaker.halyard.util.v1.GenericGetRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/config/deployments/{deploymentName:.+}/metricStores/")
public class MetricStoresController {
  private final HalconfigParser halconfigParser;
  private final MetricStoresService metricStoresService;
  private final HalconfigDirectoryStructure halconfigDirectoryStructure;
  private final ObjectMapper objectMapper;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  DaemonTask<Halconfig, MetricStores> getMetricStores(@PathVariable String deploymentName,
      @ModelAttribute ValidationSettings validationSettings) {
    return GenericGetRequest.<MetricStores>builder()
        .getter(() -> metricStoresService.getMetricStores(deploymentName))
        .validator(() -> metricStoresService.validateMetricStores(deploymentName))
        .description("Get all metric stores")
        .build()
        .execute(validationSettings);
  }

  @RequestMapping(value = "/{metricStoreType:.+}", method = RequestMethod.GET)
  DaemonTask<Halconfig, MetricStore> getMetricStore(@PathVariable String deploymentName,
      @PathVariable String metricStoreType,
      @ModelAttribute ValidationSettings validationSettings) {
    return GenericGetRequest.<MetricStore>builder()
        .getter(() -> metricStoresService.getMetricStore(deploymentName, metricStoreType))
        .validator(() -> metricStoresService.validateMetricStore(deploymentName, metricStoreType))
        .description("Get " + metricStoreType + " metric store")
        .build()
        .execute(validationSettings);
  }

  @RequestMapping(value = "/", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setMetricStores(@PathVariable String deploymentName,
      @ModelAttribute ValidationSettings validationSettings,
      @RequestBody Object rawMetricStores) {
    MetricStores metricStores = objectMapper.convertValue(rawMetricStores, MetricStores.class);

    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    Path configPath = halconfigDirectoryStructure.getConfigPath(deploymentName);
    builder.setStage(() -> metricStores.stageLocalFiles(configPath));
    builder.setSeverity(validationSettings.getSeverity());
    builder.setUpdate(() -> metricStoresService.setMetricStores(deploymentName, metricStores));

    builder.setValidate(ProblemSet::new);
    if (validationSettings.isValidate()) {
      builder.setValidate(() -> metricStoresService.validateMetricStores(deploymentName));
    }

    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());
    builder.setClean(() -> halconfigParser.cleanLocalFiles(configPath));

    return DaemonTaskHandler.submitTask(builder::build, "Edit all metric stores");
  }

  @RequestMapping(value = "/{metricStoreType:.+}", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setMetricStore(@PathVariable String deploymentName,
      @PathVariable String metricStoreType,
      @ModelAttribute ValidationSettings validationSettings,
      @RequestBody Object rawMetricStore) {
    MetricStore metricStore = objectMapper.convertValue(
        rawMetricStore,
        MetricStores.translateMetricStoreType(metricStoreType)
    );

    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    Path stagingPath = halconfigDirectoryStructure.getConfigPath(deploymentName);
    builder.setStage(() -> metricStore.stageLocalFiles(stagingPath));
    builder.setSeverity(validationSettings.getSeverity());
    builder.setUpdate(() -> metricStoresService.setMetricStore(deploymentName, metricStore));

    builder.setValidate(ProblemSet::new);
    if (validationSettings.isValidate()) {
      builder.setValidate(
          () -> metricStoresService.validateMetricStore(deploymentName, metricStoreType));
    }

    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());
    builder.setClean(() -> halconfigParser.cleanLocalFiles(stagingPath));

    return DaemonTaskHandler
        .submitTask(builder::build, "Edit " + metricStoreType + " metric store");
  }

  @RequestMapping(value = "/{metricStoreType:.+}/enabled/", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setMethodEnabled(@PathVariable String deploymentName,
      @PathVariable String metricStoreType,
      @ModelAttribute ValidationSettings validationSettings,
      @RequestBody boolean enabled) {
    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    builder.setUpdate(
        () -> metricStoresService.setMetricStoreEnabled(deploymentName, metricStoreType, enabled));
    builder.setSeverity(validationSettings.getSeverity());

    builder.setValidate(ProblemSet::new);
    if (validationSettings.isValidate()) {
      builder.setValidate(
          () -> metricStoresService.validateMetricStore(deploymentName, metricStoreType));
    }

    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());

    return DaemonTaskHandler
        .submitTask(builder::build, "Edit " + metricStoreType + " metric store");
  }
}
