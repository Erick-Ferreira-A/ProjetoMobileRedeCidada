---
name: rede-cidada-contexto
description: Mapa do Rede Cidadã — app Android (Kotlin/Compose/MVVM) + servidor Ktor (:server, deploy Railway) + relay ARTESP; arquivos-chave, identidade, nomes, ambientes (local × produção) e o que ainda é demonstrativo. Use antes de qualquer tarefa neste repositório: tela, backend, auditoria, Play Store, apresentação, galeria, ou quando citarem Rede Cidadã, CitizenViewModel, SOS, Reels, guardiões, Railway ou ARTESP.
---

# Rede Cidadã — contexto do projeto

## Finalidade
Mapa mínimo para trabalhar sem reinventar nem prometer o que o app não faz. Aponta para fontes; o código prevalece sobre este resumo.

## Fontes de verdade (ler nesta ordem)
1. `AGENTS.md` e `CLAUDE.md`.
2. `docs/CONTEXTO_ATUAL.md` (estado, matriz real/demo, pendências) e `docs/CAPACIDADES_CLAUDE.md`.
3. `server/README.md` (backend, Railway, moderação), `CHECKLIST_PLAY_STORE.md`, `relay-artesp/README.md`.
4. `README_REDE_CIDADA.md` e `GUIA_COLABORADOR_E_README.md` são **anteriores ao backend** (out/2026) e contêm afirmações desatualizadas (ex.: "Smart Sampa", "cache offline"). Não usar como prova.

## Módulos
| Módulo | Papel |
| --- | --- |
| `:app` | Android. `MainActivity.kt` (navegação por estado), `ui/screens`, `ui/components`, `ui/viewmodel` (Citizen/Auth/Transit), `data/remote` (Ktor client: Auth, SOS, Transit, backend), `data/local` (Room), `data/session`, `data/media`, `data/location` |
| `:server` | Ktor 3 + SQLite: auth JWT, incidentes, mídia, moderação, SOS/guardiões, trânsito (SPTrans/ARTESP), páginas legais. Testes em `server/src/test` |
| `relay-artesp/` | Função Vercel que intermedeia a API Trilhos da ARTESP |
| `railway.json`, `server/Dockerfile` | Deploy do servidor |

## Identidade e nomes
- Nome público: **REDE CIDADÃ** (sem "SP"). Documentos e código ainda usam "Rede Cidadã SP"; conciliar com cuidado.
- `applicationId = br.com.redecidada.app`; `namespace`/package Kotlin = `com.example.projetorevisao`. Não renomear sem pedido.
- Cores: azul-marinho/azul + laranja (#0A2540, #1D3B8B, #1E3A8A, #FF6B00); temas claro/escuro em `ui/theme/`.

## Ambientes
- Debug: `BACKEND_BASE_URL` padrão `http://10.0.2.2:8080/api/v1` (servidor local via `.\gradlew.bat :server:run`).
- Produção: Railway (`BACKEND_URL` em `local.properties` ou `-PBACKEND_URL`). **Não apontar testes para produção.**
- Segredos: `local.properties`, `server/secrets.properties`, `keystore.properties` — ignorados pelo git; nunca imprimir.

## Limites — verificar no código antes de afirmar
O backend trouxe auth, persistência e envio de relatos/SOS ao servidor. Ainda precisa de verificação ponta a ponta: o que o SOS faz além de gravar no servidor (notifica alguém?), anonimato real dos relatos, moderação, origem dos dados de trânsito e de clima. Overpass/OSM não é SSP-SP nem Smart Sampa.

## Regras
- Datas acadêmicas (13–14/10/2026, UNASP-SP) são históricas; não decidir autoria/regras acadêmicas.
- Testes nunca publicam relato real nem disparam ligação/SOS/notificação em produção.
- Branch `agent/claude-<tarefa>`; nunca push direto na `main`; nunca sobrescrever `gh-pages` a partir da `main`.
- O Fred e outros agentes também abrem PRs: rodar `git fetch` antes de começar e não editar arquivos de PR aberto alheio.
