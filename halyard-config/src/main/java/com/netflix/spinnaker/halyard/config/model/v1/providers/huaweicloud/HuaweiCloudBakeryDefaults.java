package com.netflix.spinnaker.halyard.config.model.v1.providers.huaweicloud;

import com.netflix.spinnaker.halyard.config.model.v1.node.BakeryDefaults;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HuaweiCloudBakeryDefaults extends BakeryDefaults<HuaweiCloudBaseImage> {
  private String authUrl;
  private String username;
  private String password;
  private String projectName;
  private String domainName;
  private Boolean insecure;
  private String vpcId;
  private String subnetId;
  private String securityGroup;
  private Integer eipBandwidthSize;
}
