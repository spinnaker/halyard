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
 */

package com.netflix.spinnaker.halyard.config.model.v1

import junit.framework.Test
import spock.lang.Specification

import com.netflix.spinnaker.halyard.core.registry.v1.ProfileRegistry
import com.netflix.spinnaker.halyard.core.registry.v1.ProfileReader
import com.netflix.spinnaker.halyard.core.registry.v1.LocalDiskProfileReader

import org.springframework.beans.factory.annotation.Autowired

class LocalDiskProfileReaderSpec extends Specification {

    @Autowired
    ProfileRegistry profileRegistry;

    void "Attempt to pick LocalDiskProfileReader from version"() {
        setup:
            String version = "local:test-version"
        
        when:
            def profileReader = profileRegistry.pickProfileReader("local:test-version")
        then:
            profileReader instanceof LocalDiskProfileReader
    }

}