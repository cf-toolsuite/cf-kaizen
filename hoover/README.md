# cf-kaizen

## Hoover MCP server implementation

Facilitates read-only operations via a cf-hoover instance installed on a target Cloud Foundry foundation.

### Prerequisites

* [cf-hoover](https://github.com/cf-toolsuite/cf-hoover) instance deployed 
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
"cf-kaizen-hoover-client": {
  "command": "java",
  "args": [
    "-jar",
    "-Ddefault.url=https://cf-hoover.apps.dhaka.cf-app.com",
    "<path-to-.m2-home>/repository/org/cftoolsuite/cfapp/cf-kaizen-hoover-server/0.0.1-SNAPSHOT/cf-kaizen-hoover-server-0.0.1-SNAPSHOT.jar"
  ]
}
```

> [!IMPORTANT]
> Replace <path-to.m2-home> above with $HOME/.m2 when on Linux or MacOS and %USERPROFILE%\.m2 when on Windows.  Evaluate the path options mentioned and be sure to replace with an absolute path.
> And if you're targeting a different foundation; replace the value for the default.url arg above.