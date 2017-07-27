package com.netflix.spinnaker.halyard.config.model.v1.providers.openstack;

import com.netflix.spinnaker.halyard.config.model.v1.node.HasImageProvider;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.model.v1.node.Validator;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;

public class OpenstackProvider extends HasImageProvider<OpenstackAccount, OpenstackBakeryDefaults> implements Cloneable {
  //TODO(emjburns): add support for rosco options

  @Override
  public ProviderType providerType() {
    return ProviderType.OPENSTACK;
  }

  @Override
  public void accept(ConfigProblemSetBuilder psBuilder, Validator v) {
    v.validate(psBuilder, this);
  }

  @Override
  public  OpenstackBakeryDefaults emptyBakeryDefaults() {
    OpenstackBakeryDefaults result = new OpenstackBakeryDefaults();
    // Set defaults over here
//    result.setNetwork("default");
//    result.setZone("us-central1-f");
//    result.setUseInternalIp(false);
//    result.setTemplateFile("gce.json");
    return result;
  }
}
