/*
 * Copyright (c) 2017, 2018, Oracle Corporation and/or its affiliates. All rights reserved.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.ci.wercker;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.ci.master.AbstractAddMasterCommand;
import com.netflix.spinnaker.halyard.config.model.v1.ci.wercker.WerckerMaster;
import com.netflix.spinnaker.halyard.config.model.v1.node.CIAccount;

@Parameters(separators = "=")
public class WerckerAddMasterCommand extends AbstractAddMasterCommand {
  @Override
  protected String getCiName() {
    return "wercker";
  }

  @Parameter(
    names = "--address",
    required = true,
    description = WerckerCommandProperties.ADDRESS_DESCRIPTION
  )
  private String address;

  @Parameter(
    names = "--user",
    description = WerckerCommandProperties.USER_DESCRIPTION
  )
  public String user;

  @Parameter(
    names = "--token",
    password = true,
    description = WerckerCommandProperties.TOKEN_DESCRIPTION
  )
  public String token;

  @Override
  protected CIAccount buildMaster(String masterName) {
    WerckerMaster master = (WerckerMaster) new WerckerMaster().setName(masterName);
    master.setAddress(address)
        .setToken(token)
        .setUser(user);

    return master;
  }
}
