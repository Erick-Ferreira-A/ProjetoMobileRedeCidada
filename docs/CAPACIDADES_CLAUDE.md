# Capacidades do Claude Code neste projeto

Conferido em 20/11/2026, Claude Code 2.1.322, app desktop (aba Code), Windows 11.

## Conexões
| Item | Finalidade | Estado | Ação pendente |
| --- | --- | --- | --- |
| GitHub via `gh` 2.102.0 | Repo, branches, PRs | Autenticado (conta do repo) | — |
| Figma MCP `https://mcp.figma.com/mcp` | Ler design e comparar com Compose | Registrado (escopo local deste projeto) — **precisa OAuth** | Digitar `/mcp` nesta sessão → `figma` → Authenticate |
| Canva MCP `https://mcp.canva.com/mcp` | Banner/cartaz (secundário) | Registrado (escopo local) — **precisa OAuth** | `/mcp` → `canva` → Authenticate |
| Google Drive / Docs / Gmail | Conectores da conta claude.ai | Conectados | Não usados pelo app |
| MCPs SFMC (Movida/Ancar/Ebradi) | Outros trabalhos | Conectados | Não alterar |

Registros feitos com `claude mcp add --scope local --transport http <nome> <url>` (ficam em `~/.claude.json`, não no repositório). Tokens de Codex/OpenCode não foram reutilizados.

## Plugins (diretório Anthropic)
Nenhum instalado. Avaliados:
- **Figma** (plugin oficial): duplicaria o MCP já registrado; reavaliar após OAuth.
- **Design** (knowledge-work): crítica, acessibilidade, UX writing — candidato; instalar após revisar conteúdo.
- **frontend-design**, Website Design: voltados a web/React — não instalar.
- **code-review** / `security-review`: já disponíveis como skills nativas.
- **supabase**: só se houver decisão de backend.

## Skills do projeto (`.claude/skills/`)
`rede-cidada-contexto`, `rede-cidada-compose-ux`, `rede-cidada-integracoes`, `rede-cidada-validacao-mobile`, `rede-cidada-entrega`.

## Repositório
- `main` sincronizada com `origin/main` em 20/11/2026 (`eafae4c`, PRs #3–#5 do Fred: backend Ktor/Railway, relay ARTESP, páginas legais, R8).
- Branch local `agent/claude-fontes-honestas` (não enviada): correção feita sobre a versão anterior; superada pelas mudanças do Fred. Mantida só como referência.

## Ambiente
- JBR: `C:\Program Files\Android\Android Studio\jbr` (25.0.3). Alvo Java do app: 11.
- Gradle 9.6.0, AGP 9.4.1, Kotlin 2.2.10.
- Erro "Unable to establish loopback connection" reproduz; workaround em `rede-cidada-validacao-mobile`.
- AVD disponível: `Pixel_8` (não estava em execução).
