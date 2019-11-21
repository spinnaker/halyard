/*
 * Copyright 2019 Alibaba Group.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.alicloud;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.AbstractAccountCommand;

/** Interact with the alicloud provider's accounts */
@Parameters(separators = "=")
public class AliCloudAccountCommand extends AbstractAccountCommand {
  @Override
  protected String getProviderName() {
    return "alicloud";
  }

  AliCloudAccountCommand() {
    super();
    registerSubcommand(new AliCloudAddAccountCommand());
    registerSubcommand(new AliCloudEditAccountCommand());
  }
}
