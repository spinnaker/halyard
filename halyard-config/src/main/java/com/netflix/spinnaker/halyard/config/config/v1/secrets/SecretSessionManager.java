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

import com.netflix.spinnaker.config.secrets.SecretManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 *  SecretSessionManager handles the decryption and encryption of secrets and secret files in the current session
 */
@Component
public class SecretSessionManager {
  private static ThreadLocal<SecretSession> secretSessions = new ThreadLocal<>();

  @Autowired
  private SecretManager secretManager;

  public void clearSession() {
    SecretSession session = secretSessions.get();
    if (session != null) {
      session.clearTempFiles();
      secretSessions.remove();
    }
  }

  public SecretSession getSession() {
    SecretSession session = secretSessions.get();
    if (session == null) {
      session = new SecretSession();
      secretSessions.set(session);
    }
    return session;
  }

  /**
   *  Takes an encrypted string or path to an encrypted file and returns the decrypted value.
   *
   *  Format for Encrypted Secrets:
   *
   *  encrypted:&lt;engine-identifier&gt;!&lt;param-name_1&gt;:&lt;param-value_1&gt;!..!&lt;param-name_n&gt;:&lt;param-value_n&gt;
   *
   *  Note: Valid param-names match the regex: `[a-zA-Z0-9]+`
   *  Note: secret-params may contain ':'
   *  Note: `encrypted` cannot be a param-name
   *  Note: There must be at least one &lt;param-name&gt;:&lt;param-value&gt; pair
   *  Named parameters are used to allow for adding additional options in the future.
   *
   * @param filePathOrEncryptedString the encrypted string in the format above defined by EncryptedSecret
   * @return decrypted value of the secret field or file
   */
  public String decrypt(String filePathOrEncryptedString) {
    return secretManager.decrypt(filePathOrEncryptedString);
  }

  /**
   *  Takes an encrypted string or path to an encrypted file, calls SecretManager to decrypt the contents
   *  and return the path to the decrypted temporary file.
   *
   *  Format for Encrypted Secrets:
   *
   *  encrypted:&lt;engine-identifier&gt;!&lt;param-name_1&gt;:&lt;param-value_1&gt;!..!&lt;param-name_n&gt;:&lt;param-value_n&gt;
   *
   *  Note: Valid param-names match the regex: `[a-zA-Z0-9]+`
   *  Note: secret-params may contain ':'
   *  Note: `encrypted` cannot be a param-name
   *  Note: There must be at least one &lt;param-name&gt;:&lt;param-value&gt; pair
   *  Named parameters are used to allow for adding additional options in the future.
   *
   * @param filePath the encrypted string in the format above defined by EncryptedSecret
   * @return path to the decrypted temporary file
   */
  public String decryptAsFile(String filePath) {
    Path decryptedFilePath = secretManager.decryptAsFile(filePath);

    if (decryptedFilePath != null) {
      SecretSession session = getSession();
      session.addFile(filePath, decryptedFilePath);
      return decryptedFilePath.toString();
    } else {
      return null;
    }
  }

  public String encrypt(String unencryptedString) {
    throw new UnsupportedOperationException();
  }
}
