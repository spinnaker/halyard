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
 *
 */

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.alicloud;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.account.AbstractEditAccountCommand;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.providers.alicloud.AliCloudAccount;
import java.util.List;

@Parameters(separators = "=")
public class AliCloudEditAccountCommand extends AbstractEditAccountCommand<AliCloudAccount> {
  @Override
  protected String getProviderName() {
    return "alicloud";
  }

  @Parameter(
      names = "--regions",
      variableArity = true,
      description = AliCloudCommandProperties.REGIONS_DESCRIPTION)
  private List<String> regions;

  @Parameter(
      names = "--add-region",
      description = "Add this region to the list of managed regions.")
  private String addRegion;

  @Parameter(
      names = "--remove-region",
      description = "Remove this region from the list of managed regions.")
  private String removeRegion;

  @Parameter(names = "--access-key-id", description = AliCloudCommandProperties.REGIONS_DESCRIPTION)
  private String accessKeyId;

  @Parameter(
      names = "--access-secret-key",
      description = AliCloudCommandProperties.REGIONS_DESCRIPTION)
  private String accessSecretKey;

  @Override
  protected Account editAccount(AliCloudAccount account) {
    account.setAccessKeyId(isSet(accessKeyId) ? accessKeyId : account.getAccessKeyId());
    account.setAccessSecretKey(
        isSet(accessSecretKey) ? accessSecretKey : account.getAccessSecretKey());
    try {
      account.setRegions(updateStringList(account.getRegions(), regions, addRegion, removeRegion));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Set either --regions or --[add/remove]-region");
    }

    return account;
  }
}
