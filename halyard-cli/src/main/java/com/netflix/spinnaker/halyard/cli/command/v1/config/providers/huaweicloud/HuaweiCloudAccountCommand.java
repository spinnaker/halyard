package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.huaweicloud;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.AbstractAccountCommand;

@Parameters(separators = "=")
public class HuaweiCloudAccountCommand extends AbstractAccountCommand {
  protected String getProviderName() {
    return "huaweicloud";
  }

  public HuaweiCloudAccountCommand() {
    super();
    registerSubcommand(new HuaweiCloudAddAccountCommand());
    registerSubcommand(new HuaweiCloudEditAccountCommand());
  }
}
