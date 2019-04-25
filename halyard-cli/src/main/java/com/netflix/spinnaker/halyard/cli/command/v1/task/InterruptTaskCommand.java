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
 */

package com.netflix.spinnaker.halyard.cli.command.v1.task;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=")
public class InterruptTaskCommand extends NestableCommand {
  @Getter(AccessLevel.PUBLIC)
  private String commandName = "interrupt";

  @Getter(AccessLevel.PUBLIC)
  private String shortDescription = "Interrupt (attempt to kill) a given task.";

  @Override
  public String getMainParameter() {
    return "uuid";
  }

  @Parameter(description = "The UUID of the task to interrupt", arity = 1)
  List<String> uuids = new ArrayList<>();

  private String getUuid() {
    switch (uuids.size()) {
      case 0:
        throw new IllegalArgumentException("No UUID supplied.");
      case 1:
        return uuids.get(0);
      default:
        throw new IllegalArgumentException("More than one UUID supplied");
    }
  }

  @Override
  protected void executeThis() {
    Daemon.interruptTask(getUuid());
  }
}
