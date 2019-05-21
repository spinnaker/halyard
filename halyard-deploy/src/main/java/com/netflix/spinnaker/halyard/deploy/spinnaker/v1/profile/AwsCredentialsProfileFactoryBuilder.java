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
 *
 */

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.secrets.v1.SecretSessionManager;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AwsCredentialsProfileFactoryBuilder {
  @Autowired protected ArtifactService artifactService;

  @Autowired SecretSessionManager secretSessionManager;

  @Setter private String profileName = "default";

  @Setter(AccessLevel.NONE)
  private String accessKeyId;

  @Setter(AccessLevel.NONE)
  private String secretAccessKey;

  @Setter private SpinnakerArtifact artifact;

  public AwsCredentialsProfileFactoryBuilder setAccessKeyId(String accessKeyId) {
    if (StringUtils.isEmpty(accessKeyId)) {
      try {
        this.accessKeyId =
            DefaultAWSCredentialsProviderChain.getInstance().getCredentials().getAWSAccessKeyId();
      } catch (SdkClientException e) {
        throw new HalException(
            Problem.Severity.FATAL, "Fail to set aws access key: " + e.getMessage(), e);
      }
    } else {
      this.accessKeyId = accessKeyId;
    }
    return this;
  }

  public AwsCredentialsProfileFactoryBuilder setSecretAccessKey(String secretAccessKey) {
    if (StringUtils.isEmpty(secretAccessKey)) {
      try {
        this.secretAccessKey =
            DefaultAWSCredentialsProviderChain.getInstance().getCredentials().getAWSSecretKey();
      } catch (SdkClientException e) {
        throw new HalException(
            Problem.Severity.FATAL, "Fail to set aws secret access key: " + e.getMessage(), e);
      }
    } else {
      this.secretAccessKey = secretAccessKey;
    }
    return this;
  }

  public AwsCredentialsProfileFactory build() {
    return new AwsCredentialsProfileFactory(artifact, accessKeyId, secretAccessKey);
  }

  public String getOutputFile(String spinnakerHome) {
    return Paths.get(spinnakerHome, ".aws/credentials").toString();
  }

  @EqualsAndHashCode(callSuper = false)
  @Data
  public class AwsCredentialsProfileFactory extends TemplateBackedProfileFactory {
    public AwsCredentialsProfileFactory(
        SpinnakerArtifact artifact, String accessKeyId, String secretAccessKey) {
      super();
      this.accessKeyId = accessKeyId;
      this.secretAccessKey = secretAccessKey;
      this.artifact = artifact;
    }

    private final String accessKeyId;

    private final String secretAccessKey;

    @Override
    protected ArtifactService getArtifactService() {
      return artifactService;
    }

    private String template =
        String.join(
            "\n",
            "[" + profileName + "]",
            "aws_access_key_id = {%accessKeyId%}",
            "aws_secret_access_key = {%secretAccessKey%}");

    @Override
    protected Map<String, Object> getBindings(
        DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
      Map<String, Object> result = new HashMap<>();
      result.put("accessKeyId", accessKeyId);
      result.put(
          "secretAccessKey",
          AwsCredentialsProfileFactoryBuilder.this.secretSessionManager.decrypt(secretAccessKey));
      return result;
    }

    SpinnakerArtifact artifact;

    @Override
    protected String commentPrefix() {
      return null;
    }

    @Override
    protected boolean showEditWarning() {
      return false;
    }
  }
}
