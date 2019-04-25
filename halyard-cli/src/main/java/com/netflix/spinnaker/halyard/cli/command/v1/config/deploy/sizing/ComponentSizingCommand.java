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

package com.netflix.spinnaker.halyard.cli.command.v1.config.deploy.sizing;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.deploy.ha.ClouddriverHaServiceCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.deploy.ha.EchoHaServiceCommand;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import lombok.AccessLevel;
import lombok.Getter;

import static com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService.Type.*;

@Parameters(separators = "=")
public class ComponentSizingCommand extends NestableCommand {
  @Getter(AccessLevel.PUBLIC)
  private String commandName = "component-sizing";

  @Getter(AccessLevel.PUBLIC)
  private String shortDescription = "Configure, validate, and view the component sizings for the Spinnaker services.";

  public ComponentSizingCommand() {
    for (SpinnakerService.Type spinnakerService : SpinnakerService.Type.values()) {
      registerSubcommand(new NamedComponentSizingCommand(spinnakerService));
    }
  }

  @Override
  protected void executeThis() {
    showHelp();
  }
}
