package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service.distributed;

public interface ExposedSidecarService extends SidecarService {
  String getClusterName();
}
