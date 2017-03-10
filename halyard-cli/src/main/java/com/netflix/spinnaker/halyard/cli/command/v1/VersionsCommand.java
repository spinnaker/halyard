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
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiUi;
import com.netflix.spinnaker.halyard.core.registry.v1.Versions;
import lombok.AccessLevel;
import lombok.Getter;

@Parameters()
public class VersionsCommand extends NestableCommand {
  @Getter(AccessLevel.PUBLIC)
  private String commandName = "versions";

  @Getter(AccessLevel.PUBLIC)
  private String description = "List the available Spinnaker versions and their changelogs.";

  public VersionsCommand() {
    registerSubcommand(new LatestVersionCommand());
    registerSubcommand(new BomVersionCommand());
  }

  @Override
  protected void executeThis() {
    Versions versions = new OperationHandler<Versions>()
        .setOperation(Daemon.getVersions())
        .setFailureMesssage("Failed to load available Spinnaker versions.")
        .get();

    AnsiUi.success("The following versions are available: ");
    versions.getVersions().forEach(v -> AnsiUi.listItem(v.toString()));
  }
}
