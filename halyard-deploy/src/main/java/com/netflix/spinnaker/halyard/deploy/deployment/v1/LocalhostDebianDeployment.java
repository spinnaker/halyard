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
 */

package com.netflix.spinnaker.halyard.deploy.deployment.v1;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentEnvironment.DeploymentType;
import com.netflix.spinnaker.halyard.config.spinnaker.v1.SpinnakerEndpoints;
import com.netflix.spinnaker.halyard.config.spinnaker.v1.SpinnakerEndpoints.Service;
import com.netflix.spinnaker.halyard.deploy.component.v1.ComponentType;

public class LocalhostDebianDeployment extends Deployment {
  @Override
  public DeploymentType deploymentType() {
    return DeploymentType.LocalhostDebian;
  }

  @Override
  public Object getService(ComponentType type) {
    String endpoint;
    switch (type) {
      case CLOUDDRIVER:
        Service clouddriver = getEndpoints().getServices().getClouddriver();
        endpoint = clouddriver.getAddress() + ":" + clouddriver.getPort();
        break;
      default:
        throw new IllegalArgumentException("Service for " + type + " not found");
    }

    return serviceFactory.createService(endpoint, type);
  }

  @Override
  public SpinnakerEndpoints getEndpoints() {
    return new SpinnakerEndpoints();
  }

  @Override
  public void deploy() {
    // TODO(lwander)
  }
}
