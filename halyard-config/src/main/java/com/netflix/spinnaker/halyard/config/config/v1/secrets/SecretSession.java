/*
 * Copyright 2019 Armory, Inc.
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

package com.netflix.spinnaker.halyard.config.config.v1.secrets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  SecretSession contains the cached decrypted secrets and secret files
 */
class SecretSession {
  private Map<String, String> cache = new HashMap<>();
  private List<String> filePaths = new ArrayList<>();

  void cacheResult(String encryptedString, String clearText) {
    cache.put(encryptedString, clearText);
  }

  void addFile(String filePath) {
    filePaths.add(filePath);
  }

  String getCached(String encryptedString) {
    return cache.get(encryptedString);
  }

  void clearAllFiles() {
    for (String filePath : filePaths) {
      File f = new File(filePath);
      f.delete();
    }
  }
}