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

package com.netflix.spinnaker.halyard.cli.command.v1.config;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiPrinter;
import lombok.AccessLevel;
import lombok.Getter;

@Parameters(separators = "=")
public class GenerateCommand extends AbstractConfigCommand {
  @Getter(AccessLevel.PUBLIC)
  private String commandName = "generate";

  @Getter(AccessLevel.PUBLIC)
  private String shortDescription = "Generate the full Spinnaker config for your current deployment. "
    + "This does _not_ apply that configuration to your running Spinnaker installation. "
    + "That either needs to be done manually, or with `hal deploy apply`.";

  @Override
  protected void executeThis() {
    String currentDeployment = getCurrentDeployment();
    String result = new OperationHandler<String>()
        .setOperation(Daemon.generateDeployment(currentDeployment, !noValidate))
        .setFailureMesssage("Failed to generate config.")
        .setSuccessMessage("Successfully generated config.")
        .get();

    AnsiPrinter.out.println("Wrote configuration to " + result);
  }
}
