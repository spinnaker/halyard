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
 */

package com.netflix.spinnaker.halyard.config.validate.v1.persistentStorage;

import com.netflix.spinnaker.halyard.config.model.v1.node.PersistentStorage;
import com.netflix.spinnaker.halyard.config.model.v1.node.PersistentStore;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PersistentStorageValidator extends Validator<PersistentStorage> {
  @Override
  public void validate(ConfigProblemSetBuilder ps, PersistentStorage n) {
    String persistentStoreType = n.getPersistentStoreType();
    if (persistentStoreType == null || persistentStoreType.isEmpty()) {
      ps.addProblem(Severity.WARNING, "Your deployment will most likely fail until you configure and enable a persistent store.");
    } else {
      Object[] persistentStoreTypes = Arrays.stream(PersistentStore.PersistentStoreType.values()).map(p -> p.getId()).toArray();

      if (!Arrays.stream(persistentStoreTypes).anyMatch(p -> persistentStoreType.equalsIgnoreCase((String) p))) {
        ps.addProblem(Severity.ERROR, "Unknown persistent store type \"" + persistentStoreType + "\".")
            .setRemediation("Set a persistent store from the following types: " + Arrays.toString(persistentStoreTypes));
      }
    }
  }
}
