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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiFormatUtils;
import lombok.AccessLevel;
import lombok.Getter;

@Parameters(separators = "=")
public class EditLocationCommand extends AbstractConfigCommand {
    @Getter(AccessLevel.PUBLIC)
    private String commandName = "edit";

    @Getter(AccessLevel.PUBLIC)
    private String description = "Set the desired Spinnaker deployment location.";

    @Parameter(
            names = "--location",
            required = true,
            description = "The namespace to deploy Spinnaker to within Kubernetes."
    )
    private String location;

    @Override
    protected void executeThis() {
        String currentDeployment = getCurrentDeployment();
        new OperationHandler<Void>()
                .setOperation(Daemon.setLocation(currentDeployment, !noValidate, location))
                .setSuccessMessage("Spinnaker has been configured to update/install into \"" + location + "\". "
                        + "Deploy Spinnaker to this location with `hal deploy apply`.")
                .setFailureMesssage("Failed to update location.")
                .get();
    }
}
