# PowerShell Script
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

param (
    [Parameter(Position=0, Mandatory=$true)]
    [string]$Command,

    [Parameter(Position=1)]
    [string]$Flag
)

# --------------------------------------------------------------------------------
# ENVIRONMENT VARIABLES
# Update values here as you like

$FOUNDATION = "dhaka"
$CF_DOMAIN = "apps.dhaka.cf-app.com"
$CF_KAIZEN_BUTLER_SERVER_URL = "https://cf-kaizen-butler-server.$CF_DOMAIN"
$CF_KAIZEN_HOOVER_SERVER_URL = "https://cf-kaizen-hoover-server.$CF_DOMAIN"

$GENAI_CHAT_SERVICE_NAME = "kaizen-llm"
# -- Must be an available service plan (@see cf m -e genai)
$GENAI_CHAT_PLAN_NAME = "llama3.2"

# Where do you want applications and services deployed?
# cf-kaizen
$KAIZEN_ORG = "kaizen"
$KAIZEN_SPACE = "prod"
# cf-butler and cf-hoover
$OBSERVABILITY_ORG = "observability"
$OBSERVABILITY_SPACE = "cf-toolsuite"
# sample application
$SAMPLE_APP_ORG = "zoolabs"
$SAMPLE_APP_SPACE = "dev"
$SAMPLE_APP_NAME = "primes"
$SAMPLE_APP_ARTIFACT_HOME = "build/libs"
$SAMPLE_APP_VERSION = "1.0-SNAPSHOT"
$SAMPLE_APP_GITHUB_REPO = "fastnsilver/primes"
$SAMPLE_APP_GITHUB_BRANCH = "3.4"

# --------------------------------------------------------------------------------
# CAUTION: DO NOT CHANGE ANYTHING BELOW UNLESS YOU KNOW WHAT YOU'RE DOING

function Tool-Checks {
    # Check for required tools
    Write-Host "-- Checking whether or not required tools are installed"

    # Check if jq is installed
    try {
        $null = Get-Command jq -ErrorAction Stop
    }
    catch {
        Write-Host "Error: jq is not installed. Please install it first."
        Write-Host "Windows: winget install jqlang.jq or https://jqlang.org/download/"
        Exit 1
    }

    # Check if gh (GitHub CLI) is installed
    try {
        $null = Get-Command gh -ErrorAction Stop
    }
    catch {
        Write-Host "Error: GitHub CLI (gh) is not installed. Please install it first."
        Write-Host "Windows: winget install GitHub.cli or https://cli.github.com/"
        Exit 1
    }

    # Check if cf (Cloud Foundry CLI) is installed
    try {
        $null = Get-Command cf -ErrorAction Stop
    }
    catch {
        Write-Host "Error: Cloud Foundry CLI (cf) is not installed. Please install it first."
        Write-Host "Windows: Download from https://github.com/cloudfoundry/cli/wiki/V8-CLI-Installation-Guide"
        Exit 1
    }
}

function Determine-JarRelease {
    $date_pattern = '[0-9]{4}\.[0-9]{2}\.[0-9]{2}'
    $date = ""
    $current_date = ""

    # Use Get-ChildItem to locate jar files
    $files = Get-ChildItem -Path "target" -Filter "*.jar" -File -ErrorAction SilentlyContinue

    if ($null -eq $files -or $files.Count -eq 0) {
        Write-Host "No files found in target directory."
        return $null
    }

    foreach ($file in $files) {
        if ($file.Name -match $date_pattern) {
            $current_date = $matches[0]
            if ([string]::IsNullOrEmpty($date)) {
                $date = $current_date
            }
            elseif ($date -ne $current_date) {
                Write-Host "Varying dates found."
                return $null
            }
        }
        else {
            Write-Host "No matching date found in: $($file.Name)"
            return $null
        }
    }

    if (-not [string]::IsNullOrEmpty($date)) {
        return $date
    }
    else {
        Write-Host "No files with the expected date pattern found."
        return $null
    }
}

