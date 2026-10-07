# Registra o MCP do Figma (e opcionalmente do GitHub) no Codex.
# Pré-requisitos:
#   - App desktop do Figma aberto, com o servidor MCP do Dev Mode habilitado
#   - (opcional) variável de usuário GITHUB_PAT definida
# A sintaxe do `codex mcp add` pode variar por versão: confirme com `codex mcp add --help`.

# Figma (servidor local do Dev Mode)
codex mcp add figma --url http://127.0.0.1:3845/mcp

# GitHub (opcional): lê o token da variável de ambiente, nunca de arquivo versionado
if ($env:GITHUB_PAT) {
  codex mcp add github --url https://api.githubcopilot.com/mcp/ --bearer-token-env-var GITHUB_PAT
} else {
  Write-Warning "GITHUB_PAT não definido: pulando o MCP do GitHub (o 'gh' já cobre repositórios e PRs)."
}

codex mcp list
