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
 *
 */

package com.netflix.spinnaker.halyard.config.model.v1.security;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OAuth2 extends AuthnMethod {
  @Override
  public String getNodeName() {
    return "oauth2";
  }

  @Override
  public Method getMethod() {
    return Method.OAuth2;
  }

  private Client client = new Client();

  public void setProvider(String provider) {
    if (provider == null) {
      return;
    }

    switch (provider) {
      case "GOOGLE":
        client.getProvider().setGoogle(new HashMap<>());
        client.getRegistration().setGoogle(new HashMap<>());
        client
            .getProvider()
            .getGoogle()
            .put("token-uri", "https://www.googleapis.com/oauth2/v4/token");
        client
            .getProvider()
            .getGoogle()
            .put("authorization-uri", "https://accounts.google.com/o/oauth2/v2/auth");
        client
            .getProvider()
            .getGoogle()
            .put("user-info-uri", "https://www.googleapis.com/oauth2/v3/userinfo");

        client.getRegistration().getGoogle().put("scope", "profile, email");
        client.getRegistration().getUserInfoMapping().setEmail("email");
        client.getRegistration().getUserInfoMapping().setFirstName("given_name");
        client.getRegistration().getUserInfoMapping().setLastName("family_name");
        break;
      case "GITHUB":
        client.getProvider().setGithub(new HashMap<>());
        client.getRegistration().setGithub(new HashMap<>());
        client
            .getProvider()
            .getGithub()
            .put("token-uri", "https://github.com/login/oauth/access_token");
        client
            .getProvider()
            .getGithub()
            .put("authorization-uri", "https://github.com/login/oauth/authorize");
        client.getRegistration().getGithub().put("scope", "user:email");

        client.getProvider().getGithub().put("user-info-uri", "https://api.github.com/user");

        client.getRegistration().getUserInfoMapping().setEmail("email");
        client.getRegistration().getUserInfoMapping().setFirstName("");
        client.getRegistration().getUserInfoMapping().setLastName("name");
        client.getRegistration().getUserInfoMapping().setUsername("login");
        break;
      case "ORACLE":
        final String idcsBaseUrl = "https://idcs-${idcsTenantId}.identity.oraclecloud.com";
        client.getProvider().setOracle(new HashMap<>());
        client.getRegistration().setOracle(new HashMap<>());
        client.getProvider().getOracle().put("token-uri", idcsBaseUrl + "/oauth2/v1/token");
        client
            .getProvider()
            .getOracle()
            .put("authorization-uri", idcsBaseUrl + "/oauth2/v1/authorize");
        client.getRegistration().getOracle().put("scope", "openid urn:opc:idm:__myscopes__");

        client.getProvider().getOracle().put("user-info-uri", idcsBaseUrl + "/oauth2/v1/userinfo");

        client.getRegistration().getUserInfoMapping().setEmail("");
        client.getRegistration().getUserInfoMapping().setFirstName("given_name");
        client.getRegistration().getUserInfoMapping().setLastName("family_name");
        client.getRegistration().getUserInfoMapping().setUsername("preferred_username");
        break;
      case "AZURE":
        client.getProvider().setAzure(new HashMap<>());
        client.getRegistration().setAzure(new HashMap<>());
        client
            .getProvider()
            .getAzure()
            .put("token-uri", "https://login.microsoftonline.com/${azureTenantId}/oauth2/token");
        client
            .getProvider()
            .getAzure()
            .put(
                "authorization-uri",
                "https://login.microsoftonline.com/${azureTenantId}/oauth2/authorize?resource=https://graph.windows.net");
        client.getRegistration().getAzure().put("scope", "profile");
        client.getRegistration().getAzure().put("clientAuthenticationScheme", "query");

        client
            .getProvider()
            .getAzure()
            .put("user-info-uri", "https://graph.windows.net/me?api-version=1.6");

        client.getRegistration().getUserInfoMapping().setEmail("userPrincipalName");
        client.getRegistration().getUserInfoMapping().setFirstName("givenName");
        client.getRegistration().getUserInfoMapping().setLastName("surname");
        break;
      case "OTHER":
        client.getProvider().setOther(new HashMap<>());
        client.getRegistration().setOther(new HashMap<>());
        break;
      default:
        throw new RuntimeException("Unknown provider type " + provider);
    }
  }

  @Data
  public static class Client {
    private Registration registration = new Registration();
    private Provider provider = new Provider();
  }

  @Data
  public static class Registration {
    private UserInfoMapping userInfoMapping = new UserInfoMapping();
    private Map<String, String> userInfoRequirements;
    private Map<String, String> google;
    private Map<String, String> github;
    private Map<String, String> azure;
    private Map<String, String> oracle;
    private Map<String, String> other;
  }

  @Data
  public static class Provider {
    private Map<String, String> google;
    private Map<String, String> github;
    private Map<String, String> azure;
    private Map<String, String> oracle;
    private Map<String, String> other;
  }

  @Data
  public static class UserInfoMapping {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class UserInfoRequirements extends HashMap<String, String> {

    @Override
    public String toString() {
      return this.isEmpty() ? "(empty)" : super.toString();
    }
  }
}
