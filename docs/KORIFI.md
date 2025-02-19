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
  * yq

### Building MCP servers

```bash
cd /tmp
gh repo clone cf-toolsuite/cf-kaizen
cd cf-kaizen
mvn install
```

### Installation

Navigate to your GitHub Organization or Personal Account **Settings**.

Go to **Developer settings** &#x226B; **OAuth Apps** and click on the **New OAuth app** button. 

Fill in the required details:

| Key               | Value |
|-------------------|-------|
| Application name  | Korifi UAA OIDC |
| Homepage URL | https://localhost |
| Authorization callback URL |  http://uaa.uaa.svc.cluster.local:8080/oauth/callback |

Click **Register application**.

Note the **Client ID** and generate a new **Client Secret**.

Configure and launch a new Kind cluster.

> [!IMPORTANT]
> Set the value of the GITHUB_OIDC_CLIENT_ID environment variable before attempting to launch Kind

```bash
export GITHUB_OIDC_CLIENT_ID=
cat <<EOF | kind create cluster --name korifi --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
containerdConfigPatches:
- |-
  [plugins."io.containerd.grpc.v1.cri".registry]
    [plugins."io.containerd.grpc.v1.cri".registry.mirrors]
      [plugins."io.containerd.grpc.v1.cri".registry.mirrors."localregistry-docker-registry.default.svc.cluster.local:30050"]
        endpoint = ["http://127.0.0.1:30050"]
    [plugins."io.containerd.grpc.v1.cri".registry.configs]
      [plugins."io.containerd.grpc.v1.cri".registry.configs."127.0.0.1:30050".tls]
        insecure_skip_verify = true
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: ClusterConfiguration
    apiServer:
      extraArgs:
        oidc-issuer-url: "https://token.actions.githubusercontent.com"
        oidc-client-id: "${GITHUB_OIDC_CLIENT_ID}"
        oidc-username-claim: sub
  extraPortMappings:
  - containerPort: 32080
    hostPort: 80
    protocol: TCP
  - containerPort: 32443
    hostPort: 443
    protocol: TCP
  - containerPort: 30050
    hostPort: 30050
    protocol: TCP
EOF
```

Install Korifi with experimental UAA support enabled.

> [!IMPORTANT]
> Set the value of the ADMIN_PASSWORD environment variable before attempting to install Korifi

```bash
export ADMIN_PASSWORD=
curl -LO https://raw.githubusercontent.com/cf-toolsuite/cf-kaizen/refs/heads/main/korifi/kind-local/install-korifi-kind-w-uaa-enabled.yaml
sed -i "s|client_secret: ADMIN_PASSWORD|client_secret: \"$ADMIN_PASSWORD\"|" install-korifi-kind-w-uaa-enabled.yaml
kubectl apply -f install-korifi-kind-w-uaa-enabled.yaml
```

If you want to track each job's progress, run:

```bash
kubectl -n korifi-installer logs --follow job/install-uaa
```

or

```bash
kubectl -n korifi-installer logs --follow job/install-korifi
```

(Optional) After the job is complete, you can delete the `korifi-installer` namespace with:

```bash
kubectl delete namespace korifi-installer
```

Configure the Admin User Role Binding.

> [!IMPORTANT]
> Set the value of the GITHUB_USERNAME environment variable before attempting to apply the RoleBinding
> 
```bash
export GITHUB_USERNAME=
kubectl apply -f - <<EOF
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  annotations:
    cloudfoundry.org/propagate-cf-role: "true"
  name: github-user-admin-binding
  namespace: cf
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: korifi-controllers-admin
subjects:
- apiGroup: rbac.authorization.k8s.io
  kind: User
  name: github:${GITHUB_USERNAME}
EOF
```

### Authentication

```bash
cf api https://localhost --skip-ssl-validation
cf login
```

> When you run `cf login`, the CLI will redirect you to GitHub for authentication.
> After successful authentication, you'll be redirected back to the CLI, and you'll be logged in to Korifi using your GitHub identity.

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

Grant role for User using GitHub OIDC for UAA:

```bash
cf set-space-role $GITHUB_USERNAME $CF_ORG $CF_SPACE SpaceDeveloper --origin github
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
cf create-user-provided-service github-repository-target -p /tmp/cf-kaizen/config/secrets.hoover-on-korifi.json
cf bind-service config-server github-repository-target
cf start config-server
```

#### of cf-toolsuite applications

cf-butler 

```bash
cd /tmp
gh repo clone cf-toolsuite/cf-butler
cd cf-butler
rm -f manifest.yml
cf push cf-butler --no-start
cf create-user-provided-service cf-butler-secrets -p /tmp/cf-kaizen/config/secrets.butler-on-korifi.json
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
cf set-env cf-hoover SPRING_CLOUD_DISCOVERY_ENABLED false
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
    "-Ddefault.url=https://cf-butler.apps-127-0-0-1.nip.io",
    "<path-to-.m2-home>/repository/org/cftoolsuite/cfapp/cf-kaizen-butler-client/0.0.1-SNAPSHOT/cf-kaizen-butler-client-0.0.1-SNAPSHOT.jar"
  ]
},
"cf-kaizen-hoover-client": {
  "command": "java",
  "args": [
    "-jar",
    "-Ddefault.url=https://cf-hoover.apps-127-0-0-1.nip.io",
    "<path-to-.m2-home>/repository/org/cftoolsuite/cfapp/cf-kaizen-hoover-client/0.0.1-SNAPSHOT/cf-kaizen-hoover-client-0.0.1-SNAPSHOT.jar"
  ]
}
```

> [!IMPORTANT]
> Replace <path-to.m2-home> above with $HOME/.m2 when on Linux or MacOS and %USERPROFILE%\.m2 when on Windows.  Evaluate the path options mentioned and be sure to replace with an absolute path. 