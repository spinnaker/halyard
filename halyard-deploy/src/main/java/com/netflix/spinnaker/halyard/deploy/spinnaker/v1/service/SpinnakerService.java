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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.config.v1.HalconfigDirectoryStructure;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.CustomProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ProfileFactory;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Data
@Component
@Slf4j
abstract public class SpinnakerService<T> implements HasServiceSettings<T> {
  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  ArtifactService artifactService;

  @Autowired
  Yaml yamlParser;

  @Autowired
  HalconfigDirectoryStructure halconfigDirectoryStructure;

  @Override
  public SpinnakerService<T> getService() {
    return this;
  }

  @Override
  public final String getServiceName() {
    return getType().getServiceName();
  }

  public final String getCanonicalName() {
    return getType().getCanonicalName();
  }

  public String getSpinnakerStagingPath(String deploymentName) {
    return halconfigDirectoryStructure.getStagingPath(deploymentName).toString();
  }

  public ServiceSettings getDefaultServiceSettings(DeploymentConfiguration deploymentConfiguration) {
    File userSettingsFile = new File(
        halconfigDirectoryStructure.getUserServiceSettingsPath(deploymentConfiguration.getName()).toString(),
        getCanonicalName() + ".yml"
    );

    if (userSettingsFile.exists() && userSettingsFile.length() != 0) {
      try {
        log.info("Reading user provided service settings from " + userSettingsFile);
        return objectMapper.convertValue(
            yamlParser.load(new FileInputStream(userSettingsFile)),
            ServiceSettings.class
        );
      } catch (FileNotFoundException e) {
        throw new HalException(Problem.Severity.FATAL, "Unable to read provided user settings: " + e.getMessage(), e);
      }
    } else {
      return new ServiceSettings();
    }
  }

  public boolean isInBillOfMaterials(DeploymentConfiguration deployment) {
    String version = getArtifactService().getArtifactVersion(deployment.getName(), getArtifact());
    return (version != null);
  }

  abstract public Type getType();
  abstract public Class<T> getEndpointClass();
  abstract public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints);

  abstract protected Optional<String> customProfileOutputPath(String profileName);

  public Optional<Profile> customProfile(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings runtimeSettings, Path profilePath, String profileName) {
    return customProfileOutputPath(profileName).flatMap(outputPath -> {
      SpinnakerArtifact artifact = getArtifact();
      ProfileFactory factory = new CustomProfileFactory() {
        @Override
        public SpinnakerArtifact getArtifact() {
          return artifact;
        }

        protected ArtifactService getArtifactService() {
          return artifactService;
        }

        @Override
        protected Path getUserProfilePath() {
          return profilePath;
        }
      };

      return Optional.of(factory.getProfile(profileName, outputPath, deploymentConfiguration, runtimeSettings));
    });
  }

  @EqualsAndHashCode
  public static class Type {
    // Constants for backwards compatibility
    public static final Type CLOUDDRIVER = new Type("clouddriver");
    public static final Type CLOUDDRIVER_BOOTSTRAP = new Type("clouddriver-bootstrap");
    public static final Type CONSUL_CLIENT = new Type("consul-client");
    public static final Type CONSUL_SERVER = new Type("consul-server");
    public static final Type DECK = new Type("deck");
    public static final Type ECHO = new Type("echo");
    public static final Type FIAT = new Type("fiat");
    public static final Type FRONT50 = new Type("front50");
    public static final Type GATE = new Type("gate");
    public static final Type IGOR = new Type("igor");
    public static final Type KAYENTA = new Type("kayenta");
    public static final Type ORCA = new Type("orca");
    public static final Type ORCA_BOOTSTRAP = new Type("orca-bootstrap");
    public static final Type REDIS = new Type("redis");
    public static final Type REDIS_BOOTSTRAP = new Type("redis-bootstrap");
    public static final Type ROSCO = new Type("rosco");
    public static final Type MONITORING_DAEMON = new Type("monitoring-daemon");
    public static final Type VAULT_CLIENT = new Type("vault-client");
    public static final Type VAULT_SERVER = new Type("vault-server");

    @Getter
    final String serviceName;
    @Getter
    final String canonicalName;

    Type(String canonicalName) {
      this.serviceName = "spin-" + canonicalName;
      this.canonicalName = canonicalName;
    }

    @Override
    public String toString() {
      return canonicalName;
    }

    private static String reduceName(String name) {
      return name.replace("-", "").replace("_", "");
    }

    public static Type fromCanonicalName(String canonicalName) {
      String finalName = reduceName(canonicalName);

      Optional<Type> type = getStaticFieldsOfType(Type.class).stream()
          .filter(t -> reduceName(t.getCanonicalName()).equalsIgnoreCase(finalName))
          .findFirst();

      if (type.isPresent()) {
        return type.get();
      }

      return new Type(canonicalName.toLowerCase());
    }

    private static <T> List<T> getStaticFieldsOfType(Class<T> clazz) {
      return Arrays.stream(Type.class.getDeclaredFields())
          .filter(f -> clazz.isAssignableFrom(f.getType()))
          .map(f -> {
            try {
              return (T) f.get(null);
            } catch (IllegalAccessException e) {
              throw new RuntimeException("Unable to access static field " + f.getName());
            }
          }).collect(Collectors.toList());
    }
  }
}
