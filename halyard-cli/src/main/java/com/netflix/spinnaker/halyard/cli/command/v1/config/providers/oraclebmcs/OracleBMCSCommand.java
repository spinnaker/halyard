/*
 * Copyright (c) 2017 Oracle America, Inc.
 *
 * The contents of this file are subject to the Apache License Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * If a copy of the Apache License Version 2.0 was not distributed with this file,
 * You can obtain one at https://www.apache.org/licenses/LICENSE-2.0.html
 */

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.oraclebmcs;

import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.AbstractNamedProviderCommand;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;

/**
 * Interact with the aws provider
 */
@Parameters(separators = "=")
public class OracleBMCSCommand extends AbstractNamedProviderCommand {
  protected String getProviderName() {
    return Provider.ProviderType.ORACLEBMCS.getName();
  }

  public OracleBMCSCommand() {
    super();
    registerSubcommand(new OracleBMCSAccountCommand());
  }
}
