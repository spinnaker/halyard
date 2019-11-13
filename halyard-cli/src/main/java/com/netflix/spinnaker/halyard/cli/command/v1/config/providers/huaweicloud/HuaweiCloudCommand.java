package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.huaweicloud;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.AbstractNamedProviderCommand;

@Parameters(separators = "=")
public class HuaweiCloudCommand extends AbstractNamedProviderCommand {
  protected String getProviderName() {
    return "huaweicloud";
  }

  public HuaweiCloudCommand() {
    super();
    registerSubcommand(new HuaweiCloudAccountCommand());
    registerSubcommand(new HuaweiCloudBakeryCommand());
  }
}
