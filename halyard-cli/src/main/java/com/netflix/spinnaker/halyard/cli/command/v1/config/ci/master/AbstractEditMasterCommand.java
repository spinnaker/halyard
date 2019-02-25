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

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiUi;
import com.netflix.spinnaker.halyard.config.model.v1.node.Master;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Parameters(separators = "=")
public abstract class AbstractEditMasterCommand<T extends Master> extends AbstractHasMasterCommand {
  @Getter(AccessLevel.PROTECTED)
  private Map<String, NestableCommand> subcommands = new HashMap<>();

  @Getter(AccessLevel.PUBLIC)
  private String commandName = "edit";

  protected abstract Master editMaster(T master);

  public String getShortDescription() {
    return "Edit a master for the " + getCiName() + " Continuous Integration service.";
  }

  @Override
  protected void executeThis() {
    String masterName = getMasterName();
    String ciName = getCiName();
    String currentDeployment = getCurrentDeployment();
    // Disable validation here, since we don't want an illegal config to prevent us from fixing it.
    Master master = new OperationHandler<Master>()
        .setOperation(Daemon.getMaster(currentDeployment, ciName, masterName, !noValidate))
        .setFailureMesssage("Failed to get " + masterName + " under " + ciName + ".")
        .get();

    int originalHash = master.hashCode();

    master = editMaster((T) master);

    if (originalHash == master.hashCode()) {
      AnsiUi.failure("No changes supplied.");
      return;
    }

    new OperationHandler<Void>()
        .setOperation(Daemon.setMaster(currentDeployment, ciName, masterName, !noValidate, master))
        .setSuccessMessage("Edited " + masterName + " for " + ciName + ".")
        .setFailureMesssage("Failed to edit " + masterName + " for " + ciName + ".")
        .get();
  }
}
