# cf-kaizen

## Getting started with Cloud Foundry

### Prerequisites

* a Cloud Foundry foundation
* the following CLIs/SDKs:
  * git
  * gh
  * java
  * mvn
  * gradle
  * (optionally) [sdk](https://sdkman.io/)
    * it might be the easiest way to install the Java SDK, Maven and Gradle

### Building MCP servers

```bash
cd /tmp
gh repo clone cf-toolsuite/cf-kaizen
cd cf-kaizen
mvn install
```

### Preparation

Target a foundation

```bash
cf api {cloud_foundry_foundation_api_endpoint}
```

> Replace `{cloud_foundry_foundation_api_endpoint}` above with an API endpoint

Sample interaction

```bash
cf api api.sys.dhaka.cf-app.com
```

Authenticate with single sign-on

```bash
cf login --sso
```

or with a username and password

```bash
cf login -u {username} -p "{password}"
```

> Replace `{username}` and `{password}` above respectively with your account's username and password.

### Deployment

Set these environment variables:

```bash
export CF_ORG=kaizen
export CF_SPACE=dev
```

Create the organization, the space within it, then target that organization and space:

```bash
cf create-org $CF_ORG && cf create-space $CF_SPACE -o $CF_ORG && cf target -o $CF_ORG -s $CF_SPACE
```

#### of sample application

```bash
cd /tmp
gh repo clone fastnsilver/primes
cd primes
git checkout 3.4
gradle build
cf push primes -m 1G -p ./build/libs/primes-1.0-SNAPSHOT.jar -s cflinuxfs4 --no-start
cf set-env primes JAVA_OPTS '-Djava.security.egd=file:///dev/urandom'
cf set-env primes JBP_CONFIG_OPEN_JDK_JRE '{ jre: { version: 21.+ } }'
cf set-env primes JBP_CONFIG_SPRING_AUTO_RECONFIGURATION '{ enabled: false }'
cf start primes
```

#### of cf-toolsuite applications

cf-butler 

```bash
cd /tmp
gh repo clone cf-toolsuite/cf-butler
cd cf-butler
mvn package
cf push cf-butler --no-start
cf create-service credhub default cf-butler-secrets -p /tmp/cf-kaizen/config/secrets.butler.json
cf bind-service cf-butler cf-butler-secrets
cf start cf-butler
```

cf-hoover

```bash
cd /tmp
gh repo clone cf-toolsuite/cf-hoover
cd cf-hoover
mvn package
cf push cf-hoover --no-start
cf create-service p.config-server standard cf-hoover-config -c config/config-server.json
while [[ $(cf service cf-hoover-config) != *"succeeded"* ]]; do
  echo "cf-hoover-config is not ready yet..."
  sleep 5
done
cf bind-service cf-hoover cf-hoover-config
cf start cf-hoover
```

### Configuring Claude Desktop

Now we need to configure Claude Desktop.
Launch the desktop.
From the File menu, choose Settings.
Click on the Developer tab in the left-hand navigation pane.
Click on the Edit Config button.
Open the file named `claude_desktop_config.json` for editing in your favorite text editor,
then insert the following stanzas within `"mcpServers": {}`.
Save your update.
Close and re-launch Claude Desktop for the update to take effect.
Validate that the additional tools are present before crafting and executing your next prompt.

```json
"cf-kaizen-butler-client": {
  "command": "java",
  "args": [
    "-jar",
    "-Ddefault.url=cf-butler.apps-127-0-0-1.nip.io",
    "<path-to-.m2-home>/repository/org/cftoolsuite/cfapp/cf-kaizen-butler-client/0.0.1-SNAPSHOT/cf-kaizen-butler-client-0.0.1-SNAPSHOT.jar"
  ]
},
"cf-kaizen-hoover-client": {
  "command": "java",
  "args": [
    "-jar",
    "-Ddefault.url=cf-hoover.apps-127-0-0-1.nip.io",
    "<path-to-.m2-home>/repository/org/cftoolsuite/cfapp/cf-kaizen-hoover-client/0.0.1-SNAPSHOT/cf-kaizen-hoover-client-0.0.1-SNAPSHOT.jar""
  ]
}
```

> [!IMPORTANT]
> Replace <path-to.m2-home> above with $HOME/.m2 when on Linux or MacOS and %USERPROFILE%\.m2 when on Windows.  Evaluate the path options mentioned and be sure to replace with an absolute path. 