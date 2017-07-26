/*
 * Copyright 2017 Target, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiFormatUtils;
import lombok.AccessLevel;
import lombok.Getter;

@Parameters(separators = "=")
public class LocationCommand extends AbstractConfigCommand {

    @Getter(AccessLevel.PUBLIC)
    private String commandName = "location";

    @Getter(AccessLevel.PUBLIC)
    private String description = "Configure & view the current deployment of Spinnaker's target location.";

    public LocationCommand() {
        registerSubcommand(new EditLocationCommand());
    }

    @Override
    protected void executeThis() {
        String currentDeployment = getCurrentDeployment();
        new OperationHandler<String>()
                .setOperation(Daemon.getLocation(currentDeployment, !noValidate))
                .setFailureMesssage("Failed to load location for Spinnaker deployment.")
                .get();
    }


}
