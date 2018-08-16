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
 *
 */

package com.netflix.spinnaker.halyard.config.model.v1.ha;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netflix.spinnaker.halyard.config.model.v1.ha.HaService.HaServiceType;
import com.netflix.spinnaker.halyard.config.model.v1.node.Node;
import com.netflix.spinnaker.halyard.config.model.v1.node.NodeIterator;
import com.netflix.spinnaker.halyard.config.model.v1.node.NodeIteratorFactory;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class HaServices extends Node implements Cloneable {

  private ClouddriverHaService clouddriver = new ClouddriverHaService();
  private EchoHaService echo = new EchoHaService();

  @Override
  public String getNodeName() {
    return "haServices";
  }

  @Override
  public NodeIterator getChildren() {
    return NodeIteratorFactory.makeReflectiveIterator(this);
  }

  @Override
  public void accept(ConfigProblemSetBuilder psBuilder, Validator v) {
    v.validate(psBuilder, this);
  }

  @JsonIgnore
  public List<HaServiceType> getEnabledHaServiceTypes() {
    return getFieldsOfType(HaService.class).stream()
        .filter(HaService::isEnabled)
        .map(HaService::haServiceType)
        .collect(Collectors.toList());
  }

  private <T> List<T> getFieldsOfType(Class<T> clazz) {
    return Arrays.stream(this.getClass().getDeclaredFields())
        .filter(f -> clazz.isAssignableFrom(f.getType()))
        .map(f -> {
          f.setAccessible(true);
          try {
            return (T) f.get(this);
          } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read high availability service " + f.getName());
          }
        }).collect(Collectors.toList());
  }
}
