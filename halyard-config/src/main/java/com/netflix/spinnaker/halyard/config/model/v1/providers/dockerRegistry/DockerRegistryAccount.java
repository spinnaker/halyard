/*
 * Copyright 2016 Google, Inc.
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

package com.netflix.spinnaker.halyard.config.model.v1.providers.dockerRegistry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.node.LocalFile;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
public class DockerRegistryAccount extends Account {
  protected static final String ECR_URL_PATTERN = "^https://([0-9]+)\\.dkr\\.ecr\\.([a-z0-9\\-]+)\\.amazonaws.com";

  private String address;
  private String username;
  private String password;
  private String email;
  private Long cacheIntervalSeconds = 30L;
  private List<String> repositories = new ArrayList<>();
  @LocalFile private String passwordFile;
  @LocalFile private String dockerconfigFile;

  public String getAddress() {
    if (address.startsWith("https://") || address.startsWith("http://")) {
      return address;
    } else {
      return "https://" + address;
    }
  }

  @JsonIgnore
  public boolean isEcr() {
    Pattern regexp = Pattern.compile(ECR_URL_PATTERN);
    return regexp.matcher(getAddress()).find();
  }

  @JsonIgnore
  public String getEcrRegistryId() {
    Pattern regexp = Pattern.compile(ECR_URL_PATTERN);
    Matcher matches = regexp.matcher(getAddress());

    if (!matches.find()) {
      throw new IllegalArgumentException("Tried getting the ECR registry id of a non-ECR registry.");
    }

    return matches.group(1);
  }

  @JsonIgnore
  public String getEcrRegistryRegion() {
    Pattern regexp = Pattern.compile(ECR_URL_PATTERN);
    Matcher matches = regexp.matcher(getAddress());

    if (!matches.find()) {
      throw new IllegalArgumentException("Tried getting the ECR registry region of a non-ECR registry.");
    }

    return matches.group(2);
  }

  @Override
  public void accept(ConfigProblemSetBuilder psBuilder, Validator v) {
    v.validate(psBuilder, this);
  }
}
