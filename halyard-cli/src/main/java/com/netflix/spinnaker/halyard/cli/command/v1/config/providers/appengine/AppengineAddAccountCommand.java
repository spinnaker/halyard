/*
 * Copyright 2017 Google, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.appengine;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.account.AbstractAddAccountCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.google.CommonGoogleCommandProperties;
import com.netflix.spinnaker.halyard.cli.command.v1.converter.LocalFileConverter;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.providers.appengine.AppengineAccount;

import static com.netflix.spinnaker.halyard.config.model.v1.providers.appengine.AppengineAccount.GcloudReleaseTrack;

@Parameters(separators = "=")
public class AppengineAddAccountCommand extends AbstractAddAccountCommand {
  protected String getProviderName() {
    return "appengine";
  }

  @Parameter(
      names = "--project",
      required = true,
      description = CommonGoogleCommandProperties.PROJECT_DESCRIPTION
  )
  private String project;

  @Parameter(
      names = "--json-path",
      converter = LocalFileConverter.class,
      description = CommonGoogleCommandProperties.JSON_PATH_DESCRIPTION
  )
  private String jsonPath;

  @Parameter(
      names = "--local-repository-directory",
      description = AppengineCommandProperties.LOCAL_REPOSITORY_DIRECTORY_DESCRIPTION
  )
  private String localRepositoryDirectory = "/var/tmp/clouddriver";

  @Parameter(
      names = "--git-https-username",
      description = AppengineCommandProperties.GIT_HTTPS_USERNAME_DESCRIPTION
  )
  private String gitHttpsUsername;

  @Parameter(
      names = "--git-https-password",
      description = AppengineCommandProperties.GIT_HTTPS_PASSWORD_DESCRIPTION,
      password = true
  )
  private String gitHttpsPassword;

  @Parameter(
      names = "--github-oauth-access-token",
      description = AppengineCommandProperties.GITHUB_OAUTH_ACCESS_TOKEN_DESCRIPTION,
      password = true
  )
  private String githubOAuthAccessToken;

  @Parameter(
      names = "--ssh-private-key-file-path",
      converter = LocalFileConverter.class,
      description = AppengineCommandProperties.SSH_PRIVATE_KEY_FILE_PATH
  )
  private String sshPrivateKeyFilePath;

  @Parameter(
      names = "--ssh-private-key-passphrase",
      description = AppengineCommandProperties.SSH_PRIVATE_KEY_PASSPHRASE,
      password = true
  )
  private String sshPrivateKeyPassphrase;
  
  @Parameter(
      names = "--ssh-known-hosts-file-path",
      converter = LocalFileConverter.class,
      description = AppengineCommandProperties.SSH_KNOWN_HOSTS_FILE_PATH
  )
  private String sshKnownHostsFilePath;

  @Parameter(
      names = "--ssh-trust-unknown-hosts",
      description = AppengineCommandProperties.SSH_TRUST_UNKNOWN_HOSTS,
      arity = 1
  )
  private boolean sshTrustUnknownHosts = false;

  @Parameter(
      names = "--gcloud-release-track",
      description = AppengineCommandProperties.GCLOUD_RELEASE_TRACK
  )
  private GcloudReleaseTrack gcloudReleaseTrack;

  @Override
  protected Account buildAccount(String accountName) {
    AppengineAccount account = (AppengineAccount) new AppengineAccount().setName(accountName);
    account.setProject(project).setJsonPath(jsonPath);
    
    account.setLocalRepositoryDirectory(localRepositoryDirectory).setGitHttpsUsername(gitHttpsUsername)
            .setGitHttpsPassword(gitHttpsPassword).setGithubOAuthAccessToken(githubOAuthAccessToken)
            .setSshPrivateKeyFilePath(sshPrivateKeyFilePath).setSshPrivateKeyPassphrase(sshPrivateKeyPassphrase)
            .setSshKnownHostsFilePath(sshKnownHostsFilePath).setSshTrustUnknownHosts(sshTrustUnknownHosts)
            .setGcloudReleaseTrack(gcloudReleaseTrack);

    return account;
  }

  @Override
  protected Account emptyAccount() {
    return new AppengineAccount();
  }
}
