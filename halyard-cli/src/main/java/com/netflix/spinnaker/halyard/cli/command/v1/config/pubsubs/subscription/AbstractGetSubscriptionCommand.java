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

package com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs.subscription;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs.subscription.AbstractHasSubscriptionCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiFormatUtils;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiUi;
import com.netflix.spinnaker.halyard.config.model.v1.node.Subscription;
import lombok.Getter;

@Parameters(separators = "=")
abstract class AbstractGetSubscriptionCommand extends AbstractHasSubscriptionCommand {
  public String getShortDescription() {
    return "Get the specified subscription details for the " + getPubsubName() + " pubsub.";
  }

  @Getter
  private String commandName = "get";

  @Override
  protected void executeThis() {
    getSubscription(getSubscriptionName());
  }

  private Subscription getSubscription(String subscriptionName) {
    String currentDeployment = getCurrentDeployment();
    String pubsubName = getPubsubName();
    return new OperationHandler<Subscription>()
        .setFailureMesssage("Failed to get subscription " + subscriptionName + " for pubsub " + pubsubName + ".")
        .setSuccessMessage("Subscription " + subscriptionName + ": ")
        .setFormat(AnsiFormatUtils.Format.STRING)
        .setUserFormatted(true)
        .setOperation(Daemon.getSubscription(currentDeployment, pubsubName, subscriptionName, false))
        .get();
  }
}
