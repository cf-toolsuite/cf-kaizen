#!/usr/bin/env bash

# This script clones and deploys the following application and service instances to a target Cloud Foundry foundation:
#
# * cf-butler
# * cf-hoover
# * cf-hoover-config
# * cf-kaizen-butler-server
# * cf-kaizen-hoover-server
# * cf-kaizen-butler-frontend
# * cf-kaizen-hoover-frontend
#
# amongst other useful duties.

COMMAND=$1

GENAI_CHAT_SERVICE_NAME="kaizen-llm"
# Whichever plan you've configured here, it must a) be available from the cf marketplace and b) have chat capability.
GENAI_CHAT_PLAN_NAME="llama3.2"

CF_DOMAIN="apps.dhaka.cf-app.com"
CF_KAIZEN_BUTLER_SERVER_URL="https://cf-kaizen-butler-server.$CF_DOMAIN"
CF_KAIZEN_HOOVER_SERVER_URL="https://cf-kaizen-hoover-server.$CF_DOMAIN"

flag="${2:-}"
flag="${flag,,}"

if [ "$flag" == "y" ]; then
  ENABLE_DROPLET_SCANNING="y"
  ENABLE_CLONE_REFRESH="y"
else
  ENABLE_DROPLET_SCANNING="n"
  ENABLE_CLONE_REFRESH="n"
fi

if [ -n "$CF_API" ]; then

get_app_url() {
  local app_name="$1"

  if [ -z "$app_name" ]; then
    echo "Error: Please provide an application name" >&2
    return 1
  fi

  # Use cf app command to get app details, then extract the routes
  local url=$(cf app "$app_name" | grep -E "routes:" | sed -E 's/routes: (.*)/\1/' | xargs)

  if [ -z "$url" ]; then
    echo "Error: Could not find URL for application '$app_name'" >&2
    return 1
  fi

  echo "$url"
}

set_cf_env_vars() {
  local app_name="$1"

  if [ -z "$app_name" ]; then
    echo "Error: Please provide an application name" >&2
    return 1
  fi

  cf set-env "$app_name" JAVA_OPTS '-Djava.security.egd=file:///dev/urandom'
  cf set-env "$app_name" JBP_CONFIG_OPEN_JDK_JRE '{ jre: { version: 21.+ } }'
  cf set-env "$app_name" JBP_CONFIG_SPRING_AUTO_RECONFIGURATION '{ enabled: false }'
}


case $COMMAND in

authenticate)
  ## Authenticate
  echo "-- Authenticating"
  if [ -n "$CF_API" ]; then
    cf login --sso
  else
    cf login
  fi
  ;;

provision)
  ## Create orgs and spaces
  echo "-- Creating organizations and spaces"
  declare -a SPACES=( "kaizen:prod" "observability:cf-toolsuite" "zoolabs:dev" )
  for space in "${SPACES[@]}"
  do
    # Split the string using ":" as delimiter
    # Extract the part before ":" for organization
    o="${space%%:*}"
    # Extract the part after ":" for space
    s="${space#*:}"
    # Now use the extracted values
    cf create-org $o
    cf create-space "$s" -o "$o"
  done
  ;;

deprovision)
  ## Delete orgs and spaces
  echo "-- Deleting organizations (and spaces)"
  declare -a ORGANIZATIONS=( "kaizen" "observability" "zoolabs" )
  for org in "${ORGANIZATIONS[@]}"
  do
    cf delete-org "$org" -f
  done
  ;;

clone)
  ## Clone repositories
  cd /tmp || exit 1
  echo "-- Cloning repositories from GitHub"
  declare -a REPOSITORIES=( "cf-toolsuite/cf-butler" "cf-toolsuite/cf-hoover" "cf-toolsuite/cf-kaizen" )
  for repo in "${REPOSITORIES[@]}"
  do
    if [ "$ENABLE_CLONE_REFRESH" == "y" ]; then
        rm -Rf "/tmp/$repo"
    fi
    gh repo clone "$repo"
  done
  gh repo clone fastnsilver/primes
  ;;

