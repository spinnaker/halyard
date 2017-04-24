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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.consul;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.JarResourceBackedProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConsulClientStartupProfileFactory extends JarResourceBackedProfileFactory {
  @Autowired
  String startupScriptPath;

  @Override
  protected String getResourceName() {
    return "/services/consul/client/startup/startup-consul.sh";
  }

  @Override
  protected boolean showEditWarning() {
    return false;
  }

  @Override
  protected Map<String, String> getBindings() {
    Map<String, String> result = new HashMap<>();
    result.put("startup-script-path", startupScriptPath);
    return result;
  }

  @Override
  public SpinnakerArtifact getArtifact() {
    return SpinnakerArtifact.CONSUL;
  }

  @Override
  protected String commentPrefix() {
    return "## ";
  }

  @Override
  protected void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    super.setProfile(profile, deploymentConfiguration, endpoints);
    profile.setExecutable(true);
  }
}
