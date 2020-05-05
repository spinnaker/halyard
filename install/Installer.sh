#!/usr/bin/env bash

# This script installs Halyard.

set -e
set -o pipefail

REPOSITORY_URL="https://dl.bintray.com/spinnaker-releases/debians"
SPINNAKER_REPOSITORY_URL="https://dl.bintray.com/spinnaker-releases/debians"
SPINNAKER_DOCKER_REGISTRY="us-docker.pkg.dev/spinnaker-community/releases"
SPINNAKER_GCE_PROJECT="marketplace-spinnaker-release"
CONFIG_BUCKET="halconfig" 

VERSION=""
HALYARD_STARTUP_TIMEOUT_SECONDS=120

if [ -z "$RELEASE_TRACK" ]; then
  >&2 echo "RELEASE_TRACK env var must be set (nightly or stable)"
  >&2 echo "Typically this script is invoked from a wrapper, e.g. "
  >&2 echo "   https://raw.githubusercontent.com/spinnaker/halyard/master/install/stable/InstallHalyard.sh"
  exit 1
fi

# We can only currently support limited releases
# First guess what sort of operating system

if [ -f /etc/lsb-release ]; then
  . /etc/lsb-release
  DISTRO=$DISTRIB_ID
elif [ -f /etc/debian_version ]; then
  DISTRO=Debian
  # XXX or Ubuntu
elif [ -f /etc/redhat-release ]; then
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
    echo "Version is $DISTRIB_RELEASE we require 14.04"
    exit 1
  fi
else
  echo "Not a supported operating system: "
  echo "It's recommended you use Ubuntu 14.04"
  echo ""
  echo "Please file an issue against github.com/spinnaker/halyard/issues "
  echo "if you'd like to see support for your OS and version"
  exit 1
fi


function print_usage() {
  cat <<EOF
usage: $0 [-y] [--quiet] [--dependencies_only]
    [--repository <debian repository url>]
    [--local-install] [--home_dir <path>]
    -y                              Accept all default options during install
                                    (non-interactive mode).

    --repository <url>              Obtain Halyard debian from <url>
                                    rather than the default repository, which is
                                    $REPOSITORY_URL.

    --spinnaker-repository <url>    Obtain Spinnaker artifact debians from <url>
                                    rather than the default repository, which is
                                    $SPINNAKER_REPOSITORY_URL.

    --spinnaker-registry <url>      Obtain Spinnaker docker images from <url>
                                    rather than the default registry, which is
                                    $SPINNAKER_DOCKER_REGISTRY.

    --spinnaker-gce-project <name>  Obtain Spinnaker GCE images from <url>
                                    rather than the default project, which is
                                    $SPINNAKER_GCE_PROJECT.

    --version <version>             Specify the exact version of Halyard to 
                                    install.

    --user <user>                   Specify the user to run Halyard as. This
                                    user must exist.

    --dependencies_only             Do not install any Spinnaker services.
                                    Only install the dependencies. This is
                                    intended for development scenarios only.

    --local-install                 For Spinnaker and Java packages, download
                                    packages and install using dpkg instead of
                                    apt. Use this option only if you are having
                                    issues with the bintray repositories.
                                    If you use this option you must manually
                                    install openjdk-8-jdk.

    --home_dir                      Override where user home directories reside
                                    example: /export/home vs /home.
EOF
}

function echo_status() {
  if [ -n "$QUIET" ]; then
    echo "$@"
  fi
}

function process_args() {
  while [ "$#" -gt "0" ]
  do
    local key="$1"
    shift
    case $key in
      --repository)
        echo "repo"
        REPOSITORY_URL="$1"
        shift
        ;;
      --spinnaker-repository)
        echo "spinnaker-repo"
        SPINNAKER_REPOSITORY_URL="$1"
        shift
        ;;
      --spinnaker-registry)
        echo "spinnaker-registry"
        SPINNAKER_DOCKER_REGISTRY="$1"
        shift
        ;;
      --spinnaker-gce-project)
        echo "spinnaker-gce-project"
        SPINNAKER_GCE_PROJECT="$1"
        shift
        ;;
      --config-bucket)
        echo "config-bucket"
        CONFIG_BUCKET="$1"
        shift
        ;;
      --user)
        echo "user"
        HAL_USER="$1"
        shift
        ;;
      --version)
        echo "version"
        VERSION="$1"
        shift
        ;;
      --dependencies_only)
        echo "deps"
        DEPENDENCIES_ONLY=true
        ;;
      -y)
        echo "non-interactive"
        YES=true
        ;;
      --local-install)
        echo "local"
        DOWNLOAD=true
        ;;
      --quiet|-q)
        QUIET=true
        ;;
      --home_dir)
        homebase="$1"
        if [ "$(basename $homebase)" = "spinnaker" ]; then
          echo "stripping trailing 'spinnaker' from --home_dir=$homebase"
          homebase=$(dirname $homebase)
        fi
        shift
        ;;
      --help|-help|-h)
        print_usage
        exit 13
        ;;
      *)
        echo "ERROR: Unknown argument '$key'"
        exit -1
    esac
  done
}

