/*
 * Copyright 2019 Google, Inc.
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

package com.netflix.spinnaker.halyard.config.services.v1.ci;

import com.netflix.spinnaker.halyard.config.model.v1.ci.jenkins.JenkinsCi;
import com.netflix.spinnaker.halyard.config.model.v1.ci.jenkins.JenkinsMaster;
import com.netflix.spinnaker.halyard.config.model.v1.node.NodeFilter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JenkinsService extends CiService<JenkinsMaster, JenkinsCi> {
  public JenkinsService(CiService.Members members) {
    super(members);
  }


  public String ciName() {
    return "jenkins";
  }

  protected List<JenkinsCi> getMatchingCiNodes(NodeFilter filter) {
    return lookupService.getMatchingNodesOfType(filter, JenkinsCi.class);
  }

  protected List<JenkinsMaster> getMatchingAccountNodes(NodeFilter filter) {
    return lookupService.getMatchingNodesOfType(filter, JenkinsMaster.class);
  }

  @Override
  public JenkinsMaster convertToAccount(Object object) {
    return objectMapper.convertValue(object, JenkinsMaster.class);
  }
}
