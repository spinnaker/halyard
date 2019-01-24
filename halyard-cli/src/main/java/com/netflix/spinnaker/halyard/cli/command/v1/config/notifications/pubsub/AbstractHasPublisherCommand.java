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

import com.beust.jcommander.Parameter;
import com.netflix.spinnaker.halyard.cli.command.v1.config.pubsubs.AbstractPubsubCommand;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHasPublisherCommand extends AbstractPubsubCommand {

  @Parameter(description = "The name of the publishers to operate on.", arity = 1)
  List<String> publishers = new ArrayList<>();

  @Override
  public String getMainParameter() {
    return "publisher";
  }

  public String getPublisherName(String defaultName) {
    try {
      return getPublisherName();
    } catch (IllegalArgumentException e) {
      return defaultName;
    }
  }

  public String getPublisherName() {
    switch (publishers.size()) {
      case 0:
        throw new IllegalArgumentException("No topic name supplied");
      case 1:
        return publishers.get(0);
      default:
        throw new IllegalArgumentException("More than one topic supplied");
    }
  }
}
