/*
 * Copyright 2016 Netflix, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.providers.dockerRegistry;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.providers.AbstractNamedProviderCommand;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Describe a specific docker-registry account
 */
@Parameters()
public class DockerRegistryCommand extends AbstractNamedProviderCommand {
  protected String getProviderName() {
    return "dockerRegistry";
  }

  @Override
  public String getCommandName() {
    return "docker-registry";
  }

  public DockerRegistryCommand() {
    super();
    registerSubcommand(new DockerAddAccountCommand());
  }
}
