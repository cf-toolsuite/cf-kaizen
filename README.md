# cf-kaizen

[![GA](https://img.shields.io/badge/Release-Alpha-darkred)](https://img.shields.io/badge/Release-Alpha-darkred) ![Github Action CI Workflow Status](https://github.com/cf-toolsuite/cf-kaizen/actions/workflows/ci.yml/badge.svg) [![Known Vulnerabilities](https://snyk.io/test/github/cf-toolsuite/cf-kaizen/badge.svg?style=plastic)](https://snyk.io/test/github/cf-toolsuite/cf-kaizen) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This multi-module project hosts clients code-generated from an OpenAPI derivative of the [cf-butler](https://github.com/cf-toolsuite/cf-butler/blob/main/docs/ENDPOINTS.md) and [cf-hoover](https://github.com/cf-toolsuite/cf-hoover/blob/main/docs/ENDPOINTS.md) APIs combined with a Spring AI implementation.
It also includes two MCP servers and MCP client configuration for use with Claude desktop.

* [Background](#background)
* [Getting started](#getting-started)
* [Prerequisites](#prerequisites)
* How to
    * [Clone](#how-to-clone)
    * [Build](#how-to-build)
    * [Consume](#how-to-consume)
    * Integrate w/ cf-butler and cf-hoover hosted on
      * [Cloud Foundry](docs/CF.md)
      * [Korifi](docs/KORIFI.md) (under development)
    * Run with
      * [Claude Desktop](docs/CLAUDE.md)
      * [LibreChat](docs/LIBRECHAT.md)

## Background

As a Spring Boot and Spring AI developer, I want
to consume libraries that make it convenient to add capabilities to my application(s)
as for the following

Use-case:

* Imagine a natural language interaction with one or more Cloud Foundry foundations without explicitly having to be aware of the Cloud Foundry APIs

## Getting started

Start with:

* A Github [account](https://github.com/signup)
* API endpoints for
  * one or more application instances of cf-butler
  * an application instance of cf-hoover

## Prerequisites

* Git CLI (2.43.0 or better)
* Github CLI (2.65.0 or better)
* Java SDK (21 or better)
* Maven (3.9.9 or better)
* Claude desktop

## How to clone

with Git CLI

```bash
git clone https://github.com/cf-toolsuite/cf-kaizen
```

with Github CLI

```bash
gh repo clone cf-toolsuite/cf-kaizen
```

## How to build

Open a terminal shell, then execute:

```bash
cd cf-kaizen
./mvnw clean install
```

## How to consume

If you want to incorporate any of the starters as dependencies in your own projects, you would:

### Add dependency

Maven

```maven
<dependency>
    <groupId>org.cftoolsuite</groupId>
    <artifactId>cf-kaizen-butler-server</artifactId>
    <version>{release-version}</version>
</dependency>
```

or

```maven
<dependency>
    <groupId>org.cftoolsuite</groupId>
    <artifactId>cf-kaizen-hoover-server</artifactId>
    <version>{release-version}</version>
</dependency>
```

Gradle

```gradle
implementation 'org.cftoolsuite:cf-kaizen-butler-server:{release-version}'
```

or

```gradle
implementation 'org.cftoolsuite:cf-kaizen-hoover-server:{release-version}'
```

> [!IMPORTANT]
> Replace occurrences of {release-version} above with a valid artifact release version number

### Add configuration

Following Spring Boot conventions, you would add a stanza like this to your:

application.properties

```properties
default.url=${CF_BUTLER_API_ENDPOINT:}
```

or

```properties
default.url=${CF_HOOVER_API_ENDPOINT:}
```

application.yml

```yaml
default:
  url: ${CF_BUTLER_API_ENDPOINT:}
```

or

```yaml
default:
  url: ${CF_HOOVER_API_ENDPOINT:}
```
