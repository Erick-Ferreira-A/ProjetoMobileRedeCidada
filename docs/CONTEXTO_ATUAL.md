# Rede Cidadã — contexto atual

Documento curto de continuidade. O código prevalece. Histórico datado fica no fim.

## Estado
- `main` = `origin/main` @ `eafae4c` (PRs #3–#5 do Fred: backend Ktor + SQLite com deploy no Railway, relay ARTESP, páginas legais, R8).
- App: `applicationId br.com.redecidada.app`; debug fala com o servidor local `10.0.2.2:8080`.
- Andamento da Play Store: `CHECKLIST_PLAY_STORE.md` (itens 👤 dependem do usuário).

## Validação (20/11/2026, JBR 25.0.3, workaround de loopback necessário)
| Etapa | `main` @ eafae4c | Branch `agent/claude-skills-continuidade` |
| --- | --- | --- |
| `assembleDebug` | OK | OK |
| Testes | 36/36 (app 3, server 33) | 36/36 |
| Lint | **1 erro** (UnrememberedMutableState em `CitizenGoogleMapView.kt:291`), 49 avisos | 0 erros, 49 avisos |

## Achados em aberto (prioridade)
1. `SpPublicSafetyApiService.fetchLiveSspAndSmartSampaTelemetry` ainda soma a contagem OSM/Overpass a `smartSampaCamerasActive` (valor fixo) e os comentários chamam isso de SSP-SP/Smart Sampa. Hoje não aparece na UI; corrigir a atribuição antes de voltar a exibir.
2. `getDistrictTelemetry` (DP, batalhão, índice de risco, protocolos 156): valores fixos.
3. Detecção INMET por texto (`contains`) e CGE por regex no HTML: frágeis.
4. `README_REDE_CIDADA.md` e `GUIA_COLABORADOR_E_README.md` são anteriores ao backend e têm afirmações desatualizadas ("Smart Sampa", "cache offline", "relatos verificados").
5. 49 avisos de lint (dependências desatualizadas, recursos não usados, `DefaultLocale` etc.) — triagem pendente.
6. Auditoria de acessibilidade/contraste e capturas no emulador (Pixel_8) ainda não feitas após as mudanças do Fred.
7. Checklist da loja, itens 🤖 pendentes: backup automático do banco; descrições curta e completa da ficha.

## Histórico
- 20/11/2026 — Claude Code assume a continuidade: skills `.claude/skills/rede-cidada-*`, Figma/Canva MCP registrados (OAuth pendente), correção do erro de lint do mapa.
