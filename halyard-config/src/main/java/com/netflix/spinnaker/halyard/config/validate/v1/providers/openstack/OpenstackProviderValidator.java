package com.netflix.spinnaker.halyard.config.validate.v1.providers.openstack;

import com.netflix.spinnaker.clouddriver.openstack.security.OpenstackNamedAccountCredentials;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackProvider;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenstackProviderValidator extends Validator<OpenstackProvider> {
    @Autowired
    private String halyardVersion;

    @Override
    public void validate(ConfigProblemSetBuilder p, OpenstackProvider n) {
        List<OpenstackNamedAccountCredentials> credentialsList = new ArrayList<>();

        OpenstackAccountValidator openstackAccountValidator = new OpenstackAccountValidator(credentialsList, halyardVersion);

        p.addProblem(Problem.Severity.WARNING, "Provider before get accounts: " + n.toString());

        DaemonTaskHandler.message("PROVIDER ACCOUNTS: " + n.toString());

        n.getAccounts().forEach(openstackAccount -> openstackAccountValidator.validate(p, openstackAccount));

//        new OpenstackBakeryDefaultsValidator(credentialsList, halyardVersion).validate(p, n.getBakeryDefaults());

    }

}
