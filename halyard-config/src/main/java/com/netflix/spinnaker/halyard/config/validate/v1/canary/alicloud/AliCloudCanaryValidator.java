/*
 * Copyright 2018 Netflix, Inc.
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

package com.netflix.spinnaker.halyard.config.validate.v1.canary.alicloud;

import com.netflix.spinnaker.halyard.config.model.v1.canary.alicloud.AliCloudCanaryAccount;
import com.netflix.spinnaker.halyard.config.model.v1.canary.alicloud.AliCloudCanaryServiceIntegration;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class AliCloudCanaryValidator extends Validator<AliCloudCanaryServiceIntegration> {

  @Override
  public void validate(ConfigProblemSetBuilder p, AliCloudCanaryServiceIntegration n) {
    if (n.isOssEnabled()) {
      List<AliCloudCanaryAccount> accountsWithBucket =
          n.getAccounts().stream().filter(a -> a.getBucket() != null).collect(Collectors.toList());

      if (CollectionUtils.isEmpty(accountsWithBucket)) {
        p.addProblem(
            Problem.Severity.ERROR,
            "At least one alicloud account must specify a bucket if OSS is enabled.");
      }
    } else {
      AliCloudCanaryAccountValidator alicloudCanaryAccountValidator =
          new AliCloudCanaryAccountValidator();
      n.getAccounts().stream().forEach(a -> alicloudCanaryAccountValidator.validate(p, a));
    }
  }
}
