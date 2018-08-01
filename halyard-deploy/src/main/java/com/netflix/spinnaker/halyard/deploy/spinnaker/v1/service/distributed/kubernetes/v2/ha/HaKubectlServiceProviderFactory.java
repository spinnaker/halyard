package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.ha;

import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService.Type;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerServiceProvider;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubectlServiceProvider;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2ClouddriverService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2DeckService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2EchoService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2FiatService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2Front50Service;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2GateService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2IgorService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2KayentaService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2MonitoringDaemonService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2OrcaService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2RedisService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2RoscoService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HaKubectlServiceProviderFactory {
  @Autowired
  KubernetesV2ClouddriverService clouddriverService;

  @Autowired
  KubernetesV2DeckService deckService;

  @Autowired
  KubernetesV2EchoService echoService;

  @Autowired
  KubernetesV2FiatService fiatService;

  @Autowired
  KubernetesV2Front50Service front50Service;

  @Autowired
  KubernetesV2GateService gateService;

  @Autowired
  KubernetesV2IgorService igorService;

  @Autowired
  KubernetesV2KayentaService kayentaService;

  @Autowired
  KubernetesV2MonitoringDaemonService monitoringDaemonService;

  @Autowired
  KubernetesV2OrcaService orcaService;

  @Autowired
  KubernetesV2RedisService redisService;

  @Autowired
  KubernetesV2RedisForGateService redisForGateService;

  @Autowired
  KubernetesV2RoscoService roscoService;

  @Autowired
  KubernetesV2SpringServiceWrapperFactory kubernetesV2ServiceWrapperFactory;

  public SpinnakerServiceProvider<AccountDeploymentDetails<KubernetesAccount>> create(List<Type> haServices) {
    Map<Type, KubernetesV2Service> services = new HashMap<>();

    // Clouddriver TODO
    services.put(Type.CLOUDDRIVER, clouddriverService);

    // Deck TODO
    services.put(Type.DECK, deckService);

    // Echo TODO
    services.put(Type.ECHO, echoService);

    // Fiat TODO
    services.put(Type.FIAT, fiatService);

    // Front50 TODO
    services.put(Type.FRONT50, front50Service);

    // Gate
    addGateServices(services, haServices);

    // Igor TODO
    services.put(Type.IGOR, igorService);

    // Kayenta TODO
    services.put(Type.KAYENTA, kayentaService);

    // Monitoring Daemon TODO
    services.put(Type.MONITORING_DAEMON, monitoringDaemonService);

    // Orca TODO
    services.put(Type.ORCA, orcaService);

    // Redis
    // TODO do not add if all services are in HA mode
    services.put(Type.REDIS, redisService);

    // Rosco TODO
    services.put(Type.ROSCO, roscoService);

    return new MapBackedKubectlServiceProvider(services);
  }

  private void addGateServices(Map<Type, KubernetesV2Service> services, List<Type> haServices) {
    String gateProfileContents = "";
    if (haServices.contains(Type.GATE)) {
      services.put(Type.REDIS_FOR_GATE, redisForGateService);
      gateProfileContents = gateProfileContents.concat("\n"
          + "redis:\n"
          + "  connection: ${services.redisForGate.baseUrl}\n");
    }
    if (haServices.contains(Type.CLOUDDRIVER)) {
      gateProfileContents = gateProfileContents.concat("\n"
          + "clouddriver:\n"
          + "  baseUrl: ${services.clouddriveRo.baseUrl}\n");
    }

    if (gateProfileContents.isEmpty()) {
      services.put(Type.GATE, gateService);
    } else {
      services.put(Type.GATE, kubernetesV2ServiceWrapperFactory.newSpringServiceInstance(gateService,
          "ha",
          gateProfileContents));
    }
  }

  @Slf4j
  private static class MapBackedKubectlServiceProvider extends KubectlServiceProvider {
    private final Map<Type, KubernetesV2Service> services;

    public MapBackedKubectlServiceProvider(Map<Type, KubernetesV2Service> services) {
      this.services = new HashMap<>(services);
    }

    @Override
    public List<SpinnakerService> getServices() {
      return services.values().stream()
          .map(s -> SpinnakerService.class.cast(s))
          .collect(Collectors.toList());
    }

    @Override
    public List<KubernetesV2Service> getServicesByPriority(List<SpinnakerService.Type> serviceTypes) {
      List<KubernetesV2Service> result = new ArrayList<>();
      for (SpinnakerService.Type type : serviceTypes) {
        if (services.containsKey(type)) {
          result.add(services.get(type));
        }
      }
      result.sort((a, b) -> {
        // Prioritize Redis services
        if (a.getService().getArtifact() == SpinnakerArtifact.REDIS) {
          return -1;
        }
        if (b.getService().getArtifact() == SpinnakerArtifact.REDIS) {
          return 1;
        }
        return 0;
      });
      return result;
    }

    @Override
    public <S> KubernetesV2Service getService(SpinnakerService.Type type, Class<S> clazz) {
      return services.get(type);
    }
  }
}
