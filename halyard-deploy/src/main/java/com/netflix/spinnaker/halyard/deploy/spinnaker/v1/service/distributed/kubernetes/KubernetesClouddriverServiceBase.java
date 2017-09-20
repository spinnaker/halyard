/*
 * Copyright 2017 Netflix, Inc.
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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes;

import com.netflix.spinnaker.clouddriver.kubernetes.deploy.description.servergroup.KubernetesEmptyDir;
import com.netflix.spinnaker.clouddriver.kubernetes.deploy.description.servergroup.KubernetesVolumeSource;
import com.netflix.spinnaker.clouddriver.kubernetes.deploy.description.servergroup.KubernetesVolumeSourceType;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.EcrTokenRefreshService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.SidecarService;

import java.util.ArrayList;
import java.util.List;

public interface KubernetesClouddriverServiceBase {
  default List<KubernetesVolumeSource> processSidecars(List<SidecarService> sidecars, SpinnakerRuntimeSettings runtimeSettings, EcrTokenRefreshService ecrTokenRefreshService) {
    List<KubernetesVolumeSource> kubernetesVolumeSources = new ArrayList<>();

    // Add ECR Token Refresh sidecar.
    ServiceSettings ecrTokenRefreshSettings = runtimeSettings.getServiceSettings(ecrTokenRefreshService);

    if (!ecrTokenRefreshSettings.getEnabled()) {
      return kubernetesVolumeSources;
    }

    sidecars.add(ecrTokenRefreshService);

    // Create an emptyDir volume source for storing the ECR token file.
    KubernetesVolumeSource ecrPassVolume = new KubernetesVolumeSource();

    ecrPassVolume.setName("ecr-pass");
    ecrPassVolume.setType(KubernetesVolumeSourceType.EmptyDir);
    ecrPassVolume.setEmptyDir(new KubernetesEmptyDir());

    kubernetesVolumeSources.add(ecrPassVolume);

    return kubernetesVolumeSources;
  }
}
