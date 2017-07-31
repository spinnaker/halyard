package com.netflix.spinnaker.halyard.config.validate.v1.providers.openstack;

import com.netflix.spinnaker.clouddriver.google.security.GoogleNamedAccountCredentials;
import com.netflix.spinnaker.clouddriver.openstack.security.OpenstackCredentials;
import com.netflix.spinnaker.clouddriver.openstack.security.OpenstackNamedAccountCredentials;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackBakeryDefaults;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackBaseImage;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.config.validate.v1.providers.google.GoogleBaseImageValidator;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class OpenstackBakeryDefaultsValidator extends Validator<OpenstackBakeryDefaults> {

    final private List<OpenstackNamedAccountCredentials> credentialsList;

    final private String halyardVersion;

    @Override
    public void validate(ConfigProblemSetBuilder p, OpenstackBakeryDefaults n) {
        DaemonTaskHandler.message("Validating " + n.getNodeName() + " with " + OpenstackBakeryDefaultsValidator.class.getSimpleName());

        String authUrl = n.getAuthUrl();
        String networkId = n.getNetworkId();
        String floatingIpPool = n.getFloatingIpPool();
        String securityGroups = n.getSecurityGroups();
        String projectName = n.getProjectName();
        String username = n.getUsername();
        String password = n.getPassword();
        boolean insecure = n.isInsecure();
        List<OpenstackBaseImage> baseImages = n.getBaseImages();

        if (StringUtils.isEmpty(authUrl) &&
                StringUtils.isEmpty(networkId) &&
                StringUtils.isEmpty(floatingIpPool) &&
                StringUtils.isEmpty(securityGroups) &&
                StringUtils.isEmpty(projectName) &&
                StringUtils.isEmpty(username) &&
                StringUtils.isEmpty(password) &&
                StringUtils.isEmpty(networkId) &&
                CollectionUtils.isEmpty(baseImages)) {
            return;
        }


        // Check if all inputs are valid
        if (StringUtils.isEmpty(authUrl)) {
            p.addProblem(Problem.Severity.ERROR, "No auth url supplied for openstack bakery defaults.");
        }

        if (StringUtils.isEmpty(networkId)) {
            p.addProblem(Problem.Severity.ERROR, "No network id supplied for openstack bakery defaults.");
        }

        if (StringUtils.isEmpty(floatingIpPool)) {
            p.addProblem(Problem.Severity.ERROR, "No floating ip pool supplied for openstack bakery defaults.");
        }

        if (StringUtils.isEmpty(securityGroups)) {
            p.addProblem(Problem.Severity.ERROR, "No security groups supplied for openstack bakery defaults.");
        }

        if (StringUtils.isEmpty(projectName)) {
            p.addProblem(Problem.Severity.ERROR, "No project name supplied for openstack bakery defaults.");
        }

        if (StringUtils.isEmpty(username)) {
            p.addProblem(Problem.Severity.ERROR, "No username supplied for openstack bakery defaults.");
        }

        if (StringUtils.isEmpty(password)) {
            p.addProblem(Problem.Severity.ERROR, "No password supplied for openstack bakery defaults.");
        }

        if (insecure) {
            p.addProblem(Problem.Severity.WARNING, "You've chosen to not validate SSL connections. This setup is not recommended in production deployments.");
        }


        OpenstackBaseImageValidator openstackBaseImageValidator = new OpenstackBaseImageValidator(credentialsList, halyardVersion);

        baseImages.forEach(openstackBaseImage ->  openstackBaseImageValidator.validate(p, openstackBaseImage));
    }

}
