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

package com.netflix.spinnaker.halyard.controllers.v1;

import com.netflix.spinnaker.halyard.config.model.v1.HalconfigCoordinates;
import com.netflix.spinnaker.halyard.config.model.v1.providers.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.Providers;
import com.netflix.spinnaker.halyard.config.services.v1.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/config/deployments/{deployment:.+}/providers")
public class ProviderController {
  @Autowired
  ProviderService providerService;

  @RequestMapping(value = "/{provider:.+}", method = RequestMethod.GET)
  Provider provider(@PathVariable String deployment, @PathVariable String provider) {
    HalconfigCoordinates coordinates = new HalconfigCoordinates().setDeployment(deployment).setProvider(provider);
    return providerService.getProvider(coordinates);
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  Providers providers(@PathVariable String deployment) {
    HalconfigCoordinates coordinates = new HalconfigCoordinates().setDeployment(deployment);
    return providerService.getProviders(coordinates);
  }
}
