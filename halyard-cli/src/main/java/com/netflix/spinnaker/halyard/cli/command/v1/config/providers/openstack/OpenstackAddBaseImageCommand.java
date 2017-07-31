/*
 * Copyright 2017 Target, Inc.
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
 */

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.openstack;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.bakery.AbstractAddBaseImageCommand;
import com.netflix.spinnaker.halyard.config.model.v1.node.BaseImage;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackBaseImage;

@Parameters(separators = "=")
public class OpenstackAddBaseImageCommand extends AbstractAddBaseImageCommand{
    protected String getProviderName() {return "openstack"; }

    @Parameter(
        names = "--region",
        description = OpenstackCommandProperties.REGION_DESCRIPTION
    )
    private String region;

    @Parameter(
        names = "--instance-type",
        description = OpenstackCommandProperties.INSTANCE_TYPE_DESCRIPTION
    )
    private String instanceType;

    @Parameter(
        names = "--source-image-id",
        description = OpenstackCommandProperties.SOURCE_IMAGE_ID_DESCRIPTION
    )
    private String sourceImageId;

    @Parameter(
        names = "--ssh-user-name",
        description = OpenstackCommandProperties.SSH_USER_NAME_DESCRIPTION
    )
    private String sshUserName;

    @Override
    protected BaseImage buildBaseImage(String baseImageId){
        OpenstackBaseImage baseImage = new OpenstackBaseImage();
        OpenstackBaseImage.OpenstackImageSettings imageSettings = new OpenstackBaseImage.OpenstackImageSettings();
        baseImage.setBaseImage(imageSettings);

        OpenstackBaseImage.OpenstackVirtualizationSettings virtualizationSettings = new OpenstackBaseImage.OpenstackVirtualizationSettings();
        virtualizationSettings.setSourceImageId(sourceImageId);
        virtualizationSettings.setRegion(region);
        virtualizationSettings.setInstanceType(instanceType);
        virtualizationSettings.setSshUserName(sshUserName);
        baseImage.setVirtualizationSettings(virtualizationSettings);

        return baseImage;
    }
}

