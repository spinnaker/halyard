/*
 * Copyright 2019 Google, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.notifications.pubsub;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiUi;
import com.netflix.spinnaker.halyard.config.model.v1.node.Publisher;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;

@Parameters(separators = "=")
public abstract class AbstractEditPublisherCommand<T extends Publisher> extends
    AbstractHasPublisherCommand {

  @Getter(AccessLevel.PROTECTED)
  private Map<String, NestableCommand> subcommands = new HashMap<>();

  @Getter(AccessLevel.PUBLIC)
  private String commandName = "edit";

  protected abstract Publisher editPublisher(T publisher);

  public String getShortDescription() {
    return "Edit an publisher in the " + getPubsubName() + " pubsub.";
  }

  @Override
  protected void executeThis() {
    String publisherName = getPublisherName();
    String pubsubName = getPubsubName();
    String currentDeployment = getCurrentDeployment();
    // Disable validation here, since we don't want an illegal config to prevent us from fixing it.
    Publisher publisher = new OperationHandler<Publisher>()
        .setFailureMesssage(
            "Failed to get publisher " + publisherName + " for pubsub " + pubsubName + ".")
        .setOperation(Daemon.getPublisher(currentDeployment, pubsubName, publisherName, false))
        .get();

    int originalHash = publisher.hashCode();

    publisher = editPublisher((T) publisher);

    if (originalHash == publisher.hashCode()) {
      AnsiUi.failure("No changes supplied.");
      return;
    }

    new OperationHandler<Void>()
        .setFailureMesssage(
            "Failed to edit publisher " + publisherName + " for pubsub " + pubsubName + ".")
        .setSuccessMessage(
            "Successfully edited publisher " + publisherName + " for pubsub " + pubsubName + ".")
        .setOperation(Daemon
            .setPublisher(currentDeployment, pubsubName, publisherName, !noValidate, publisher))
        .get();
  }
}
