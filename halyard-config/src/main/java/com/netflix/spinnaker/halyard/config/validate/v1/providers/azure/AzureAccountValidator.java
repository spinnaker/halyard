/*
 * Copyright 2017 Microsoft, Inc.
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

package com.netflix.spinnaker.halyard.config.validate.v1.providers.azure;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourcesOperations;
import com.netflix.spinnaker.clouddriver.azure.client.AzureResourceManagerClient;
import com.netflix.spinnaker.clouddriver.azure.security.AzureCredentials;
import com.netflix.spinnaker.halyard.core.secrets.v1.SecretSessionManager;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.azure.AzureAccount;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class AzureAccountValidator extends Validator<AzureAccount> {
  final private List<AzureCredentials> credentialsList;

  final private String halyardVersion;

  @Autowired
  private SecretSessionManager secretSessionManager;

  @Override
  public void validate(ConfigProblemSetBuilder p, AzureAccount n) {
    String clientId = n.getClientId();
    String appKey = secretSessionManager.decrypt(n.getAppKey());
    String tenantId = n.getTenantId();
    String subscriptionId = n.getSubscriptionId();
    String defaultResourceGroup = n.getDefaultResourceGroup();
    String defaultKeyVault = n.getDefaultKeyVault();
    String packerResourceGroup = n.getPackerResourceGroup();
    String packerStorageAccount = n.getPackerStorageAccount();

    AzureCredentials credentials = new AzureCredentials(
            tenantId,
            clientId,
            appKey,
            subscriptionId,
            defaultKeyVault,
            defaultResourceGroup,
            "SpinnakerHalyard " + halyardVersion);

    AzureResourceManagerClient rmClient = credentials.getResourceManagerClient();
    try {
      rmClient.healthCheck();
      credentialsList.add(credentials);
    } catch (Exception e) {
      // the healthCheck() call always wraps exceptions with a generic "Unable to ping azure." exception, so use the cause instead
      Throwable cause = e.getCause();
      String errorMessage = CloudException.class.isInstance(cause) ? CloudException.class.cast(cause).getBody().getMessage() : cause.getMessage();
      if (errorMessage.contains("AADSTS90002")) {
        p.addProblem(Severity.ERROR, "Tenant Id '" + tenantId + "' is invalid.", "tenantId")
          .setRemediation("Follow instructions here https://aka.ms/azspinconfig to retrieve the tenantId for your subscription.");
      } else if (errorMessage.contains("AADSTS70001")) {
        p.addProblem(Severity.ERROR, "Client Id '" + clientId + "' is invalid for tenant '" + tenantId + "'.", "clientId")
          .setRemediation("Follow instructions here https://aka.ms/azspinconfig to create a service principal and retrieve the clientId.");
      } else if (errorMessage.contains("AADSTS70002")) {
        p.addProblem(Severity.ERROR, "AppKey is invalid.", "appKey.")
          .setRemediation("Follow instructions here https://aka.ms/azspinconfig to specify an appKey when creating a service principal.");
      } else {
        p.addProblem(Severity.ERROR, "Error instantiating Azure credentials: " + errorMessage)
          .setRemediation("Follow instructions here https://aka.ms/azspinconfig to setup azure credentials.");
      }
      return;
    }

    ResourcesOperations resourceOperations = rmClient.getResourceOperations();
    try {
      resourceOperations.get(
        defaultResourceGroup,
        "",
        "",
        "Microsoft.KeyVault/vaults",
        defaultKeyVault,
        "2015-06-01").getBody();
    } catch (Exception e) {
      p.addProblem(Severity.ERROR, "The KeyVault '" + defaultKeyVault +
        "' does not exist in the Resource Group '" + defaultResourceGroup +
        "' for the Subscription '" + subscriptionId + "'.")
        .setRemediation("Follow instructions here https://aka.ms/azspinconfig to setup a default Resource Group and KeyVault.");
    }

    if ((packerResourceGroup != null && !packerResourceGroup.isEmpty()) || 
        (packerStorageAccount != null && !packerStorageAccount.isEmpty())) {
      try {
        resourceOperations.get(
          packerResourceGroup,
          "",
          "",
          "Microsoft.Storage/storageAccounts",
          packerStorageAccount,
          "2016-01-01").getBody();
      } catch (Exception e) {
        p.addProblem(Severity.ERROR, "The Packer storage account '" + packerStorageAccount +
          "' does not exist in the Resource Group '" + packerResourceGroup +
          "' for the Subscription '" + subscriptionId + "'.")
          .setRemediation("If you want to use Packer to bake images, create a storage account in the specified resource group. Otherwise, leave the packerStorageAccount and packerResourceGroup blank.");
      }
    }
  }
}