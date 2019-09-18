package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.kubernetes;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.account.AbstractHasAccountCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.converter.LocalFileConverter;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.utils.k8s.ClusterRegistry;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import java.util.*;
import lombok.AccessLevel;
import lombok.Getter;

@Parameters(separators = "=")
public class KubernetesSyncAccountCommand extends AbstractHasAccountCommand {

  @Getter(AccessLevel.PROTECTED)
  private Map<String, NestableCommand> subcommands = new HashMap<>();

  @Getter(AccessLevel.PUBLIC)
  private String commandName = "sync";

  @Parameter(
      names = "--kubeconfig-file",
      converter = LocalFileConverter.class,
      description = KubernetesCommandProperties.KUBECONFIG_DESCRIPTION,
      required = true)
  private String kubeconfigFile;

  @Parameter(
      names = "--sync-policy",
      description = KubernetesCommandProperties.CLUSTER_REGISTRY,
      required = true)
  private String syncPolicy;

  public String getShortDescription() {
    return "sync account from the " + getProviderName() + " provider cluster registry.";
  }

  @Override
  protected void executeThis() {
    String providerName = getProviderName();
    String currentDeployment = getCurrentDeployment();
    if (providerName.equals(providerName)) {
      Provider provider = getProvider();
      List<Account> existKubernetesAccountList = provider.getAccounts();
      Set<String> existKubernetesAccountNameSet = new HashSet<>();
      for (Account existKubernetesAccount : existKubernetesAccountList) {
        existKubernetesAccountNameSet.add(existKubernetesAccount.getName());
      }
      List<KubernetesAccount> kubernetesAccounts = ClusterRegistry.ListCluster(kubeconfigFile);
      for (KubernetesAccount kubernetesAccount : kubernetesAccounts) {
        if (existKubernetesAccountNameSet.contains(kubernetesAccount.getName())) {
          if (syncPolicy.equals("skip")) {
            System.out.printf("account %s exist should not add\n", kubernetesAccount.getName());
            continue;
          } else if (syncPolicy.equals("overwrite")) {
            new OperationHandler<Void>()
                .setFailureMesssage(
                    "Failed to edit account "
                        + kubernetesAccount.getName()
                        + " for provider "
                        + providerName
                        + ".")
                .setSuccessMessage(
                    "Successfully edit account "
                        + kubernetesAccount.getName()
                        + " for provider "
                        + providerName
                        + ".")
                .setOperation(
                    Daemon.setAccount(
                        currentDeployment,
                        providerName,
                        kubernetesAccount.getName(),
                        !noValidate,
                        kubernetesAccount))
                .get();
            continue;
          } else {
            throw new IllegalArgumentException("not supported sync-policy: " + syncPolicy);
          }
        }
        new OperationHandler<Void>()
            .setFailureMesssage(
                "Failed to add account "
                    + kubernetesAccount.getName()
                    + " for provider "
                    + providerName
                    + ".")
            .setSuccessMessage(
                "Successfully added account "
                    + kubernetesAccount.getName()
                    + " for provider "
                    + providerName
                    + ".")
            .setOperation(
                Daemon.addAccount(currentDeployment, providerName, !noValidate, kubernetesAccount))
            .get();
      }
    }
  }

  private Provider getProvider() {
    String currentDeployment = getCurrentDeployment();
    String providerName = getProviderName();
    return new OperationHandler<Provider>()
        .setFailureMesssage("Failed to get provider " + providerName + ".")
        .setOperation(Daemon.getProvider(currentDeployment, providerName, !noValidate))
        .get();
  }

  @Override
  protected String getProviderName() {
    return "kubernetes";
  }
}
