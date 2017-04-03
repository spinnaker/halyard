#!/usr/bin/env bash

## auto-generated debian install file written by halyard

set -e
set -o pipefail

# install redis as a local service
INSTALL_REDIS="{%install-redis%}"

# install dependencies required on images running a distributed spinnaker
INSTALL_REMOTE_DEPENDENCIES="{%install-remote-dependencies%}"

# install first-time spinnaker dependencies (java, setup apt repos)
PREPARE_ENVIRONMENT="{%prepare-environment%}"

# TODO(lwander/jtk54) move these versions to the BOM
CONSUL_VERSION=0.7.5
VAULT_VERSION=0.7.0

REPOSITORY_URL="{%debian-repository%}"

## check that the user is root
if [[ `/usr/bin/id -u` -ne 0 ]]; then
  echo "$0 must be executed with root permissions; exiting"
  exit 1
fi

if [[ -f /etc/lsb-release ]]; then
  . /etc/lsb-release
  DISTRO=$DISTRIB_ID
elif [[ -f /etc/debian_version ]]; then
  DISTRO=Debian
  # XXX or Ubuntu
elif [[ -f /etc/redhat-release ]]; then
  if grep -iq cent /etc/redhat-release; then
    DISTRO="CentOS"
  elif grep -iq red /etc/redhat-release; then
    DISTRO="RedHat"
  fi
else
  DISTRO=$(uname -s)
fi

# If not Ubuntu 14.xx.x or higher

if [ "$DISTRO" = "Ubuntu" ]; then
  if [ "${DISTRIB_RELEASE%%.*}" -lt "14" ]; then
    echo "Not a supported version of Ubuntu"
    echo "Version is $DISTRIB_RELEASE we require 14.04 or higher"
    exit 1
  fi
else
  echo "Not a supported operating system: " $DISTRO
  echo "It's recommended you use Ubuntu 14.04 or higher."
  echo ""
  echo "Please file an issue against https://github.com/spinnaker/spinnaker/issues"
  echo "if you'd like to see support for your OS and version"
  exit 1
fi

function add_redis_apt_repository() {
  add-apt-repository -y ppa:chris-lea/redis-server
}

function add_spinnaker_apt_repository() {
  REPOSITORY_HOST=$(echo $REPOSITORY_URL | cut -d/ -f3)
  if [[ "$REPOSITORY_HOST" == "dl.bintray.com" ]]; then
    REPOSITORY_ORG=$(echo $REPOSITORY_URL | cut -d/ -f4)
    # Personal repositories might not be signed, so conditionally check.
    gpg=""
    gpg=$(curl -s -f "https://bintray.com/user/downloadSubjectPublicKey?username=$REPOSITORY_ORG") || true
    if [[ ! -z "$gpg" ]]; then
      echo "$gpg" | apt-key add -
    fi
  fi
  echo "deb $REPOSITORY_URL $DISTRIB_CODENAME spinnaker" | tee /etc/apt/sources.list.d/spinnaker.list > /dev/null
}

function add_java_apt_repository() {
  add-apt-repository -y ppa:openjdk-r/ppa
}

function install_java() {
  apt-get install -y --force-yes unzip
  apt-get install -y --force-yes openjdk-8-jdk

  # https://bugs.launchpad.net/ubuntu/+source/ca-certificates-java/+bug/983302
  # It seems a circular dependency was introduced on 2016-04-22 with an openjdk-8 release, where
  # the JRE relies on the ca-certificates-java package, which itself relies on the JRE.
  # This causes the /etc/ssl/certs/java/cacerts file to never be generated, causing a startup
  # failure in Clouddriver.
  dpkg --purge --force-depends ca-certificates-java
  apt-get install ca-certificates-java
}

function install_remote_dependencies() {
  apt-get install unzip
  TEMPDIR=$(mktemp -d installspinnaker.XXXX)

  mkdir $TEMPDIR/consul && pushd $TEMPDIR/consul
  curl -s -L -O https://releases.hashicorp.com/consul/${CONSUL_VERSION}/consul_${CONSUL_VERSION}_linux_amd64.zip
  unzip -u -o -q consul_${CONSUL_VERSION}_linux_amd64.zip -d /usr/bin
  popd
  rm -rf $TEMPDIR/consul

  mkdir $TEMPDIR/vault && pushd $TEMPDIR/vault
  curl -s -L -O https://releases.hashicorp.com/vault/${VAULT_VERSION}/vault_${VAULT_VERSION}_linux_amd64.zip
  unzip -u -o -q vault_${VAULT_VERSION}_linux_amd64.zip -d /usr/bin
  popd
  rm -rf $TEMPDIR/vault

  rm -rf $TEMPDIR
}

echo "Updating apt package lists..."

if [ -n "$INSTALL_REDIS" ]; then
  add_redis_apt_repository
fi

if [ -n "$PREPARE_ENVIRONMENT" ]; then
  add_java_apt_repository
  add_spinnaker_apt_repository
  {%upstart-init%}
fi

apt-get update ||:

echo "Installing desired components..."

if [ -n "$PREPARE_ENVIRONMENT" ]; then
  install_java
fi

if [ -n "$INSTALL_REMOTE_DEPENDENCIES" ]; then
  install_remote_dependencies
fi

mkdir -p /opt/spinnaker/config
chown spinnaker /opt/spinnaker/config

mkdir -p /opt/spinnaker-monitoring/config
chown spinnaker /opt/spinnaker-monitoring/config

mkdir -p /opt/spinnaker-monitoring/registry
chown spinnaker /opt/spinnaker-monitoring/registry

{%install-commands%}

# so this script can be used for updates
set +e
service spinnaker {%service-action%}
