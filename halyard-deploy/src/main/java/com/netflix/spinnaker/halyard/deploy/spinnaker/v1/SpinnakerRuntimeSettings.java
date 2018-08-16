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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService.Type;
import java.util.Collections;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SpinnakerRuntimeSettings {
  protected Services services = new Services();

  // For serialization
  public SpinnakerRuntimeSettings() {}

  public static class Services extends HashMap<Type,ServiceSettings> {
    @Override
    public ServiceSettings get(Object key) {
      if (!Type.class.isInstance(key)) {
        throw new HalException(Severity.FATAL, "Key must be of type SpinnakerService.Type");
      }
      if (!this.containsKey(key)) {
        throw new HalException(Severity.FATAL, "Service " + Type.class.cast(key).getCanonicalName() + " does not exist");
      }
      return super.get(key);
    }

    // Getters for backwards compatibility
    @JsonIgnore
    public ServiceSettings getClouddriver() { return this.get(Type.CLOUDDRIVER); }
    @JsonIgnore
    public ServiceSettings getClouddriverBootstrap() { return this.get(Type.CLOUDDRIVER_BOOTSTRAP); }
    @JsonIgnore
    public ServiceSettings getConsulClient() { return this.get(Type.CONSUL_CLIENT); }
    @JsonIgnore
    public ServiceSettings getConsulServer() { return this.get(Type.CONSUL_SERVER); }
    @JsonIgnore
    public ServiceSettings getDeck() { return this.get(Type.DECK); }
    @JsonIgnore
    public ServiceSettings getEcho() { return this.get(Type.ECHO); }
    @JsonIgnore
    public ServiceSettings getFiat() { return this.get(Type.FIAT); }
    @JsonIgnore
    public ServiceSettings getFront50() { return this.get(Type.FRONT50); }
    @JsonIgnore
    public ServiceSettings getGate() { return this.get(Type.GATE); }
    @JsonIgnore
    public ServiceSettings getIgor() { return this.get(Type.IGOR); }
    @JsonIgnore
    public ServiceSettings getKayenta() { return this.get(Type.KAYENTA); }
    @JsonIgnore
    public ServiceSettings getOrca() { return this.get(Type.ORCA); }
    @JsonIgnore
    public ServiceSettings getOrcaBootstrap() { return this.get(Type.ORCA_BOOTSTRAP); }
    @JsonIgnore
    public ServiceSettings getRosco() { return this.get(Type.ROSCO); }
    @JsonIgnore
    public ServiceSettings getRedis() { return this.get(Type.REDIS); }
    @JsonIgnore
    public ServiceSettings getRedisBootstrap() { return this.get(Type.REDIS_BOOTSTRAP); }
    @JsonIgnore
    public ServiceSettings getMonitoringDaemon() { return this.get(Type.MONITORING_DAEMON); }
    @JsonIgnore
    public ServiceSettings getVaultClient() { return this.get(Type.VAULT_CLIENT); }
    @JsonIgnore
    public ServiceSettings getVaultServer() { return this.get(Type.VAULT_SERVER); }
  }

  @JsonIgnore
  public Map<Type, ServiceSettings> getAllServiceSettings() {
    return Collections.unmodifiableMap(services);
  }

  public void setServiceSettings(Type type, ServiceSettings settings) {
    services.put(type, settings);
  }

  public ServiceSettings getServiceSettings(SpinnakerService service) {
    return getServiceSettings(service.getType());
  }

  private ServiceSettings getServiceSettings(Type type) {
    return services.get(type);
  }
}
