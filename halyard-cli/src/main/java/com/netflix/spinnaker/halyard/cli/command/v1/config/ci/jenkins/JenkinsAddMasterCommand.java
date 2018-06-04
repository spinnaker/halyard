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
 *
 */

package com.netflix.spinnaker.halyard.cli.command.v1.config.ci.jenkins;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.ci.master.AbstractAddMasterCommand;
import com.netflix.spinnaker.halyard.config.model.v1.ci.jenkins.JenkinsMaster;
import com.netflix.spinnaker.halyard.config.model.v1.node.Master;

@Parameters(separators = "=")
public class JenkinsAddMasterCommand extends AbstractAddMasterCommand {
  protected String getCiName() {
    return "jenkins";
  }

  @Parameter(
      names = "--address",
      required = true,
      description = JenkinsCommandProperties.ADDRESS_DESCRIPTION
  )
  private String address;

  @Parameter(
      names = "--username",
      description = JenkinsCommandProperties.USERNAME_DESCRIPTION
  )
  public String username;

  @Parameter(
      names = "--password",
      password = true,
      description = JenkinsCommandProperties.PASSWORD_DESCRIPTION
  )
  public String password;

  @Parameter(
      names = "--csrf",
      arity = 1,
      description = JenkinsCommandProperties.CSRF_DESCRIPTION
  )
  public Boolean csrf;

  @Override
  protected Master buildMaster(String masterName) {
    JenkinsMaster master = (JenkinsMaster) new JenkinsMaster().setName(masterName);
    master.setAddress(address)
        .setPassword(password)
        .setUsername(username)
        .setCsrf(csrf);

    return master;
  }
}
