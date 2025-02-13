# cf-kaizen

## Getting started with Korifi

> [!NOTE]
> This is a scratchpad!  Here is where we will trial and iterate upon the steps necessary to get a Korifi installation on a Kind cluster enabling experimental UAA support integrating with Github OIDC.

### Background

Crafted a prompt and asked Google AI Studio for help with authoring step-by-step instructions.

What follows is a starting point.  Of course, we will want to test this out and refine the instructions to make them clear(er). 

The end goal is to add a link to a final functional revision of these instructions in [KORIFI.md](KORIFI.md)

### Installation with Github OIDC for UAA

These steps outline how to configure Korifi to use a GitHub OIDC provider for UAA integration. This allows users to authenticate with their GitHub accounts when using the `cf login` command.

1.  **Register a New OIDC Application in GitHub:**

    *   Navigate to your GitHub organization or personal account settings.
    *   Go to **Developer settings** -> **OAuth Apps** and click on **Register a new application**.
    *   Fill in the required details:
        *   **Application name:** A descriptive name for your Korifi integration (e.g., "Korifi UAA OIDC").
        *   **Homepage URL:** The URL of your Korifi API server (e.g., `https://api.korifi.example.org`).
        *   **Authorization callback URL:**  The callback URL for your UAA instance. This URL should be in the format `<UAA_URL>/oauth/callback`.  For example: `https://uaa.korifi.example.org/oauth/callback`.
    *   Click **Register application**.
    *   Note the **Client ID** and generate a new **Client Secret**.  You'll need these later.

2.  **Configure Kubernetes OIDC Authentication:**

    *   You need to configure your Kubernetes cluster to trust the GitHub OIDC provider.  The specific steps vary depending on your Kubernetes distribution.  Here's a general example for a Kind cluster, which you'll need to adapt to your environment. This configuration is applied during cluster creation. Add the below to your Kind config and update values that apply to your environment.

        ```yaml
        kind: Cluster
        apiVersion: kind.x-k8s.io/v1alpha4
        nodes:
        - role: control-plane
          kubeadmConfigPatches:
          - |
            kind: ClusterConfiguration
            apiServer:
              extraArgs:
                oidc-issuer-url: https://token.actions.githubusercontent.com # or your Github Enterprise Server URL
                oidc-client-id: YOUR_GITHUB_OIDC_CLIENT_ID # Replace with your Client ID
                oidc-username-claim: sub
                oidc-groups-claim: groups # Optional, if you want to map GitHub teams to Kubernetes groups
        ```

    *   **Replace `YOUR_GITHUB_OIDC_CLIENT_ID` with the Client ID you obtained in step 1.**  If you are using a Github Enterprise server you must replace the `oidc-issuer-url` with your instance URL.

    *   Apply the Kind config when creating your cluster:

        ```bash
        kind create cluster --config kind-config.yaml
        ```

3. **Configure UAA with GitHub OIDC:**
   Since UAA doesn't natively support Github OIDC, you can implement a custom External IDP (Identity Provider).
    * Deploy an OAuth2/OIDC shim that can transform Github's OIDC protocol to what UAA expects.
    * There are two ways of achieving this:
        *  Deploy a generic OIDC-to-OAuth2 shim between GitHub and UAA and configure UAA to point to the shim.  This approach is more complex.
        *  Create a simplified UAA endpoint that authenticates against GitHub OIDC. This would essentially extend the UAA with a new endpoint, offering another sign-in way.

4.  **Configure Korifi Helm Chart:**

    *   Enable UAA support and configure the UAA URL in your `values.yaml` file or via `--set` flags when installing the Helm chart:

        ```bash
        helm install korifi https://github.com/cloudfoundry/korifi/releases/download/v<VERSION>/korifi-<VERSION>.tgz \
            --namespace="$KORIFI_NAMESPACE" \
            --set=generateIngressCertificates=true \
            --set=rootNamespace="$ROOT_NAMESPACE" \
            --set=adminUserName="$ADMIN_USERNAME" \
            --set=api.apiServer.url="api.$BASE_DOMAIN" \
            --set=defaultAppDomainName="apps.$BASE_DOMAIN" \
            --set=containerRepositoryPrefix=europe-docker.pkg.dev/my-project/korifi/ \
            --set=kpackImageBuilder.builderRepository=europe-docker.pkg.dev/my-project/korifi/kpack-builder \
            --set=networking.gatewayClass=$GATEWAY_CLASS_NAME \
            --set=experimental.uaa.enabled=true \
            --set=experimental.uaa.url="<UAA_URL>" \
            --wait
        ```

        **Replace `<UAA_URL>` with the actual URL of your UAA instance.**

5.  **Configure Admin User Role Binding:**

    *   Create a `RoleBinding` for your GitHub user to grant them admin privileges in Korifi.  You'll need the user's GitHub username. Note that this needs to be prefixed as specified in the kind cluster config.

        ```bash
        kubectl apply -f - <<EOF
        apiVersion: rbac.authorization.k8s.io/v1
        kind: RoleBinding
        metadata:
          annotations:
            cloudfoundry.org/propagate-cf-role: "true"
          name: github-user-admin-binding
          namespace: "$ROOT_NAMESPACE"
        roleRef:
          apiGroup: rbac.authorization.k8s.io
          kind: ClusterRole
          name: korifi-controllers-admin
        subjects:
        - apiGroup: rbac.authorization.k8s.io
          kind: User
          name: github:<github-username> # Replace with your GitHub username
        EOF
        ```

        **Replace `<github-username>` with your GitHub username.**

6.  **Login with the CF CLI:**

    *   Now, when you run `cf login`, the CLI will redirect you to GitHub for authentication.
    *   After successful authentication, you'll be redirected back to the CLI, and you'll be logged in to Korifi using your GitHub identity.

        ```bash
        cf api https://api.korifi.example.org --skip-ssl-validation
        cf login
        ```

7. **Granting roles for Users using Github OIDC for UAA**

When configuring user roles via the `cf cli`, the OIDC prefix (origin) must be specified as `github`:

```bash
cf set-space-role "your-github-username" org space SpaceDeveloper --origin github
```