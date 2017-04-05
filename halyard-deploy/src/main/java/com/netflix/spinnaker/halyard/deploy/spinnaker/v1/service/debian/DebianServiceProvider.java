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
 *
 */

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.debian;

import com.netflix.spinnaker.halyard.core.resource.v1.JarResource;
import com.netflix.spinnaker.halyard.core.resource.v1.TemplatedResource;
import com.netflix.spinnaker.halyard.deploy.services.v1.GenerateService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.LocalServiceProvider;
import io.fabric8.utils.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DebianServiceProvider extends LocalServiceProvider {
  @Autowired
  private String debianRepository;

  @Autowired
  DebianClouddriverService clouddriverService;

  @Autowired
  DebianConsulClientService consulClientService;

  @Autowired
  DebianDeckService deckService;

  @Autowired
  DebianEchoService echoService;

  @Autowired
  DebianFiatService fiatService;

  @Autowired
  DebianFront50Service front50Service;

  @Autowired
  DebianGateService gateService;

  @Autowired
  DebianIgorService igorService;

  @Autowired
  DebianMonitoringDaemonService monitoringDaemonService;

  @Autowired
  DebianOrcaService orcaService;

  @Autowired
  DebianRedisService redisService;

  @Autowired
  DebianRoscoService roscoService;

  @Override
  public String getInstallCommand(GenerateService.ResolvedConfiguration resolvedConfiguration, Map<String, String> installCommands) {
    Map<String, String> bindings = new HashMap<>();
    List<String> serviceNames = new ArrayList<>(installCommands.keySet());
    List<String> upstartNames = getInstallableServices(serviceNames)
        .stream()
        .filter(i -> resolvedConfiguration.getServiceSettings(i.getService()).isEnabled())
        .map(i -> ((LocalDebianService) i).getUpstartServiceName())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    List<String> serviceInstalls = serviceNames.stream()
        .map(installCommands::get)
        .collect(Collectors.toList());

    TemplatedResource resource = new JarResource("/debian/init.sh");
    bindings.put("services", Strings.join(upstartNames, " "));
    String upstartInit = resource.setBindings(bindings).toString();

    resource = new JarResource("/debian/install.sh");
    bindings = new HashMap<>();
    bindings.put("prepare-environment", "true");
    bindings.put("install-redis", "true");
    bindings.put("install-remote-dependencies", "true");
    bindings.put("debian-repository", debianRepository);
    bindings.put("install-commands", String.join("\n", serviceInstalls));
    bindings.put("service-action", "restart");
    bindings.put("upstart-init", upstartInit);

    return resource.setBindings(bindings).toString();
  }
}
