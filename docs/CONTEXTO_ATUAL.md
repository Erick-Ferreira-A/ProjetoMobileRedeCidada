# Rede Cidadã — contexto atual (para continuar em outro chat)

Atualizado em 25/11/2026. O código prevalece sobre este resumo.

## Onde está o trabalho
- Repositório: `Erick-Ferreira-A/ProjetoMobileRedeCidada`. Servidor e back-end são do **Fred** (Ktor + SQLite no Railway).
- Redesign das telas: branch `agent/claude-design-system`, **PR #7** aberto para a `main`, aguardando revisão do Fred (PR #6 fechado, conteúdo incluído no #7).
- Pasta do projeto (desde 24/11/2026): `C:\Users\Erick Rondonia\Documents\REDE CIDADA\`. Repo em `01_app\ProjetoMobileRedeCidada` (abrir este caminho no Android Studio). Índice em `00_LEIA-ME.md`; decisões, changelog, aprendizados, pendências e rotina de sessão em `08_conhecimento\`.
- APKs de teste (não vão para o git): `C:\Users\Erick Rondonia\Documents\REDE CIDADA\03_apks\` — último: `RedeCidada-teste-2026-11-25-v7.apk`, apontando para o servidor do Railway.
- Gerar APK de teste: `.\gradlew.bat :app:assembleDebug -PBACKEND_URL=https://projetomobileredecidada-production.up.railway.app/api/v1` (com o workaround de loopback da skill `rede-cidada-validacao-mobile`).

## Documentos que mandam
- `docs/design/GUIA_VISUAL.md` (regras visuais), `docs/design/TELAS.md` (como cada tela funciona), `docs/design/guia-visual.html` (para ver/apresentar).
- `docs/ARQUITETURA_BACKEND.md`: recomendação para evoluir o servidor do Fred (Postgres, Cloudflare R2, FCM, backup; ~US$ 5/mês). **Não implementado** — o usuário decidiu seguir o caminho do Fred.
- Skills do projeto em `.claude/skills/rede-cidada-*`.

## Como o usuário quer trabalhar
- Foco em **tela** (estrutura, experiência, visual em Compose), não em integrações nem back-end.
- Opções ainda não integradas **aparecem na tela** com selo "Em breve" e comentário `INTEGRAR` no código.
- Valida no **celular real** (o emulador é lento); entregar APK a cada rodada.
- Comunicação curta em português.

## Telas no estilo novo (PR #7)
| Tela | Estado |
| --- | --- |
| Tema | Neutros quentes claro/escuro; fontes Bricolage (títulos) + Atkinson (texto); componentes em `ui/designsystem/` |
| Navegação | Início · Mobilidade · Relatar · Vídeos · Perfil; escudo SOS no topo |
| Onboarding | Brilho azul radial na logo (sem degradê vertical); passo 1 funções principais; passo 2 anônimo + score de confiabilidade; passo 3 SOS; rolável; "Pular" à direita; sem 190 |
| Início | Mapa em tela cheia, botão Filtros (categorias + raio), painel de baixo com resumo, clima com ícone e relatos |
| Mapa | Sem chave Google válida usa Leaflet + OpenStreetMap (pinos reais, centro na pessoa) |
| SOS | Segurar 2 s; números 190/193/192/199 abrem o discador |
| Vídeos | Feed vertical estilo Reels (toque pausa, toque duplo = "Também vi", sem som no início) |
| Perfil | Foto/iniciais, score de confiabilidade (0–1000), atalhos, preferências, sair/excluir; Editar perfil salva no aparelho |
| Login / Localização | Cores do tema; localização com 190 no topo |

**Ainda no layout antigo:** Relatar (sheet), Mobilidade, Detalhe do alerta, Login (só cores ajustadas).

## Pendências e decisões
- **Google Maps:** a chave só autoriza a máquina do Fred. Ele precisa cadastrar `br.com.redecidada.app` + SHA-1 `EB:30:E4:A5:D1:04:E8:3C:5B:0D:2E:47:8F:79:AC:79:43:62:C7:C7`. Na máquina do Erick a chave está comentada em `local.properties`.
- **Cadastro no celular do Erick não funcionou:** o servidor está no ar e valida certo; falta o Erick mandar a mensagem de erro que aparece no formulário.
- **Score de confiabilidade:** fixo em 940 no servidor; a "análise automática (IA)" citada no onboarding **não existe** (marcado `INTEGRAR`) — implementar ou tirar antes da Play Store.
- **CPF/CNPJ e RG:** decisão do usuário = manter **desativados** (a política de privacidade diz que não coleta documentos).
- **Assaltos "por região":** escrito como "relatos de assalto agora", sem rotular bairro como perigoso (guia §2.1).
- Vídeos usam `VideoView` (faixas pretas) → trocar por Media3 ExoPlayer.
- Play Store pela conta do professor; teste fechado com 15 pessoas (ver `CHECKLIST_PLAY_STORE.md`).

## Próximos passos sugeridos
1. Fred revisar e mesclar o PR #7.
2. Resolver o cadastro (pegar a mensagem de erro do celular).
3. Redesenhar Relatar (3 passos), Detalhe do alerta e Mobilidade conforme `TELAS.md`.
4. Com o Fred: Postgres + R2 + push, depois comentários, menções, avaliação de notícia e score real.

## Testes e regras
- `.\gradlew.bat assembleDebug test lint`: app 17 testes, servidor 33, lint 0 erros (25/11/2026). Repetido no caminho novo em 24/11/2026 22:54: tudo passou, lint 0 erros / 51 warnings / 2 hints (linha de base de warnings).
- Nunca tocar em SOS/190 nem publicar relato real em teste; no emulador, conferir o foco do app antes de cada toque (skill de validação).
