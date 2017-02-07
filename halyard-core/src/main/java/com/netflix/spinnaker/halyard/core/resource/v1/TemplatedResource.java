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
 */

package com.netflix.spinnaker.halyard.core.resource.v1;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Setter;

/**
 * Given a key/value set of bindings, and a resource that has "bindable" sites matching
 * {%key%} (this can be overridden), this class replaces those sites with their appropriate values.
 */
abstract public class TemplatedResource {
  @Setter
  Map<String, String> bindings = new HashMap<>();

  protected String formatKey(String key) {
    return "{%" + key + "%}";
  }

  abstract protected String getContents();

  @Override
  public String toString() {
    String contents = getContents();
    for (Entry<String, String> binding : bindings.entrySet()) {
      contents = contents.replace(formatKey(binding.getKey()), binding.getValue());
    }

    return contents;
  }
}
