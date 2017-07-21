/*
 * Copyright 2017 Netflix, Inc.
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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.providers.dockerRegistry.DockerRegistryAccount;
import com.netflix.spinnaker.halyard.deploy.services.v1.GenerateService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.SidecarService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Component
abstract public class EcrTokenRefreshService extends SpinnakerService<EcrTokenRefreshService.EcrTokenRefresh> implements SidecarService {
  protected static final String CONFIG_PATH = "/opt/config/ecr-token-refresh/";
  protected static final String CONFIG_PROFILE_NAME = "config.yaml";
  public static final String PASSWORD_BASE_PATH = "/opt/passwords/";

  @Override
  public SpinnakerArtifact getArtifact() {
    return null;
  }

  @Override
  public Type getType() {
    return Type.ECR_TOKEN_REFRESH;
  }

  @Override
  public Class<EcrTokenRefresh> getEndpointClass() {
    return EcrTokenRefresh.class;
  }

  @Override
  public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    List<Profile> results = new ArrayList<>();
    List<EcrTokenRefreshConfig.EcrRegistry> registries = new ArrayList<>();

    for (DockerRegistryAccount account : deploymentConfiguration.getProviders().getDockerRegistry().getAccounts()) {
      if (!account.isEcr()) {
        continue;
      }

      EcrTokenRefreshConfig.EcrRegistry registry = new EcrTokenRefreshConfig.EcrRegistry();
      registry.setRegistryId(account.getEcrRegistryId());
      registry.setRegion(account.getEcrRegistryRegion());

      String pwdName = registry.getRegistryId() + ".pass";
      String pwdPath = Paths.get(EcrTokenRefreshService.PASSWORD_BASE_PATH, pwdName).toString();

      registry.setPasswordFile(pwdPath);

      registries.add(registry);
    }

    String path = Paths.get(CONFIG_PATH, CONFIG_PROFILE_NAME).toString();
    Profile profile = new Profile(CONFIG_PROFILE_NAME, "1.0.0", path, "");

    EcrTokenRefreshConfig config = new EcrTokenRefreshConfig();
    config.setInterval("30m"); // TODO(orfeasz) make this configurable
    config.setRegistries(registries);

    ObjectMapper strictObjectMapper = new ObjectMapper();
    String profileContents = yamlParser.dump(strictObjectMapper.convertValue(config, Map.class));

    profile.appendContents(profileContents);

    results.add(profile);

    return results;
  }

  @Override
  public List<Profile> getSidecarProfiles(GenerateService.ResolvedConfiguration resolvedConfiguration, SpinnakerService service) {
    List<Profile> results = new ArrayList<>();
    Map<String, Profile> mainProfiles = resolvedConfiguration.getProfilesForService(getType());

    Profile profile = mainProfiles.get(CONFIG_PROFILE_NAME);
    results.add(profile);

    return results;
  }

  public interface EcrTokenRefresh { }

  @EqualsAndHashCode(callSuper = true)
  @Data
  public class Settings extends ServiceSettings {
    Integer port = 3277;
    String address = "localhost";
    String host = "0.0.0.0";
    String scheme = "http";
    Map<String, String> env = new HashMap<>();
  }

  @Override
  protected Optional<String> customProfileOutputPath(String profileName) {
    return Optional.empty();
  }

  @Data
  private static class EcrTokenRefreshConfig {
    @Data
    private static class EcrRegistry {
      String registryId;
      String region;
      String passwordFile;
    }

    String interval;
    List<EcrRegistry> registries;
  }
}
