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

package com.netflix.spinnaker.halyard.config.model.v1.node;

import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Features extends Node {
  @Override
  public void accept(ConfigProblemSetBuilder psBuilder, Validator v) {
    v.validate(psBuilder, this);
  }

  @Override
  public String getNodeName() {
    return "features";
  }

  @Override
  public NodeIterator getChildren() {
    return NodeIteratorFactory.makeEmptyIterator();
  }

  private boolean auth;
  private boolean fiat;
  private boolean chaos;
  private boolean entityTags;
  private boolean jobs;
  @ValidForSpinnakerVersion(lowerBound = "1.2.0", message = "Pipeline templates are not stable prior to this release.")
  private Boolean pipelineTemplates;
  @ValidForSpinnakerVersion(lowerBound = "1.5.0", message = "Artifacts are not configurable prior to this release. Will be stable at a later release.")
  private Boolean artifacts;
  @ValidForSpinnakerVersion(lowerBound = "1.5.0", message = "Canary is not configurable prior to this release. Will be stable at a later release.")
  private Boolean mineCanary;

  public boolean isAuth(DeploymentConfiguration deploymentConfiguration) {
    return deploymentConfiguration.getSecurity().getAuthn().isEnabled();
  }
}
