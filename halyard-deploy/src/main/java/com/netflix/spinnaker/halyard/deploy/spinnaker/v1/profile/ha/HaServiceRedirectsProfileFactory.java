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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ha;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.StringBackedProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService.Type;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public abstract class HaServiceRedirectsProfileFactory extends StringBackedProfileFactory {
  protected static final ServiceSettings CLOUDDRIVER_TO_CLOUDDRIVER_RO_REDIRECT = ServiceSettings.serviceBaseUrlRedirect("${services.clouddriver-ro.baseUrl:http://localhost:7002}");
  protected static final ServiceSettings CLOUDDRIVER_TO_CLOUDDRIVER_RW_REDIRECT = ServiceSettings.serviceBaseUrlRedirect("${services.clouddriver-rw.baseUrl:http://localhost:7002}");
  protected static final ServiceSettings ECHO_TO_ECHO_SLAVE_REDIRECT = ServiceSettings.serviceBaseUrlRedirect("${services.echo-slave.baseUrl:http://localhost:8089}");

  protected abstract ServiceRedirectsConfig generateServiceRedirects(DeploymentConfiguration deploymentConfiguration);

  @Override
  protected String getRawBaseProfile() {
    return "";
  }

  @Override
  protected void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    profile.appendContents(yamlToString(generateServiceRedirects(deploymentConfiguration)));
  }

  @Override
  protected String commentPrefix() {
    return "## ";
  }

  @Data
  static class ServiceRedirectsConfig {
    Map<Type,ServiceSettings> services = new HashMap<>();
  }
}
