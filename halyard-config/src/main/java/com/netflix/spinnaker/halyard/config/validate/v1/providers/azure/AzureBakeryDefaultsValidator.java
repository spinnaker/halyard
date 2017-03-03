/*
 * Copyright 2017 Microsoft, Inc.
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

package com.netflix.spinnaker.halyard.config.validate.v1.providers.azure;

import com.netflix.spinnaker.clouddriver.azure.security.AzureCredentials;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.azure.AzureBakeryDefaults;
import com.netflix.spinnaker.halyard.config.model.v1.providers.azure.AzureBaseImage;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import lombok.Data;

import java.util.List;

@Data
public class AzureBakeryDefaultsValidator extends Validator<AzureBakeryDefaults> {
  final private List<AzureCredentials> credentialsList;

  @Override
  public void validate(ConfigProblemSetBuilder p, AzureBakeryDefaults n) {
    List<AzureBaseImage> baseImages = n.getBaseImages();

    AzureBaseImageValidator baseImageValidator = new AzureBaseImageValidator(credentialsList);

    baseImages.forEach(baseImage -> baseImageValidator.validate(p, baseImage));
  }
}
