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
 *
 */

package com.netflix.spinnaker.halyard.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigDirectoryStructure;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigParser;
import com.netflix.spinnaker.halyard.config.model.v1.node.ArtifactProvider;
import com.netflix.spinnaker.halyard.config.model.v1.node.Artifacts;
import com.netflix.spinnaker.halyard.config.model.v1.node.Halconfig;
import com.netflix.spinnaker.halyard.config.services.v1.ArtifactProviderService;
import com.netflix.spinnaker.halyard.core.DaemonResponse.StaticRequestBuilder;
import com.netflix.spinnaker.halyard.core.DaemonResponse.UpdateRequestBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import com.netflix.spinnaker.halyard.core.problem.v1.ProblemSet;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTask;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/v1/config/deployments/{deploymentName:.+}/artifactProviders")
public class ArtifactProviderController {

  @Autowired
  HalconfigParser halconfigParser;

  @Autowired
  ArtifactProviderService providerService;

  @Autowired
  HalconfigDirectoryStructure halconfigDirectoryStructure;

  @Autowired
  ObjectMapper objectMapper;

  @RequestMapping(value = "/{providerName:.+}", method = RequestMethod.GET)
  DaemonTask<Halconfig, ArtifactProvider> get(
      @PathVariable String deploymentName,
      @PathVariable String providerName,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.severity) Severity severity) {
    StaticRequestBuilder<ArtifactProvider> builder = new StaticRequestBuilder<>(
        () -> providerService.getArtifactProvider(deploymentName, providerName));

    builder.setSeverity(severity);

    if (validate) {
      builder.setValidateResponse(
          () -> providerService.validateArtifactProvider(deploymentName, providerName));
    }

    return DaemonTaskHandler.submitTask(builder::build, "Get the " + providerName + " provider");
  }

  @RequestMapping(value = "/{providerName:.+}", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setArtifactProvider(
      @PathVariable String deploymentName,
      @PathVariable String providerName,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.severity) Severity severity,
      @RequestBody Object rawArtifactProvider) {
    ArtifactProvider provider = objectMapper.convertValue(
        rawArtifactProvider,
        Artifacts.translateArtifactProviderType(providerName)
    );

    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    Path stagingPath = halconfigDirectoryStructure.getConfigPath(deploymentName);
    builder.setStage(() -> provider.stageLocalFiles(stagingPath));
    builder.setUpdate(() -> providerService.setArtifactProvider(deploymentName, provider));
    builder.setSeverity(severity);

    Supplier<ProblemSet> doValidate = ProblemSet::new;
    if (validate) {
      doValidate = () -> providerService.validateArtifactProvider(deploymentName, providerName);
    }

    builder.setValidate(doValidate);
    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());
    builder.setClean(() -> halconfigParser.cleanLocalFiles(stagingPath));

    return DaemonTaskHandler.submitTask(builder::build, "Edit the " + providerName + " provider");
  }

  @RequestMapping(value = "/{providerName:.+}/enabled", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setEnabled(
      @PathVariable String deploymentName,
      @PathVariable String providerName,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.severity) Severity severity,
      @RequestBody boolean enabled) {
    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    builder.setUpdate(() -> providerService.setEnabled(deploymentName, providerName, enabled));
    builder.setSeverity(severity);

    Supplier<ProblemSet> doValidate = ProblemSet::new;
    if (validate) {
      doValidate = () -> providerService.validateArtifactProvider(deploymentName, providerName);
    }

    builder.setValidate(doValidate);
    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());

    return DaemonTaskHandler.submitTask(builder::build, "Edit the " + providerName + " provider");
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  DaemonTask<Halconfig, List<ArtifactProvider>> providers(@PathVariable String deploymentName,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.severity) Severity severity) {
    StaticRequestBuilder<List<ArtifactProvider>> builder = new StaticRequestBuilder<>(
        () -> providerService.getAllArtifactProviders(deploymentName));

    builder.setSeverity(severity);

    if (validate) {
      builder
          .setValidateResponse(() -> providerService.validateAllArtifactProviders(deploymentName));
    }

    return DaemonTaskHandler.submitTask(builder::build, "Get all providers");
  }
}
