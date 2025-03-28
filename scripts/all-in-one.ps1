# PowerShell Script
# This script is supplemental to deploy-on-tp4cf.ps1
# It deploys the following applications to a configured organization and space

# * cf-kaizen-butler-server
# * cf-kaizen-hoover-server
# * cf-kaizen-butler-frontend
# * cf-kaizen-hoover-frontend

# --------------------------------------------------------------------------------
# ENVIRONMENT VARIABLES
# Update values here as you like

# -- If Single sign-on is enabled then set $IS_SSO="y"
$IS_SSO = ""
$ORGANIZATION = "cf-toolsuite"
$SPACE = "observability"
$CF_API_ENDPOINT = "https://api.sys.tas-ndc.kuhn-labs.com"
$CF_APPS_DOMAIN = "app.tas-ndc.kuhn-labs.com"
$LLM_SERVICE_NAME = "kaizen-llm"
# -- Must be an available service plan (@see cf m -e genai)
$LLM_PLAN_NAME = "mistral-nemo:12b-instruct-2407-q4_K_S"
$FOUNDATION = "kuhn-labs"

# --------------------------------------------------------------------------------
# CAUTION: DO NOT CHANGE ANYTHING BELOW UNLESS YOU KNOW WHAT YOU'RE DOING

function Tool-Checks {
    Write-Host "-- Checking whether or not required tools are installed"

    # Check if ytt is installed
    try {
        $null = Get-Command ytt -ErrorAction Stop
    }
    catch {
        Write-Host "Error: ytt is not installed. Please install it first."
        Write-Host "Windows: Download from https://carvel.dev/install"
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

    # Get all jar files safely
    $files = Get-ChildItem -Path "target" -Filter "*.jar" -File -ErrorAction SilentlyContinue
    
    if ($null -eq $files -or $files.Count -eq 0) {
        Write-Host "No files found in target directory."
        return $null
    }

    # Process each file individually for better error handling and robustness
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

$ORIGINAL_MANIFEST = "config/manifest.default.yml"
$NEW_MANIFEST = "config/manifest.$FOUNDATION.yml"

# --------------------------------------------------------------------------------
# START SCRIPT

Tool-Checks

# Create directories if they don't exist
if (-not (Test-Path "deploy")) { New-Item -Path "deploy" -ItemType Directory | Out-Null }
if (-not (Test-Path "dist")) { New-Item -Path "dist" -ItemType Directory | Out-Null }

Write-Host "-- Fetching latest available cf-kaizen artifacts from Github Packages repository"
gh release download --repo cf-toolsuite/cf-kaizen --pattern "*.jar" -D dist --skip-existing
$ARTIFACT_VERSION = Determine-JarRelease
if ($null -eq $ARTIFACT_VERSION) {
    Write-Host "Error: Could not determine release version."
    Exit 1
}
Write-Host "-- Latest version available is $ARTIFACT_VERSION"

# Make a copy of the original manifest and populate with correct information
Write-Host "-- Updating manifest"
ytt -f $ORIGINAL_MANIFEST -v "llm_service_name=$LLM_SERVICE_NAME" -v "cf_apps_domain=$CF_APPS_DOMAIN" -v "artifact_version=$ARTIFACT_VERSION" | Out-File -FilePath $NEW_MANIFEST -Encoding utf8

Write-Host "-- Manifest updated successfully!"

Write-Host "-- Authenticating"
cf api "$CF_API_ENDPOINT" --skip-ssl-validation
if ($IS_SSO) {
    cf login --sso
}
else {
    cf login
}

Write-Host "-- Creating organizations and spaces"
cf create-org $ORGANIZATION
cf create-space "$SPACE" -o "$ORGANIZATION"

Write-Host "-- Targeting $ORGANIZATION/$SPACE"
cf target -o "$ORGANIZATION" -s "$SPACE"

Write-Host "-- Creating LLM service instance"
cf create-service genai $LLM_PLAN_NAME $LLM_SERVICE_NAME

Write-Host "-- Copying cf-kaizen artifacts into place"
# Create directories
$directories = @(
    "butler/target",
    "hoover/target",
    "clients/butler/target",
    "clients/hoover/target"
)

foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -Path $dir -ItemType Directory -Force | Out-Null
    }
}

# Copy files
Copy-Item "dist/cf-kaizen-butler-server-$ARTIFACT_VERSION.jar" "butler/target"
Copy-Item "dist/cf-kaizen-hoover-server-$ARTIFACT_VERSION.jar" "hoover/target"
Copy-Item "dist/cf-kaizen-butler-frontend-$ARTIFACT_VERSION.jar" "clients/butler/target"
Copy-Item "dist/cf-kaizen-hoover-frontend-$ARTIFACT_VERSION.jar" "clients/hoover/target"

Write-Host "-- Deploying applications"
cf push -f $NEW_MANIFEST

# END SCRIPT
# --------------------------------------------------------------------------------