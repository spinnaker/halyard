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
 */

package com.netflix.spinnaker.halyard.models.v1;

import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@ControllerAdvice
public class ValidationSettingsHandler {
  @ModelAttribute
  public ValidationSettings controllerValues(
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.validate) boolean validate,
      @RequestParam(required = false, defaultValue = DefaultValidationSettings.severity) Problem.Severity severity
  ) {
    ValidationSettings values = new ValidationSettings();
    values.setValidate(validate);
    values.setSeverity(severity);
    return values;
  }

  private static class DefaultValidationSettings {
    final static String validate = "false";
    final static String severity = "WARNING";
  }
}
