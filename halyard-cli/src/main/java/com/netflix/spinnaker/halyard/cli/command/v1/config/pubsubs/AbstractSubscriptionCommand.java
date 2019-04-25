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
 *
 */

package com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs.subscription.AbstractHasSubscriptionCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs.subscription.DeleteSubscriptionCommandBuilder;
import com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs.subscription.GetSubscriptionCommandBuilder;
import com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs.subscription.ListSubscriptionsCommandBuilder;

@Parameters(separators = "=")
public abstract class AbstractSubscriptionCommand extends AbstractHasSubscriptionCommand {
  @Override
  public String getCommandName() {
    return "subscription";
  }

  @Override
  public String getShortDescription() {
    return "Manage and view Spinnaker configuration for the " + getPubsubName() + " pubsub's subscription";
  }

  protected AbstractSubscriptionCommand() {
    registerSubcommand(new DeleteSubscriptionCommandBuilder()
        .setPubsubName(getPubsubName())
        .build()
    );

    registerSubcommand(new GetSubscriptionCommandBuilder()
        .setPubsubName(getPubsubName())
        .build()
    );

    registerSubcommand(new ListSubscriptionsCommandBuilder()
        .setPubsubName(getPubsubName())
        .build()
    );
  }

  @Override
  protected void executeThis() {
    showHelp();
  }
}
