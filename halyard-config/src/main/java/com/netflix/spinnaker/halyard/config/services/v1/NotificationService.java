/*
 * Copyright 2017 Johan Kasselman.
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

package com.netflix.spinnaker.halyard.config.services.v1;

import com.netflix.spinnaker.halyard.config.model.v1.node.*;
import com.netflix.spinnaker.halyard.config.model.v1.node.Notification;
import com.netflix.spinnaker.halyard.config.model.v1.node.Notifications;
import com.netflix.spinnaker.halyard.config.model.v1.notifications.SlackNotification;
import com.netflix.spinnaker.halyard.config.model.v1.notifications.HipchatNotification;
import com.netflix.spinnaker.halyard.config.model.v1.notifications.SmsNotification;
import com.netflix.spinnaker.halyard.config.model.v1.notifications.EmailNotification;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.config.error.v1.ConfigNotFoundException;
import com.netflix.spinnaker.halyard.config.error.v1.IllegalConfigException;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemBuilder;

import com.netflix.spinnaker.halyard.core.problem.v1.ProblemSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.List;


@Component
public class NotificationService {

  @Autowired
  private LookupService lookupService;

  @Autowired
  private DeploymentService deploymentService;

  @Autowired
  private ValidateService validateService;

  public Notification getNotification(String deploymentName, String notificationName) {
    NodeFilter filter = new NodeFilter().setDeployment(deploymentName).setNotification(notificationName);
    List<Notification> matching = lookupService.getMatchingNodesOfType(filter, Notification.class);

    switch (matching.size()) {
      case 0:
        throw new ConfigNotFoundException(new ConfigProblemBuilder(Problem.Severity.FATAL,
            "Notification with name \"" + notificationName + "\" not found/supported yet!").setRemediation("Please see documentation for supported notifications!").build());
      case 1:
        return matching.get(0);
      default:
      throw new IllegalConfigException(new ConfigProblemBuilder(Problem.Severity.FATAL,
          "More than one notification with name \"" + notificationName + "\" found").setRemediation("This is a bug!").build());
    }
  }

//  TODO Get this to work.. :/
//  public Notification getAllNotifications(String deploymentName) {
//    NodeFilter filter = new NodeFilter().setDeployment(deploymentName);
//    List<Notification> matching = lookupService.getMatchingNodesOfType(filter, Notification.class);
//
//    switch (matching.size()) {
//      case 0:
//        throw new ConfigNotFoundException(new ConfigProblemBuilder(Problem.Severity.FATAL,
//            "Notification with name \"" + notificationName + "\" not found/supported yet!").setRemediation("").build());
//      case 1:
//        return matching.get(0);
//      default:
//        throw new IllegalConfigException(new ConfigProblemBuilder(Problem.Severity.FATAL,
//            "More than one notification with name \"" + notificationName + "\" found").setRemediation("This is a bug!").build());
//    }
//  }

  public void setNotification(String deploymentName, Notification notification) {
    DeploymentConfiguration deploymentConfiguration = deploymentService.getDeploymentConfiguration(deploymentName);
    Notifications notifications = deploymentConfiguration.getNotifications();
    switch (notification.notificationType()) {
      case EMAIL:
        notifications.setEmail((EmailNotification) notification);
        break;
//      case SMS:
//        notifications.setSms((SmsNotification) notification);
//        break;
//      case HIPCHAT:
//        notifications.setHipchat((HipchatNotification) notification);
//        break;
      case SLACK:
        notifications.setSlack((SlackNotification) notification);
        break;
      default:
        throw new IllegalArgumentException("Unknonwn notification type " + notification.notificationType());
    }
  }

  public void setEnabled(String deploymentName, String notificationName, boolean enabled) {
    Notification notification = getNotification(deploymentName, notificationName);
    notification.setEnabled(enabled);
  }

  public ProblemSet validateNotification(String deploymentName, String notificationName) {
    NodeFilter filter = new NodeFilter()
        .setDeployment(deploymentName)
        .setNotification(notificationName);

    return validateService.validateMatchingFilter(filter);
  }

  public ProblemSet validateAllNotifications(String deploymentName) {
    NodeFilter filter = new NodeFilter()
        .setDeployment(deploymentName);

    return validateService.validateMatchingFilter(filter);
  }
}
