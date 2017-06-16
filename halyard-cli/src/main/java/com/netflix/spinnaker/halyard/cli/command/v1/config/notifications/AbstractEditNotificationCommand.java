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
 *
 *
 */

package com.netflix.spinnaker.halyard.cli.command.v1.config.notifications;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiUi;
import com.netflix.spinnaker.halyard.config.model.v1.node.Notification;
import lombok.Data;

@Data
@Parameters(separators = "=")
abstract public class AbstractEditNotificationCommand<N extends Notification> extends AbstractNotificationCommand {
  String commandName = "edit";

  @Override
  protected void executeThis() {
    String notificationName = getNotificationName();
    String currentDeployment = getCurrentDeployment();

    Notification notification = new OperationHandler<Notification>()
      .setFailureMesssage("Failed to get notification " + notificationName + ".")
      .setOperation(Daemon.getNotification(currentDeployment, notificationName, false))
      .get();

      int originalHash = notification.hashCode();

      notification = editNotification((N) notification);

      if (originalHash == notification.hashCode()) {
        AnsiUi.failure("No changes supplied.");
        return;
      }

      new OperationHandler<Void>()
        .setFailureMesssage("Failed to edit notification " + notificationName + ".")
        .setSuccessMessage("Successfully edited notification " + notificationName + ".")
        .setOperation(Daemon.setNotification(currentDeployment, notificationName, !noValidate, notification))
        .get();
  }

  protected abstract Notification editNotification(N notification);
}
