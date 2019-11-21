/*
 * Copyright 2019 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.alicloud;

public class AliCloudCommandProperties {

  public static final String REGIONS_DESCRIPTION =
      "The AliCloud regions this Spinnaker account will manage.";

  public static final String ACCESS_KEY_ID_DESCRIPTION =
      "Your AliCloud Access Key ID. See https://www.alibabacloud.com/help/zh/doc-detail/53045.htm";

  static final String ACCESS_SECRET_KEY_DESCRIPTION =
      "Your AliCloud Secret Key. See https://www.alibabacloud.com/help/zh/doc-detail/53045.htm";

  static final String SOURCE_IMAGE_DESCRIPTION =
      "The source image. If both source image and source image family are set, source image will take precedence.";

  static final String INSTANCE_TYPE_DESCRIPTION =
      "Resource specifications for the instance. See https://www.alibabacloud.com/help/zh/doc-detail/25378.htm";

  static final String SSH_USER_NAME_DESCRIPTION =
      "User name for connecting to a Linux instance using SSH.";
}
