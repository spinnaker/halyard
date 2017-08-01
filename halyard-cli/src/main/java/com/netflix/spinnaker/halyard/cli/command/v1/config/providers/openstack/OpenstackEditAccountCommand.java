package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.openstack;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.account.AbstractEditAccountCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.converter.PathExpandingConverter;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackAccount;

import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=")
public class OpenstackEditAccountCommand extends AbstractEditAccountCommand<OpenstackAccount> {
  protected String getProviderName() {
    return "openstack";
  }

  @Parameter(
          names = "--environment",
          description = OpenstackCommandProperties.ENVIRONMENT_DESCRIPTION
  )
  private String environment;

  @Parameter(
          names = "--account-type",
          description = OpenstackCommandProperties.ACCOUNT_TYPE_DESCRIPTION
  )
  private String accountType;

  @Parameter(
      names = "--auth-url",
      description = OpenstackCommandProperties.AUTH_URL_DESCRIPTION
  )
  private String authUrl;

  @Parameter(
      names = "--username",
      description = OpenstackCommandProperties.USERNAME_DESCRIPTION
  )
  private String username;

  @Parameter(
      names = "--password",
      description = OpenstackCommandProperties.PASSWORD_DESCRIPTION
  )
  private String password;

  @Parameter(
      names = "--project-name",
      description = OpenstackCommandProperties.PROJECT_NAME_DESCRIPTION
  )
  private String projectName;

  @Parameter(
      names = "--domain-name",
      description = OpenstackCommandProperties.DOMAIN_NAME_DESCRIPTION
  )
  private String domainName;

  @Parameter(
      names = "--regions",
      variableArity = true,
      description = OpenstackCommandProperties.REGIONS_DESCRIPTION
  )
  private List<String> regions = new ArrayList<>();

  @Parameter(
      names = "--add-region",
      description = "Add this region to the list of managed regions."
  )
  private String addRegion;

  @Parameter(
      names = "--remove-region",
      description = "Remove this region from the list of managed regions."
  )
  private String removeRegion;

  @Parameter(
      names = "--insecure",
      description = OpenstackCommandProperties.INSECURE_DESCRIPTION
  )
  private Boolean insecure;

  @Parameter(
      names = "--heat-template-location",
      converter = PathExpandingConverter.class,
      description = OpenstackCommandProperties.HEAT_TEMPLATE_LOCATION_DESCRIPTION
  )
  private String heatTemplateLocation;

  @Parameter(
      names = "--consul-config",
      description = OpenstackCommandProperties.CONSUL_CONFIG_DESCRIPTION
  )
  private String consulConfig;

  @Parameter(
      names = "--user-data-file",
      description = OpenstackCommandProperties.USER_DATA_FILE_DESCRIPTION
  )
  private String userDataFile;

  @Parameter(
      names = "--lbaas-poll-timeout",
      description = OpenstackCommandProperties.LBAAS_POLL_TIMEOUT_DESCRIPTION
  )
  private Integer lbaasPollTimeout;

  @Parameter(
      names = "--lbaas-poll-interval",
      description = OpenstackCommandProperties.LBAAS_POLL_INTERVAL_DESCRIPTION
  )
  private Integer lbaasPollInterval;

  @Override
  protected Account editAccount(OpenstackAccount account) {
    account.setUserDataFile(isSet(userDataFile) ? userDataFile : account.getUserDataFile());
    account.setHeatTemplateLocation(isSet(heatTemplateLocation) ? heatTemplateLocation : account.getHeatTemplateLocation());
    account.setConsulConfig(isSet(consulConfig) ? consulConfig : account.getConsulConfig());
    account.setAccountType(isSet(accountType) ? accountType : account.getAccountType());
    account.setEnvironment(isSet(environment) ? environment : account.getEnvironment());
    account.setAuthUrl(isSet(authUrl) ? authUrl : account.getAuthUrl());
    account.setUsername(isSet(username) ? username : account.getUsername());
    account.setPassword(isSet(password) ? password : account.getPassword());
    account.setProjectName(isSet(projectName) ? projectName : account.getProjectName());
    account.setDomainName(isSet(domainName) ? domainName : account.getDomainName());
    account.setInsecure(isSet(insecure) ? insecure : account.getInsecure());

    try {
      List<String> existingRegions = account.getRegions();
      List<String> newRegions = updateStringList(existingRegions, regions, addRegion, removeRegion);
      account.setRegions(newRegions);
    }catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Set either --regions or --[add/remove]-region");
    }

    OpenstackAccount.OpenstackLbaasOptions lbaas = account.getLbaas();
    if (isSet(lbaasPollInterval) || isSet(lbaasPollTimeout)) {
      if (isSet(lbaasPollInterval)) {
        lbaas.setPollInterval(lbaasPollInterval);
      }
      if (isSet(lbaasPollTimeout)) {
        lbaas.setPollTimeout(lbaasPollTimeout);
      }
      account.setLbaas(lbaas);
    }

    return account;
  }
}
