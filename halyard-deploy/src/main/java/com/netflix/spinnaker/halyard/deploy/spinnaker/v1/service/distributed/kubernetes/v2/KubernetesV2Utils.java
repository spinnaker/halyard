/*
 * Copyright 2018 Google, Inc.
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.job.v1.JobRequest;
import com.netflix.spinnaker.halyard.core.job.v1.JobStatus;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.resource.v1.JinjaJarResource;
import com.netflix.spinnaker.halyard.core.resource.v1.TemplatedResource;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskInterrupted;
import java.util.Arrays;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class KubernetesV2Utils {
  static private ObjectMapper mapper = new ObjectMapper();

  public static List<String> kubectlPrefix(KubernetesAccount account) {
    List<String> command = new ArrayList<>();
    command.add("kubectl");

    if (account.usesServiceAccount()) {
      return command;
    }

    String context = account.getContext();
    if (context != null && !context.isEmpty()) {
      command.add("--context");
      command.add(context);
    }

    String kubeconfig = account.getKubeconfigFile();
    if (kubeconfig != null && !kubeconfig.isEmpty()) {
      command.add("--kubeconfig");
      command.add(kubeconfig);
    }

    return command;
  }

  static List<String> kubectlPodServiceCommand(KubernetesAccount account, String namespace, String service) {
    List<String> command = kubectlPrefix(account);

    if (StringUtils.isNotEmpty(namespace)) {
      command.add("-n=" + namespace);
    }

    command.add("get");
    command.add("po");

    command.add("-l=cluster=" + service);
    command.add("-o=jsonpath='{.items[0].metadata.name}'");

    return command;
  }

  static List<String> kubectlConnectPodCommand(KubernetesAccount account, String namespace, String name, int port) {
    List<String> command = kubectlPrefix(account);

    if (StringUtils.isNotEmpty(namespace)) {
      command.add("-n=" + namespace);
    }

    command.add("port-forward");
    command.add(name);
    command.add(port + "");

    return command;
  }

    public static SecretSpec createSecretSpec(String namespace, String clusterName, String name, List<SecretMountPair> files) {
    Map<String, String> contentMap = new HashMap<>();
    for (SecretMountPair pair: files) {
      String contents;
      try {
        contents = new String(Base64.getEncoder().encode(IOUtils.toByteArray(new FileInputStream(pair.getContents()))));
      } catch (IOException e) {
        throw new HalException(Problem.Severity.FATAL, "Failed to read required config file: " + pair.getContents().getAbsolutePath() + ": " + e.getMessage(), e);
      }

      contentMap.put(pair.getName(), contents);
    }

    SecretSpec spec = new SecretSpec();
    spec.name = name + "-" + Math.abs(contentMap.hashCode());

    spec.resource = new JinjaJarResource("/kubernetes/manifests/secret.yml");
    Map<String, Object> bindings = new HashMap<>();

    bindings.put("files", contentMap);
    bindings.put("name", spec.name);
    bindings.put("namespace", namespace);
    bindings.put("clusterName", clusterName);

    spec.resource.extendBindings(bindings);

    return spec;
  }

  static public String prettify(String input) {
    Yaml yaml = new Yaml(new SafeConstructor());
    return yaml.dump(yaml.load(input));
  }

  static public Map<String, Object> parseManifest(String input) {
    Yaml yaml = new Yaml(new SafeConstructor());
    return mapper.convertValue(yaml.load(input), new TypeReference<Map<String, Object>>() {});
  }

  static public class SecretSpec {
    TemplatedResource resource;
    String name;
  }

  @Data
  static public class SecretMountPair {
    File contents;
    String name;

    public SecretMountPair(File inputFile) {
      this(inputFile, inputFile);
    }

    public SecretMountPair(File inputFile, File outputFile) {
      this.contents = inputFile;
      this.name = outputFile.getName();
    }
  }
}
