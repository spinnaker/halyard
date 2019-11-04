package com.netflix.spinnaker.halyard.config.model.v1.providers.huaweicloud;

import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.node.Secret;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HuaweiCloudAccount extends Account {
  private String accountName;
  private String accountType;
  private String authUrl;
  private String username;
  @Secret private String password;
  private String projectName;
  private String domainName;
  private Boolean insecure = false;
  private List<String> regions;
}
