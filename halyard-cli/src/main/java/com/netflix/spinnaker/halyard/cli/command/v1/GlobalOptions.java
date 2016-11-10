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

package com.netflix.spinnaker.halyard.cli.command.v1;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.Data;

/**
 * This is the collection of general, top-level flags to be interpreted by halyard.
 */
@Data
@Parameters(separators = "=")
public class GlobalOptions {
  @Parameter(names = { "-v", "--verbose" }, description = "Enable verbose output")
  private boolean verbose = false;

  @Parameter(names = { "-c", "--color" }, description = "Enable terminal color output.", arity = 1)
  private boolean color = true;
}
