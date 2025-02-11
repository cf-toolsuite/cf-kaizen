# cf-kaizen

## Getting started with Korifi

### Prerequisites

* a [Kubernetes-conformant](https://www.cncf.io/training/certification/software-conformance/) cluster
* the following CLIs/SDKs:
  * git
  * gh
  * helm
  * kubectl
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

### Installation

Solely following these [instructions](https://github.com/cloudfoundry/korifi/blob/main/INSTALL.md) would be great, but we also have to weave in [experimental support for UAA](https://github.com/cloudfoundry/korifi/blob/main/docs/experimental-uaa-authentication.md). 

> [!TIP]
> Arguably the simplest thing to do is to launch a Kind cluster.  If you choose that path, follow these [instructions](https://github.com/cloudfoundry/korifi/blob/main/INSTALL.kind.md).
> When you're ready to "test Korifi" with a cf push, choose to push a Docker app, like nginx.

// TODO Author step-by-step instructions for standing up Korifi integrating Github OIDC credentials.  

### Deployment

Set these environment variables:

```bash
export CF_ORG=zoolabs
export CF_SPACE=dev
```

Create the organization, the space within it, then target that organization and space:

```bash
cf create-org $CF_ORG && cf create-space -o $CF_ORG $SPACE && cf target -o $CF_ORG -s $CF_SPACE
```

#### of sample application

```bash
cd /tmp
gh repo clone fastnsilver/primes
cd primes
git checkout 3.4
cf push primes
http --verify=no https://primes.apps-127-0-0-1.nip.io/primes/37/99
```

#### of infrastructure services

Since Korifi does not have a Marketplace service offering for [Spring Cloud Config Server](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_quick_start), we will configure and deploy it as an application instance.

You built the MCP servers, right?

```bash
cd /tmp/cf-kaizen/support
cd config-server
cf push config-server --no-start
cf set-env config-server EUREKA_CLIENT_REGISTER_WITH_EUREKA false
cf set-env config-server EUREKA_CLIENT_FETCH_REGISTRY false
cf set-env config-server JAVA_OPTS '-XX:+UseG1GC -XX:+UseStringDeduplication'
cf create-user-provided-service github-repository-target -p /tmp/cf-kaizen/config/secrets.hoover.json
cf bind-service config-server github-repository-target
cf start config-server
```

#### of cf-toolsuite applications

> [!WARNING]
> This has NOT been tested! Some further research, diagnosis, and troubleshooting may be required.

cf-butler 

```bash
cd /tmp
gh repo clone cf-toolsuite/cf-butler
cd cf-butler
rm -f manifest.yml
cf push cf-butler --no-start
cf create-user-provided-service cf-butler-secrets -p /tmp/cf-kaizen/config/secrets.butler.json
cf bind-service cf-butler cf-butler-secrets
cf set-env cf-butler JAVA_OPTS '-Djava.security.egd=file:///dev/urandom -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1 -XX:+UseStringDeduplication -XX:MaxDirectMemorySize=1G'
cf set-env cf-butler SPRING_PROFILES_ACTIVE 'on-demand,cloud'
cf start cf-butler
```

cf-hoover

```bash
cd /tmp
gh repo clone cf-toolsuite/cf-hoover
cd cf-hoover
rm -f manifest.yml
cf set-env cf-hoover SPRING_CONFIG_IMPORT 'optional:configserver:https://config-server.apps-127-0-0-1.nip.io'
cf push cf-hoover --no-start
cf set-env cf-hoover JAVA_OPTS '-Djava.security.egd=file:///dev/urandom -XX:+UseG1GC -XX:+UseStringDeduplication'
cf set-env cf-hoover SPRING_PROFILES_ACTIVE 'on-demand,cloud'
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