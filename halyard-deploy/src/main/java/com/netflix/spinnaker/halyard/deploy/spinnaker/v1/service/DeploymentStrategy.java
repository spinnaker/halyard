package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.service;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class DeploymentStrategy {
    DeploymentStrategy.Type type = Type.rollingUpdate;
    Map<String, String> strategySettings = new HashMap<>();
    public enum Type {
        rollingUpdate,
        recreate
    }
}
