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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class MetricRegistryProfileFactoryBuilder {
  @Autowired
  protected ArtifactService artifactService;

  @Autowired
  protected String spinnakerStagingPath;

  @Autowired
  protected Yaml yamlParser;

  @Autowired
  protected ObjectMapper objectMapper;

  public ProfileFactory build(ServiceSettings settings) {
    return new ProfileFactory() {
      @Override
      protected ArtifactService getArtifactService() {
        return artifactService;
      }

      @Override
      protected void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
        URI uri;
        try {
          String baseUrl;
          if (settings.isBasicAuthEnabled()) {
            baseUrl = settings.getAuthBaseUrl();
          } else {
            baseUrl = settings.getBaseUrl();
          }
          uri = new URIBuilder(baseUrl).setHost("localhost").setPath("/spectator/metrics").build();
        } catch (URISyntaxException e) {
          throw new HalException(Problem.Severity.FATAL, "Unable to build service URL: " + e.getMessage());
        }
        profile.appendContents("metrics_url: " + uri.toString());
      }

      @Override
      protected Profile getBaseProfile(String name, String version, String outputFile) {
        return new Profile(name, version, outputFile, "");
      }

      @Override
      public SpinnakerArtifact getArtifact() {
        return SpinnakerArtifact.SPINNAKER_MONITORING_DAEMON;
      }

      @Override
      protected String commentPrefix() {
        return "## ";
      }
    };
  }
}
