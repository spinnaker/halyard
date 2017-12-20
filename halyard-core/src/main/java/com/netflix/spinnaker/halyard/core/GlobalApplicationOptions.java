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

package com.netflix.spinnaker.halyard.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;

/**
 * This is the collection of general, top-level flags that come from the application configuration
 * for halyard.
 */
@Data
public class GlobalApplicationOptions {

  private static final String CONFIG_PATH = "/tmp/hal.config";

  private boolean useRemoteDaemon = false;

  public boolean isUseRemoteDaemon() {
    return options.useRemoteDaemon;
  }

  public static GlobalApplicationOptions getInstance() {
    if (GlobalApplicationOptions.options == null) {
      Yaml yamlParser = new Yaml();
      ObjectMapper objectMapper = new ObjectMapper();

      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

      try {
        GlobalApplicationOptions.options = objectMapper.convertValue(
            yamlParser.load(FileUtils.openInputStream(new File(CONFIG_PATH))),
            GlobalApplicationOptions.class
        );
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return GlobalApplicationOptions.options;
  }

  private static GlobalApplicationOptions options = null;
}
