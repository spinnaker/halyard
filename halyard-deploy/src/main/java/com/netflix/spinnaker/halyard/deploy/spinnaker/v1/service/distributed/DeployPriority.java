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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed;

// Used to ensure dependencies are deployed first. The higher the priority, the sooner the service is deployed.
public class DeployPriority {
  public static final DeployPriority ZERO_DEPLOY_PRIORITY = new DeployPriority(0);

  final Integer priority;

  public DeployPriority(Integer priority) {
    this.priority = priority;
  }

  public int compareTo(DeployPriority other) {
    return this.priority.compareTo(other.priority);
  }
}
