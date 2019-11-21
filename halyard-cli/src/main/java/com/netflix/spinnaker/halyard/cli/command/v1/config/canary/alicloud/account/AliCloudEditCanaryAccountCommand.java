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

package com.netflix.spinnaker.halyard.cli.command.v1.config.canary.alicloud.account;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.canary.CommonCanaryCommandProperties;
import com.netflix.spinnaker.halyard.cli.command.v1.config.canary.account.AbstractEditCanaryAccountCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.canary.alicloud.CommonCanaryAliCloudCommandProperties;
import com.netflix.spinnaker.halyard.config.model.v1.canary.AbstractCanaryAccount;
import com.netflix.spinnaker.halyard.config.model.v1.canary.alicloud.AliCloudCanaryAccount;

@Parameters(separators = "=")
public class AliCloudEditCanaryAccountCommand
    extends AbstractEditCanaryAccountCommand<AliCloudCanaryAccount> {

  @Override
  protected String getServiceIntegration() {
    return "alicloud";
  }

  @Parameter(names = "--bucket", description = CommonCanaryCommandProperties.BUCKET)
  private String bucket;

  @Parameter(
      names = "--region",
      description = CommonCanaryAliCloudCommandProperties.REGION_DESCRIPTION)
  private String region;

  @Parameter(names = "--root-folder", description = CommonCanaryCommandProperties.ROOT_FOLDER)
  private String rootFolder;

  @Parameter(
      names = "--endpoint",
      description = CommonCanaryAliCloudCommandProperties.ENDPOINT_DESCRIPTION)
  private String endpoint;

  @Parameter(
      names = "--access-key-id",
      description = CommonCanaryAliCloudCommandProperties.ACCESS_KEY_ID_DESCRIPTION)
  private String accessKeyId;

  @Parameter(
      names = "--secret-access-key",
      description = CommonCanaryAliCloudCommandProperties.SECRET_KEY_DESCRIPTION,
      password = true)
  private String secretAccessKey;

  @Override
  protected AbstractCanaryAccount editAccount(AliCloudCanaryAccount account) {
    account.setBucket(isSet(bucket) ? bucket : account.getBucket());
    account.setRegion(isSet(region) ? region : account.getRegion());
    account.setRootFolder(isSet(rootFolder) ? rootFolder : account.getRootFolder());
    account.setEndpoint(isSet(endpoint) ? endpoint : account.getEndpoint());
    account.setAccessKeyId(isSet(accessKeyId) ? accessKeyId : account.getAccessKeyId());
    account.setSecretAccessKey(
        isSet(secretAccessKey) ? secretAccessKey : account.getSecretAccessKey());

    return account;
  }
}
