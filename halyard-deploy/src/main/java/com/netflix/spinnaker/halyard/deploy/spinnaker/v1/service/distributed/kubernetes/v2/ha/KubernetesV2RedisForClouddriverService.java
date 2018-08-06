package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.ha;

import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.RedisSentinelForClouddriverService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.RedisService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.ServiceSettings;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.SpinnakerMonitoringDaemonService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.SidecarService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2MonitoringDaemonService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2Service;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed.kubernetes.v2.KubernetesV2ServiceDelegate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class KubernetesV2RedisForClouddriverService extends RedisService implements
    KubernetesV2Service<Jedis> {
  @Autowired
  @Getter
  KubernetesV2RedisSentinelForClouddriverService redisSetinelForClouddriverService;

  @Override
  public Type getType() {
    return Type.REDIS_FOR_CLOUDDRIVER;
  }

  @Delegate
  @Autowired
  KubernetesV2ServiceDelegate serviceDelegate;

  public String getArtifactId(String deploymentName) {
    return "gcr.io/kubernetes-spinnaker/redis-cluster:v2";
  }

  @Override
  public ServiceSettings defaultServiceSettings() {
    return new Settings();
  }

  public List<SidecarService> getSidecars(SpinnakerRuntimeSettings runtimeSettings) {
    List<SidecarService> result = KubernetesV2Service.super.getSidecars(runtimeSettings);
    result.add(redisSetinelForClouddriverService);
    return result;
  }

}
