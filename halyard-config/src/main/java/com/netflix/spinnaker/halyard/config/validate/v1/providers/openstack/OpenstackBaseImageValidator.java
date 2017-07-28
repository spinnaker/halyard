package com.netflix.spinnaker.halyard.config.validate.v1.providers.openstack;

import com.netflix.spinnaker.clouddriver.openstack.security.OpenstackCredentials;
import com.netflix.spinnaker.clouddriver.openstack.security.OpenstackNamedAccountCredentials;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackBaseImage;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class OpenstackBaseImageValidator extends Validator<OpenstackBaseImage> {
    final private List<OpenstackNamedAccountCredentials> credentialsList;

    final private String halyardVersion;

    public void validate(ConfigProblemSetBuilder p, OpenstackBaseImage n) {
        DaemonTaskHandler.message("Validating " + n.getNodeName() + " with " + OpenstackBaseImageValidator.class.getSimpleName());

        String region = n.getVirtualizationSettings().getRegion();
        String instanceType = n.getVirtualizationSettings().getInstanceType();
        String sourceImageId = n.getVirtualizationSettings().getSourceImageId();
        String sshUserName = n.getVirtualizationSettings().getSshUserName();

        if (StringUtils.isEmpty(region)) {
            p.addProblem(Problem.Severity.ERROR, "No region supplied for openstack base image.");
        }

        if (StringUtils.isEmpty(instanceType)) {
            p.addProblem(Problem.Severity.ERROR, "No instance type supplied for openstack base image.");
        }

        if (StringUtils.isEmpty(sourceImageId)) {
            p.addProblem(Problem.Severity.ERROR, "No source image id supplied for openstack base image.");
        }

        if (StringUtils.isEmpty(sshUserName)) {
            p.addProblem(Problem.Severity.ERROR, "No ssh username supplied for openstack base image.");
        }

        // TODO(shazy792) Add check to see if image actually exists on openstack instance(thum
    }
}
