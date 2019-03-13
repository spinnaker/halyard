package com.netflix.spinnaker.halyard.config.validate.v1.providers.dcos;

import com.google.common.base.Strings;
import com.netflix.spinnaker.halyard.core.secrets.v1.SecretSessionManager;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.model.v1.providers.dcos.DCOSCluster;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity.ERROR;
import static com.netflix.spinnaker.halyard.core.problem.v1.Problem.Severity.WARNING;

/**
 * TODO: use clouddriver components for full validation
 */
@Component
public class DCOSClusterValidator extends Validator<DCOSCluster> {
  @Autowired
  private SecretSessionManager secretSessionManager;

  @Override
  public void validate(final ConfigProblemSetBuilder problems, final DCOSCluster cluster) {

    if (cluster.getInsecureSkipTlsVerify() != null && cluster.getInsecureSkipTlsVerify()) {
      problems.addProblem(WARNING, "You've chosen to not validate SSL connections. This setup is not recommended in production deployments.");
    }

    if (!Strings.isNullOrEmpty(cluster.getCaCertFile())) {
      String resolvedServiceKey = validatingFileDecrypt(problems, cluster.getCaCertFile());
      if (Strings.isNullOrEmpty(resolvedServiceKey)) {
        problems.addProblem(ERROR, "The supplied CA certificate file does not exist or is empty.")
                .setRemediation("Supply a valid CA certificate file.");
      }
    }

    if (Strings.isNullOrEmpty(cluster.getDcosUrl())) {
      problems.addProblem(ERROR, "Cluster must have a URL");
    }

    final DCOSCluster.LoadBalancer loadBalancer = cluster.getLoadBalancer();
    if (loadBalancer == null || Strings.isNullOrEmpty(loadBalancer.getImage())) {
      problems.addProblem(WARNING,
          "Load balancer pipeline stages will not be able to be used unless a marathon-lb image is specified");
    }
  }
}