function Get-AppUrl {
    param (
        [Parameter(Mandatory=$true)]
        [string]$AppName
    )

    if ([string]::IsNullOrEmpty($AppName)) {
        Write-Error "Error: Please provide an application name"
        return $null
    }

    # Use cf app command to get app details, then extract the routes
    $appDetails = cf app $AppName
    $routes = ($appDetails | Select-String -Pattern "routes:").Line

    if ($routes) {
        $url = $routes -replace "routes:", "" -replace "\s+", ""
        return $url.Trim()
    }
    else {
        Write-Error "Error: Could not find URL for application '$AppName'"
        return $null
    }
}

function Set-CFEnvVars {
    param (
        [Parameter(Mandatory=$true)]
        [string]$AppName
    )

    if ([string]::IsNullOrEmpty($AppName)) {
        Write-Error "Error: Please provide an application name"
        return
    }

    cf set-env "$AppName" "JAVA_OPTS" '-Djava.security.egd=file:///dev/urandom'
    cf set-env "$AppName" "JBP_CONFIG_OPEN_JDK_JRE" '{ jre: { version: 21.+ } }'
    cf set-env "$AppName" "JBP_CONFIG_SPRING_AUTO_RECONFIGURATION" '{ enabled: false }'
}

function Uber-Build {
    if (Test-Path -Path "pom.xml") {
        Build-With-Maven
    }

    if (Test-Path -Path "build.gradle") {
        Build-With-Gradle
    }
}

function Build-With-Maven {
    if (Test-Path -Path "./mvnw.cmd") {
        Set-ItemProperty -Path "./mvnw.cmd" -Name IsReadOnly -Value $false
        ./mvnw.cmd install
    }
    else {
        mvn install
    }
}

function Build-With-Gradle {
    if (Test-Path -Path "./gradlew.bat") {
        Set-ItemProperty -Path "./gradlew.bat" -Name IsReadOnly -Value $false
        ./gradlew.bat build
    }
    else {
        gradle build
    }
}

# --------------------------------------------------------------------------------
# START SCRIPT

Tool-Checks

if ($Flag -eq "y" -or $Flag -eq "Y") {
    $ENABLE_DROPLET_SCANNING = "y"
    $ENABLE_CLONE_REFRESH = "y"
}
else {
    $ENABLE_DROPLET_SCANNING = "n"
    $ENABLE_CLONE_REFRESH = "n"
}

