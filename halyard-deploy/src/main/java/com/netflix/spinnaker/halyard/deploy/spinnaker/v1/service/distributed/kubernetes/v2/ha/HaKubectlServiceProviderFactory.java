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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.ha;

import com.netflix.spinnaker.halyard.config.model.v1.ha.HaService.HaServiceType;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService.Type;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerServiceProvider;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubectlServiceProvider;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2ClouddriverService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2EchoService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2FiatService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2GateService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2IgorService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2OrcaService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2RedisService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2RoscoService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2Service;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2ServiceFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HaKubectlServiceProviderFactory {
  @Autowired
  KubernetesV2ServiceFactory serviceFactory;

  public SpinnakerServiceProvider<AccountDeploymentDetails<KubernetesAccount>> create(
      List<HaServiceType> haServices) {
    Map<Type,KubernetesV2Service> services = new HashMap<>();

    addClouddriver(services, haServices);
    addDeck(services, haServices);
    addEcho(services, haServices);
    addFiat(services, haServices);
    addFront50(services, haServices);
    addGate(services, haServices);
    addIgor(services, haServices);
    addKayenta(services, haServices);
    addMonitoringDaemon(services, haServices);
    addOrca(services, haServices);
    addRedis(services, haServices);
    addRosco(services, haServices);

    return new MapBackedKubectlServiceProvider(services);
  }

  private void addClouddriver(Map<Type,KubernetesV2Service> services,
      List<HaServiceType> haServices) {
    if (!haServices.contains(HaServiceType.CLOUDDRIVER)) {
      services.put(Type.CLOUDDRIVER, serviceFactory.getClouddriverService());
      return;
    }

    String clouddriverRoExtraProfileContents = ""
        + "server:\n"
        + "  port: ${services.clouddriver-ro.port:7002}\n"
        + "  address: ${services.clouddriver-ro.host:localhost}\n"
        + "\n"
        + "redis:\n"
        + "  connection: ${services.redis.baseUrl:redis://localhost:6379}\n" // TODO(joonlim): Issue 2934 - Update to services.redis-slave-clouddriver.baseUrl
        + "\n"
        + "caching:\n"
        + "  redis:\n"
        + "    hashingEnabled: false\n"
        + "  writeEnabled: false\n";
    KubernetesV2ClouddriverService clouddriverRoService = serviceFactory.newClouddriverServiceBuilder()
        .setTypeNameSuffix("ro")
        .addProfile("ro", clouddriverRoExtraProfileContents)
        .build();
    services.put(clouddriverRoService.getType(), clouddriverRoService);

    String clouddriverRwExtraProfileContents = ""
        + "server:\n"
        + "  port: ${services.clouddriver-rw.port:7002}\n"
        + "  address: ${services.clouddriver-rw.host:localhost}\n"
        + "\n"
        + "redis:\n"
        + "  connection: ${services.redis.baseUrl:redis://localhost:6379}\n" // TODO(joonlim): Issue 2934 - Update to services.redis-master-clouddriver.baseUrl
        + "\n"
        + "caching:\n"
        + "  redis:\n"
        + "    hashingEnabled: false\n"
        + "  writeEnabled: false\n";
    KubernetesV2ClouddriverService clouddriverRwService = serviceFactory.newClouddriverServiceBuilder()
        .setTypeNameSuffix("rw")
        .addProfile("rw", clouddriverRwExtraProfileContents)
        .build();
    services.put(clouddriverRwService.getType(), clouddriverRwService);

    String clouddriverCachingExtraProfileContents = ""
        + "server:\n"
        + "  port: ${services.clouddriver-caching.port:7002}\n"
        + "  address: ${services.clouddriver-caching.host:localhost}\n"
        + "\n"
        + "redis:\n"
        + "  connection: ${services.redis.baseUrl:redis://localhost:6379}\n" // TODO(joonlim): Issue 2934 - Update to services.redis-master-clouddriver.baseUrl
        + "\n"
        + "caching:\n"
        + "  redis:\n"
        + "    hashingEnabled: true\n"
        + "  writeEnabled: true\n";
    KubernetesV2ClouddriverService clouddriverCachingService = serviceFactory.newClouddriverServiceBuilder()
        .setTypeNameSuffix("caching")
        .addProfile("caching", clouddriverCachingExtraProfileContents)
        .build();
    services.put(clouddriverCachingService.getType(), clouddriverCachingService);
  }

  private void addDeck(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    services.put(Type.DECK, serviceFactory.getDeckService());
  }

  private void addEcho(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    if (!haServices.contains(HaServiceType.ECHO)) {
      services.put(Type.ECHO, serviceFactory.getEchoService());
      return;
    }

    String echoSchedulerExtraProfileContents = ""
        + "server:\n"
        + "  port: ${services.echo-scheduler.port:8089}\n"
        + "  address: ${services.echo-scheduler.host:localhost}\n"
        + "\n"
        + "scheduler:\n"
        + "  enabled: true\n"
        + "  threadPoolSize: 20\n"
        + "  triggeringEnabled: true\n"
        + "  pipelineConfigsPoller:\n"
        + "    enabled: true\n"
        + "    pollingIntervalMs: 30000\n"
        + "  cron:\n"
        + "    timezone: ${global.spinnaker.timezone:America/Los_Angeles}\n"
        + "\n"
        + "redis:\n"
        + "  connection: ${services.redis-echo-scheduler.baseUrl:redis://localhost:6379}\n";
    KubernetesV2EchoService echoSchedulerService = serviceFactory.newEchoServiceBuilder()
        .setTypeNameSuffix("scheduler")
        .addProfile("scheduler", echoSchedulerExtraProfileContents)
        .build();
    services.put(echoSchedulerService.getType(), echoSchedulerService);

    String echoSlaveExtraProfileContents = ""
        + "server:\n"
        + "  port: ${services.echo-slave.port:8089}\n"
        + "  address: ${services.echo-slave.host:localhost}\n"
        + "\n"
        + "scheduler:\n"
        + "  enabled: false\n"
        + "\n"
        + "redis:\n"
        + "  connection: ${services.redis-echo-slave.baseUrl:redis://localhost:6379}\n";
    KubernetesV2EchoService echoSlaveService = serviceFactory.newEchoServiceBuilder()
        .setTypeNameSuffix("slave")
        .addProfile("slave", echoSlaveExtraProfileContents)
        .build();
    services.put(echoSlaveService.getType(), echoSlaveService);
  }

  private void addFiat(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    if (!haServices.contains(HaServiceType.CLOUDDRIVER)) {
      services.put(Type.FIAT, serviceFactory.getFiatService());
      return;
    }

    String fiatExtraProfileContents = ""
        + "services:\n"
        + "  clouddriver:\n"
        + "    baseUrl: ${services.clouddriver-ro.baseUrl:http://localhost:7002}\n";
    KubernetesV2FiatService service = serviceFactory.newFiatServiceBuilder()
        .addProfile("ha", fiatExtraProfileContents)
        .build();
    services.put(service.getType(), service);
  }

  private void addFront50(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    services.put(Type.FRONT50, serviceFactory.getFront50Service());
  }

  private void addGate(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    if (!haServices.contains(HaServiceType.CLOUDDRIVER)) {
      services.put(Type.GATE, serviceFactory.getGateService());
      return;
    }

    String gateExtraProfileContents = ""
        + "services:\n"
        + "  clouddriver:\n"
        + "    baseUrl: ${services.clouddriver-ro.baseUrl:http://localhost:7002}\n";
    KubernetesV2GateService service = serviceFactory.newGateServiceBuilder()
        .addProfile("ha", gateExtraProfileContents)
        .build();
    services.put(service.getType(), service);
  }

  private void addIgor(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    if (!haServices.contains(HaServiceType.CLOUDDRIVER) && !haServices.contains(HaServiceType.ECHO)) {
      services.put(Type.IGOR, serviceFactory.getIgorService());
      return;
    }

    String igorExtraProfileContents = ""
        + "services:\n";
    if (haServices.contains(HaServiceType.CLOUDDRIVER)) {
      igorExtraProfileContents += ""
          + "  clouddriver:\n"
          + "    baseUrl: ${services.clouddriver-ro.baseUrl:http://localhost:7002}\n";
    }
    if (haServices.contains(HaServiceType.ECHO)) {
      igorExtraProfileContents += ""
          + "  echo:\n"
          + "    baseUrl: ${services.echo-slave.baseUrl:http://localhost:8089}\n";
    }
    KubernetesV2IgorService service = serviceFactory.newIgorServiceBuilder()
        .addProfile("ha", igorExtraProfileContents)
        .build();
    services.put(service.getType(), service);
  }

  private void addKayenta(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    services.put(Type.KAYENTA, serviceFactory.getKayentaService());
  }

  private void addMonitoringDaemon(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    services.put(Type.MONITORING_DAEMON, serviceFactory.getMonitoringDaemonService());
  }

  private void addOrca(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    if (!haServices.contains(HaServiceType.CLOUDDRIVER) && !haServices.contains(HaServiceType.ECHO)) {
      services.put(Type.ORCA, serviceFactory.getOrcaService());
      return;
    }

    String orcaExtraProfileContents = "";
    if (haServices.contains(HaServiceType.CLOUDDRIVER)) {
      orcaExtraProfileContents += ""
          + "oort:\n"
          + "  baseUrl: ${services.clouddriver-rw.baseUrl:http://localhost:7002}\n"
          + "\n"
          + "mort:\n"
          + "  baseUrl: ${services.clouddriver-rw.baseUrl:http://localhost:7002}\n"
          + "\n"
          + "kato:\n"
          + "  baseUrl: ${services.clouddriver-rw.baseUrl:http://localhost:7002}\n";
    }
    if (haServices.contains(HaServiceType.ECHO)) {
      orcaExtraProfileContents += ""
          + "echo:\n"
          + "  baseUrl: ${services.echo-slave.baseUrl:http://localhost:8089}\n";
    }
    KubernetesV2OrcaService service = serviceFactory.newOrcaServiceBuilder()
        .addProfile("ha", orcaExtraProfileContents)
        .build();
    services.put(service.getType(), service);
  }

  private void addRedis(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    services.put(Type.REDIS, serviceFactory.getRedisService());

    // TODO(joonlim): Issue 2934 - Setup redis-master-clouddriver and redis-slave-clouddriver

    if (haServices.contains(HaServiceType.ECHO)) {
      KubernetesV2RedisService redisEchoSchedulerService = serviceFactory.newRedisServiceBuilder()
          .setTypeNameSuffix("echo-scheduler")
          .build();
      services.put(redisEchoSchedulerService.getType(), redisEchoSchedulerService);

      KubernetesV2RedisService redisEchoSlaveService = serviceFactory.newRedisServiceBuilder()
          .setTypeNameSuffix("echo-slave")
          .build();
      services.put(redisEchoSlaveService.getType(), redisEchoSlaveService);
    }
  }

  private void addRosco(Map<Type,KubernetesV2Service> services, List<HaServiceType> haServices) {
    if (!haServices.contains(HaServiceType.CLOUDDRIVER)) {
      services.put(Type.ROSCO, serviceFactory.getRoscoService());
      return;
    }

    String roscoExtraProfileContents = ""
        + "services:\n"
        + "  clouddriver:\n"
        + "    baseUrl: ${services.clouddriver-ro.baseUrl:http://localhost:7002}\n";
    KubernetesV2RoscoService service = serviceFactory.newRoscoServiceBuilder()
        .addProfile("ha", roscoExtraProfileContents)
        .build();
    services.put(service.getType(), service);
  }

  private static class MapBackedKubectlServiceProvider extends KubectlServiceProvider {

    private final Map<Type,KubernetesV2Service> services;

    public MapBackedKubectlServiceProvider(Map<Type,KubernetesV2Service> services) {
      this.services = new HashMap<>(services);
    }

    @Override
    public List<SpinnakerService> getServices() {
      return services.values().stream()
          .map(s -> SpinnakerService.class.cast(s))
          .collect(Collectors.toList());
    }

    @Override
    public List<KubernetesV2Service> getServicesByPriority(List<Type> serviceTypes) {
      List<KubernetesV2Service> result = services.values().stream()
          .filter(d -> serviceTypes.contains(d.getService().getType()))
          .sorted((d1, d2) -> d2.getDeployPriority().compareTo(d1.getDeployPriority()))
          .collect(Collectors.toList());
      return result;
    }

    public KubernetesV2Service getService(Type type) {
      return services.get(type);
    }

    public <S> KubernetesV2Service getService(Type type, Class<S> clazz) {
      return getService(type);
    }
  }
}
