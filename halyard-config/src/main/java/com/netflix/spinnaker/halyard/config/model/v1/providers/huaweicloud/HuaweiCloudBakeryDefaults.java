package com.netflix.spinnaker.halyard.config.model.v1.providers.huaweicloud;

import com.netflix.spinnaker.halyard.config.model.v1.node.BakeryDefaults;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HuaweiCloudBakeryDefaults extends BakeryDefaults<HuaweiCloudBaseImage> {}