# Main command switch
switch ($Command) {
    "authenticate" {
        ## Authenticate
        Write-Host "-- Authenticating"
        if ($env:CF_API) {
            cf login --sso
        }
        else {
            cf login
        }
    }

    "provision" {
        ## Create orgs and spaces
        Write-Host "-- Creating organizations and spaces"
        $SPACES = @("$KAIZEN_ORG`:$KAIZEN_SPACE", "$OBSERVABILITY_ORG`:$OBSERVABILITY_SPACE", "$SAMPLE_APP_ORG`:$SAMPLE_APP_SPACE")
        foreach ($space in $SPACES) {
            # Split the string using ":" as delimiter
            $parts = $space -split ":"
            $o = $parts[0]
            $s = $parts[1]

            # Now use the extracted values
            cf create-org $o
            cf create-space "$s" -o "$o"
        }
    }

    "deprovision" {
        ## Delete orgs and spaces
        Write-Host "-- Deleting organizations (and spaces)"
        $ORGANIZATIONS = @($KAIZEN_ORG, $OBSERVABILITY_ORG, $SAMPLE_APP_ORG)
        foreach ($org in $ORGANIZATIONS) {
            cf delete-org "$org" -f
        }
    }

    "clone" {
        ## Clone repositories
        Set-Location -Path $env:TEMP
        Write-Host "-- Cloning repositories from GitHub"
        $REPOSITORIES = @("cf-toolsuite/cf-butler", "cf-toolsuite/cf-hoover", "cf-toolsuite/cf-kaizen")
        foreach ($repo in $REPOSITORIES) {
            # Extract the repo name from the full path
            $repo_name = Split-Path -Leaf $repo

            if ($ENABLE_CLONE_REFRESH -eq "y") {
                if (Test-Path -Path "$env:TEMP\$repo_name") {
                    Remove-Item -Path "$env:TEMP\$repo_name" -Recurse -Force
                }
            }
            gh repo clone "$repo"
        }
        gh repo clone $SAMPLE_APP_GITHUB_REPO
    }

    "build" {
        ## Build projects
        Set-Location -Path $env:TEMP
        Write-Host "-- Building projects"
        $PROJECTS = @("cf-butler", "cf-hoover", "cf-kaizen")
        foreach ($proj in $PROJECTS) {
            Set-Location -Path "$proj"
            Uber-Build
            Set-Location -Path $env:TEMP
        }

        Set-Location -Path $SAMPLE_APP_NAME
        git checkout $SAMPLE_APP_GITHUB_BRANCH
        Uber-Build
    }

    "deploy-sample-app" {
        Write-Host "-- Deploying application instance"
        cf target -o $SAMPLE_APP_ORG -s $SAMPLE_APP_SPACE

        Set-Location -Path "$env:TEMP\$SAMPLE_APP_NAME"
        cf push $SAMPLE_APP_NAME -m 1G -p "$SAMPLE_APP_ARTIFACT_HOME/$SAMPLE_APP_NAME-$SAMPLE_APP_VERSION.jar" -s cflinuxfs4 --no-start
        Set-CFEnvVars -AppName $SAMPLE_APP_NAME
        cf start $SAMPLE_APP_NAME
    }

    "deploy-observability" {
        ## Deploy application and service instances to observability/cf-toolsuite
        Write-Host "-- Deploying cf-butler and cf-hoover and supporting service instances"
        cf target -o $OBSERVABILITY_ORG -s $OBSERVABILITY_SPACE

        Set-Location -Path "$env:TEMP\cf-butler"
        cf push --no-start --no-route

        # Check if CF config file exists
        $CF_CONFIG_FILE = "$env:USERPROFILE\.cf\config.json"
        if (-not (Test-Path -Path $CF_CONFIG_FILE)) {
            # Try alternative location for Windows
            $CF_CONFIG_FILE = "$env:APPDATA\cf\config.json"
            if (-not (Test-Path -Path $CF_CONFIG_FILE)) {
                Write-Host "Error: CF config file not found at expected locations"
                Exit 1
            }
        }

        $refreshToken = (Get-Content -Path $CF_CONFIG_FILE | ConvertFrom-Json).RefreshToken
        $secrets = Get-Content -Path "$env:TEMP\cf-kaizen\config\secrets.butler-on-$FOUNDATION.json" | ConvertFrom-Json
        $secrets | Add-Member -MemberType NoteProperty -Name "CF_REFRESH-TOKEN" -Value $refreshToken -Force
        $secrets | ConvertTo-Json | Out-File -FilePath "$env:TEMP\cf-kaizen\config\secrets.butler-on-$FOUNDATION-updated.json" -Encoding utf8

        cf create-service credhub default cf-butler-secrets -c "$env:TEMP\cf-kaizen\config\secrets.butler-on-$FOUNDATION-updated.json"
        cf bind-service cf-butler cf-butler-secrets
        cf create-route $CF_DOMAIN --hostname cf-butler-dev
        cf map-route cf-butler $CF_DOMAIN --hostname cf-butler-dev

        if ($ENABLE_DROPLET_SCANNING -eq "y") {
            Write-Host "-- Droplet scanning will be enabled"
            cf set-env cf-butler JAVA_ARTIFACTS_FETCH_MODE list-jars-in-droplet
        }

        cf set-health-check cf-butler http --endpoint /actuator/health --invocation-timeout 180
        cf start cf-butler

        Set-Location -Path "$env:TEMP\cf-hoover"
        cf push cf-hoover --no-start
        cf create-service p.config-server standard cf-hoover-config -c "$env:TEMP\cf-kaizen\config\secrets.hoover-on-$FOUNDATION.json"

        # Wait for service to be ready
        do {
            Write-Host "cf-hoover-config is not ready yet..."
            Start-Sleep -Seconds 5
            $serviceStatus = cf service cf-hoover-config
        } while ($serviceStatus -notmatch "succeeded")

        cf bind-service cf-hoover cf-hoover-config
        cf set-env cf-hoover SPRING_CLOUD_DISCOVERY_ENABLED false
        cf set-health-check cf-hoover http --endpoint /actuator/health --invocation-timeout 180
        cf start cf-hoover
    }

    "deploy-kaizen" {
        ## Deploy application instances to kaizen/prod
        Write-Host "-- Deploying MCP server and client application instances"

        cf target -o $OBSERVABILITY_ORG -s $OBSERVABILITY_SPACE
        $CF_BUTLER_API_ENDPOINT = Get-AppUrl -AppName "cf-butler"
        $CF_HOOVER_API_ENDPOINT = Get-AppUrl -AppName "cf-hoover"

        cf target -o $KAIZEN_ORG -s $KAIZEN_SPACE

        Set-Location -Path "$env:TEMP\cf-kaizen\target"
        $VERSION = "0.0.1-SNAPSHOT"
        if ($Flag -eq "--pre-built") {
            $VERSION = Determine-JarRelease
        }

        $ARTIFACT_HOME = "$env:TEMP\cf-kaizen\target"

        if ($Flag -eq "--pre-built") {
            $ARTIFACT_NAME = "cf-kaizen-butler-openai-$VERSION.jar"
        }
        else {
            $ARTIFACT_NAME = "cf-kaizen-butler-server-$VERSION.jar"
            $ARTIFACT_HOME = "$env:TEMP\cf-kaizen\butler"
        }

        Set-Location -Path $ARTIFACT_HOME
        cf push cf-kaizen-butler-server -m 1G -k 512M -p "target\$ARTIFACT_NAME" -s cflinuxfs4 --no-start
        Set-CFEnvVars -AppName "cf-kaizen-butler-server"
        cf set-env cf-kaizen-butler-server CF_BUTLER_API_ENDPOINT "$CF_BUTLER_API_ENDPOINT"
        cf set-env cf-kaizen-butler-server SPRING_PROFILES_ACTIVE "default,cloud"
        cf set-health-check cf-kaizen-butler-server http --endpoint /actuator/health --invocation-timeout 180
        cf start cf-kaizen-butler-server

        if ($Flag -eq "--pre-built") {
            $ARTIFACT_NAME = "cf-kaizen-hoover-openai-$VERSION.jar"
        }
        else {
            $ARTIFACT_NAME = "cf-kaizen-hoover-server-$VERSION.jar"
            $ARTIFACT_HOME = "$env:TEMP\cf-kaizen\hoover"
        }

        Set-Location -Path $ARTIFACT_HOME
        cf push cf-kaizen-hoover-server -m 1G -k 512M -p "target\$ARTIFACT_NAME" -s cflinuxfs4 --no-start
        Set-CFEnvVars -AppName "cf-kaizen-hoover-server"
        cf set-env cf-kaizen-hoover-server CF_HOOVER_API_ENDPOINT "$CF_HOOVER_API_ENDPOINT"
        cf set-env cf-kaizen-hoover-server SPRING_PROFILES_ACTIVE "default,cloud"
        cf set-health-check cf-kaizen-hoover-server http --endpoint /actuator/health --invocation-timeout 180
        cf start cf-kaizen-hoover-server

        Write-Host "-- Readying a GenAI service instance"
        cf create-service genai $GENAI_CHAT_PLAN_NAME $GENAI_CHAT_SERVICE_NAME

        if ($Flag -eq "--pre-built") {
            $ARTIFACT_NAME = "cf-kaizen-butler-client-openai-$VERSION.jar"
        }
        else {
            $ARTIFACT_NAME = "cf-kaizen-butler-frontend-$VERSION.jar"
            $ARTIFACT_HOME = "$env:TEMP\cf-kaizen\clients\butler"
        }

        Set-Location -Path $ARTIFACT_HOME
        cf push cf-kaizen-butler-frontend -m 1G -k 512M -p "target\$ARTIFACT_NAME" -s cflinuxfs4 --no-start
        Set-CFEnvVars -AppName "cf-kaizen-butler-frontend"
        cf set-env cf-kaizen-butler-frontend SPRING_PROFILES_ACTIVE "default,cloud,openai"
        cf set-env cf-kaizen-butler-frontend CF_KAIZEN_BUTLER_SERVER_URL $CF_KAIZEN_BUTLER_SERVER_URL
        cf bind-service cf-kaizen-butler-frontend $GENAI_CHAT_SERVICE_NAME
        cf set-health-check cf-kaizen-butler-frontend http --endpoint /actuator/health --invocation-timeout 180
        cf start cf-kaizen-butler-frontend

        if ($Flag -eq "--pre-built") {
            $ARTIFACT_NAME = "cf-kaizen-hoover-client-openai-$VERSION.jar"
        }
        else {
            $ARTIFACT_NAME = "cf-kaizen-hoover-frontend-$VERSION.jar"
            $ARTIFACT_HOME = "$env:TEMP\cf-kaizen\clients\hoover"
        }

        Set-Location -Path $ARTIFACT_HOME
        cf push cf-kaizen-hoover-frontend -m 1G -k 512M -p "target\$ARTIFACT_NAME" -s cflinuxfs4 --no-start
        Set-CFEnvVars -AppName "cf-kaizen-hoover-frontend"
        cf set-env cf-kaizen-hoover-frontend SPRING_PROFILES_ACTIVE "default,cloud,openai"
        cf set-env cf-kaizen-hoover-frontend CF_KAIZEN_HOOVER_SERVER_URL $CF_KAIZEN_HOOVER_SERVER_URL
        cf bind-service cf-kaizen-hoover-frontend $GENAI_CHAT_SERVICE_NAME
        cf set-health-check cf-kaizen-hoover-frontend http --endpoint /actuator/health --invocation-timeout 180
        cf start cf-kaizen-hoover-frontend
    }

    "destroy" {
        Write-Host "-- Deleting all application and service instances"
        cf target -o $KAIZEN_ORG -s $KAIZEN_SPACE
        cf unbind-service cf-kaizen-butler-frontend $GENAI_CHAT_SERVICE_NAME
        cf unbind-service cf-kaizen-hoover-frontend $GENAI_CHAT_SERVICE_NAME
        cf delete-service $GENAI_CHAT_SERVICE_NAME -f
        $APPS = @("cf-kaizen-hoover-frontend", "cf-kaizen-butler-frontend", "cf-kaizen-hoover-server", "cf-kaizen-butler-server")
        foreach ($app in $APPS) {
            cf delete "$app" -r -f
        }

        cf target -o $OBSERVABILITY_ORG -s $OBSERVABILITY_SPACE
        cf unbind-service cf-hoover cf-hoover-config
        cf delete-service cf-hoover-config -f
        cf delete cf-hoover -r -f
        cf delete cf-butler -r -f

        cf target -o $SAMPLE_APP_ORG -s $SAMPLE_APP_SPACE
        cf delete $SAMPLE_APP_NAME -r -f
    }

    "download-artifacts" {
        Set-Location -Path "$env:TEMP\cf-kaizen"
        if (-not (Test-Path "target")) { New-Item -Path "target" -ItemType Directory -Force | Out-Null }
        Write-Host "-- Fetching latest available cf-kaizen artifacts from Github Packages repository"
        gh release download --pattern "*.jar" -D target --skip-existing
        $RELEASE = Determine-JarRelease
        if ($null -eq $RELEASE) {
            Write-Host "Error: Could not determine release version."
            Exit 1
        }
        Write-Host "-- Latest version available is $RELEASE"
    }

    default {
        Write-Host "`n`e[31m⏹  Usage: .\deploy-on-tp4cf.ps1 <authenticate|provision|deprovision|clone|build|deploy-sample-app|deploy-observability|deploy-kaizen|destroy|download-artifacts> [y]`e[m`n"
    }
}

# END SCRIPT
# --------------------------------------------------------------------------------