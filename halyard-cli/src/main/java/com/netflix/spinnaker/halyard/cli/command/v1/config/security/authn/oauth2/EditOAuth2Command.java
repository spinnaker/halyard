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

package com.netflix.spinnaker.halyard.cli.command.v1.config.security.authn.oauth2;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.AbstractConfigCommand;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.config.model.v1.security.OAuth2;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Parameters(separators = "=")
public class EditOAuth2Command extends AbstractConfigCommand {

  @Parameter(
      names = "--client-id",
      description = "The OAuth client ID you have configured with your OAuth provider.")
  private String clientId;

  @Parameter(
      names = "--client-secret",
      description = "The OAuth client secret you have configured with your OAuth provider.")
  private String clientSecret;

  @Parameter(
      names = "--access-token-uri",
      description = "The access token uri for your OAuth provider.")
  private String accessTokenUri;

  @Parameter(
      names = "--user-authorization-uri",
      description = "The user authorization uri for your OAuth provider.")
  private String userAuthorizationUri;

  @Parameter(names = "--user-info-uri", description = "The user info uri for your OAuth provider.")
  private String userInfoUri;

  @Parameter(
      names = "--client-authentication-scheme",
      description = "The client authentication scheme for your OAuth provider.")
  private String clientAuthenticationScheme;

  @Parameter(names = "--scope", description = "The scope for your OAuth provider.")
  private String scope;

  @Parameter(
      names = "--user-info-mapping-email",
      description = "The email field returned from your OAuth provider.")
  private String userInfoMappingEmail;

  @Parameter(
      names = "--user-info-mapping-first-name",
      description = "The first name field returned from your OAuth provider.")
  private String userInfoMappingFirstName;

  @Parameter(
      names = "--user-info-mapping-last-name",
      description = "The last name field returned from your OAuth provider.")
  private String userInfoMappingLastName;

  @Parameter(
      names = "--user-info-mapping-username",
      description = "The username field returned from your OAuth provider.")
  private String userInfoMappingUsername;

  @Parameter(
      names = "--provider",
      description =
          "The OAuth provider handling authentication. The supported options are Google, GitHub, Oracle, Azure and Other")
  private String provider;

  @Parameter(
      names = "--pre-established-redirect-uri",
      description =
          "The externally accessible URL for Gate. For use with load balancers that "
              + "do any kind of address manipulation for Gate traffic, such as an SSL terminating load "
              + "balancer.")
  private String preEstablishedRedirectUri;

  @DynamicParameter(
      names = "--user-info-requirements",
      description =
          "The map of requirements the userInfo request must have. This is used to "
              + "restrict user login to specific domains or having a specific attribute. Use equal "
              + "signs between key and value, and additional key/value pairs need to repeat the "
              + "flag. Example: '--user-info-requirements foo=bar --userInfoRequirements baz=qux'.")
  private OAuth2.UserInfoRequirements userInfoRequirements = new OAuth2.UserInfoRequirements();

  private OAuth2 editOAuth2(OAuth2 oAuth2) {
    OAuth2.Client client = oAuth2.getClient();
    Map<String, String> registration = new HashMap<>();
    Map<String, String> provider = new HashMap<>();
    if (isSet(clientId)) {
      registration.put("client-id", clientId);
    }

    if (isSet(clientSecret)) {
      registration.put("client-secret", clientSecret);
    }
    if (isSet(accessTokenUri)) {
      provider.put("token-uri", accessTokenUri);
    }

    if (isSet(userAuthorizationUri)) {
      provider.put("authorization-uri", userAuthorizationUri);
    }

    if (isSet(scope)) {
      registration.put("scope", scope);
    }

    if (isSet(clientAuthenticationScheme)) {
      registration.put("clientAuthenticationScheme", clientAuthenticationScheme);
    }

    if (isSet(userInfoUri)) {
      provider.put("user-info-uri", userInfoUri);
    }

    oAuth2.setProvider(this.provider);
    OAuth2.UserInfoMapping userInfoMapping = client.getRegistration().getUserInfoMapping();

    Optional.ofNullable(userInfoMappingEmail).ifPresent(userInfoMapping::setEmail);
    Optional.ofNullable(userInfoMappingFirstName).ifPresent(userInfoMapping::setFirstName);
    Optional.ofNullable(userInfoMappingLastName).ifPresent(userInfoMapping::setLastName);
    Optional.ofNullable(userInfoMappingUsername).ifPresent(userInfoMapping::setUsername);

    if (!userInfoRequirements.isEmpty()) {
      oAuth2.getClient().getRegistration().setUserInfoRequirements(userInfoRequirements);
    }

    switch (this.provider) {
      case "GOOGLE":
        if (client.getRegistration() != null) {
          client.getRegistration().getGoogle().putAll(registration);
        } else {
          client.getRegistration().setGoogle(registration);
        }

        if (client.getProvider() != null) {
          client.getProvider().getGoogle().putAll(provider);
        } else {
          client.getProvider().setGoogle(provider);
        }
        break;

      case "GITHUB":
        if (client.getRegistration() != null) {
          client.getRegistration().getGithub().putAll(registration);
        } else {
          client.getRegistration().setGithub(registration);
        }

        if (client.getProvider() != null) {
          client.getProvider().getGithub().putAll(provider);
        } else {
          client.getProvider().setGithub(provider);
        }
        break;

      case "ORACLE":
        if (client.getRegistration() != null) {
          client.getRegistration().getOracle().putAll(registration);
        } else {
          client.getRegistration().setOracle(registration);
        }

        if (client.getProvider() != null) {
          client.getProvider().getOracle().putAll(provider);
        } else {
          client.getProvider().setOracle(provider);
        }
        break;

      case "AZURE":
        if (client.getRegistration() != null) {
          client.getRegistration().getAzure().putAll(registration);
        } else {
          client.getRegistration().setAzure(registration);
        }

        if (client.getProvider() != null) {
          client.getProvider().getAzure().putAll(provider);
        } else {
          client.getProvider().setAzure(provider);
        }
        break;

      case "OTHER":
        if (client.getRegistration() != null) {
          client.getRegistration().getOther().putAll(registration);
        } else {
          client.getRegistration().setOther(registration);
        }

        if (client.getProvider() != null) {
          client.getProvider().getOther().putAll(provider);
        } else {
          client.getProvider().setOther(provider);
        }
        break;
    }

    return oAuth2;
  }

  @Override
  public String getCommandName() {
    return "edit";
  }

  @Override
  protected void executeThis() {
    String currentDeployment = getCurrentDeployment();

    // Disable validation here, since we don't want an illegal config to prevent us from fixing it.
    OAuth2 oAuth2 =
        new OperationHandler<OAuth2>()
            .setOperation(Daemon.getOAuth2(currentDeployment, false))
            .setFailureMesssage("Failed to get OAuth2 config.")
            .get();

    new OperationHandler<Void>()
        .setOperation(Daemon.setOAuth2(currentDeployment, !noValidate, editOAuth2(oAuth2)))
        .setFailureMesssage("Failed to edit OAuth2 config.")
        .setSuccessMessage("Successfully edited OAuth2 config.")
        .get();
  }

  public String getShortDescription() {
    return "Edit OAuth2 config.";
  }
}
