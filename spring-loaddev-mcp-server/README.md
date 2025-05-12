# Spring Load Development MCP Server

An MCP server for Spring Load Development project.

## Configuring Copilot Connection

To connect GitHub Copilot to this server, you need to configure the `.vscode/mcp.json` file in your project directory:

1. Create an `mcp.json` file inside the `.vscode` folder with the following structure:

```json
{
  "servers": {
    "reloading-mcp-server": {
      "url": "http://localhost:8083/"
    }
  }
}
```