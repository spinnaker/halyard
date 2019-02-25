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

package com.netflix.spinnaker.halyard.cli.command.v1;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.versions.BomVersionCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.versions.LatestVersionCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.versions.ListVersionCommand;
import lombok.AccessLevel;
import lombok.Getter;

@Parameters(separators = "=")
public class VersionCommand extends NestableCommand {
  @Getter(AccessLevel.PUBLIC)
  private String commandName = "version";

  @Getter(AccessLevel.PUBLIC)
  private String shortDescription = "Get information about the available Spinnaker versions.";

  public VersionCommand() {
    registerSubcommand(new LatestVersionCommand());
    registerSubcommand(new BomVersionCommand());
    registerSubcommand(new ListVersionCommand());
  }

  @Override
  protected void executeThis() {
    showHelp();
  }
}
