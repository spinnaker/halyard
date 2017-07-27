package com.netflix.spinnaker.halyard.config.validate.v1.providers.openstack;

import com.netflix.spinnaker.clouddriver.openstack.security.OpenstackCredentials;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackBaseImage;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class OpenstackBaseImageValidator extends Validator<OpenstackBaseImage> {
    final private List<OpenstackCredentials> credentialsList;

    final private String halyardVersion;

    public void validate(ConfigProblemSetBuilder p, OpenstackBaseImage n) {
        // Check to see if image exists
    }
}
