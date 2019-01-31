/*
 * Copyright 2018 Mirantis, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.artifacts.helm;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.artifacts.account.AbstractArtifactEditAccountCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.converter.LocalFileConverter;
import com.netflix.spinnaker.halyard.config.model.v1.artifacts.helm.HelmArtifactAccount;
import com.netflix.spinnaker.halyard.config.model.v1.node.ArtifactAccount;
import com.netflix.spinnaker.halyard.config.model.v1.node.Secret;
import com.netflix.spinnaker.halyard.config.model.v1.node.SecretFile;

@Parameters(separators = "=")
public class HelmEditArtifactAccountCommand extends AbstractArtifactEditAccountCommand<HelmArtifactAccount> {
  @Parameter(
      names = "--repository",
      description = "Helm chart repository"
  )
  private String repository;
  @Parameter(
      names = "--username",
      description = "Helm chart repository basic auth username"
  )
  private String username;
  @Parameter(
      names = "--password",
      password = true,
      description = "Helm chart repository basic auth password"
  )
  private String password;
  @Parameter(
      names = "--username-password-file",
      converter = LocalFileConverter.class,
      description = "File containing \"username:password\" to use for helm chart repository basic auth"
  )
  private String usernamePasswordFile;

  @Override
  protected ArtifactAccount editArtifactAccount(HelmArtifactAccount account) {
    account.setRepository(isSet(repository) ? repository : account.getRepository());
    account.setUsername(isSet(username) ? username : account.getUsername());
    account.setPassword(isSet(password) ? password : account.getPassword());
    account.setUsernamePasswordFile(isSet(usernamePasswordFile) ? usernamePasswordFile : account.getUsernamePasswordFile());
    return account;
  }

  @Override
  protected String getArtifactProviderName() {
        return "helm";
    }
}
