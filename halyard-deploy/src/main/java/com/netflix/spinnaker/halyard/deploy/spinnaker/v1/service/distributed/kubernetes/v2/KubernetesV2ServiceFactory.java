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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KubernetesV2ServiceFactory {
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
  KubernetesV2RoscoService roscoService;

  public KubernetesV2ClouddriverService.Builder newClouddriverServiceBuilder() {
    return new KubernetesV2ClouddriverService.Builder(clouddriverService);
  }

  public KubernetesV2EchoService.Builder newEchoServiceBuilder() {
    return new KubernetesV2EchoService.Builder(echoService);
  }

  public KubernetesV2FiatService.Builder newFiatServiceBuilder() {
    return new KubernetesV2FiatService.Builder(fiatService);
  }

  public KubernetesV2GateService.Builder newGateServiceBuilder() {
    return new KubernetesV2GateService.Builder(gateService);
  }

  public KubernetesV2IgorService.Builder newIgorServiceBuilder() {
    return new KubernetesV2IgorService.Builder(igorService);
  }

  public KubernetesV2OrcaService.Builder newOrcaServiceBuilder() {
    return new KubernetesV2OrcaService.Builder(orcaService);
  }

  public KubernetesV2RedisService.Builder newRedisServiceBuilder() {
    return new KubernetesV2RedisService.Builder(redisService);
  }

  public KubernetesV2RoscoService.Builder newRoscoServiceBuilder() {
    return new KubernetesV2RoscoService.Builder(roscoService);
  }
}