function add_apt_repositories() {
  # Spinnaker
  # DL Repo goes here
  REPOSITORY_HOST=$(echo $REPOSITORY_URL | cut -d/ -f3)
  if [ "$REPOSITORY_HOST" = "dl.bintray.com" ]; then
    REPOSITORY_ORG=$(echo $REPOSITORY_URL | cut -d/ -f4)
    # Personal repositories might not be signed, so conditionally check.
    gpg=""
    gpg=$(curl -s -f "https://bintray.com/user/downloadSubjectPublicKey?username=$REPOSITORY_ORG") || true
    if [ -n "$gpg" ]; then
      echo "$gpg" | apt-key add -
    fi
  fi
  echo "deb $REPOSITORY_URL ${DISTRIB_CODENAME}-${RELEASE_TRACK} spinnaker" | tee /etc/apt/sources.list.d/halyard.list > /dev/null
  # Java 8
  # https://launchpad.net/~openjdk-r/+archive/ubuntu/ppa
  add-apt-repository -y ppa:openjdk-r/ppa
  apt-get update ||:
}

function install_java() {
  if [ -z "$DOWNLOAD" ]; then
    apt-get install -y --force-yes openjdk-8-jdk

    # https://bugs.launchpad.net/ubuntu/+source/ca-certificates-java/+bug/983302
    # It seems a circular dependency was introduced on 2016-04-22 with an openjdk-8 release, where
    # the JRE relies on the ca-certificates-java package, which itself relies on the JRE. D'oh!
    # This causes the /etc/ssl/certs/java/cacerts file to never be generated, causing a startup
    # failure in Clouddriver.
    dpkg --purge --force-depends ca-certificates-java
    apt-get install ca-certificates-java
  elif [[ "x`java -version 2>&1|head -1`" != *"1.8.0"* ]]; then
    echo "you must manually install java 8 and then rerun this script; exiting"
    exit 13
  fi
}

function install_halyard() {
  local package installed_package
  package="spinnaker-halyard"
  installed_package=$package
  if [ -n "$VERSION" ]; then
    installed_package="$package=$VERSION"
  fi
  apt-get install -y --force-yes --allow-unauthenticated $installed_package
  local apt_status=$?
  if [ $apt_status -ne 0 ]; then
    if [ -n "$DOWNLOAD" ] && [ "$apt_status" -eq "100" ]; then
      local debfile version
      echo "$(tput bold)Downloading packages for installation by dpkg...$(tput sgr0)"
      if [ -n "$VERSION" ]; then
        version="${package}_${VERSION}_all.deb"
        debfile=$version
      else 
        version=`curl $REPOSITORY_URL/dists/${DISTRIB_CODENAME}-${RELEASE_TRACK}/spinnaker/binary-amd64/Packages | grep "^Filename" | grep $package | awk '{print $2}' | awk -F'/' '{print $NF}' | sort -t. -k 1,1n -k 2,2n -k 3,3n | tail -1`
        debfile=`echo $version | awk -F "/" '{print $NF}'`
      fi
      filelocation=`curl $REPOSITORY_URL/dists/${DISTRIB_CODENAME}-${RELEASE_TRACK}/spinnaker/binary-amd64/Packages | grep "^Filename" | grep $version | awk '{print $2}'`
      curl -L -o /tmp/$debfile $REPOSITORY_URL/$filelocation
      dpkg -i /tmp/$debfile && rm -f /tmp/$debfile
    else
      echo "Error installing halyard."
      echo "cannot continue installation; exiting."
      exit 13
    fi
  fi
}

function configure_bash_completion() {
  local yes
  echo ""
  if [ -z "$YES" ]; then
    read -p "Would you like to configure halyard to use bash auto-completion? [default=Y]: " yes
  else
    yes="y"
  fi

  local home=$(getent passwd $HAL_USER | cut -d: -f6)
  completion_script="/etc/bash_completion.d/hal"
  if [ "$yes" = "y" ] || [ "$yes = "Y" ] || [ "$yes = "yes" ] || [ "$yes" = "" ]; then
    local bashrc
    hal --print-bash-completion | tee $completion_script  > /dev/null
    if [ -z "$YES" ]; then
      echo ""
      read -p "Where is your bash RC? [default=$home/.bashrc]: " bashrc
    fi
    
    if [ -z "$bashrc" ]; then
      bashrc="$home/.bashrc"
    fi

    if [ -z "$(grep $completion_script $bashrc)" ]; then
      echo "# configure hal auto-complete " >> $bashrc
      echo ". /etc/bash_completion.d/hal" >> $bashrc
    fi

    echo "Bash auto-completion configured."
    echo "$(tput bold)To use the auto-completion either restart your shell, or run$(tput sgr0)"
    echo "$(tput bold). $bashrc$(tput sgr0)"
  fi
  
}

