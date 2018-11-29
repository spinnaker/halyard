package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration
import com.netflix.spinnaker.halyard.config.model.v1.node.SidecarConfig
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails
import com.netflix.spinnaker.halyard.deploy.services.v1.GenerateService
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings
import spock.lang.Specification

class KubernetesV2ServiceTest extends Specification {

    private KubernetesV2Service testService
    private AccountDeploymentDetails details

    def setup() {
        testService = new KubernetesV2OrcaService() {
            @Override
            String getSpinnakerStagingPath(String deploymentName) {
                return "/dummy/value"
            }

            @Override
            ServiceSettings buildServiceSettings(DeploymentConfiguration deploymentConfiguration) {
                return Mock(ServiceSettings)
            }

            @Override
            int hashCode() {
                return 42
            }
        }
        testService.serviceDelegate = Mock(KubernetesV2ServiceDelegate)
        GenerateService.ResolvedConfiguration config = Mock(GenerateService.ResolvedConfiguration)
        config.runtimeSettings = Mock(SpinnakerRuntimeSettings)
        details = new AccountDeploymentDetails()
    }
    def "Can we submit an empty port?"() {
        setup:
        SidecarConfig car = new SidecarConfig()
        car.setName("cloudsql-proxy")
        car.setMountPath("/cloudsql")

        when:
        String customSidecar = testService.buildCustomSidecar(car)

        then:
        customSidecar.contains('"ports": [],')
    }

    def "Does a port get converted to a containerPort?"() {
        setup:
        SidecarConfig car = new SidecarConfig()
        car.setName("cloudsql-proxy")
        car.setMountPath("/cloudsql")
        car.setPort(8080)
        when:
        String customSidecar = testService.buildCustomSidecar(car)

        then:
        customSidecar.contains('"ports": [{ "containerPort": 8080 }]')
    }

    def "Do SecretVolumeMounts end up being valid mountPaths?"() {
        setup:
        SidecarConfig car = new SidecarConfig()
        car.setName("cloudsql-proxy")
        car.setDockerImage("gcr.io/cloudsql-docker/gce-proxy:1.13")
        car.setCommand(["/cloud_sql_proxy"])
        car.setArgs([
                "--dir=/cloudsql",
                "-instances=instance-name:gcp-region:database-name=tcp:3306",
                "-credential_file=/secrets/cloudsql/credentials-cloudsql-instance.json",
        ])
        car.setMountPath("/cloudsql")

        SidecarConfig.SecretVolumeMount secret1 = new SidecarConfig.SecretVolumeMount()
        secret1.mountPath = "/secrets/cloudsql"
        secret1.secretName = "credentials-cloudsql-instance"

        SidecarConfig.SecretVolumeMount secret2 = new SidecarConfig.SecretVolumeMount()
        secret2.mountPath = "/var/run/secrets/kubernetes.io/serviceaccount"
        secret2.secretName = "default-token-8pndx"

        car.setSecretVolumeMounts([secret1, secret2])
        details.deploymentConfiguration = new DeploymentConfiguration()
        details.deploymentConfiguration.deploymentEnvironment.sidecars.put("spin-orca", [car])

        when:
        String customSidecar = testService.buildCustomSidecar(car)

        then:
        customSidecar.contains('"mountPath": "/secrets/cloudsql"')
        customSidecar.contains('"mountPath": "/var/run/secrets/kubernetes.io/serviceaccount"')
    }
}
