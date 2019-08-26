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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.frigga.Names;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.KubernetesUtil;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.loadbalancer.KubernetesLoadBalancerDescription;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.loadbalancer.KubernetesNamedServicePort;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.DeployKubernetesAtomicOperationDescription;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesContainerDescription;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesContainerPort;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesEnvVar;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesHandler;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesHandlerType;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesHttpGetAction;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesImageDescription;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesProbe;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesResourceDescription;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesSecretVolumeSource;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesTcpSocketAction;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesVolumeMount;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesVolumeSource;
import com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.description.servergroup.KubernetesVolumeSourceType;
import com.netflix.spinnaker.halyard.config.model.v1.node.CustomSizing;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentEnvironment;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.job.v1.JobExecutor;
import com.netflix.spinnaker.halyard.core.job.v1.JobRequest;
import com.netflix.spinnaker.halyard.core.job.v1.JobStatus;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.services.v1.GenerateService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.RunningServiceDetails;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.RunningServiceDetails.Instance;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ConfigSource;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.LogCollector;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceInterfaceFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerMonitoringDaemonService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.DistributedService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.SidecarService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.KubernetesService;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.utils.Strings;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.SocketUtils;

public interface KubernetesV1DistributedService<T>
    extends DistributedService<T, KubernetesAccount>,
        LogCollector<T, AccountDeploymentDetails<KubernetesAccount>>,
        KubernetesService {
  ArtifactService getArtifactService();

  ServiceInterfaceFactory getServiceInterfaceFactory();

  ObjectMapper getObjectMapper();

  default String getRootHomeDirectory() {
    return "/root";
  }

  default String getHomeDirectory() {
    return "/home/spinnaker";
  }

  default JobExecutor getJobExecutor() {
    return DaemonTaskHandler.getJobExecutor();
  }

  default String getNamespace(ServiceSettings settings) {
    return getRegion(settings);
  }

  default String buildAddress(String namespace) {
    return Strings.join(".", getServiceName(), namespace);
  }

  default List<LocalObjectReference> getImagePullSecrets(ServiceSettings settings) {
    List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
    if (settings.getKubernetes().getImagePullSecrets() != null) {
      for (String imagePullSecret : settings.getKubernetes().getImagePullSecrets()) {
        imagePullSecrets.add(new LocalObjectReference(imagePullSecret));
      }
    }
    return imagePullSecrets;
  }

  default Provider.ProviderType getProviderType() {
    return Provider.ProviderType.KUBERNETES;
  }

  default List<String> getHealthProviders() {
    List<String> healthProviders = new ArrayList<>();
    healthProviders.add("KubernetesContainer");
    healthProviders.add("KubernetesPod");
    return healthProviders;
  }

  default Map<String, List<String>> getAvailabilityZones(ServiceSettings settings) {
    String namespace = getNamespace(settings);
    List<String> zones = new ArrayList<>();
    zones.add(namespace);
    Map<String, List<String>> availabilityZones = new HashMap<>();
    availabilityZones.put(namespace, zones);
    return availabilityZones;
  }

  default void resizeVersion(
      AccountDeploymentDetails<KubernetesAccount> details,
      ServiceSettings settings,
      int version,
      int targetSize) {
    String name = getVersionedName(version);
    String namespace = getNamespace(settings);
    KubernetesV1ProviderUtils.resize(details, namespace, name, targetSize);
  }

  @Override
  default Map<String, Object> buildRollbackPipeline(
      AccountDeploymentDetails<KubernetesAccount> details,
      SpinnakerRuntimeSettings runtimeSettings) {
    ServiceSettings settings = runtimeSettings.getServiceSettings(getService());
    Map<String, Object> pipeline =
        DistributedService.super.buildRollbackPipeline(details, runtimeSettings);

    List<Map<String, Object>> stages = (List<Map<String, Object>>) pipeline.get("stages");
    assert (stages != null && !stages.isEmpty());

    for (Map<String, Object> stage : stages) {
      stage.put("namespaces", Collections.singletonList(getNamespace(settings)));
      stage.put("interestingHealthProviderNames", Collections.singletonList("KubernetesService"));
      stage.remove("region");
    }

    return pipeline;
  }

  default Map<String, Object> getLoadBalancerDescription(
      AccountDeploymentDetails<KubernetesAccount> details,
      SpinnakerRuntimeSettings runtimeSettings) {
    ServiceSettings settings = runtimeSettings.getServiceSettings(getService());
    int port = settings.getPort();
    String accountName = details.getAccount().getName();

    KubernetesLoadBalancerDescription description = new KubernetesLoadBalancerDescription();

    String namespace = getNamespace(settings);
    String name = getServiceName();
    Names parsedName = Names.parseName(name);
    description.setApp(parsedName.getApp());
    description.setStack(parsedName.getStack());
    description.setDetail(parsedName.getDetail());

    description.setName(name);
    description.setNamespace(namespace);
    description.setAccount(accountName);

    KubernetesNamedServicePort servicePort = new KubernetesNamedServicePort();
    servicePort.setPort(port);
    servicePort.setTargetPort(port);
    servicePort.setName("http");
    servicePort.setProtocol("TCP");

    KubernetesNamedServicePort monitoringPort = new KubernetesNamedServicePort();
    monitoringPort.setPort(8008);
    monitoringPort.setTargetPort(8008);
    monitoringPort.setName("monitoring");
    monitoringPort.setProtocol("TCP");

    List<KubernetesNamedServicePort> servicePorts = new ArrayList<>();
    servicePorts.add(servicePort);
    servicePorts.add(monitoringPort);
    description.setPorts(servicePorts);

    return getObjectMapper().convertValue(description, new TypeReference<Map<String, Object>>() {});
  }

  default List<ConfigSource> stageProfiles(
      AccountDeploymentDetails<KubernetesAccount> details,
      GenerateService.ResolvedConfiguration resolvedConfiguration) {
    SpinnakerService thisService = getService();
    ServiceSettings thisServiceSettings = resolvedConfiguration.getServiceSettings(thisService);
    SpinnakerRuntimeSettings runtimeSettings = resolvedConfiguration.getRuntimeSettings();
    Integer version = getRunningServiceDetails(details, runtimeSettings).getLatestEnabledVersion();
    if (version == null) {
      version = 0;
    } else {
      version++;
    }

    String namespace = getNamespace(thisServiceSettings);
    KubernetesV1ProviderUtils.createNamespace(details, namespace);

    String name = getServiceName();
    Map<String, String> env = new HashMap<>();
    List<ConfigSource> configSources = new ArrayList<>();

    Map<String, Profile> serviceProfiles =
        resolvedConfiguration.getProfilesForService(thisService.getType());
    Set<String> requiredFiles = new HashSet<>();

    for (SidecarService sidecarService : getSidecars(runtimeSettings)) {
      for (Profile profile :
          sidecarService.getSidecarProfiles(resolvedConfiguration, thisService)) {
        if (profile == null) {
          throw new HalException(
              Problem.Severity.FATAL,
              "Service "
                  + sidecarService.getService().getCanonicalName()
                  + " is required but was not supplied for deployment.");
        }

        serviceProfiles.put(profile.getName(), profile);
        requiredFiles.addAll(profile.getRequiredFiles());
      }
    }

    Map<String, Set<Profile>> collapseByDirectory = new HashMap<>();

    for (Map.Entry<String, Profile> entry : serviceProfiles.entrySet()) {
      Profile profile = entry.getValue();
      String mountPoint = Paths.get(profile.getOutputFile()).getParent().toString();
      Set<Profile> profiles = collapseByDirectory.getOrDefault(mountPoint, new HashSet<>());
      profiles.add(profile);
      requiredFiles.addAll(profile.getRequiredFiles());
      collapseByDirectory.put(mountPoint, profiles);
    }

    String stagingPath = getSpinnakerStagingPath(details.getDeploymentName());
    if (!requiredFiles.isEmpty()) {
      String secretName = KubernetesV1ProviderUtils.componentDependencies(name, version);
      String mountPoint = null;
      for (String file : requiredFiles) {
        String nextMountPoint = Paths.get(file).getParent().toString();
        if (mountPoint == null) {
          mountPoint = nextMountPoint;
        }
        assert (mountPoint.equals(nextMountPoint));
      }

      Set<Pair<File, String>> pairs =
          requiredFiles.stream()
              .map(
                  f -> {
                    return new ImmutablePair<>(new File(f), new File(f).getName());
                  })
              .collect(Collectors.toSet());

      KubernetesV1ProviderUtils.upsertSecret(details, pairs, secretName, namespace);
      configSources.add(new ConfigSource().setId(secretName).setMountPath(mountPoint));
    }

    int ind = 0;
    for (Map.Entry<String, Set<Profile>> entry : collapseByDirectory.entrySet()) {
      env.clear();
      String mountPoint = entry.getKey();
      Set<Profile> profiles = entry.getValue();
      env.putAll(
          profiles.stream()
              .reduce(
                  new HashMap<>(),
                  (acc, profile) -> {
                    acc.putAll(profile.getEnv());
                    return acc;
                  },
                  (a, b) -> {
                    a.putAll(b);
                    return a;
                  }));

      String secretName = KubernetesV1ProviderUtils.componentSecret(name + ind, version);
      ind += 1;

      Set<Pair<File, String>> pairs =
          profiles.stream()
              .map(
                  p -> {
                    return new ImmutablePair<>(
                        new File(stagingPath, p.getName()), new File(p.getOutputFile()).getName());
                  })
              .collect(Collectors.toSet());

      KubernetesV1ProviderUtils.upsertSecret(details, pairs, secretName, namespace);
      configSources.add(new ConfigSource().setId(secretName).setMountPath(mountPoint).setEnv(env));
    }

    return configSources;
  }

  default Map<String, Object> getServerGroupDescription(
      AccountDeploymentDetails<KubernetesAccount> details,
      SpinnakerRuntimeSettings runtimeSettings,
      List<ConfigSource> configSources) {
    DeployKubernetesAtomicOperationDescription description =
        new DeployKubernetesAtomicOperationDescription();
    SpinnakerMonitoringDaemonService monitoringService = getMonitoringDaemonService();
    ServiceSettings settings = runtimeSettings.getServiceSettings(getService());
    DeploymentEnvironment deploymentEnvironment =
        details.getDeploymentConfiguration().getDeploymentEnvironment();
    String accountName = details.getAccount().getName();
    String namespace = getNamespace(settings);
    String name = getServiceName();
    Names parsedName = Names.parseName(name);

    description.setNamespace(namespace);
    description.setAccount(accountName);

    description.setApplication(parsedName.getApp());
    description.setStack(parsedName.getStack());
    description.setFreeFormDetails(parsedName.getDetail());

    List<KubernetesVolumeSource> volumeSources = new ArrayList<>();
    for (ConfigSource configSource : configSources) {
      KubernetesVolumeSource volumeSource = new KubernetesVolumeSource();
      volumeSource.setName(configSource.getId());
      volumeSource.setType(KubernetesVolumeSourceType.Secret);
      KubernetesSecretVolumeSource secretVolumeSource = new KubernetesSecretVolumeSource();
      secretVolumeSource.setSecretName(configSource.getId());
      volumeSource.setSecret(secretVolumeSource);
      volumeSources.add(volumeSource);
    }

    description.setVolumeSources(volumeSources);
    description.setPodAnnotations(settings.getKubernetes().getPodAnnotations());
    description.setNodeSelector(deploymentEnvironment.getNodeSelectors());

    List<String> loadBalancers = new ArrayList<>();
    loadBalancers.add(name);
    description.setLoadBalancers(loadBalancers);

    List<KubernetesContainerDescription> containers = new ArrayList<>();
    ServiceSettings serviceSettings = runtimeSettings.getServiceSettings(getService());
    KubernetesContainerDescription container =
        buildContainer(name, serviceSettings, configSources, deploymentEnvironment, description);
    containers.add(container);

    ServiceSettings monitoringSettings = runtimeSettings.getServiceSettings(monitoringService);
    if (monitoringSettings.getEnabled() && serviceSettings.getMonitored()) {
      serviceSettings = runtimeSettings.getServiceSettings(monitoringService);
      container =
          buildContainer(
              monitoringService.getServiceName(),
              serviceSettings,
              configSources,
              deploymentEnvironment,
              description);
      containers.add(container);
    }

    description.setContainers(containers);

    return getObjectMapper().convertValue(description, new TypeReference<Map<String, Object>>() {});
  }

  default KubernetesHandler buildProbeHandler(int port, String scheme, String healthEndpoint) {
    KubernetesHandler handler = new KubernetesHandler();
    if (healthEndpoint != null) {
      handler.setType(KubernetesHandlerType.HTTP);
      KubernetesHttpGetAction action = new KubernetesHttpGetAction();
      action.setPath(healthEndpoint);
      action.setPort(port);
      action.setUriScheme(scheme);
      handler.setHttpGetAction(action);
    } else {
      handler.setType(KubernetesHandlerType.TCP);
      KubernetesTcpSocketAction action = new KubernetesTcpSocketAction();
      action.setPort(port);
      handler.setTcpSocketAction(action);
    }
    return handler;
  }

  default KubernetesContainerDescription buildContainer(
      String name,
      ServiceSettings settings,
      List<ConfigSource> configSources,
      DeploymentEnvironment deploymentEnvironment,
      DeployKubernetesAtomicOperationDescription description) {
    KubernetesContainerDescription container = new KubernetesContainerDescription();
    String healthEndpoint = settings.getHealthEndpoint();
    int port = settings.getPort();
    String scheme = settings.getScheme();
    if (StringUtils.isNotEmpty(scheme)) {
      scheme = scheme.toUpperCase();
    } else {
      scheme = null;
    }

    KubernetesProbe readinessProbe = new KubernetesProbe();
    KubernetesHandler readinessHandler = buildProbeHandler(port, scheme, healthEndpoint);
    readinessProbe.setHandler(readinessHandler);
    container.setReadinessProbe(readinessProbe);

    DeploymentEnvironment.LivenessProbeConfig livenessProbeConfig =
        deploymentEnvironment.getLivenessProbeConfig();
    if (livenessProbeConfig != null
        && livenessProbeConfig.isEnabled()
        && livenessProbeConfig.getInitialDelaySeconds() != null) {
      KubernetesProbe livenessProbe = new KubernetesProbe();
      KubernetesHandler livenessHandler = buildProbeHandler(port, scheme, healthEndpoint);
      livenessProbe.setHandler(livenessHandler);
      livenessProbe.setInitialDelaySeconds(livenessProbeConfig.getInitialDelaySeconds());
      container.setLivenessProbe(livenessProbe);
    }

    applyCustomSize(container, deploymentEnvironment, name, description);

    KubernetesImageDescription imageDescription =
        KubernetesUtil.buildImageDescription(settings.getArtifactId());
    container.setImageDescription(imageDescription);
    container.setName(name);

    List<KubernetesContainerPort> ports = new ArrayList<>();
    KubernetesContainerPort containerPort = new KubernetesContainerPort();
    containerPort.setContainerPort(port);
    ports.add(containerPort);
    container.setPorts(ports);

    List<KubernetesVolumeMount> volumeMounts = new ArrayList<>();
    for (ConfigSource configSource : configSources) {
      KubernetesVolumeMount volumeMount = new KubernetesVolumeMount();
      volumeMount.setName(configSource.getId());
      volumeMount.setMountPath(configSource.getMountPath());
      volumeMounts.add(volumeMount);
    }

    container.setVolumeMounts(volumeMounts);

    List<KubernetesEnvVar> envVars = new ArrayList<>();
    settings
        .getEnv()
        .forEach(
            (k, v) -> {
              KubernetesEnvVar envVar = new KubernetesEnvVar();
              envVar.setName(k);
              envVar.setValue(v);

              envVars.add(envVar);
            });

    configSources.forEach(
        c -> {
          c.getEnv()
              .entrySet()
              .forEach(
                  envEntry -> {
                    KubernetesEnvVar envVar = new KubernetesEnvVar();
                    envVar.setName(envEntry.getKey());
                    envVar.setValue(envEntry.getValue());
                    envVars.add(envVar);
                  });
        });

    container.setEnvVars(envVars);

    return container;
  }

  default void applyCustomSize(
      KubernetesContainerDescription container,
      DeploymentEnvironment deploymentEnvironment,
      String componentName,
      DeployKubernetesAtomicOperationDescription description) {
    Map<String, Map> componentSizing = deploymentEnvironment.getCustomSizing().get(componentName);

    if (componentSizing != null) {

      if (componentSizing.get("requests") != null) {
        container.setRequests(retrieveKubernetesResourceDescription(componentSizing, "requests"));
      }

      if (componentSizing.get("limits") != null) {
        container.setLimits(retrieveKubernetesResourceDescription(componentSizing, "limits"));
      }

      if (componentSizing.get("replicas") != null) {
        description.setTargetSize(retrieveKubernetesTargetSize(componentSizing));
      }
    }

    /* TODO(lwander) this needs work
      SizingTranslation.ServiceSize serviceSize = sizingTranslation.getServiceSize(deploymentEnvironment.getSize(), service);
    */
  }

  default KubernetesResourceDescription retrieveKubernetesResourceDescription(
      Map<String, Map> componentSizing, String resourceType) {
    KubernetesResourceDescription requests = new KubernetesResourceDescription();
    requests.setCpu(CustomSizing.stringOrNull(componentSizing.get(resourceType).get("cpu")));
    requests.setMemory(CustomSizing.stringOrNull(componentSizing.get(resourceType).get("memory")));
    return requests;
  }

  default Integer retrieveKubernetesTargetSize(Map componentSizing) {
    return (componentSizing != null && componentSizing.get("replicas") != null)
        ? (Integer) componentSizing.get("replicas")
        : 1;
  }

  default void ensureRunning(
      AccountDeploymentDetails<KubernetesAccount> details,
      GenerateService.ResolvedConfiguration resolvedConfiguration,
      List<ConfigSource> configSources,
      boolean recreate) {
    ServiceSettings settings = resolvedConfiguration.getServiceSettings(getService());
    SpinnakerRuntimeSettings runtimeSettings = resolvedConfiguration.getRuntimeSettings();
    String namespace = getNamespace(settings);
    String serviceName = getServiceName();
    String replicaSetName = serviceName + "-v000";
    int port = settings.getPort();

    SpinnakerMonitoringDaemonService monitoringService = getMonitoringDaemonService();
    ServiceSettings monitoringSettings = runtimeSettings.getServiceSettings(monitoringService);

    KubernetesClient client = KubernetesV1ProviderUtils.getClient(details);
    KubernetesV1ProviderUtils.createNamespace(details, namespace);

    Map<String, String> serviceSelector = new HashMap<>();
    serviceSelector.put("load-balancer-" + serviceName, "true");

    Map<String, String> replicaSetSelector = new HashMap<>();
    replicaSetSelector.put("replication-controller", replicaSetName);

    Map<String, String> podLabels = new HashMap<>();
    podLabels.putAll(replicaSetSelector);
    podLabels.putAll(serviceSelector);

    Map<String, String> serviceLabels = new HashMap<>();
    serviceLabels.put("app", "spin");
    serviceLabels.put("stack", getCanonicalName());

    ServiceBuilder serviceBuilder = new ServiceBuilder();
    serviceBuilder =
        serviceBuilder
            .withNewMetadata()
            .withName(serviceName)
            .withNamespace(namespace)
            .withLabels(serviceLabels)
            .endMetadata()
            .withNewSpec()
            .withSelector(serviceSelector)
            .withPorts(
                new ServicePortBuilder().withPort(port).withName("http").build(),
                new ServicePortBuilder()
                    .withPort(monitoringSettings.getPort())
                    .withName("monitoring")
                    .build())
            .endSpec();

    boolean create = true;
    if (client.services().inNamespace(namespace).withName(serviceName).get() != null) {
      if (recreate) {
        client.services().inNamespace(namespace).withName(serviceName).delete();
      } else {
        create = false;
      }
    }

    if (create) {
      client.services().inNamespace(namespace).create(serviceBuilder.build());
    }

    List<Container> containers = new ArrayList<>();
    DeploymentEnvironment deploymentEnvironment =
        details.getDeploymentConfiguration().getDeploymentEnvironment();
    containers.add(
        ResourceBuilder.buildContainer(
            serviceName, settings, configSources, deploymentEnvironment));

    for (SidecarService sidecarService : getSidecars(runtimeSettings)) {
      String sidecarName = sidecarService.getService().getServiceName();
      ServiceSettings sidecarSettings =
          resolvedConfiguration.getServiceSettings(sidecarService.getService());
      containers.add(
          ResourceBuilder.buildContainer(
              sidecarName, sidecarSettings, configSources, deploymentEnvironment));
    }

    List<Volume> volumes =
        configSources.stream()
            .map(
                c -> {
                  return new VolumeBuilder()
                      .withName(c.getId())
                      .withSecret(new SecretVolumeSourceBuilder().withSecretName(c.getId()).build())
                      .build();
                })
            .collect(Collectors.toList());

    ReplicaSetBuilder replicaSetBuilder = new ReplicaSetBuilder();
    List<LocalObjectReference> imagePullSecrets = getImagePullSecrets(settings);
    Map componentSizing = deploymentEnvironment.getCustomSizing().get(serviceName);

    replicaSetBuilder =
        replicaSetBuilder
            .withNewMetadata()
            .withName(replicaSetName)
            .withNamespace(namespace)
            .endMetadata()
            .withNewSpec()
            .withReplicas(retrieveKubernetesTargetSize(componentSizing))
            .withNewSelector()
            .withMatchLabels(replicaSetSelector)
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .withAnnotations(settings.getKubernetes().getPodAnnotations())
            .withLabels(podLabels)
            .endMetadata()
            .withNewSpec()
            .withContainers(containers)
            .withTerminationGracePeriodSeconds(5L)
            .withVolumes(volumes)
            .withImagePullSecrets(imagePullSecrets)
            .endSpec()
            .endTemplate()
            .endSpec();

    create = true;
    if (client.extensions().replicaSets().inNamespace(namespace).withName(replicaSetName).get()
        != null) {
      if (recreate) {
        client.extensions().replicaSets().inNamespace(namespace).withName(replicaSetName).delete();

        RunningServiceDetails runningServiceDetails =
            getRunningServiceDetails(details, runtimeSettings);
        while (runningServiceDetails.getLatestEnabledVersion() != null) {
          DaemonTaskHandler.safeSleep(TimeUnit.SECONDS.toMillis(5));
          runningServiceDetails = getRunningServiceDetails(details, runtimeSettings);
        }
      } else {
        create = false;
      }
    }

    if (create) {
      client.extensions().replicaSets().inNamespace(namespace).create(replicaSetBuilder.build());
    }

    RunningServiceDetails runningServiceDetails =
        getRunningServiceDetails(details, runtimeSettings);
    Integer version = runningServiceDetails.getLatestEnabledVersion();
    while (version == null
        || runningServiceDetails.getInstances().get(version).stream()
            .anyMatch(i -> !(i.isHealthy() && i.isRunning()))) {
      DaemonTaskHandler.safeSleep(TimeUnit.SECONDS.toMillis(5));
      runningServiceDetails = getRunningServiceDetails(details, runtimeSettings);
      version = runningServiceDetails.getLatestEnabledVersion();
    }
  }

  @Override
  default RunningServiceDetails getRunningServiceDetails(
      AccountDeploymentDetails<KubernetesAccount> details,
      SpinnakerRuntimeSettings runtimeSettings) {
    ServiceSettings settings = runtimeSettings.getServiceSettings(getService());
    RunningServiceDetails res = new RunningServiceDetails();

    KubernetesClient client = KubernetesV1ProviderUtils.getClient(details);
    String name = getServiceName();
    String namespace = getNamespace(settings);

    RunningServiceDetails.LoadBalancer lb = new RunningServiceDetails.LoadBalancer();
    lb.setExists(client.services().inNamespace(namespace).withName(name).get() != null);
    res.setLoadBalancer(lb);

    List<Pod> pods =
        client
            .pods()
            .inNamespace(namespace)
            .withLabel("load-balancer-" + name, "true")
            .list()
            .getItems();
    pods.addAll(
        client
            .pods()
            .inNamespace(namespace)
            .withLabel("load-balancer-" + name, "false")
            .list()
            .getItems());

    Map<Integer, List<Instance>> instances = res.getInstances();
    for (Pod pod : pods) {
      String podName = pod.getMetadata().getName();
      String serverGroupName = podName.substring(0, podName.lastIndexOf("-"));
      Names parsedName = Names.parseName(serverGroupName);
      Integer version = parsedName.getSequence();
      if (version == null) {
        throw new IllegalStateException(
            "Server group for service "
                + getServiceName()
                + " has unknown sequence ("
                + serverGroupName
                + ")");
      }

      String location = pod.getMetadata().getNamespace();
      String id = pod.getMetadata().getName();

      Instance instance = new Instance().setId(id).setLocation(location);
      List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
      if (!containerStatuses.isEmpty()
          && containerStatuses.stream().allMatch(ContainerStatus::getReady)) {
        instance.setHealthy(true);
      }

      if (!containerStatuses.isEmpty()
          && containerStatuses.stream()
              .allMatch(
                  s -> s.getState().getRunning() != null && s.getState().getTerminated() == null)) {
        instance.setRunning(true);
      }

      List<Instance> knownInstances = instances.getOrDefault(version, new ArrayList<>());
      knownInstances.add(instance);
      instances.put(version, knownInstances);
    }

    List<ReplicaSet> replicaSets =
        client.extensions().replicaSets().inNamespace(settings.getLocation()).list().getItems();
    for (ReplicaSet rs : replicaSets) {
      String rsName = rs.getMetadata().getName();
      Names parsedRsName = Names.parseName(rsName);
      if (!parsedRsName.getCluster().equals(getServiceName())) {
        continue;
      }

      instances.computeIfAbsent(parsedRsName.getSequence(), i -> new ArrayList<>());
    }

    return res;
  }

  @Override
  default <S> S connectToInstance(
      AccountDeploymentDetails<KubernetesAccount> details,
      SpinnakerRuntimeSettings runtimeSettings,
      SpinnakerService<S> sidecar,
      String instanceId) {
    ServiceSettings settings = runtimeSettings.getServiceSettings(sidecar);
    String namespace = getNamespace(settings);
    int localPort = SocketUtils.findAvailableTcpPort();
    int targetPort = settings.getPort();
    List<String> command =
        KubernetesV1ProviderUtils.kubectlPortForwardCommand(
            details, namespace, instanceId, targetPort, localPort);
    JobRequest request = new JobRequest().setTokenizedCommand(command);
    String jobId = getJobExecutor().startJob(request);

    // Wait for the proxy to spin up.
    DaemonTaskHandler.safeSleep(TimeUnit.SECONDS.toMillis(5));

    JobStatus status = getJobExecutor().updateJob(jobId);

    // This should be a long-running job.
    if (status.getState() == JobStatus.State.COMPLETED) {
      throw new HalException(
          Problem.Severity.FATAL,
          "Unable to establish a proxy against "
              + getServiceName()
              + ":\n"
              + status.getStdOut()
              + "\n"
              + status.getStdErr());
    }

    return getServiceInterfaceFactory()
        .createService(settings.getScheme() + "://localhost:" + localPort, sidecar);
  }

  @Override
  default <S> S connectToService(
      AccountDeploymentDetails<KubernetesAccount> details,
      SpinnakerRuntimeSettings runtimeSettings,
      SpinnakerService<S> service) {
    ServiceSettings settings = runtimeSettings.getServiceSettings(service);

    KubernetesV1ProviderUtils.Proxy proxy =
        KubernetesV1ProviderUtils.openProxy(getJobExecutor(), details);
    String endpoint =
        KubernetesV1ProviderUtils.proxyServiceEndpoint(
                proxy, getNamespace(settings), getServiceName(), settings.getPort())
            .toString();

    return getServiceInterfaceFactory().createService(endpoint, service);
  }

  default String connectCommand(
      AccountDeploymentDetails<KubernetesAccount> details,
      SpinnakerRuntimeSettings runtimeSettings,
      int localPort) {
    ServiceSettings settings = runtimeSettings.getServiceSettings(getService());
    RunningServiceDetails runningServiceDetails =
        getRunningServiceDetails(details, runtimeSettings);
    Map<Integer, List<Instance>> instances = runningServiceDetails.getInstances();
    Integer latest = runningServiceDetails.getLatestEnabledVersion();
    String namespace = getNamespace(settings);

    List<Instance> latestInstances = instances.get(latest);
    if (latestInstances == null || latestInstances.isEmpty()) {
      throw new HalException(
          Problem.Severity.FATAL,
          "No instances running in latest server group for service "
              + getServiceName()
              + " in namespace "
              + namespace);
    }

    return Strings.join(
        KubernetesV1ProviderUtils.kubectlPortForwardCommand(
            details, namespace, latestInstances.get(0).getId(), settings.getPort(), localPort),
        " ");
  }

  default String connectCommand(
      AccountDeploymentDetails<KubernetesAccount> details,
      SpinnakerRuntimeSettings runtimeSettings) {
    return connectCommand(
        details, runtimeSettings, runtimeSettings.getServiceSettings(getService()).getPort());
  }

  default void deleteVersion(
      AccountDeploymentDetails<KubernetesAccount> details,
      ServiceSettings settings,
      Integer version) {
    String name = getVersionedName(version);
    String namespace = getNamespace(settings);
    KubernetesV1ProviderUtils.deleteReplicaSet(details, namespace, name);
  }
}
