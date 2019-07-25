package com.netflix.spinnaker.halyard.deploy.config.v1.secrets;

import com.netflix.spinnaker.halyard.core.secrets.v1.SecretSessionManager;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import com.netflix.spinnaker.kork.secrets.EncryptedSecret;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.RandomStringUtils;

public class BindingsSecretDecrypter {
  protected Profile profile;
  protected Path decryptedOutputDirectory;
  protected SecretSessionManager secretSessionManager;

  public BindingsSecretDecrypter(
      SecretSessionManager secretSessionManager, Profile profile, Path decryptedOutputDirectory) {
    super();
    this.secretSessionManager = secretSessionManager;
    this.profile = profile;
    this.decryptedOutputDirectory = decryptedOutputDirectory;
  }

  public String trackSecretFile(String value, String fieldName) {
    if (EncryptedSecret.isEncryptedSecret(value)) {
      String name = newRandomFilePath(fieldName);
      byte[] bytes = secretSessionManager.decryptAsBytes(value);
      profile.getDecryptedFiles().put(name, bytes);
      value = getCompleteFilePath(name);
    }
    return value;
  }

  protected String newRandomFilePath(String fieldName) {
    return fieldName + "-" + RandomStringUtils.randomAlphanumeric(5);
  }

  protected String getCompleteFilePath(String filename) {
    return Paths.get(decryptedOutputDirectory.toString(), filename).toString();
  }
}
