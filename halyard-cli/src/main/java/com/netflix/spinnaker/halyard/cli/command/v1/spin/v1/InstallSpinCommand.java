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
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.core.RemoteAction;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Parameters(separators = "=")
public class InstallSpinCommand extends AbstractRemoteActionCommand {
  @Getter(AccessLevel.PUBLIC)
  private String commandName = "install";

  @Getter(AccessLevel.PUBLIC)
  private String shortDescription = "Installs the spin CLI.";

  @Getter(AccessLevel.PUBLIC)
  private String longDescription = String.join(" ",
      "This command installs the spin CLI.");

  @Parameter(names = "--version",
             description = "When supplied, install spin CLI at the version specified.")
  String version;

  @Override
  protected OperationHandler<RemoteAction> getRemoteAction() {
    if (StringUtils.isEmpty(version)) {
      return new OperationHandler<RemoteAction>()
              .setFailureMesssage("Failed to generate spin CLI install script.")
              .setOperation(Daemon.installSpin());
    } else {
      return new OperationHandler<RemoteAction>()
              .setFailureMesssage("Failed to generate spin CLI install script.")
              .setOperation(Daemon.installSpin(version));
    }
  }
}
