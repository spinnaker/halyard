package com.netflix.spinnaker.halyard.cli.utils.k8s;

import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;

import java.util.List;

public class testK8s {
    public static void main(String[] args) {
        List<KubernetesAccount> kubernetesAccounts = K8s.ListCluster("/Users/ruizou/.kube/config");
        for (KubernetesAccount k:
             kubernetesAccounts) {
            System.out.println(k.getName());
        }
    }
}

