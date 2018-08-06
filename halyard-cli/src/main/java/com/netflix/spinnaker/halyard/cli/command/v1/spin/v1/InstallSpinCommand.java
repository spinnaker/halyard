/*
 * Copyright 2018 Google, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.spin.v1;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.AbstractRemoteActionCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiFormatUtils;
import lombok.AccessLevel;
import lombok.Getter;

@Parameters(separators = "=")
public class InstallSpinCommand extends NestableCommand {
  @Getter(AccessLevel.PUBLIC)
  private String commandName = "install";

  @Getter(AccessLevel.PUBLIC)
  private String shortDescription = "Installs the spin CLI.";

  @Getter(AccessLevel.PUBLIC)
  private String longDescription = String.join(" ",
      "This command installs the spin CLI.");

  @Parameter(names = "--version",
             description = "When supplied, install spin CLI at the version specified.")
  String version = "nightly";

  @Override
  protected void executeThis() {
    new OperationHandler<String>()
            .setFailureMesssage("Failed to generate spin CLI install script.")
            .setSuccessMessage("Install spin CLI with this bash script: \n")
            .setOperation(Daemon.installSpin(version))
            .setFormat(AnsiFormatUtils.Format.STRING)
            .get();
  }
}
