/*
 * Copyright 2018 Google, Inc.
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

package com.netflix.spinnaker.halyard.config.validate.v1.security;

import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.security.IAP;
import com.netflix.spinnaker.halyard.config.model.v1.security.OAuth2;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import org.springframework.stereotype.Component;

@Component
public class IAPValidator extends Validator<IAP> {
  @Override
  public void validate(ConfigProblemSetBuilder p, IAP iap) {

    if (!iap.isEnabled()) {
      return;
    }

    if (iap.getAudience() == null) {
      p.addProblem(Problem.Severity.ERROR,
          "No Audience was supplied. You can retrieve this field from the IAP console: "
          + "https://cloud.google.com/iap/docs/signed-headers-howto#verify_the_id_token_header.");
    }
  }
}
