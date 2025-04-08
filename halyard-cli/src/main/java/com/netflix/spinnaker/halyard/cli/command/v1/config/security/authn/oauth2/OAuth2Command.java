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

package com.netflix.spinnaker.halyard.cli.command.v1.config.security.authn.oauth2;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.AbstractConfigCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiFormatUtils;
import com.netflix.spinnaker.halyard.config.model.v1.security.AuthnMethod;
import com.netflix.spinnaker.halyard.config.model.v1.security.OAuth2;

@Parameters(separators = "=")
public class OAuth2Command extends AbstractConfigCommand {
  public AuthnMethod.Method getMethod() {
    return AuthnMethod.Method.OAuth2;
  }

  public OAuth2Command() {
    registerSubcommand(new EditOAuth2Command());
  }

  @Override
  public String getCommandName() {
    return AuthnMethod.Method.OAuth2.id;
  }

  @Override
  protected void executeThis() {
    String currentDeployment = getCurrentDeployment();
    String authnMethodName = getMethod().id;

    new OperationHandler<OAuth2>()
        .setOperation(Daemon.getOAuth2(currentDeployment, !noValidate))
        .setFailureMesssage("Failed to get " + authnMethodName + " method.")
        .setSuccessMessage("Configured " + authnMethodName + " method: ")
        .setFormat(AnsiFormatUtils.Format.STRING)
        .setUserFormatted(true)
        .get();
  }

  @Override
  protected String getShortDescription() {
    return "";
  }
}
