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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.deck;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.config.model.v1.node.Features;
import com.netflix.spinnaker.halyard.config.model.v1.node.Notifications;
import com.netflix.spinnaker.halyard.config.model.v1.notifications.SlackNotification;
import com.netflix.spinnaker.halyard.config.model.v1.providers.appengine.AppengineProvider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.azure.AzureProvider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.dcos.DCOSProvider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.google.GoogleProvider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesProvider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackAccount;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackProvider;
import com.netflix.spinnaker.halyard.config.model.v1.security.UiSecurity;
import com.netflix.spinnaker.halyard.config.services.v1.AccountService;
import com.netflix.spinnaker.halyard.config.services.v1.VersionsService;
import com.netflix.spinnaker.halyard.core.registry.v1.Versions;
import com.netflix.spinnaker.halyard.core.resource.v1.StringResource;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.RegistryBackedProfileFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class DeckProfileFactory extends RegistryBackedProfileFactory {

  @Autowired
  AccountService accountService;

  @Autowired
  VersionsService versionsService;

  @Override
  public String commentPrefix() {
    return "// ";
  }

  @Override
  public SpinnakerArtifact getArtifact() {
    return SpinnakerArtifact.DECK;
  }

  @Override
  protected void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
    StringResource configTemplate = new StringResource(profile.getBaseContents());
    UiSecurity uiSecurity = deploymentConfiguration.getSecurity().getUiSecurity();
    profile.setUser(ApacheSettings.APACHE_USER);

    Features features = deploymentConfiguration.getFeatures();
    Notifications notifications = deploymentConfiguration.getNotifications();
    Map<String, String> bindings = new HashMap<>();
    String version = deploymentConfiguration.getVersion();

    // Configure global settings
    bindings.put("gate.baseUrl", endpoints.getServices().getGate().getBaseUrl());
    bindings.put("timezone", deploymentConfiguration.getTimezone());
    bindings.put("version", deploymentConfiguration.getVersion());

    Optional<Versions.Version> validatedVersion = versionsService.getVersions().getVersion(version);

    validatedVersion.ifPresent(v -> {
      String changelog = v.getChangelog();
      bindings.put("changelog.gist.id", changelog.substring(changelog.lastIndexOf("/") + 1));
      bindings.put("changelog.gist.name", "changelog.md");
    });

    // Configure feature-flags
    bindings.put("features.auth", Boolean.toString(features.isAuth(deploymentConfiguration)));
    bindings.put("features.chaos", Boolean.toString(features.isChaos()));
    bindings.put("features.jobs", Boolean.toString(features.isJobs()));
    bindings.put("features.fiat", Boolean.toString(deploymentConfiguration.getSecurity().getAuthz().isEnabled()));
    bindings.put("features.pipelineTemplates", Boolean.toString(features.getPipelineTemplates() != null ? features.getPipelineTemplates() : false));
    bindings.put("features.artifacts", Boolean.toString(features.getArtifacts() != null ? features.getArtifacts() : false));
    bindings.put("features.mineCanary", Boolean.toString(features.getMineCanary() != null ? features.getMineCanary() : false));

    // Configure Kubernetes
    KubernetesProvider kubernetesProvider = deploymentConfiguration.getProviders().getKubernetes();
    bindings.put("kubernetes.default.account", kubernetesProvider.getPrimaryAccount());
    bindings.put("kubernetes.default.namespace", "default");
    bindings.put("kubernetes.default.proxy", "localhost:8001");

    // Configure GCE
    GoogleProvider googleProvider = deploymentConfiguration.getProviders().getGoogle();
    bindings.put("google.default.account", googleProvider.getPrimaryAccount());
    bindings.put("google.default.region", "us-central1");
    bindings.put("google.default.zone", "us-central1-f");

    // Configure Azure
    AzureProvider azureProvider = deploymentConfiguration.getProviders().getAzure();
    bindings.put("azure.default.account", azureProvider.getPrimaryAccount());
    bindings.put("azure.default.region", "westus");

    // Configure Appengine
    AppengineProvider appengineProvider = deploymentConfiguration.getProviders().getAppengine();
    bindings.put("appengine.default.account", appengineProvider.getPrimaryAccount());
    bindings.put("appengine.enabled", Boolean.toString(appengineProvider.getPrimaryAccount() != null));

    // Configure DC/OS
    final DCOSProvider dcosProvider = deploymentConfiguration.getProviders().getDcos();
    bindings.put("dcos.default.account", dcosProvider.getPrimaryAccount());
    //TODO(willgorman) need to set the proxy url somehow

    // Configure Openstack
    OpenstackProvider openstackProvider = deploymentConfiguration.getProviders().getOpenstack();
    bindings.put("openstack.default.account", openstackProvider.getPrimaryAccount());
    if (openstackProvider.getPrimaryAccount() != null) {
      OpenstackAccount openstackAccount = (OpenstackAccount) accountService.getProviderAccount(deploymentConfiguration.getName(), "openstack", openstackProvider.getPrimaryAccount());
      String firstRegion = openstackAccount.getRegions().get(0);
      bindings.put("openstack.default.region", firstRegion);
    }

    // Configure notifications
    bindings.put("notifications.enabled", notifications.isEnabled() + "");

    SlackNotification slackNotification = notifications.getSlack();
    bindings.put("notifications.slack.enabled", slackNotification.isEnabled() + "");
    bindings.put("notifications.slack.botName", slackNotification.getBotName());

    profile.appendContents(configTemplate.setBindings(bindings).toString())
        .setRequiredFiles(backupRequiredFiles(uiSecurity, deploymentConfiguration.getName()));
  }
}
