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

### Installation

Follow these [instructions](https://github.com/cloudfoundry/korifi/blob/main/INSTALL.md).

> [!TIP]
> Arguably the simplest thing to do is to launch a Kind cluster.  If you choose that path, follow these [instructions](https://github.com/cloudfoundry/korifi/blob/main/INSTALL.kind.md).
> When you're ready to "test Korifi" with a cf push, choose to push a Docker app, like nginx.

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

Since Korifi does not have marketplace service offerings for Spring Cloud Config Server and Spring Cloud Service Registry, we will configure and deploy each as application instances.

TBD

#### of cf-toolsuite applications

TBD

### Configuring Claude Desktop

TBD 