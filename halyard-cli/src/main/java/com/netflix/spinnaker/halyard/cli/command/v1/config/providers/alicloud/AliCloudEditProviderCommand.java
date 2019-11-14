/*
 * Copyright 2019 Alibaba, Group.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.alicloud;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.AbstractEditProviderCommand;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.alicloud.AliCloudAccount;
import com.netflix.spinnaker.halyard.config.model.v1.providers.alicloud.AliCloudProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Parameters(separators = "=")
@Data
public class AliCloudEditProviderCommand
    extends AbstractEditProviderCommand<AliCloudAccount, AliCloudProvider> {
  String shortDescription = "Set provider-wide properties for the alicloud provider";

  String longDescription =
      "The alicloud provider requires a central \"Managing Account\" to authenticate on behalf of other "
          + "AliCLoud accounts, or act as your sole, credential-based account. Since this configuration, as well as some defaults, span "
          + "all AliCLoud accounts, it is generally required to edit the AliCLoud provider using this command.";

  @Override
  protected String getProviderName() {
    return Provider.ProviderType.ALICLOUD.getName();
  }

  @Override
  protected Provider editProvider(AliCloudProvider provider) {
    return provider;
  }
}
