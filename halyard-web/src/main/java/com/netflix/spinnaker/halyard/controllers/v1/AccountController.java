/*
 * Copyright 2016 Google, Inc.
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
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigDirectoryStructure;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigParser;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.node.Halconfig;
import com.netflix.spinnaker.halyard.config.model.v1.node.Providers;
import com.netflix.spinnaker.halyard.config.services.v1.AccountService;
import com.netflix.spinnaker.halyard.core.DaemonOptions;
import com.netflix.spinnaker.halyard.core.DaemonResponse;
import com.netflix.spinnaker.halyard.core.DaemonResponse.UpdateRequestBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import com.netflix.spinnaker.halyard.core.problem.v1.ProblemSet;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTask;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import com.netflix.spinnaker.halyard.models.v1.DefaultValidationSettings;
import com.netflix.spinnaker.halyard.models.v1.ValidationSettings;
import com.netflix.spinnaker.halyard.util.v1.GenericGetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/v1/config/deployments/{deploymentName:.+}/providers/{providerName:.+}/accounts")
public class AccountController {
  @Autowired
  AccountService accountService;

  @Autowired
  HalconfigParser halconfigParser;

  @Autowired
  HalconfigDirectoryStructure halconfigDirectoryStructure;

  @Autowired
  ObjectMapper objectMapper;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  DaemonTask<Halconfig, List<Account>> accounts(@PathVariable String deploymentName,
      @PathVariable String providerName,
      @ModelAttribute ValidationSettings validationSettings) {
    return GenericGetRequest.<List<Account>>builder()
        .getter(() -> accountService.getAllAccounts(deploymentName, providerName))
        .validator(() -> accountService.validateAllAccounts(deploymentName, providerName))
        .description("Get all " + providerName + " accounts")
        .build()
        .execute(validationSettings);
  }

  @RequestMapping(value = "/account/{accountName:.+}", method = RequestMethod.GET)
  DaemonTask<Halconfig, Account> account(@PathVariable String deploymentName,
      @PathVariable String providerName,
      @PathVariable String accountName,
      @ModelAttribute ValidationSettings validationSettings) {
    return GenericGetRequest.<Account>builder()
        .getter(() -> accountService.getProviderAccount(deploymentName, providerName, accountName))
        .validator(() -> accountService.validateAccount(deploymentName, providerName, accountName))
        .description("Get " + accountName + " account")
        .build()
        .execute(validationSettings);
  }

  @RequestMapping(value = "/options", method = RequestMethod.POST)
  DaemonTask<Halconfig, List<String>> newAccountOptions(
      @PathVariable String deploymentName,
      @PathVariable String providerName,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.severity) Severity severity,
      @RequestBody DaemonOptions rawAccountOptions) {
    String fieldName = rawAccountOptions.getField();
    Account account = objectMapper.convertValue(
        rawAccountOptions.getResource(),
        Providers.translateAccountType(providerName)
    );
    DaemonResponse.UpdateOptionsRequestBuilder builder = new DaemonResponse.UpdateOptionsRequestBuilder();
    String accountName = account.getName();

    builder.setUpdate(() -> accountService.addAccount(deploymentName, providerName, account));
    builder.setFieldOptionsResponse(() -> accountService
        .getAccountOptions(deploymentName, providerName, accountName, fieldName));
    builder.setSeverity(severity);

    return DaemonTaskHandler.submitTask(builder::build, "Get " + fieldName + " options");
  }

  @RequestMapping(value = "/account/{accountName:.+}/options", method = RequestMethod.PUT)
  DaemonTask<Halconfig, List<String>> existingAccountOptions(
      @PathVariable String deploymentName,
      @PathVariable String providerName,
      @PathVariable String accountName,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.severity) Severity severity,
      @RequestBody DaemonOptions rawAccountOptions) {
    String fieldName = rawAccountOptions.getField();
    DaemonResponse.StaticOptionsRequestBuilder builder = new DaemonResponse.StaticOptionsRequestBuilder();

    builder.setFieldOptionsResponse(() -> accountService
        .getAccountOptions(deploymentName, providerName, accountName, fieldName));
    builder.setSeverity(severity);

    return DaemonTaskHandler.submitTask(builder::build, "Get " + fieldName + " options");
  }

  @RequestMapping(value = "/account/{accountName:.+}", method = RequestMethod.DELETE)
  DaemonTask<Halconfig, Void> deleteAccount(
      @PathVariable String deploymentName,
      @PathVariable String providerName,
      @PathVariable String accountName,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.severity) Severity severity) {
    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    builder
        .setUpdate(() -> accountService.deleteAccount(deploymentName, providerName, accountName));
    builder.setSeverity(severity);

    Supplier<ProblemSet> doValidate = ProblemSet::new;
    if (validate) {
      doValidate = () -> accountService.validateAllAccounts(deploymentName, providerName);
    }

    builder.setValidate(doValidate);
    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());
    Path configPath = halconfigDirectoryStructure.getConfigPath(deploymentName);
    builder.setClean(() -> halconfigParser.cleanLocalFiles(configPath));

    return DaemonTaskHandler.submitTask(builder::build, "Delete the " + accountName + " account");
  }

  @RequestMapping(value = "/account/{accountName:.+}", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setAccount(
      @PathVariable String deploymentName,
      @PathVariable String providerName,
      @PathVariable String accountName,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.severity) Severity severity,
      @RequestBody Object rawAccount) {
    Account account = objectMapper.convertValue(
        rawAccount,
        Providers.translateAccountType(providerName)
    );

    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    Path configPath = halconfigDirectoryStructure.getConfigPath(deploymentName);
    builder.setStage(() -> account.stageLocalFiles(configPath));
    builder.setUpdate(
        () -> accountService.setAccount(deploymentName, providerName, accountName, account));
    builder.setSeverity(severity);

    Supplier<ProblemSet> doValidate = ProblemSet::new;
    if (validate) {
      doValidate = () -> accountService
          .validateAccount(deploymentName, providerName, account.getName());
    }

    builder.setValidate(doValidate);
    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());
    builder.setClean(() -> halconfigParser.cleanLocalFiles(configPath));

    return DaemonTaskHandler.submitTask(builder::build, "Edit the " + accountName + " account");
  }

  @RequestMapping(value = "/", method = RequestMethod.POST)
  DaemonTask<Halconfig, Void> addAccount(
      @PathVariable String deploymentName,
      @PathVariable String providerName,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.severity) Severity severity,
      @RequestBody Object rawAccount) {
    Account account = objectMapper.convertValue(
        rawAccount,
        Providers.translateAccountType(providerName)
    );

    UpdateRequestBuilder builder = new UpdateRequestBuilder();

    Path configPath = halconfigDirectoryStructure.getConfigPath(deploymentName);
    builder.setStage(() -> account.stageLocalFiles(configPath));
    builder.setSeverity(severity);
    builder.setUpdate(() -> accountService.addAccount(deploymentName, providerName, account));

    Supplier<ProblemSet> doValidate = ProblemSet::new;
    if (validate) {
      doValidate = () -> accountService
          .validateAccount(deploymentName, providerName, account.getName());
    }

    builder.setValidate(doValidate);
    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());
    builder.setClean(() -> halconfigParser.cleanLocalFiles(configPath));

    return DaemonTaskHandler
        .submitTask(builder::build, "Add the " + account.getName() + " account");
  }
}
