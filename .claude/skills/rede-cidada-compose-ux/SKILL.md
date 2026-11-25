---
name: rede-cidada-compose-ux
description: Revisão e implementação de UI Compose/Material 3 no Rede Cidadã — tokens de tema, insets, acessibilidade, contraste, textos honestos e comparação com o Figma (file key du450qJibmxDGDMZmG7EIL). Use ao criar/ajustar telas, componentes, cores, tipografia, cópias de interface ou ao comparar com o design.
---

# Compose UX

## Entradas
- `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` (fonte das cores; `CitizenStatusColors` para estados).
- Tela em `ui/screens/` e componentes em `ui/components/`.
- Figma (MCP `figma`, se autenticado): obter URL/node-id reais antes de chamar ferramentas; nunca afirmar que consultou o Figma sem resposta da ferramenta.

## Correções que não podem regredir
- `statusBarsPadding()` no cabeçalho de `CitizenNavigationShell.kt`.
- Pesos/larguras no `MetroScreen` (títulos × selos não se sobrepõem).
- Altura adaptável: botão anônimo do login, botão publicar relato.
- Coluna/selo do score do perfil sem compressão vertical.
- Sheets modais com rolagem; detalhe de alerta sem estado parcialmente expandido.
(Conferir se continuam presentes após as mudanças do Fred de 18–19/11/2026 antes de afirmar.)
- Espaço entre título e ação no cabeçalho dos Reels.

## Procedimento
1. Reutilizar tokens; nenhuma cor hex nova sem justificativa no tema.
2. Alvos de toque ≥48dp; `contentDescription` em ícones acionáveis, `null` em decorativos.
3. Texto com `maxLines`/`Ellipsis` só quando o conteúdo completo existe em outro lugar; preferir quebra.
4. Cópia honesta: nada de "100% anônimo", "ao vivo", "verificado por IA", "oficial" sem implementação (ver `rede-cidada-integracoes`).
5. Previews claro e escuro para telas alteradas.
6. Validar com `rede-cidada-validacao-mobile` e capturas no emulador.

## Limites
Skills/plugins de design web (React/CSS) servem só para crítica visual; não transformar o app em web.
