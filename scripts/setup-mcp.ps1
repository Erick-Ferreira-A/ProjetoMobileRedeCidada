# Registra o MCP remoto do Figma no Codex (recomendado pela documentação do Figma).
# Fonte: https://developers.figma.com/docs/figma-mcp-server/remote-server-installation/
# Não precisa do app desktop: a autenticação é por OAuth na sua conta Figma.

codex mcp add figma --url https://mcp.figma.com/mcp
codex mcp login figma   # abre o navegador: clique em "Allow access"

# GitHub (opcional): o 'gh' já cobre repositórios e PRs.
# Se quiser o MCP, defina a variável de usuário GITHUB_PAT antes (nunca coloque o token em arquivo versionado).
if ($env:GITHUB_PAT) {
  codex mcp add github --url https://api.githubcopilot.com/mcp/ --bearer-token-env-var GITHUB_PAT
} else {
  Write-Warning "GITHUB_PAT não definido: pulando o MCP do GitHub."
}

codex mcp list
