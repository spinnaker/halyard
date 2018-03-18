/*
 * Copyright 2018 Google, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.canary;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.CommandBuilder;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.security.authz.AbstractEnableDisableAuthzCommand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class EnableDisableCanaryCommandBuilder implements CommandBuilder {
  @Setter
  boolean enable;

  @Override
  public NestableCommand build() {
    return new EnableDisableCanaryCommand(enable);
  }

  @Parameters(separators = "=")
  private static class EnableDisableCanaryCommand extends AbstractEnableDisableCanaryCommand {
    private EnableDisableCanaryCommand(boolean enable) {
      this.enable = enable;
    }

    @Getter(AccessLevel.PROTECTED)
    boolean enable;
  }
}
