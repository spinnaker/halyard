FROM openjdk:8

MAINTAINER delivery-engineering@netflix.com

ENV KUBECTL_RELEASE=v1.7.3

COPY . workdir/

WORKDIR workdir

RUN GRADLE_USER_HOME=cache ./gradlew installDist -x test -Prelease.useLastTag=true && \
  cp -r ./halyard-web/build/install/halyard /opt && \
  cd .. && \
  rm -rf workdir

RUN echo '#!/usr/bin/env bash' | tee /usr/local/bin/hal > /dev/null && \
  echo '/opt/halyard/bin/hal "$@"' | tee /usr/local/bin/hal > /dev/null

RUN chmod +x /usr/local/bin/hal

RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/${KUBECTL_RELEASE}/bin/linux/amd64/kubectl && \
    chmod +x ./kubectl && \
    mv ./kubectl /usr/local/bin/kubectl

RUN curl -o heptio-authenticator-aws https://amazon-eks.s3-us-west-2.amazonaws.com/1.10.3/2018-06-05/bin/linux/amd64/heptio-authenticator-aws && \
  chmod +x ./heptio-authenticator-aws && \
  mv ./heptio-authenticator-aws /usr/local/bin/heptio-authenticator-aws

ENV PATH "$PATH:/usr/local/bin/heptio-authenticator-aws"

RUN useradd -m spinnaker

USER spinnaker

CMD "/opt/halyard/bin/halyard"
