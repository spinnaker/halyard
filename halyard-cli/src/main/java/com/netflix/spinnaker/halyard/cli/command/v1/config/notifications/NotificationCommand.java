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
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.slack.SlackCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.email.EmailCommand;
//import com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.sms.SmsCommand;
//import com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.hipchat.HipchatCommand;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * This is a top-level command for dealing with your halconfig.
 *
 * Usage is `$ hal config`
 */
@Parameters(separators = "=")
public class NotificationCommand extends NestableCommand {
  @Getter(AccessLevel.PUBLIC)
  private String commandName = "notification";

  @Getter(AccessLevel.PUBLIC)
  private String description = "Configure, validate, and view the specified notification.";

  public NotificationCommand() {
    registerSubcommand(new SlackCommand());
    registerSubcommand(new EmailCommand());
//    registerSubcommand(new SmsCommand());
//    registerSubcommand(new HipchatCommand());
  }

  @Override
  protected void executeThis() {
        showHelp();
    }
}
