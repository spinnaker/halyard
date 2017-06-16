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

package com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.slack;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.AbstractEditNotificationCommand;
import com.netflix.spinnaker.halyard.config.model.v1.node.Notification;
import com.netflix.spinnaker.halyard.config.model.v1.notifications.SlackNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Parameters(separators = "=")
@Data
public class SlackEditNotificationCommand extends AbstractEditNotificationCommand<SlackNotification> {
    String shortDescription = "Set properties for Slack notification";

    String longDescription = "The SLACK notification requires a TOKEN generated from your slack admin page"
      + "and a botName to post as in slack channels!"
      + "The bot must then be invited to join the channel to be able to send messages there.";

    @Parameter(
      names = "--token",
      description = SlackCommandProperties.TOKEN,
      password = true
    )
    private String token;

    @Parameter(
      names = "--bot-name",
      description = SlackCommandProperties.BOT_NAME
    )
    private String botName;


    protected String getNotificationName() {
        return Notification.NotificationType.SLACK.getName();
    }

    @Override
    protected Notification editNotification(SlackNotification notification) {
      notification.setToken(isSet(token) ? token : notification.getToken());
      notification.setBotName(isSet(botName) ? botName : notification.getBotName());
      return notification;
    }
}
