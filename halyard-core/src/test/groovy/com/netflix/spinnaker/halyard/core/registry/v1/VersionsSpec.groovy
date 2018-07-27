/*
 * Copyright 2018 Google, Inc.
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

package com.netflix.spinnaker.halyard.core.registry.v1

import spock.lang.Specification

class VersionsSpec extends Specification {
    def "lessThan respects semantic versioning"() {
        expect:
        Versions.lessThan(a, b) == result

        where:
        a        | b                        | result
        "1.0.0"  | "1.0.0"                  | false
        "1.0.0"  | "1.0.1"                  | true
        "1.0.1"  | "1.0.0"                  | false
        "2.0.0"  | "10.0.0"                 | true
        "1.0.0"  | "branch:upstream/master" | true
    }

    def "lessThan looks up to the first hyphen"() {
        expect:
        Versions.lessThan(a, b) == result

        where:
        a              | b                        | result
        "1.0.0-99999"  | "1.0.0-00000"            | false
        "1.0.0-12345"  | "1.0.1-99999"            | true
        "1.0.0-12345"  | "branch:upstream/master" | true
    }

    def "lessThan throws an exception for invalid versions"() {
        when:
        def result = Versions.lessThan("1.0.0", badVersion)

        then:
        thrown Exception

        where:
        badVersion << ["1.0", "1.0.0.0", "1.a.b", "zzz"]
    }
}
