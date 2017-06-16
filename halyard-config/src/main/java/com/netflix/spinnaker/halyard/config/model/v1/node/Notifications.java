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

package com.netflix.spinnaker.halyard.config.model.v1.node;

import com.netflix.spinnaker.halyard.config.model.v1.notifications.SlackNotification;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class Notifications extends Node implements Cloneable {
    SlackNotification slack = new SlackNotification();

    @Override
    public String getNodeName() {
        return "notification";
    }

    @Override
    public NodeIterator getChildren() {
        return NodeIteratorFactory.makeReflectiveIterator(this);
    }

    @Override
    public void accept(ConfigProblemSetBuilder psBuilder, Validator v) {
        v.validate(psBuilder, this);
    }

    public static Class<? extends Notification> translateNotificationType(String notificationName) {
        Optional<? extends Class<?>> res = Arrays.stream(Notification.class.getDeclaredFields())
                .filter(f -> f.getName().equals(notificationName))
                .map(Field::getType)
                .findFirst();

        if (res.isPresent()) {
            return (Class<? extends Notification>)res.get();
        } else {
            throw new IllegalArgumentException("No notification with name \"" + notificationName + "\" handled by halyard");
        }
    }
}
