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

package com.netflix.spinnaker.halyard.cli.command.v1;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.AbstractConfigCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.DeploymentEnvironmentCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.EditConfigCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.FeaturesCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.GenerateCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.MetricStoresCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.NotificationCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.PersistentStorageCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.SecurityCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.VersionConfigCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.artifacts.ArtifactProviderCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.canary.CanaryCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.ci.CiCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.ProviderCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs.PubsubCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiFormatUtils;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * This is a top-level command for stoping the halyard daemon.
 *
 * Usage is `$ hal shutdown`
 */
@Parameters(separators =  "=")
public class ShutdownCommand extends NestableCommand {

  @Getter(AccessLevel.PUBLIC)
  private String commandName = "shutdown";

  @Getter(AccessLevel.PUBLIC)
  private String description = "Shutdown the halyard daemon.";


  @Override
  protected void executeThis() {
    Daemon.shutdown();
  }
}
