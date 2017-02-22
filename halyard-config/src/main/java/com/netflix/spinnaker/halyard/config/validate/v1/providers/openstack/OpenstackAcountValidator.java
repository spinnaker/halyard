package com.netflix.spinnaker.halyard.config.validate.v1.providers.openstack;

import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.openstack.OpenstackAccount;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import org.springframework.stereotype.Component;

@Component
public class OpenstackAcountValidator extends Validator<OpenstackAccount> {
    @Override
    public void validate(ConfigProblemSetBuilder psBuilder, OpenstackAccount account){
        psBuilder.addProblem(Problem.Severity.WARNING, "No validators exist for this provider.");
    }
}
