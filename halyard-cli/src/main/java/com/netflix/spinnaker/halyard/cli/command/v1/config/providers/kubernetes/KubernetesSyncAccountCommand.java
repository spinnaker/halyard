package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.kubernetes;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.netflix.spinnaker.halyard.cli.command.v1.NestableCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.config.providers.account.AbstractHasAccountCommand;
import com.netflix.spinnaker.halyard.cli.command.v1.converter.LocalFileConverter;
import com.netflix.spinnaker.halyard.cli.services.v1.Daemon;
import com.netflix.spinnaker.halyard.cli.services.v1.OperationHandler;
import com.netflix.spinnaker.halyard.cli.utils.k8s.K8s;
import com.netflix.spinnaker.halyard.config.model.v1.node.Account;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;

@Parameters(separators = "=")
public  class KubernetesSyncAccountCommand extends AbstractHasAccountCommand {

    @Getter(AccessLevel.PROTECTED)
    private Map<String, NestableCommand> subcommands = new HashMap<>();

    @Getter(AccessLevel.PUBLIC)
    private String commandName = "sync";

    @Parameter(
            names = "--kubeconfig-file",
            converter = LocalFileConverter.class,
            description = KubernetesCommandProperties.KUBECONFIG_DESCRIPTION)
    private String kubeconfigFile;

    protected Account buildAccount(String accountName) {
        KubernetesAccount account = (KubernetesAccount) new KubernetesAccount().setName(accountName);
        account.setKubeconfigFile(kubeconfigFile);
        return account;
    }

    protected Account emptyAccount() {
        return new KubernetesAccount();
    }

    public String getShortDescription() {
        return "sync account from the " + getProviderName() + " provider cluster registry.";
    }

    @Override
    protected List<String> options(String fieldName) {
        String currentDeployment = getCurrentDeployment();
        String accountName = getAccountName("hal-default-account");
        Account account = buildAccount(accountName);
        String providerName = getProviderName();

        return new OperationHandler<List<String>>()
                .setFailureMesssage("Failed to get options for field " + fieldName)
                .setOperation(
                        Daemon.getNewAccountOptions(currentDeployment, providerName, fieldName, account))
                .get();
    }

    @Override
    protected void executeThis() {
        String accountName = getAccountName();
        Account account = buildAccount(accountName);
        String providerName = getProviderName();
        String currentDeployment = getCurrentDeployment();
        if (providerName.equals("kubernetes")) {
            Provider provider = getProvider();
            List<Account> accounts = provider.getAccounts();
            Set<String> accountNameSet = new HashSet<>();
            for (Account a: accounts){
                accountNameSet.add(a.getName());
            }
            KubernetesAccount k8sAccount = (KubernetesAccount) account;
            List<KubernetesAccount> kubernetesAccounts = K8s.ListCluster(k8sAccount.getKubeconfigFile());
            for(KubernetesAccount K8sAC: kubernetesAccounts){
                if (accounts.contains(K8sAC.getName())){
                    System.out.printf("account %s exist should not add\n",K8sAC.getName());
                    continue;
                }
                new OperationHandler<Void>()
                        .setFailureMesssage(
                                "Failed to add account " + K8sAC.getName() + " for provider " + providerName + ".")
                        .setSuccessMessage(
                                "Successfully added account " + K8sAC.getName() + " for provider " + providerName + ".")
                        .setOperation(Daemon.addAccount(currentDeployment, providerName, !noValidate, K8sAC))
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
