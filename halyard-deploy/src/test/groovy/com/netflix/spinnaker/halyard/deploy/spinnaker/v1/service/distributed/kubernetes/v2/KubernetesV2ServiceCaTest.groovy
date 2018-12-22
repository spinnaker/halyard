/*
 * Copyright 2019 Bol.com
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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2

import com.netflix.spinnaker.halyard.config.config.v1.StrictObjectMapper
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration
import com.netflix.spinnaker.halyard.config.model.v1.node.SidecarConfig
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount
import com.netflix.spinnaker.halyard.core.job.v1.DaemonLocalJobExecutor
import com.netflix.spinnaker.halyard.core.job.v1.JobRequest
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTask
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails
import com.netflix.spinnaker.halyard.deploy.services.v1.GenerateService
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ConfigSource
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.CustomCaSettings
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.KubernetesSettings
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.SidecarService
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings

import java.io.InputStreamReader

import spock.lang.Specification

class KubernetesV2ServiceCaTest extends Specification {

    ServiceSettings settings
    CustomCaSettings caSettings

    def setup() {
        settings = new ServiceSettings()
        caSettings = new CustomCaSettings()
        def loader = CustomCaSettings.class.getClassLoader()
        caSettings.getFiles().add(loader.getResource("bar.pem").getPath().toString())
        caSettings.getFiles().add(loader.getResource("sun.pem").getPath().toString())
        caSettings.getFiles().add(loader.getResource("foo/bar.pem").getPath().toString())
        settings.setCustomCas(caSettings)
    }
    def "Does initContainers ignore non custom ca"() {
        setup:
        ServiceSettings settings = Mock(ServiceSettings)

        when:
        def ic = KubernetesV2ServiceCa.initContainers(settings, new StrictObjectMapper())

        then:
        ic.size() == 0
    }
    def "Does initContainers produce correct ca container"() {
        setup:

        when:
        def ic = KubernetesV2ServiceCa.initContainers(settings, new StrictObjectMapper())
        def initJson = ic.get(0)

        then:
        initJson.contains('''"image":"openjdk:8-jre-alpine"''')
        initJson.contains('''"name":"init-custom-ca"''')
        initJson.contains('''"command":["sh","-c","cp /etc/java/cacerts /var/run/java/cacerts & keytool import -alias bar_122fb946 -file /var/run/java/certs/bar_122fb946.pem -keyStore /var/run/java/cacerts & keytool import -alias bar_c6cb9462 -file /var/run/java/certs/bar_c6cb9462.pem -keyStore /var/run/java/cacerts & keytool import -alias sun_c6cb9462 -file /var/run/java/certs/sun_c6cb9462.pem -keyStore /var/run/java/cacerts"]''')
    }
    
    def "Does secretConfig return a proper config"() {
        setup:
        SpinnakerRuntimeSettings runtimeSettings = Mock(SpinnakerRuntimeSettings)
        String canonicalSpinnakerServiceName = "cSn"
        String namespace = "nmspc"
        String serviceName = "sN"

        when:
        def secret = KubernetesV2ServiceCa.secretConfig(runtimeSettings, canonicalSpinnakerServiceName, namespace, caSettings, serviceName)

        then:
        secret.config.getId() == "sN-ca-1444819983"
        secret.config.getMountPath() == "/var/run/java/certs"
    }
}
