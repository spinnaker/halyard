/*
 * Copyright 2019 Google, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.ci.gcb;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.ci.master.AbstractHasAccountCommand;

/**
 * Interact with Google Cloud Build accounts
 */
@Parameters(separators = "=")
public class GoogleCloudBuildAccountCommand extends AbstractHasAccountCommand {
  protected String getCiName() {
    return "gcb";
  }

  @Override
  public String getCommandName() {
    return "account";
  }

  public GoogleCloudBuildAccountCommand() {
    super();
    registerSubcommand(new GoogleCloudBuildListAccountsCommand());
    registerSubcommand(new GoogleCloudBuildAddAccountCommand());
    registerSubcommand(new GoogleCloudBuildEditAccountCommand());
    registerSubcommand(new GooglecloudBuildDeleteAccountCommand());
  }

  @Override
  public String getShortDescription() {
    return "Manage and view Spinnaker configuration for the Google Cloud Build service account.";
  }

  @Override
  protected void executeThis() {
    showHelp();
  }
}
