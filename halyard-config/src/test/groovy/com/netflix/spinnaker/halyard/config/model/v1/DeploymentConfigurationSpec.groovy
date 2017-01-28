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

package com.netflix.spinnaker.halyard.config.model.v1

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration
import com.netflix.spinnaker.halyard.config.model.v1.node.NodeFilter
import com.netflix.spinnaker.halyard.config.model.v1.node.Providers
import com.netflix.spinnaker.halyard.config.model.v1.node.Webhooks
import spock.lang.Specification

class DeploymentConfigurationSpec extends Specification {
  void "filter matches deployment configuration "() {
    setup:
    def name = "my-deployment"

    when:
    def filter = new NodeFilter().setDeployment(name)
    def deploymentConfiguration = new DeploymentConfiguration().setName(name)

    then:
    deploymentConfiguration.matchesToRoot(filter)
  }

  void "filter doesn't match deployment configuration "() {
    setup:
    def name = "my-deployment"

    when:
    def filter = new NodeFilter().setDeployment(name)
    def deploymentConfiguration = new DeploymentConfiguration().setName(name + "-bad")

    then:
    !deploymentConfiguration.matchesToRoot(filter)
  }

  void "deployment configuration iterator reports providers & webhooks"() {
    setup:
    def deploymentConfiguration = new DeploymentConfiguration()
    def iterator = deploymentConfiguration.getChildren()
    def webhooks = false
    def providers = false

    when:
    def child = iterator.getNext()
    while (child != null) {
      if (child instanceof Webhooks) {
        webhooks = true
      }

      if (child instanceof Providers) {
        providers = true
      }

      child = iterator.getNext()
    }

    then:
    providers
    webhooks
  }
}
