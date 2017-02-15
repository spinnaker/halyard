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

package com.netflix.spinnaker.halyard.cli.command.v1.config.providers.aws;

public class AwsCommandProperties {
  static final String DEFAULT_KEY_PAIR_DESCRIPTION = "Provide the name of the AWS key-pair to use."
      + " See http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html for more information.";

  static final String EDDA_DESCRIPTION = "The endpoint Edda is reachable at. Edda is not a hard dependency of Spinnaker,"
      + " but is helpful for reducing the request volume against AWS."
      + " See https://github.com/Netflix/edda for more information.";

  static final String DISCOVERY_DESCRIPTION = "The endpoint your Eureka discovery system is reachable at."
      + " See https://github.com/Netflix/eureka for more information.\n\n"
      + "Example: http://{{region}}.eureka.url.to.use:8080/eureka-server/v2 \n\nUsing {{region}} will make Spinnaker"
      + " use AWS regions in the hostname to access discovery so that you can have discovery for multiple regions.";

  static final String ACCOUNT_ID_DESCRIPTION = "Your AWS account ID to manage."
      + " See http://docs.aws.amazon.com/IAM/latest/UserGuide/console_account-alias.html for more information.";

  static final String REGIONS_DESCRIPTION = "The AWS regions this Spinnaker account will manage.";
}
