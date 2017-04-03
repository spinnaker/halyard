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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service;


import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.MetricRegistryProfileFactoryBuilder;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.ProfileFactory;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.SpinnakerMonitoringDaemonProfileFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Component
abstract public class SpinnakerMonitoringDaemonService extends SpinnakerService<SpinnakerMonitoringDaemonService.SpinnakerMonitoringDaemon> {
  final boolean safeToUpdate = true;
  final boolean monitored = false;

  protected final String CONFIG_OUTPUT_PATH = "/opt/spinnaker-monitoring/config/";
  protected final String REGISTRY_OUTPUT_PATH = "/opt/spinnaker-monitoring/registry/";

  @Autowired
  SpinnakerMonitoringDaemonProfileFactory spinnakerMonitoringDaemonProfileFactory;

  @Autowired
  MetricRegistryProfileFactoryBuilder metricRegistryProfileFactoryBuilder;

  @Autowired
  List<SpinnakerService> services;

  @Override
  public SpinnakerArtifact getArtifact() {
    return SpinnakerArtifact.SPINNAKER_MONITORING_DAEMON;
  }

  @Override
  public Type getType() {
    return Type.MONITORING_DAEMON;
  }

  @Override
  public Class<SpinnakerMonitoringDaemon> getEndpointClass() {
    return SpinnakerMonitoringDaemon.class;
  }

  public static String serviceRegistryProfileName(String serviceName) {
    return "registry/" + serviceName + ".yml";
  }

  @Override
  public List<Profile> getProfiles(DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    List<Profile> results = new ArrayList<>();
    for (SpinnakerService service : services) {
      if (service.isMonitored()) {
        String profileName = serviceRegistryProfileName(service.getCanonicalName());
        String profilePath = Paths.get(REGISTRY_OUTPUT_PATH, profileName).toString();
        ProfileFactory factory = metricRegistryProfileFactoryBuilder.build(service);
        results.add(factory.getProfile(profileName, profilePath, deploymentConfiguration, endpoints));
      }
    }

    String profileName = "spinnaker-monitoring.yml";
    String profilePath = Paths.get(CONFIG_OUTPUT_PATH, profileName).toString();

    results.add(spinnakerMonitoringDaemonProfileFactory.getProfile(profileName, profilePath, deploymentConfiguration, endpoints));
    return results;
  }

  public interface SpinnakerMonitoringDaemon { }

  @EqualsAndHashCode(callSuper = true)
  @Data
  public static class Settings extends ServiceSettings {
    int port = 8008;
    String address = "localhost";
    String host = "0.0.0.0";
    String scheme = "http";
    String healthEndpoint = null;
    boolean enabled = true;
    boolean monitor = false;
  }
}
