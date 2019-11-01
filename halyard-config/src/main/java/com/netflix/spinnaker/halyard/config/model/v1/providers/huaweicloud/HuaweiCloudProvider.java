package com.netflix.spinnaker.halyard.config.model.v1.providers.huaweicloud;

import com.netflix.spinnaker.halyard.config.model.v1.node.HasImageProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HuaweiCloudProvider
    extends HasImageProvider<HuaweiCloudAccount, HuaweiCloudBakeryDefaults> implements Cloneable {
  @Override
  public ProviderType providerType() {
    return ProviderType.HUAWEICLOUD;
  }

  @Override
  public HuaweiCloudBakeryDefaults emptyBakeryDefaults() {
    return new HuaweiCloudBakeryDefaults();
  }
}
