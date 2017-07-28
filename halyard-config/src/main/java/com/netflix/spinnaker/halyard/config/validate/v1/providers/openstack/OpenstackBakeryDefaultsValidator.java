package com.netflix.spinnaker.halyard.config.validate.v1.providers.openstack;

import com.netflix.spinnaker.clouddriver.google.security.GoogleNamedAccountCredentials;
import com.netflix.spinnaker.clouddriver.openstack.security.OpenstackCredentials;
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

    final private List<OpenstackCredentials> credentialsList;

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

//        if (StringUtils.
// isEmpty(zone)) {
//            p.addProblem(Problem.Severity.ERROR, "No zone supplied for google bakery defaults.");
//        } else {
//            int i = 0;
//            boolean foundZone = false;
//
//            while (!foundZone && i < credentialsList.size()) {
//                GoogleNamedAccountCredentials credentials = credentialsList.get(i);
//
//                try {
//                    credentials.getCompute().zones().get(credentials.getProject(), zone).execute();
//                    foundZone = true;
//                } catch (Exception e) {
//                }
//
//                i++;
//            }
//
//            if (!foundZone) {
//                p.addProblem(Problem.Severity.ERROR, "Zone " + zone + " not found via any configured google account.");
//            }
//        }
//
//        if (StringUtils.isEmpty(network)) {
//            p.addProblem(Problem.Severity.ERROR, "No network supplied for google bakery defaults.");
//        } else {
//            int j = 0;
//            boolean foundNetwork = false;
//
//            while (!foundNetwork && j < credentialsList.size()) {
//                GoogleNamedAccountCredentials credentials = credentialsList.get(j);
//
//                try {
//                    String project = !StringUtils.isEmpty(networkProjectId) ? networkProjectId : credentials.getProject();
//                    credentials.getCompute().networks().get(project, network).execute();
//                    foundNetwork = true;
//                } catch (Exception e) {
//                }
//
//                j++;
//            }
//
//            if (!foundNetwork) {
//                p.addProblem(Problem.Severity.ERROR, "Network " + network + " not found via any configured google account.");
//            }
//        }

        OpenstackBaseImageValidator openstackBaseImageValidator = new OpenstackBaseImageValidator(credentialsList, halyardVersion);

        baseImages.forEach(openstackBaseImage ->  openstackBaseImageValidator.validate(p, openstackBaseImage));
    }

}
