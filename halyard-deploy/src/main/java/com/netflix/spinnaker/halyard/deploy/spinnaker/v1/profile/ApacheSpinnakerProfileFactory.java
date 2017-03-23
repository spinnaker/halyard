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

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ApacheSpinnakerProfileFactory extends TemplateBackedProfileFactory {
  private static String SPINNAKER_TEMPLATE = "<VirtualHost {%deck-host%}:{%deck-port%}>\n"
      + "  DocumentRoot /opt/deck/html\n"
      + "\n"
      + "  <Directory \"/opt/deck/html/\">\n"
      + "     Require all granted\n"
      + "  </Directory>\n"
      + "</VirtualHost>\n";

  @Override
  protected String getTemplate() {
    return SPINNAKER_TEMPLATE;
  }

  @Override
  protected Map<String, String> getBindings(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    Map<String, String> bindings = new HashMap<>();
    bindings.put("deck-host", endpoints.getServices().getDeck().getHost());
    bindings.put("deck-port", endpoints.getServices().getDeck().getPort() + "");
    return bindings;
  }

  @Override
  public SpinnakerArtifact getArtifact() {
    return SpinnakerArtifact.DECK;
  }

  @Override
  protected String commentPrefix() {
    return "## ";
  }
}
