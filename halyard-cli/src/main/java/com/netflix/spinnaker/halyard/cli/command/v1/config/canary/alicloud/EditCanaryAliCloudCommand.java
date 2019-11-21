/*
 * Copyright 2019 Alibaba Group.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.canary.alicloud;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.canary.AbstractEditCanaryServiceIntegrationCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.canary.account.CanaryUtils;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.ui.v1.AnsiUi;
import com.netflix.spinnaker.halyard.config.model.v1.canary.AbstractCanaryServiceIntegration;
import com.netflix.spinnaker.halyard.config.model.v1.canary.Canary;
import com.netflix.spinnaker.halyard.config.model.v1.canary.alicloud.AliCloudCanaryServiceIntegration;

@Parameters(separators = "=")
public class EditCanaryAliCloudCommand extends AbstractEditCanaryServiceIntegrationCommand {

  @Override
  protected String getServiceIntegration() {
    return "alicloud";
  }

  @Parameter(
      names = "--oss-enabled",
      arity = 1,
      description = "Whether or not to enable oss as a persistent store (*Default*: `false`).")
  private Boolean ossEnabled;

  @Override
  protected void executeThis() {
    String currentDeployment = getCurrentDeployment();
    // Disable validation here, since we don't want an illegal config to prevent us from fixing it.
    Canary canary =
        new OperationHandler<Canary>()
            .setFailureMesssage("Failed to get canary.")
            .setOperation(Daemon.getCanary(currentDeployment, false))
            .get();

    int originalHash = canary.hashCode();
    AliCloudCanaryServiceIntegration aliCloudCanaryServiceIntegration =
        (AliCloudCanaryServiceIntegration)
            CanaryUtils.getServiceIntegrationByClass(
                canary, AliCloudCanaryServiceIntegration.class);

    aliCloudCanaryServiceIntegration.setOssEnabled(
        isSet(ossEnabled) ? ossEnabled : aliCloudCanaryServiceIntegration.isOssEnabled());

    if (aliCloudCanaryServiceIntegration.isOssEnabled()) {
      aliCloudCanaryServiceIntegration
          .getAccounts()
          .forEach(
              a ->
                  a.getSupportedTypes()
                      .add(AbstractCanaryServiceIntegration.SupportedTypes.CONFIGURATION_STORE));
      aliCloudCanaryServiceIntegration
          .getAccounts()
          .forEach(
              a ->
                  a.getSupportedTypes()
                      .add(AbstractCanaryServiceIntegration.SupportedTypes.OBJECT_STORE));
    } else {
      aliCloudCanaryServiceIntegration
          .getAccounts()
          .forEach(
              a ->
                  a.getSupportedTypes()
                      .remove(AbstractCanaryServiceIntegration.SupportedTypes.CONFIGURATION_STORE));
      aliCloudCanaryServiceIntegration
          .getAccounts()
          .forEach(
              a ->
                  a.getSupportedTypes()
                      .remove(AbstractCanaryServiceIntegration.SupportedTypes.OBJECT_STORE));
    }

    if (originalHash == canary.hashCode()) {
      AnsiUi.failure("No changes supplied.");
      return;
    }

    new OperationHandler<Void>()
        .setOperation(Daemon.setCanary(currentDeployment, !noValidate, canary))
        .setFailureMesssage("Failed to edit canary analysis OSS service integration settings.")
        .setSuccessMessage("Successfully edited canary analysis OSS service integration settings.")
        .get();
  }
}
