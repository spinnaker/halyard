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

package com.netflix.spinnaker.halyard.deploy.provider.v1.google;

import com.google.api.services.compute.Compute;
import com.google.api.services.compute.model.*;
import com.netflix.spinnaker.clouddriver.google.security.GoogleNamedAccountCredentials;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.google.GoogleAccount;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.deploy.deployment.v1.AccountDeploymentDetails;
import com.netflix.spinnaker.halyard.deploy.provider.v1.ProviderInterface;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.RunningServiceDetails;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerEndpoints;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerMonitoringDaemonService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GoogleProviderInterface extends ProviderInterface<GoogleAccount> {
  @Autowired
  String halyardVersion;

  @Autowired
  ApplicationContext applicationContext;

  @Override
  public Provider.ProviderType getProviderType() {
    return null;
  }

  @Override
  protected String componentArtifact(AccountDeploymentDetails<GoogleAccount> details, SpinnakerArtifact artifact) {
    switch (artifact) {
      case CONSUL:
        return "projects/marketplace-spinnaker-release/global/images/consul-bootstrapping-server";
      default:
        throw new RuntimeException("Unhandled artifact: " + artifact);
    }
  }

  @Override
  public <S> S connectTo(AccountDeploymentDetails<GoogleAccount> details, SpinnakerService<S> service) {
    return null;
  }

  @Override
  public String connectToCommand(AccountDeploymentDetails<GoogleAccount> details, SpinnakerService service) {
    return null;
  }

  @Override
  protected Map<String, Object> upsertLoadBalancerTask(AccountDeploymentDetails<GoogleAccount> details, SpinnakerService service) {
    return null;
  }

  @Override
  protected Map<String, Object> deployServerGroupPipeline(AccountDeploymentDetails<GoogleAccount> details, SpinnakerService service, SpinnakerMonitoringDaemonService monitoringService, boolean update) {
    return null;
  }

  @Override
  public void ensureServiceIsRunning(AccountDeploymentDetails<GoogleAccount> details, SpinnakerService service) {

  }

  @Override
  public boolean serviceExists(AccountDeploymentDetails<GoogleAccount> details, SpinnakerService service) {
    return false;
  }

  @Override
  public void bootstrapSpinnaker(AccountDeploymentDetails<GoogleAccount> details, SpinnakerEndpoints.Services services) {

  }

  @Override
  public RunningServiceDetails getRunningServiceDetails(AccountDeploymentDetails<GoogleAccount> details, SpinnakerService service) {
    return null;
  }

  private void deployService(AccountDeploymentDetails<GoogleAccount> details, SpinnakerService service) {
    GoogleAccount account = details.getAccount();
    String project = account.getProject();
    String region = account.getDefaultRegion();
    ConfigProblemSetBuilder problemSetBuilder = new ConfigProblemSetBuilder(applicationContext);
    GoogleNamedAccountCredentials credentials = account.getNamedAccountCredentials(halyardVersion, problemSetBuilder);
    SpinnakerArtifact artifact = service.getArtifact();
    String artifactName = artifact.getName();

    if (credentials == null) {
      throw new HalException(problemSetBuilder.build().getProblems());
    }

    Compute compute = credentials.getCompute();

    InstanceGroupManager manager = new InstanceGroupManager();

    InstanceTemplate template = new InstanceTemplate()
        .setName(artifactName + "-hal-" + System.currentTimeMillis())
        .setDescription("Halyard-generated instance template for deploying Spinnaker");

    InstanceProperties properties = new InstanceProperties()
        .setMachineType("n1-standard-1");

    AttachedDisk disk = new AttachedDisk()
        .setBoot(true);

    AttachedDiskInitializeParams diskParams = new AttachedDiskInitializeParams()
        .setSourceImage(componentArtifact(details, artifact));

    disk.setInitializeParams(diskParams);
    List<AttachedDisk> disks = new ArrayList<>();

    disks.add(disk);
    properties.setDisks(disks);
    template.setProperties(properties);

    String instanceTemplateUrl;
    Operation operation;
    try {
      operation = compute.instanceTemplates().insert(project, template).execute();
      instanceTemplateUrl = operation.getTargetLink();
      waitOnGlobalOperation(compute, project, operation);
    } catch (IOException e) {
      throw new HalException(Problem.Severity.FATAL, "Failed to create instance template for " + artifactName + ": " + e.getMessage());
    }

    manager.setInstanceTemplate(instanceTemplateUrl);
    manager.setTargetSize(3);

    try {
      compute.regionInstanceGroupManagers().insert(project, region, manager).execute();
    } catch (IOException e) {
      throw new HalException(Problem.Severity.FATAL, "Failed to create instance group to run artifact " + artifactName + ": " + e.getMessage());
    }
  }

  private static void waitOnGlobalOperation(Compute compute, String project, Operation operation) throws IOException {
    while (!operation.getStatus().equals("DONE")) {
      operation = compute.globalOperations().get(project, operation.getName()).execute();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }
    }
  }
}
