/*
 * Copyright 2019 Andreas Bergmeier
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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.KubernetesUtil;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesImageDescription;
import com.netflix.spinnaker.halyard.config.model.v1.node.CustomSizing;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.node.SidecarConfig;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.registry.v1.Versions;
import com.netflix.spinnaker.halyard.core.resource.v1.JinjaJarResource;
import com.netflix.spinnaker.halyard.core.resource.v1.TemplatedResource;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.services.v1.GenerateService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ConfigSource;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.CustomCaSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.HasServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerMonitoringDaemonService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.DistributedService.DeployPriority;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.SidecarService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.KubernetesSharedServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2Utils.SecretMountPair;
import io.fabric8.utils.Strings;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class KubernetesV2ServiceCa {

  public static class MappedFileHash {
    private String f;
    private int hash;
    public MappedFileHash(String f) {
      this.f = f;
      this.hash = KubernetesV2Hash.forContent(f).hashCode();
    }

    String getKey() {
      return f;
    }

    int getValue() {
      return hash;
    }
  }

  public static String getJvmKeyStorePath(ServiceSettings settings) {
    CustomCaSettings customCas = settings.getCustomCas();
    if (customCas == null) {
      return null;
    }

    List<String> files = customCas.getFiles();
    if (files == null || files.isEmpty()) {
      return null;
    }

    return "/var/run/java/cacerts";
  }

  private static String getBasename(String path) {
    int pathIndex = path.lastIndexOf("/");
    if (pathIndex == -1) {
      return path;
    }

    return path.substring(pathIndex + 1);
  }

  private static String[] splitExtension(String path) {
    int lastIndex = path.lastIndexOf(".");
    if (lastIndex == -1) {
      String[] array = new String[1];
      array[0] = path;
      return array;
    }

    String[] array = new String[2];
    array[0] = path.substring(0, lastIndex);
    array[1] = path.substring(lastIndex + 1);
    return array;
  }

  public static List<String> initContainers(ServiceSettings settings, ObjectMapper objectMapper) {
    String jvmKeyStorePath = getJvmKeyStorePath(settings);
    if (jvmKeyStorePath == null || jvmKeyStorePath.isEmpty()) {
      return new ArrayList<String>();
    }

    List<String> cpCmd = new ArrayList<>();
    Collections.addAll(cpCmd, "cp", "/etc/java/cacerts", jvmKeyStorePath);

    List<MappedFileHash> fileHashes = settings.getCustomCas().getFiles()
      .stream()
      .map(f ->
        new MappedFileHash(f)
      )
      .collect(Collectors.toList());

    Map<String, Integer> basenames = new HashMap<String, Integer>();
    Map<String, String> fileAlias = new HashMap<String, String>();
    for (MappedFileHash fileHash : fileHashes) {
      String basename = getBasename(fileHash.getKey());
      String[] splitName = splitExtension(basename);
      String alias;
      String file;
      if (splitName.length == 1) {
        alias = String.format("%s_%h", splitName[0], fileHash.getValue());
        file = alias;
      } else {
        alias = String.format("%s_%h.%s", splitName[0], fileHash.getValue(), splitName[1]);
        file = String.format("%s_%h", splitName[0], fileHash.getValue());
      }
      
      fileAlias.put(file, alias);
    }

    List<List<String>> cmd = new ArrayList<>();
    cmd
      .add(cpCmd);

    cmd
      .addAll(fileAlias
        .entrySet()
        .stream()
        .sorted(Comparator.comparing(Map.Entry::getKey, Comparator.naturalOrder()))
        .map(e ->
          new ArrayList<String>() {{
            Collections.addAll(this, "keytool", "import", "-alias", e.getKey(), "-file", String.format("/var/run/java/certs/%s", e.getValue()), "-keyStore", jvmKeyStorePath);
          }}
        )
        .collect(Collectors.toList()));

    String chained = cmd
      .stream()
      .map(l ->
        l
          .stream()
          .collect(Collectors.joining(" "))
      )
      .collect(Collectors.joining(" & "));

    List<String> shCmd = new ArrayList<>();
    Collections.addAll(shCmd, "sh", "-c", chained);

    Map<String, Object> initSettings = new HashMap<String, Object>() {{
      put("name", "init-custom-ca");
      put("image", "openjdk:8-jre-alpine");
      put("command", shCmd);
    }};
    String initSettingsString;
    try {
      initSettingsString = objectMapper.writeValueAsString(initSettings);
    } catch (JsonProcessingException e) {
      throw new HalException(Problem.Severity.FATAL, "Invalid init container format: " + e.getMessage(), e);
    }

    return new ArrayList<String>() {{
      add(initSettingsString);
    }};
  }

  public static class SecretConfigSource {
    KubernetesV2Utils.SecretSpec spec;
    ConfigSource config;
  }

  public static SecretConfigSource secretConfig(SpinnakerRuntimeSettings runtimeSettings,
      String canonicalSpinnakerServiceName, String namespace, CustomCaSettings customCas, String serviceName) {
    KubernetesV2Utils.SecretSpec spec = createSecretSpec(runtimeSettings, canonicalSpinnakerServiceName, namespace, customCas, serviceName);
    SecretConfigSource config = new SecretConfigSource();
    config.spec = spec;
    config.config = new ConfigSource()
        .setId(spec.name)
        .setMountPath("/var/run/java/certs");
    return config;
  }

  private static KubernetesV2Utils.SecretSpec createSecretSpec(SpinnakerRuntimeSettings runtimeSettings,
      String canonicalSpinnakerServiceName, String namespace, CustomCaSettings customCas, String serviceName) {

    List<SecretMountPair> files = customCas.getFiles()
        .stream()
        .map(f -> {
            return new File(f);
        })
        .map(SecretMountPair::new)
        .collect(Collectors.toList());


    String secretNamePrefix = serviceName + "-ca";
    return KubernetesV2Utils.createSecretSpec(namespace, canonicalSpinnakerServiceName, secretNamePrefix, files);
  }

}
