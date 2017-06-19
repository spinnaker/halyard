/*
 * Copyright 2017 Johan Kasselman.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.email;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.AbstractEditNotificationCommand;
import com.netflix.spinnaker.halyard.config.model.v1.node.Notification;
import com.netflix.spinnaker.halyard.config.model.v1.notifications.EmailNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Parameters(separators = "=")
@Data
public class EmailEditNotificationCommand extends AbstractEditNotificationCommand<EmailNotification> {
  String shortDescription = "Set properties for Email notification";

  String longDescription = "";

  @Parameter(
    names = "--host",
    description = EmailCommandProperties.HOST,
    password = true
  )
  private String host;

  @Parameter(
    names = "--from-address",
    description = EmailCommandProperties.FROM_ADDRESS
  )
  private String fromAddress;


  protected String getNotificationName() {
    return Notification.NotificationType.EMAIL.getName();
  }

  @Override
  protected Notification editNotification(EmailNotification notification) {
    notification.setHost(isSet(host) ? host : notification.getHost());
    notification.setFromAddress(isSet(fromAddress) ? fromAddress : notification.getFromAddress());
    return notification;
  }
}
