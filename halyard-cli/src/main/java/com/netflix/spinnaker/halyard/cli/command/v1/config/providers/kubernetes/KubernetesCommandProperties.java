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

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.kubernetes;

public class KubernetesCommandProperties {
  static final String CONTEXT_DESCRIPTION = "The kubernetes context to be managed by Spinnaker. "
      + "See http://kubernetes.io/docs/user-guide/kubeconfig-file/#context for more information.\n"
      + "When no context is configured for an account the 'current-context' in your kubeconfig is assumed.";

  static final String NAMESPACES_DESCRIPTION = "A list of namespaces this Spinnaker account can deploy to and will cache.\n"
      + "When no namespaces are configured, this defaults to 'all namespaces'.";

  static final String OMIT_NAMESPACES_DESCRIPTION = "A list of namespaces this Spinnaker account cannot deploy to or cache.\n"
      + "This can only be set when --namespaces is empty or not set.";

  static final String KINDS_DESCRIPTION = "(V2 Only) A list of resource kinds this Spinnaker account can deploy to and will cache.\n"
      + "When no kinds are configured, this defaults to 'all kinds described here https://spinnaker.io/reference/providers/kubernetes-v2'.";

  static final String OMIT_KINDS_DESCRIPTION = "(V2 Only) A list of resource kinds this Spinnaker account cannot deploy to or cache.\n"
      + "This can only be set when --kinds is empty or not set.";

  static final String DOCKER_REGISTRIES_DESCRIPTION = "A list of the Spinnaker docker registry account names this Spinnaker account can use as image sources. "
      + "These docker registry accounts must be registered in your halconfig before you can add them here.";

  static final String KUBECONFIG_DESCRIPTION = "The path to your kubeconfig file. By default, it will be under the Spinnaker user's home directory in the typical "
      + ".kube/config location.";

  static final String SERVICE_ACCOUNT_DESCRIPTION = "When true, Spinnaker attempt to authenticate against Kubernetes using a Kubernetes service account. "
      + "This only works when Halyard & Spinnaker are deployed in Kubernetes. Read more about service accounts here: https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/.";

  static final String CONFIGURE_IMAGE_PULL_SECRETS_DESCRIPTION = "(Only applicable to the v1 provider). When true, Spinnaker will create & manage your image pull "
      + "secrets for you; when false, you will have to create and attach them to your pod specs by hand.";

  static final String ONLY_SPINNAKER_MANAGED_DESCRIPTION = "(V2 Only) When true, Spinnaker will only cache/display applications that have been\n"
      + "created by Spinnaker; as opposed to attempting to configure applications for resources already present in Kubernetes.";

  static final String CHECK_PERMISSIONS_ON_STARTUP = "When false, clouddriver will skip the permission checks for all kubernetes kinds at startup. This can save a great deal of time\n"
      + "during clouddriver startup when you have many kubernetes accounts configured. This disables the log messages at startup about missing permissions.";

  static final String LIVE_MANIFEST_CALLS = "When true, clouddriver will query manifest status during pipeline executions using live data rather than the cache.\n"
      + "This eliminates all time spent in the \"force cache refresh\" task in pipelines, greatly reducing execution time.";
}
