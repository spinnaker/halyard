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

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class SlackDetailsProfileFactoryBuilder {
  @Autowired
  protected ArtifactService artifactService;

  @Setter
  private String token;

  @Setter
  private String botName;

  @Setter
  private SpinnakerArtifact artifact;

  public SlackDetailsProfileFactory build() {
    return new SlackDetailsProfileFactory(artifact, token, botName);
  }

  public String getOutputFile(String spinnakerHome) {
    return Paths.get(spinnakerHome, "path/to/echo.yml").toString();
  }

  @EqualsAndHashCode(callSuper = false)
  @Data
  public class SlackDetailsProfileFactory extends TemplateBackedProfileFactory {
    public SlackDetailsProfileFactory(SpinnakerArtifact artifact, String token, String botName) {
      super();
      this.token = token;
      this.botName = botName;
      this.artifact = artifact;
    }

    final private String token;

    final private String botName;

    @Override
    protected ArtifactService getArtifactService() {
      return artifactService;
    }

    private String template = String.join("\n",
        "slack:",
        "  token: {%token%}",
        "  botName: {%botName%}"
    );

    @Override
    protected Map<String, String> getBindings(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
      Map<String, String> result = new HashMap<>();
      result.put("token", token);
      result.put("botName", botName);
      return result;
    }

    SpinnakerArtifact artifact;

    @Override
    protected String commentPrefix() {
      return null;
    }

    @Override
    protected boolean showEditWarning() {
      return false;
    }
  }
}
