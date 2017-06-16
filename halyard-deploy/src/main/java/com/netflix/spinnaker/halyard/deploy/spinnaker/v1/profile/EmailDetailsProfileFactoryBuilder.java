/*
 * Copyright 2017 Johan Kasselman.
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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailDetailsProfileFactoryBuilder {
  @Autowired
  protected ArtifactService artifactService;

  @Autowired
  Yaml yamlParser;

  @Autowired
  ObjectMapper objectMapper;

  @Setter
  private String host;

  @Setter
  private String fromAddress;

  @Setter
  private SpinnakerArtifact artifact;

  public EmailDetailsProfileFactory build() {
    return new EmailDetailsProfileFactory(artifact, host, fromAddress);
  }

  public String getOutputFile(String spinnakerHome) {
    return Paths.get(spinnakerHome, "path/to/echo.yml").toString();
  }s

  @EqualsAndHashCode(callSuper = false)
  @Data
  public class EmailDetailsProfileFactory extends TemplateBackedProfileFactory {
    public EmailDetailsProfileFactory(SpinnakerArtifact artifact, String host, String fromAddress) {
      super();
      this.host = host;
      this.fromAddress = fromAddress;
      this.artifact = artifact;
    }

    final private String host;

    final private String fromAddress;

    @Override
    protected ArtifactService getArtifactService() {
      return artifactService;
    }

    private String template = String.join("\n",
      "email:",
      "  host: {%host%}",
      "  fromAddress: {%fromAddress%}"
    );

    @Override
    protected Map<String, String> getBindings(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
      Map<String, String> result = new HashMap<>();
      result.put("host", host);
      result.put("fromAddress", fromAddress);
      return result;
    }

    SpinnakerArtifact artifact;

    @Override
    protected String commentPrefix() {
      return null;
    }

    @Override
    protected boolean showEditWarning() { return false; }

  }
}
