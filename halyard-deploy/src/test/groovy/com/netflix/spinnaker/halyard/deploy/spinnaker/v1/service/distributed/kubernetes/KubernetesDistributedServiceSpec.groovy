/*
 * Copyright 2017 Target, Inc.
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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.clouddriver.kubernetes.deploy.description.servergroup.KubernetesContainerDescription
import com.netflix.spinnaker.halyard.config.model.v1.node.CustomSizing
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentEnvironment
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails
import com.netflix.spinnaker.halyard.deploy.deployment.v1.DeploymentDetails
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceInterfaceFactory
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerMonitoringDaemonService
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.DistributedService
import spock.lang.Specification
import spock.lang.Unroll

class KubernetesDistributedServiceSpec extends Specification {

    @Unroll()
    def "applies request and limit overrides: #description"() {
        setup:
        KubernetesContainerDescription container = new KubernetesContainerDescription()
        def service = createServiceTestDouble()
        def deploymentEnvironment = new DeploymentEnvironment()
        deploymentEnvironment.customSizing["echo"] = new CustomSizing(requests: requests, limits: limits)

        when:
        service.applyCustomSize(container, deploymentEnvironment, "echo")

        then:
        container.requests?.memory == requestsMemory
        container.requests?.cpu == requestsCpu
        container.limits?.memory == limitsMemory
        container.limits?.cpu == limitsCpu

        where:
        description   | requests                                                         | limits                                                             | requestsMemory | requestsCpu | limitsMemory | limitsCpu
        "all"         | new CustomSizing.ResourceSpecification(memory: "1Mi", cpu: "1m") | new CustomSizing.ResourceSpecification(memory: "50Mi", cpu: "50m") | "1Mi"          | "1m"        | "50Mi"       | "50m"
        "only cpu"    | new CustomSizing.ResourceSpecification(cpu: "1m")                | new CustomSizing.ResourceSpecification(cpu: "50m")                 | null           | "1m"        | null         | "50m"
        "only mem"    | new CustomSizing.ResourceSpecification(memory: "1Mi")            | new CustomSizing.ResourceSpecification(memory: "50Mi"            ) | "1Mi"          | null        | "50Mi"       | null
        "only reqs"   | new CustomSizing.ResourceSpecification(memory: "1Mi", cpu: "1m") | null                                                               | "1Mi"          | "1m"        | null         | null
        "only limits" | null                                                             | new CustomSizing.ResourceSpecification(memory: "50Mi", cpu: "50m") | null           | null        | "50Mi"       | "50m"
    }

    def "adds no requests or limits when not specified"() {
        setup:
        KubernetesContainerDescription container = new KubernetesContainerDescription()
        def service = createServiceTestDouble()
        def deploymentEnvironment = new DeploymentEnvironment()

        when:
        service.applyCustomSize(container, deploymentEnvironment, "echo")

        then:
        container.requests == null
        container.limits == null
    }

    def "noops when given null component"() {
        setup:
        KubernetesContainerDescription container = new KubernetesContainerDescription()
        def service = createServiceTestDouble()
        def requests = new CustomSizing.ResourceSpecification(memory: "1Mi", cpu: "1m")
        def limits = new CustomSizing.ResourceSpecification(memory: "50Mi", cpu: "50m")
        def deploymentEnvironment = new DeploymentEnvironment()
        deploymentEnvironment.customSizing["echo"] = new CustomSizing(requests: requests, limits: limits)

        when:
        service.applyCustomSize(container, deploymentEnvironment, null)

        then:
        container.requests == null
        container.limits == null
    }

    private KubernetesDistributedService createServiceTestDouble() {
        new KubernetesDistributedService() {
            @Override
            String getDockerRegistry(String deploymentName) {
                return null
            }

            @Override
            ArtifactService getArtifactService() {
                return null
            }

            @Override
            ServiceInterfaceFactory getServiceInterfaceFactory() {
                return null
            }

            @Override
            ObjectMapper getObjectMapper() {
                return null
            }

            void collectLogs(DeploymentDetails details, SpinnakerRuntimeSettings runtimeSettings) {

            }

            @Override
            String getSpinnakerStagingPath(String deploymentName) {
                return null
            }

            @Override
            String getServiceName() {
                return null
            }

            @Override
            SpinnakerMonitoringDaemonService getMonitoringDaemonService() {
                return null
            }

            @Override
            Object connectToService(AccountDeploymentDetails details, SpinnakerRuntimeSettings runtimeSettings, SpinnakerService sidecar) {
                return null
            }

            @Override
            Object connectToInstance(AccountDeploymentDetails details, SpinnakerRuntimeSettings runtimeSettings, SpinnakerService sidecar, String instanceId) {
                return null
            }

            @Override
            boolean isRequiredToBootstrap() {
                return false
            }

            @Override
            DistributedService.DeployPriority getDeployPriority() {
                return null
            }

            @Override
            SpinnakerService getService() {
                return null
            }

            @Override
            ServiceSettings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
                return null
            }

            @Override
            ServiceSettings getDefaultServiceSettings(DeploymentConfiguration deploymentConfiguration) {
                return null
            }

            @Override
            SpinnakerArtifact getArtifact() {
                return null
            }
        }
    }
}
