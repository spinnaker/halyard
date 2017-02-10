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

package com.netflix.spinnaker.halyard.config.validate.v1.providers.appengine;

import com.amazonaws.util.IOUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.netflix.spinnaker.clouddriver.appengine.security.AppengineNamedAccountCredentials;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.config.model.v1.providers.appengine.AppengineAccount;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Component
public class AppengineAccountValidator extends Validator<AppengineAccount> {
  @Autowired
  String halyardVersion;
  
  @Override
  public void validate(ConfigProblemSetBuilder p, AppengineAccount account) {
    String jsonKey = null;
    String jsonPath = account.getJsonPath();
    String project = account.getProject();
    AppengineNamedAccountCredentials credentials = null;

    boolean hasPassword = account.getGitHttpsPassword() != null;
    boolean hasUsername = account.getGitHttpsUsername() != null && !account.getGitHttpsUsername().isEmpty();
    if (hasPassword != hasUsername) {
      if (!hasUsername) {
        p.addProblem(Severity.ERROR, "Git HTTPS password supplied without git HTTPS username.");
      } else {
        p.addProblem(Severity.ERROR, "Git HTTPS username supplied without git HTTPS password.");
      }
    }

    boolean hasSshPrivateKeyPassphrase = account.getSshPrivateKeyPassphrase() != null;
    boolean hasSshPrivateKeyFilePath = account.getSshPrivateKeyFilePath() != null && !account.getSshPrivateKeyFilePath().isEmpty();
    if (hasSshPrivateKeyPassphrase != hasSshPrivateKeyFilePath) {
      if (!hasSshPrivateKeyFilePath) {
        p.addProblem(Severity.ERROR, "SSH private key passphrase supplied without SSH private key filepath.");
      } else {
        p.addProblem(Severity.ERROR, "SSH private key filepath supplied without SSH private key passphrase.");
      }
    } else if (hasSshPrivateKeyPassphrase && hasSshPrivateKeyFilePath) {
      try {
        String sshPrivateKey = IOUtils.toString(new FileInputStream(account.getSshPrivateKeyFilePath()));
        if (sshPrivateKey.isEmpty()) {
          p.addProblem(Severity.WARNING, "The supplied SSH private key file is empty.");
        } else {
          try {
            // Assumes that the public key is sitting next to the private key with the extension ".pub".
            KeyPair keyPair = KeyPair.load(new JSch(), account.getSshPrivateKeyFilePath());
            boolean decrypted = keyPair.decrypt(account.getSshPrivateKeyPassphrase());
            if (!decrypted) {
              p.addProblem(Severity.ERROR, "Could not unlock SSH public/private keypair with supplied passphrase.");
            }
          } catch (JSchException e) {
            p.addProblem(Severity.ERROR, "Could not unlock SSH public/private keypair: " + e.getMessage() + ".");
          }
        }
      } catch (FileNotFoundException e) {
        p.addProblem(Severity.ERROR, "SSH private key not found: " + e.getMessage() + ".");
      } catch (IOException e) {
        p.addProblem(Severity.ERROR, "Error opening specified path to SSH private key: " + e.getMessage() + ".");
      }
    }

    try {
      if (jsonPath != null && !jsonPath.isEmpty()) {
        jsonKey = IOUtils.toString(new FileInputStream(account.getJsonPath()));
        if (jsonKey.isEmpty()) {
          p.addProblem(Severity.WARNING, "The supplied credentials file is empty.");
        }
      }
    } catch (FileNotFoundException e) {
      p.addProblem(Severity.ERROR, "Json path not found: " + e.getMessage() + ".");
    } catch (IOException e) {
      p.addProblem(Severity.ERROR, "Error opening specified json path: " + e.getMessage() + ".");
    }

    if (account.getProject() == null || account.getProject().isEmpty()) {
      p.addProblem(Severity.ERROR, "No appengine project supplied.");
      return;
    }
    
    try {
      credentials = new AppengineNamedAccountCredentials.Builder()
              .jsonKey(jsonKey)
              .project(project)
              .applicationName("halyard " + halyardVersion)
              .build();
              
    } catch (Exception e) {
      p.addProblem(Severity.ERROR, "Error instantiating appengine credentials: " + e.getMessage() + ".");
      return;
    }
    
    try {
      // Location is the only App Engine resource guaranteed to exist. The API only accepts '-' here
      // rather than project name, because the list of locations is static and not a property of an individual project.
      credentials.getAppengine().apps().locations().list("-").execute();
    } catch (Exception e) {
      p.addProblem(Severity.ERROR, "Failed to connect to appengine Admin API: " + e.getMessage() + ".");
    }
  }
}
