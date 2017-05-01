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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.google;

import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import com.netflix.spinnaker.halyard.core.RemoteAction;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.DistributedServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleDistributedServiceProvider extends DistributedServiceProvider<KubernetesAccount> {
  @Autowired
  GoogleClouddriverBootstrapService clouddriverBootstrapService;

  @Autowired
  GoogleConsulClientService consulClientService;

  @Autowired
  GoogleConsulServerService consulServerService;

  @Autowired
  GoogleMonitoringDaemonService monitoringDaemonService;

  @Autowired
  GoogleVaultServerService vaultServerService;

  // For serialization
  public GoogleDistributedServiceProvider() {}

  @Override
  public RemoteAction clean(AccountDeploymentDetails<KubernetesAccount> details, SpinnakerRuntimeSettings runtimeSettings) {
    throw new HalException(Problem.Severity.FATAL, "not yet implemented.");
  }
}
