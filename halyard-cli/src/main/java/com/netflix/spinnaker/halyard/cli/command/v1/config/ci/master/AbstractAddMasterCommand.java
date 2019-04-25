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

package com.netflix.spinnaker.halyard.cli.command.v1.config.ci.master;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.fiat.model.Authorization;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.config.model.v1.node.Master;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Parameters(separators = "=")
public abstract class AbstractAddMasterCommand extends AbstractHasMasterCommand {
  @Getter(AccessLevel.PROTECTED)
  private Map<String, NestableCommand> subcommands = new HashMap<>();

  @Getter(AccessLevel.PUBLIC)
  private String commandName = "add";

  @Parameter(
      variableArity = true,
      names = "--read-permissions",
      description = MasterCommandProperties.READ_PERMISSION_DESCRIPTION
  )
  private List<String> readPermissions = new ArrayList<>();

  @Parameter(
      variableArity = true,
      names = "--write-permissions",
      description = MasterCommandProperties.WRITE_PERMISSION_DESCRIPTION
  )
  private List<String> writePermissions = new ArrayList<>();

  protected abstract Master buildMaster(String masterName);

  public String getShortDescription() {
    return "Add a master for the " + getCiName() + " Continuous Integration service.";
  }

  @Override
  protected void executeThis() {
    String masterName = getMasterName();
    Master master = buildMaster(masterName);
    String ciName = getCiName();
    master.getPermissions().add(Authorization.READ, readPermissions);
    master.getPermissions().add(Authorization.WRITE, writePermissions);

    String currentDeployment = getCurrentDeployment();
    new OperationHandler<Void>()
        .setOperation(Daemon.addMaster(currentDeployment, ciName, !noValidate, master))
        .setSuccessMessage("Added " + masterName + " for " + ciName + ".")
        .setFailureMesssage("Failed to add " + masterName + " for " + ciName + ".")
        .get();
  }
}
