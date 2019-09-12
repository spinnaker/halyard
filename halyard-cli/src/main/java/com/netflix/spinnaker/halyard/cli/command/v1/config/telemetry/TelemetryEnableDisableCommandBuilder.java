/*
 * Copyright 2019 Armory, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.telemetry;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.CommandBuilder;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class TelemetryEnableDisableCommandBuilder implements CommandBuilder {
  @Setter boolean enable;

  @Override
  public NestableCommand build() {
    return new TelemetryEnableDisableCommand(enable);
  }

  @Parameters(separators = "=")
  private static class TelemetryEnableDisableCommand extends AbstractEnableDisableTelemetryCommand {
    private TelemetryEnableDisableCommand(boolean enable) {
      this.enable = enable;
    }

    @Getter(AccessLevel.PROTECTED)
    boolean enable;
  }
}
