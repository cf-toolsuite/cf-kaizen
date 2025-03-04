# cf-kaizen

## Butler MCP server implementation

Facilitates read-and-write operations via one of more cf-butler instance(s) installed on a target Cloud Foundry foundation.

### Prerequisites

* one or more [cf-butler](https://github.com/cf-toolsuite/cf-butler) instance(s) deployed 
* the following CLIs/SDKs:
  * git
  * gh
  * java
  * mvn
  * (optionally) [sdk](https://sdkman.io/)
    * it might be the easiest way to install the Java SDK and Maven

### Building

```bash
cd /tmp
gh repo clone cf-toolsuite/cf-kaizen
cd cf-kaizen
mvn install
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
    "-Ddefault.url=https://cf-butler.apps.dhaka.cf-app.com",
    "<path-to-.m2-home>/repository/org/cftoolsuite/cfapp/cf-kaizen-butler-server/0.0.1-SNAPSHOT/cf-kaizen-butler-server-0.0.1-SNAPSHOT.jar"
  ]
}
```

> [!IMPORTANT]
> Replace <path-to.m2-home> above with $HOME/.m2 when on Linux or MacOS and %USERPROFILE%\.m2 when on Windows.  Evaluate the path options mentioned and be sure to replace with an absolute path.
> And if you're targeting either one or multiple foundations; if multiple, copy-paste and comma-separate the stanza above, then replace each value of the default.url.