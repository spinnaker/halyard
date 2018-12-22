/*
 * Copyright 2018 Andreas Bergmeier
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

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * These are custom Certificate Authority files, which get injected into the service.
 *
 * Warning: Do not define default values for the below fields in this class, since they will override user-supplied values.
 */
@Data
public class CustomCaSettings {
  String name;
  List<String> files = new ArrayList<>();

  public CustomCaSettings() {}
}
