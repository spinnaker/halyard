package com.netflix.spinnaker.halyard.cli.utils.k8s;

import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.providers.kubernetes.KubernetesAccount;
import io.alauda.devops.java.clusterregistry.client.apis.ClusterregistryK8sIoV1alpha1Api;
import io.alauda.devops.java.clusterregistry.client.models.V1alpha1Cluster;
import io.alauda.devops.java.clusterregistry.client.models.V1alpha1ClusterList;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1SecretList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterRegistry {

  public static ClusterregistryK8sIoV1alpha1Api GetApi(String kubeconfig) throws IOException {
    ApiClient client =
        ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeconfig))).build();
    Configuration.setDefaultApiClient(client);
    ClusterregistryK8sIoV1alpha1Api api = new ClusterregistryK8sIoV1alpha1Api(client);
    return api;
  }

  public static List<KubernetesAccount> ListCluster(String kubeconfig) {
    List<KubernetesAccount> clusters = new ArrayList<>();
    try {
      V1alpha1ClusterList v1alpha1ClusterList =
          GetApi(kubeconfig).listClusterForAllNamespaces("", "", false, "", 0, "", "", 0, false);
      for (V1alpha1Cluster c : v1alpha1ClusterList.getItems()) {
        if (c.getSpec().getAuthInfo().getUser().getKind().equals("Secret")) {
          clusters.add(ConvertK8sAccount(c));
        } else {
          log.warn("cluster userInfo kind only support Secret.");
        }
      }
    } catch (ApiException | IOException e) {
      e.printStackTrace();
    }
    return clusters;
  }

  public static KubernetesAccount ConvertK8sAccount(V1alpha1Cluster cluster)
      throws ApiException, IOException {
    KubernetesAccount account = new KubernetesAccount();
    account.setName(cluster.getMetadata().getName());
    account.setProviderVersion(Provider.ProviderVersion.V2);
    CoreV1Api api = new CoreV1Api();
    V1SecretList v1SecretList =
        api.listNamespacedSecret(
            cluster.getSpec().getAuthInfo().getUser().getNamespace(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    for (V1Secret sec : v1SecretList.getItems()) {
      if (sec.getMetadata().getName().equals(cluster.getSpec().getAuthInfo().getUser().getName())) {
        for (String key : sec.getData().keySet()) {
          String str =
              System.getProperties().getProperty("user.home")
                  + "/.hal/tmp-k8s-conf-"
                  + account.getName();
          Path path = Paths.get(str);
          if (!Files.exists(path)) {
            try {
              Files.createFile(path);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          Files.write(path, sec.getData().get(key));
          account.setKubeconfigFile(str);
        }
      }
    }

    return account;
  }
}
