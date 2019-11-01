package com.netflix.spinnaker.halyard.config.model.v1.providers.huaweicloud;

import com.netflix.spinnaker.halyard.config.model.v1.node.BaseImage;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class HuaweiCloudBaseImage
    extends BaseImage<
        HuaweiCloudBaseImage.HuaweiCloudImageSettings,
        List<HuaweiCloudBaseImage.HuaweiCloudVirtualizationSettings>> {

  private HuaweiCloudImageSettings baseImage;
  private List<HuaweiCloudVirtualizationSettings> virtualizationSettings;

  @EqualsAndHashCode(callSuper = true)
  @Data
  @ToString(callSuper = true)
  public static class HuaweiCloudImageSettings extends BaseImage.ImageSettings {}

  @Data
  @ToString
  public static class HuaweiCloudVirtualizationSettings {
    String region;
    String instanceType;
    String sourceImageId;
    String sshUserName;
  }
}
