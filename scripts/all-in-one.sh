#!/usr/bin/env bash

# This script is supplemental to scripts/deploy-on-tp4cf.yml
# It deploys the following applications to a configured organization and space

# * cf-kaizen-butler-server
# * cf-kaizen-hoover-server
# * cf-kaizen-butler-frontend
# * cf-kaizen-hoover-frontend
#

# --------------------------------------------------------------------------------
# ENVIRONMENT VARIABLES
# Update values here as you like

# -- If Single sign-on is enabled then set IS_SSO="y"
export IS_SSO=""
export ORGANIZATION="cf-toolsuite"
export SPACE="observability"
export ARTIFACT_VERSION="2025.03.18"
export CF_API_ENDPOINT="https://api.sys.tas-ndc.kuhn-labs.com"
export CF_APPS_DOMAIN="app.tas-ndc.kuhn-labs.com"
export LLM_SERVICE_NAME="kaizen-llm"
# -- Must be an available service plan (@see cf m -e genai)
export LLM_PLAN_NAME="mistral-nemo:12b-instruct-2407-q4_K_S"
export FOUNDATION="kuhn-labs"

# --------------------------------------------------------------------------------
# CAUTION: DO NOT CHANGE ANYTHING BELOW UNLESS YOU KNOW WHAT YOU'RE DOING

function determine_jar_release() {
  local date_pattern='[0-9]{4}\.[0-9]{2}\.[0-9]{2}'
  local date=""
  local current_date=""

  for file in target/*.jar; do
    if [[ $file =~ $date_pattern ]]; then
      current_date="${BASH_REMATCH[0]}"
      if [[ -z $date ]]; then
        date="$current_date"
      elif [[ $date != "$current_date" ]]; then
        echo "Varying dates found."
        return 1
      fi
    else
      echo "No matching date found in: $file"
      return 1
    fi
  done

  if [[ -n $date ]]; then
    echo $date
  else
    echo "No files with the expected date pattern found."
    return 1
  fi
}

ORIGINAL_MANIFEST="config/manifest.default.yml"
NEW_MANIFEST="config/manifest.$FOUNDATION.yml"

# Make a copy of the original manifest and populate with correct information
ytt -f $ORIGINAL_MANIFEST -v "llm_service_name=$LLM_SERVICE_NAME" -v "cf_apps_domain=$CF_APPS_DOMAIN" -v "artifact_version=$ARTIFACT_VERSION" > $NEW_MANIFEST

echo "-- Manifest updated successfully!"

echo "-- Authenticating"
cf api "$CF_API_ENDPOINT" --skip-ssl-validation
if [ -n "$IS_SSO" ]; then
  cf login --sso
else
  cf login
fi

echo "-- Creating organizations and spaces"
cf create-org $ORGANIZATION
cf create-space "$SPACE" -o "$ORGANIZATION"

echo "-- Targeting $ORGANIZATION/$SPACE"
cf target -o "$ORGANIZATION" -s "$SPACE"

echo "-- Creating LLM service instance"
cf create-service genai $LLM_PLAN_NAME $LLM_SERVICE_NAME

mkdir -p deploy
mkdir -p dist
echo "-- Fetching latest available cf-kaizen artifacts from Github Packages repository"
gh release download --repo cf-toolsuite/cf-kaizen --pattern '*.jar' -D dist --skip-existing
RELEASE=$(determine_jar_release)
echo "-- Latest version available is $RELEASE"

echo "-- Copying cf-kaizen artifacts into place"
mkdir -p butler/target
mkdir -p hoover/target
mkdir -p clients/butler/target
mkdir -p clients/hoover/target
cp dist/cf-kaizen-butler-server-$RELEASE.jar butler/target
cp dist/cf-kaizen-hoover-server-$RELEASE.jar hoover/target
cp dist/cf-kaizen-butler-frontend-$RELEASE.jar clients/butler/target
cp dist/cf-kaizen-hoover-frontend-$RELEASE.jar clients/hoover/target

echo "-- Deploying applications"
cf push -f $NEW_MANIFEST
