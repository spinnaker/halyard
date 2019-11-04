package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.huaweicloud;

public class HuaweiCloudCommandProperties {
  static final String ACCOUNT_TYPE_DESCRIPTION = "The type of account.";

  static final String AUTH_URL_DESCRIPTION = "The auth url of cloud.";

  static final String USERNAME_DESCRIPTION = "The username used to access cloud.";

  static final String PASSWORD_DESCRIPTION = "The password used to access cloud.";

  static final String PROJECT_NAME_DESCRIPTION = "The name of the project within the cloud.";

  static final String DOMAIN_NAME_DESCRIPTION = "The domain name of the cloud.";

  static final String REGIONS_DESCRIPTION = "The region(s) of the cloud.";

  static final String INSECURE_DESCRIPTION =
      "Disable certificate validation on SSL connections. Needed if certificates are self signed. Default false.";
}