build)
  ## Build projects
  cd /tmp || exit 1
  echo "-- Building projects"
  declare -a PROJECTS=( "cf-butler" "cf-hoover" "cf-kaizen" )
  for proj in "${PROJECTS[@]}"
  do
    cd "$proj" || exit 1
    if [ "$ENABLE_DROPLET_SCANNING" == "y" ]  && [ "$proj" == "cf-butler" ]; then
      git checkout memory-optimizations-for-droplet-read-processing
    fi
    ./mvnw install
    cd /tmp || exit 1
  done
  cd primes || exit 1
  git checkout 3.4
  ./gradlew build
  ;;

deploy)
  ## Deploy application instance to zoolabs/dev
  echo "-- Deploying application instance to zoolabs/dev"
  cf target -o zoolabs -s dev

  cd /tmp/primes || exit 1
  cf push primes -m 1G -p build/libs/primes-1.0-SNAPSHOT.jar -s cflinuxfs4 --no-start
  set_cf_env_vars primes
  cf start primes

  ## Deploy application and service instances to observability/cf-toolsuite
  echo "-- Deploying cf-butler and cf-hoover and supporting service instances to observability/cf-toolsuite"
  cf target -o observability -s cf-toolsuite

  cd /tmp/cf-butler || exit 1
  cf push --no-start --no-route
  jq --arg token "$(jq -r '.RefreshToken' $HOME/.cf/config.json)" '.["CF_REFRESH-TOKEN"] = $token' /tmp/cf-kaizen/config/secrets.butler-on-dhaka.json > /tmp/cf-kaizen/config/secrets.butler-on-dhaka-updated.json
  cf create-service credhub default cf-butler-secrets -c /tmp/cf-kaizen/config/secrets.butler-on-dhaka-updated.json
  cf bind-service cf-butler cf-butler-secrets
  cf create-route apps.dhaka.cf-app.com --hostname cf-butler-dev
  cf map-route cf-butler apps.dhaka.cf-app.com --hostname cf-butler-dev
  if [ "$ENABLE_DROPLET_SCANNING" == "y" ]; then
    echo "-- Droplet scanning will be enabled"
    cf set-env cf-butler JAVA_ARTIFACTS_FETCH_MODE list-jars-in-droplet
  fi
  cf set-health-check cf-butler http --endpoint /actuator/health --invocation-timeout 180
  cf start cf-butler
  CF_BUTLER_API_ENDPOINT=$(get_app_url cf-butler)

  cd /tmp/cf-hoover || exit 1
  cf push cf-hoover --no-start
  cf create-service p.config-server standard cf-hoover-config -c /tmp/cf-kaizen/config/secrets.hoover-on-dhaka.json
  while [[ $(cf service cf-hoover-config) != *"succeeded"* ]]; do
    echo "cf-hoover-config is not ready yet..."
    sleep 5
  done
  cf bind-service cf-hoover cf-hoover-config
  cf set-env cf-hoover SPRING_CLOUD_DISCOVERY_ENABLED false
  cf set-health-check cf-hoover http --endpoint /actuator/health --invocation-timeout 180
  cf start cf-hoover
  CF_HOOVER_API_ENDPOINT=$(get_app_url cf-hoover)

  ## Deploy application instances to kaizen/prod
  echo "-- Deploying MCP server and client application instances to kaizen/prod"
  cf target -o kaizen -s prod

  cd /tmp/cf-kaizen/butler || exit 1
  cf push cf-kaizen-butler-server -m 1G -k 256M -p target/cf-kaizen-butler-server-0.0.1-SNAPSHOT.jar -s cflinuxfs4 --no-start
  set_cf_env_vars cf-kaizen-butler-server
  cf set-env cf-kaizen-butler-server CF_BUTLER_API_ENDPOINT "$CF_BUTLER_API_ENDPOINT"
  cf set-env cf-kaizen-butler-server SPRING_PROFILES_ACTIVE "default,cloud"
  cf set-health-check cf-kaizen-butler-server http --endpoint /actuator/health --invocation-timeout 180
  cf start cf-kaizen-butler-server

  cd /tmp/cf-kaizen/hoover || exit 1
  cf push cf-kaizen-hoover-server -m 1G -k 256M -p target/cf-kaizen-butler-server-0.0.1-SNAPSHOT.jar -s cflinuxfs4 --no-start
  set_cf_env_vars cf-kaizen-hoover-server
  cf set-env cf-kaizen-hoover-server CF_HOOVER_API_ENDPOINT "$CF_HOOVER_API_ENDPOINT"
  cf set-env cf-kaizen-hoover-server SPRING_PROFILES_ACTIVE "default,cloud"
  cf set-health-check cf-kaizen-hoover-server http --endpoint /actuator/health --invocation-timeout 180
  cf start cf-kaizen-hoover-server

  echo "-- Readying a GenAI service instance"
  cf create-service genai $GENAI_CHAT_PLAN_NAME $GENAI_CHAT_SERVICE_NAME

  cd /tmp/cf-kaizen/clients/butler || exit 1
  cf push cf-kaizen-butler-frontend -m 1G -k 256M -p target/cf-kaizen-butler-frontend-0.0.1-SNAPSHOT.jar -s cflinuxfs4 --no-start
  set_cf_env_vars cf-kaizen-butler-frontend
  cf set-env cf-kaizen-butler-frontend SPRING_PROFILES_ACTIVE "default,cloud"
  cf set-env cf-kaizen-butler-frontend CF_KAIZEN_BUTLER_SERVER_URL $CF_KAIZEN_BUTLER_SERVER_URL
  cf bind-service cf-kaizen-butler-frontend $GENAI_CHAT_SERVICE_NAME
  cf set-health-check cf-kaizen-butler-frontend http --endpoint /actuator/health --invocation-timeout 180
  cf start cf-kaizen-butler-frontend

  cd /tmp/cf-kaizen/clients/hoover || exit 1
  cf push cf-kaizen-hoover-frontend -m 1G -k 256M -p target/cf-kaizen-hoover-frontend-0.0.1-SNAPSHOT.jar -s cflinuxfs4 --no-start
  set_cf_env_vars cf-kaizen-hoover-frontend
  cf set-env cf-kaizen-hoover-frontend SPRING_PROFILES_ACTIVE "default,cloud"
  cf set-env cf-kaizen-hoover-frontend CF_KAIZEN_HOOVER_SERVER_URL $CF_KAIZEN_HOOVER_SERVER_URL
  cf bind-service cf-kaizen-hoover-frontend $GENAI_CHAT_SERVICE_NAME
  cf set-health-check cf-kaizen-hoover-frontend http --endpoint /actuator/health --invocation-timeout 180
  cf start cf-kaizen-hoover-frontend
  ;;

destroy)
  echo "-- Deleting all application and service instances"
  cf target -o kaizen -s prod
  cf unbind-service cf-kaizen-butler-frontend $GENAI_CHAT_SERVICE_NAME
  cf unbind-service cf-kaizen-hoover-frontend $GENAI_CHAT_SERVICE_NAME
  cf delete-service $GENAI_CHAT_SERVICE_NAME -f
  declare -a APPS=( "cf-kaizen-hoover-frontend" "cf-kaizen-butler-frontend" "cf-kaizen-hoover-server" "cf-kaizen-butler-server" )
  for app in "${APPS[@]}"
  do
    cf delete "$app" -r -f
  done

  cf target -o observability -s cf-toolsuite
  cf unbind-service cf-hoover cf-hoover-config
  cf delete-service cf-hoover-config -f
  cf delete cf-hoover -r -f
  cf delete cf-butler -r -f

  cf target -o zoolabs -s dev
  cf delete primes -r -f
  ;;

*)
  echo && printf "\e[31m⏹  Usage: deploy-on-tp4cf.sh authenticate|provision|deprovision|clone|build|deploy|destroy \e[m\n" && echo
  ;;
esac