function get_user() {
  local user 

  user=$(who -m | awk '{print $1;}')
  if [ -z "$YES" ]; then
    if [ "$user" = "root" ] || [ -z "$user" ]; then
      read -p "Please supply a non-root user to run Halyard as: " user
    fi
  fi

  echo $user
}

function configure_halyard_defaults() {
  home=$(getent passwd $HAL_USER | cut -d: -f6)
  local halconfig_dir="$home/.hal"

  echo "$(tput bold)Halconfig will be stored at $halconfig_dir/config$(tput sgr0)"

  mkdir -p $halconfig_dir
  chown $HAL_USER $halconfig_dir

  mkdir -p /opt/spinnaker/config
  chmod +rx /opt/spinnaker/config

  cat > /opt/spinnaker/config/halyard.yml <<EOL
halyard:
  halconfig:
    directory: $halconfig_dir

spinnaker:
  artifacts:
    debianRepository: $SPINNAKER_REPOSITORY_URL
    dockerRegistry: $SPINNAKER_DOCKER_REGISTRY
    googleImageProject: $SPINNAKER_GCE_PROJECT
  config:
    input:
      bucket: $CONFIG_BUCKET
EOL

  echo $HAL_USER > /opt/spinnaker/config/halyard-user

  cat > $halconfig_dir/uninstall.sh <<EOL
#!/usr/bin/env bash

if [[ \`/usr/bin/id -u\` -ne 0 ]]; then
  echo "$0 must be executed with root permissions; exiting"
  exit 1
fi

read -p "This script uninstalls Halyard and deletes all of its artifacts, are you sure you want to continue? (Y/n): " yes

if [ "\$yes" != "y" ] && [ "\$yes" != "Y" ]; then
  echo "Aborted"
  exit 0
fi

apt-get purge spinnaker-halyard

echo "Deleting halconfig and artifacts"
rm /opt/spinnaker/config/halyard* -rf
rm $halconfig_dir -rf
EOL

  chmod +x $halconfig_dir/uninstall.sh
  echo "$(tput bold)Uninstall script is located at $halconfig_dir/uninstall.sh$(tput sgr0)"
}

process_args "$@"

if [ -z "$HAL_USER" ]; then 
  HAL_USER=$(get_user)
fi

if [ -z "$HAL_USER" ]; then 
  >&2 echo "You have not supplied a user to run Halyard as."
  exit 1
fi

if [ "$HAL_USER" = "root" ]; then
  >&2 echo "Halyard may not be run as root. Supply a user to run Halyard as: "
  >&2 echo "  sudo bash $0 --user <user>"
  exit 1
fi

set +e
getent passwd $HAL_USER &> /dev/null

if [ "$?" != "0" ]; then
  >&2 echo "Supplied user $HAL_USER does not exist"
  exit 1
fi
set -e

configure_halyard_defaults

echo "$(tput bold)Configuring external apt repos...$(tput sgr0)"
add_apt_repositories

echo "$(tput bold)Installing Java 8...$(tput sgr0)"

install_java

if [ -n "$DEPENDENCIES_ONLY" ]; then
  exit 0
fi

## Spinnaker
echo "$(tput bold)Installing Halyard...$(tput sgr0)"
install_halyard
groupadd spinnaker || true
usermod -G spinnaker -a $HAL_USER || true

configure_bash_completion

printf 'Waiting for the Halyard daemon to start running'

WAIT_START=$(date +%s)

set +e 
hal --ready &> /dev/null

while [ "$?" != "0" ]; do
  WAIT_NOW=$(date +%s)
  WAIT_TIME=$(( $WAIT_NOW - $WAIT_START ))

  if [ "$WAIT_TIME" -gt "$HALYARD_STARTUP_TIMEOUT_SECONDS" ]; then
    >&2 echo ""
    >&2 echo "Waiting for halyard to start timed out after $WAIT_TIME seconds"
    exit 1
  fi

  printf '.'
  sleep 2
  hal --ready &> /dev/null
done

echo 

if [ -z "$QUIET" ]; then
cat <<EOF

Halyard is now installed and running. To interact with it, use:

$ hal --help

More information can be found here:
https://github.com/spinnaker/halyard/blob/master/README.md

EOF
fi
