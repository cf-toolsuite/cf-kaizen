# cf-kaizen

[![GA](https://img.shields.io/badge/Release-Alpha-darkred)](https://img.shields.io/badge/Release-Alpha-darkred) ![Github Action CI Workflow Status](https://github.com/cf-toolsuite/cf-kaizen/actions/workflows/ci.yml/badge.svg) [![Known Vulnerabilities](https://snyk.io/test/github/cf-toolsuite/cf-kaizen/badge.svg?style=plastic)](https://snyk.io/test/github/cf-toolsuite/cf-kaizen)

This multi-module project hosts a client code-generated from an OpenAPI derivative of the [cf-butler](https://github.com/cf-toolsuite/cf-butler/blob/main/docs/ENDPOINTS.md) and [cf-hoover](https://github.com/cf-toolsuite/cf-hoover/blob/main/docs/ENDPOINTS.md) APIs combined with a Spring AI implementation.
It also includes an MCP server, MCP client configuration for use with Claude desktop.

* [Background](#background)
* [Getting started](#getting-started)
* [Prerequisites](#prerequisites)
* How to
    * [Clone](#how-to-clone)
    * [Build](#how-to-build)
    * [Consume](#how-to-consume)
    * [Run](#how-to-run)

## Background

As a Spring Boot and Spring AI developer, I want
to consume libraries that make it convenient to add capabilities to my application(s)
as for the following

Use-case:

* Imagine a natural language interaction with one or more Cloud Foundry foundations without explicitly having to be aware of the Cloud Foundry APIs

## Getting started

Start with:

* A Github [account](https://github.com/signup)
* Endpoints for
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
    <artifactId>cf-kaizen-butler-client</artifactId>
    <version>{release-version}</version>
</dependency>
```

or

```maven
<dependency>
    <groupId>org.cftoolsuite</groupId>
    <artifactId>cf-kaizen-hoover-client</artifactId>
    <version>{release-version}</version>
</dependency>
```

Gradle

```gradle
implementation 'org.cftoolsuite:cf-kaizen-butler-client:{release-version}'
```

or

```gradle
implementation 'org.cftoolsuite:cf-kaizen-hoover-client:{release-version}'
```

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

## How to run

You're going to need to know of one or more endpoints for cf-butler and/or cf-hoover application instance(s).


Then you'll want to integrate one or multiple clients with Claude desktop via MCP client configuration that will consume an MCP server implementation.

Follow these instructions.

Add the following stanza(s) to a file called `claude_desktop_config.json`:

```json
"cf-kaizen-butler-client": {
  "command": "java",
  "args": [
    "-jar",
    "-Ddefault.url=<cf-butler-application-instance-endpoint-api>",
    "<path-to-project>/target/cf-kaizen-butler-client-0.0.1-SNAPSHOT.jar"
  ]
}
```

or 

```json
"cf-kaizen-hoover-client": {
  "command": "java",
  "args": [
    "-jar",
    "-Ddefault.url=<cf-hoover-application-instance-endpoint-api>",
    "<path-to-project>/target/cf-kaizen-hoover-client-0.0.1-SNAPSHOT.jar"
  ]
}
```

> Replace occurrences of `<path-to-project>` and `<cf-kaizen-*-application-instance-endpoint-api>` above with appropriate values

Restart Claude Desktop instance.
Verify that you have a new set of tool calls available.
Chat with Claude.
