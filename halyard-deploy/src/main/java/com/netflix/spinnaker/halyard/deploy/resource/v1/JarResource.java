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

package com.netflix.spinnaker.halyard.deploy.resource.v1;

import com.amazonaws.util.IOUtils;
import com.netflix.spinnaker.halyard.config.resource.v1.TemplatedResource;
import java.io.IOException;
import java.io.InputStream;

public class JarResource extends TemplatedResource {
  @Override
  protected String getContents() {
    InputStream contents = getClass().getResourceAsStream(path);

    if (contents == null) {
      throw new IllegalArgumentException("Path " + path + " could not be found in the JAR");
    }
    try {
      return IOUtils.toString(contents);
    } catch (IOException e) {
      throw new RuntimeException("Path " + path + " could not be opened", e);
    }
  }

  public JarResource(String path) {
    this.path = path;
  }

  private String path;
}
