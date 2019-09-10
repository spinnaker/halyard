/*
 * Copyright 2019 Pivotal, Inc.
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

package com.netflix.spinnaker.halyard.cli.command.v1.converter;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentEnvironment.ImageVariant;

public class ImageVariantConverter implements IStringConverter<ImageVariant> {
  @Override
  public ImageVariant convert(String value) {
    try {
      return ImageVariant.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new ParameterException(e.getMessage(), e);
    }
  }
}
