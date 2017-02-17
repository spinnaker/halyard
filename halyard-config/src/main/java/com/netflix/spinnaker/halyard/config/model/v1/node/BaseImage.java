/*
 * Copyright 2017 Google, Inc.
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
 *
 */

package com.netflix.spinnaker.halyard.config.model.v1.node;

import lombok.Data;
import lombok.Getter;

abstract public class BaseImage<I extends BaseImage.ImageSettings, V> extends Node {
  @Getter
  final private String nodeName = getBaseImage().getId();

  @Override
  public NodeIterator getChildren() {
    return NodeIteratorFactory.makeEmptyIterator();
  }

  abstract public I getBaseImage();

  abstract public V getVirtualizationSettings();

  @Data
  public static abstract class ImageSettings {
    String id;
    String shortDescription;
    String detailedDescription;
    String packageType;
    String templateFile;
  }
}
