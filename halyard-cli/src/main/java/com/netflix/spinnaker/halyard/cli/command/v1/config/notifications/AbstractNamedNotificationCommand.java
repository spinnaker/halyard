/*
 * Copyright 2017 Johan Kasselman
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.notifications;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.config.model.v1.node.Notification;

import static com.netflix.spinnaker.halyard.cli.ui.v1.AnsiFormatUtils.Format.STRING;

@Parameters(separators = "=")
public abstract class AbstractNamedNotificationCommand extends AbstractNotificationCommand {
  @Override
  public String getCommandName() {
    return getNotificationName();
  }

  @Override
  protected String getShortDescription() {
    return "Manage and view Spinnaker configuration for the " + getNotificationName() + " notification";
  }

  @Override
  public String getDescription() {
    return "Manage and view Spinnaker configuration for the " + getNotificationName() + " notification";
  }

  protected AbstractNamedNotificationCommand() {
    registerSubcommand(new NotificationEnableDisableCommandBuilder()
      .setNotificationName(getNotificationName())
      .setEnable(false)
      .build()
    );
  }

  @Override
  protected void executeThis() {
    String currentDeployment = getCurrentDeployment();
    String notificationName = getNotificationName();
    new OperationHandler<Notification>()
      .setFailureMesssage("Failed to get notification " + notificationName + ".")
      .setSuccessMessage("Successfully got notification " + notificationName + ".")
      .setFormat(STRING)
      .setUserFormatted(true)
      .setOperation(Daemon.getNotification(currentDeployment, notificationName, !noValidate))
      .get();
  }
}
