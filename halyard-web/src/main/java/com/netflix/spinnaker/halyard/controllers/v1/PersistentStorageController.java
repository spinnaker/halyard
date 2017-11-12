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

package com.netflix.spinnaker.halyard.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigParser;
import com.netflix.spinnaker.halyard.config.model.v1.node.Halconfig;
import com.netflix.spinnaker.halyard.config.model.v1.node.PersistentStorage;
import com.netflix.spinnaker.halyard.config.model.v1.node.PersistentStore;
import com.netflix.spinnaker.halyard.config.services.v1.PersistentStorageService;
import com.netflix.spinnaker.halyard.core.DaemonResponse;
import com.netflix.spinnaker.halyard.core.DaemonResponse.UpdateRequestBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import com.netflix.spinnaker.halyard.core.problem.v1.ProblemSet;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTask;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@RestController
@RequestMapping("/v1/config/deployments/{deploymentName:.+}/persistentStorage")
public class PersistentStorageController {
  @Autowired
  HalconfigParser halconfigParser;

  @Autowired
  PersistentStorageService persistentStorageService;

  @Autowired
  ObjectMapper objectMapper;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  DaemonTask<Halconfig, PersistentStorage> getPersistentStorage(@PathVariable String deploymentName,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.severity) Severity severity) {
    DaemonResponse.StaticRequestBuilder<PersistentStorage> builder = new DaemonResponse.StaticRequestBuilder<>(
            () -> persistentStorageService.getPersistentStorage(deploymentName));

    builder.setSeverity(severity);

    if (validate) {
      builder.setValidateResponse(() -> persistentStorageService.validatePersistentStorage(deploymentName));
    }

    return DaemonTaskHandler.submitTask(builder::build, "Get persistent storage settings");
  }

  @RequestMapping(value = "/", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setPersistentStorage(@PathVariable String deploymentName,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.severity) Severity severity,
      @RequestBody Object rawPersistentStorage) {
    PersistentStorage persistentStorage = objectMapper.convertValue(rawPersistentStorage, PersistentStorage.class);

    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    builder.setUpdate(() -> persistentStorageService.setPersistentStorage(deploymentName, persistentStorage));
    builder.setSeverity(severity);

    Supplier<ProblemSet> doValidate = ProblemSet::new;

    if (validate) {
      doValidate = () -> persistentStorageService.validatePersistentStorage(deploymentName);
    }

    builder.setValidate(doValidate);
    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());

    return DaemonTaskHandler.submitTask(builder::build, "Edit persistent storage settings");
  }

  @RequestMapping(value = "/{persistentStoreType:.+}", method = RequestMethod.GET)
  DaemonTask<Halconfig, PersistentStore> getPersistentStore(@PathVariable String deploymentName,
      @PathVariable String persistentStoreType,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.severity) Severity severity) {
    DaemonResponse.StaticRequestBuilder<PersistentStore> builder = new DaemonResponse.StaticRequestBuilder<>(
            () -> persistentStorageService.getPersistentStore(deploymentName, persistentStoreType));

    builder.setSeverity(severity);

    if (validate) {
      builder.setValidateResponse(() -> persistentStorageService.validatePersistentStore(deploymentName, persistentStoreType));
    }

    return DaemonTaskHandler.submitTask(builder::build, "Get persistent store");
  }

  @RequestMapping(value = "/{persistentStoreType:.+}", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setPersistentStore(@PathVariable String deploymentName,
      @PathVariable String persistentStoreType,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultControllerValues.severity) Severity severity,
      @RequestBody Object rawPersistentStore) {
    PersistentStore persistentStore = objectMapper.convertValue(rawPersistentStore, PersistentStorage.translatePersistentStoreType(persistentStoreType));

    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    builder.setUpdate(() -> persistentStorageService.setPersistentStore(deploymentName, persistentStore));
    builder.setSeverity(severity);

    Supplier<ProblemSet> doValidate = ProblemSet::new;

    if (validate) {
      doValidate = () -> persistentStorageService.validatePersistentStore(deploymentName, persistentStoreType);
    }

    builder.setValidate(doValidate);
    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());

    return DaemonTaskHandler.submitTask(builder::build, "Edit persistent store");
  }
}
