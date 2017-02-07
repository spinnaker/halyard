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

package com.netflix.spinnaker.halyard.config.errors.v1;

import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;

import java.util.List;

/**
 * This is reserved for Halyard configs that fall between unparseable (not valid yaml), and incorrectly configured
 * (provider-specific error). Essentially, when a config has problems that prevent halyard from validating it, although
 * it is readable by our yaml parser into the halconfig Object, this is thrown
 */
public class IllegalConfigException extends HalException {
  public IllegalConfigException(List<Problem> problems) {
    super(problems);
  }

  public IllegalConfigException(Problem problem) {
    super(problem);
  }
}
