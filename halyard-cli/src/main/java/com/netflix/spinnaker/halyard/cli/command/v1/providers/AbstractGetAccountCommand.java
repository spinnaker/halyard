/*
 * Copyright 2016 Netflix, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.providers;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.DaemonService;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiUi;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.node.Providers;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describe a specific PROVIDER account
 */
@Parameters()
public abstract class AbstractGetAccountCommand extends AbstractProviderCommand {
  @Getter(AccessLevel.PROTECTED)
  private Map<String, NestableCommand> subcommands = new HashMap<>();

  @Getter(AccessLevel.PUBLIC)
  private String commandName = "get-account";

  public String getDescription() {
    return "Get details for a specific " + getProviderName() + " account";
  }

  @Parameter(description = "The name of the account to show", arity = 1)
  List<String> accounts = new ArrayList<>();

  @Override
  public String getMainParameter() {
    return "account";
  }

  public String getAccountName() {
    switch (accounts.size()) {
      case 0:
        throw new IllegalArgumentException("No account name supplied");
      case 1:
        return accounts.get(0);
      default:
        throw new IllegalArgumentException("More than one account supplied");
    }
  }

  @Override
  protected void executeThis() {
    AnsiUi.success(getAccount(getAccountName()).toString());
  }

  private Account getAccount(String accountName) {
    DaemonService service = Daemon.getService();
    String currentDeployment = service.getCurrentDeployment();
    ObjectMapper mapper = new ObjectMapper();
    return mapper.convertValue(
        service.getAccount(currentDeployment, getProviderName(), accountName, !noValidate),
        Providers.translateAccountType(getProviderName())
    );
  }
}
