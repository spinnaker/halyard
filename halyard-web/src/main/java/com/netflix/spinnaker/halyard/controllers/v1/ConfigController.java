/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.halyard.controllers.v1;

import com.netflix.spinnaker.halyard.config.config.v1.HalconfigParser;
import com.netflix.spinnaker.halyard.config.model.v1.node.Halconfig;
import com.netflix.spinnaker.halyard.config.services.v1.ConfigService;
import com.netflix.spinnaker.halyard.core.DaemonResponse;
import com.netflix.spinnaker.halyard.core.DaemonResponse.StaticRequestBuilder;
import com.netflix.spinnaker.halyard.core.StringBodyRequest;
import com.netflix.spinnaker.halyard.core.problem.v1.ProblemSet;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTask;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTaskHandler;
import com.netflix.spinnaker.halyard.proto.ConfigGrpc;
import com.netflix.spinnaker.halyard.proto.ConfigOuterClass;
import com.netflix.spinnaker.halyard.proto.ConfigOuterClass.ValidateConfigResponse;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reports the entire contents of ~/.hal/config
 */
@GRpcService
@RestController
@RequestMapping("/v1/config")
public class ConfigController extends ConfigGrpc.ConfigImplBase {
  @Autowired
  ConfigService configService;

  @Autowired
  HalconfigParser halconfigParser;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  DaemonTask<Halconfig, Halconfig> config() {
    StaticRequestBuilder<Halconfig> builder = new StaticRequestBuilder<>(() -> configService.getConfig());
    return DaemonTaskHandler.submitTask(builder::build, "Get halconfig");
  }

  @Override
  public void validateConfig(ConfigOuterClass.ValidateConfigRequest request, StreamObserver<ValidateConfigResponse> responseObserver) {
    DaemonTask<Halconfig, Halconfig> config = config();

    final ValidateConfigResponse.Builder response = ValidateConfigResponse.newBuilder().setMessage(config.getUuid());
    responseObserver.onNext(response.build());
    responseObserver.onCompleted();
  }

  @RequestMapping(value = "/currentDeployment", method = RequestMethod.GET)
  DaemonTask<Halconfig, String> currentDeployment() {
    StaticRequestBuilder<String> builder = new StaticRequestBuilder<>(() -> configService.getCurrentDeployment());
    return DaemonTaskHandler.submitTask(builder::build, "Get current deployment");
  }

  @RequestMapping(value = "/currentDeployment", method = RequestMethod.PUT)
  DaemonTask<Halconfig, Void> setDeployment(@RequestBody StringBodyRequest name) {
    DaemonResponse.UpdateRequestBuilder builder = new DaemonResponse.UpdateRequestBuilder();
    builder.setUpdate(() -> configService.setCurrentDeployment(name.getValue()));
    builder.setRevert(() -> halconfigParser.undoChanges());
    builder.setSave(() -> halconfigParser.saveConfig());
    builder.setValidate(ProblemSet::new);
    return DaemonTaskHandler.submitTask(builder::build, "Set current deployment");
  }
}
