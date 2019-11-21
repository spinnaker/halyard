/*
 * Copyright 2019 Alibaba Group.
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

package com.netflix.spinnaker.halyard.config.validate.v1.providers.alicloud;

import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.alicloud.AliCloudAccount;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.config.services.v1.ProviderService;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AliCloudAccountValidator extends Validator<AliCloudAccount> {
  @Autowired ProviderService providerService;

  /**
   * When the parameter noValidate = true of the command enters the validator. When the Severity
   * level is error, the program reports an error and throws an exception.
   */
  @Override
  public void validate(ConfigProblemSetBuilder p, AliCloudAccount n) {
    p.addProblem(Severity.WARNING, "No validation for the alicloud provider has been implemented.");
  }
}
