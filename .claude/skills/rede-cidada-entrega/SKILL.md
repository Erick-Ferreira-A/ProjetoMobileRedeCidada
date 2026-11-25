---
name: rede-cidada-entrega
description: Fechar uma entrega no Rede Cidadã — revisar diff, atualizar docs de continuidade, abrir PR com gh, e tratar galeria GitHub Pages (gh-pages) e materiais Canva sem quebrar links/QR já impressos. Use ao commitar, abrir PR, atualizar documentação, regenerar capturas/galeria ou falar em banner/cartaz.
---

# Entrega

## Procedimento
0. `git fetch` e conferir PRs abertos (`gh pr list`) — outras pessoas (Fred) e agentes também enviam mudanças.
1. `git status` / `git diff` — só arquivos da tarefa; nada de `local.properties`, `server/secrets.properties`, `keystore.properties`, `.idea/`, chaves.
2. Validação completa (`rede-cidada-validacao-mobile`) nesta sessão.
3. Atualizar `docs/CONTEXTO_ATUAL.md` (estado/pendências) e, se mudou capacidade, `docs/CAPACIDADES_CLAUDE.md`. Histórico datado fica na seção "Histórico", não nas regras.
4. Commit na branch `agent/claude-<tarefa>`; `gh pr create --base main`. Descrição: antes/depois, o que foi testado, limitações reais.
5. Nunca push direto na `main`, force-push ou exclusão de branch alheia.

## Galeria pública
- URL: https://erick-ferreira-a.github.io/ProjetoMobileRedeCidada/ (branch `gh-pages`, `index.html` + `.nojekyll`). O QR impresso aponta para ela: **não mudar URL nem estrutura**.
- Clone de publicação: `C:\Users\Erick Rondonia\Documents\REDE CIDADA\05_publicacao\gh-pages`; scripts do banner em `C:\Users\Erick Rondonia\Documents\REDE CIDADA\04_academico\banner-codex\work-banner`.
- Ao fechar uma entrega, siga também o "Fim" de `C:\Users\Erick Rondonia\Documents\REDE CIDADA\08_conhecimento\ROTINA_DE_SESSAO.md` (CHANGELOG, DECISOES, APRENDIZADOS, PENDENCIAS).
- Publicar nova versão só com pedido explícito do usuário. Sem pedido: gerar localmente e informar o que mudaria.

## Railway / Play Store
Deploy do servidor e publicação na loja são ações externas: só com pedido explícito. Ver `server/README.md` e `CHECKLIST_PLAY_STORE.md`.

## Canva
Banner `DAHXxOqOolE`, cartaz A4 `DAHXxbnLb2M` (REDE CIDADÃ). Edições do usuário no Canva prevalecem sobre PDFs locais. Não editar automaticamente durante correção de código.

## Limites
Não declarar "seguro", "sem erros", "produção" ou "100% anônimo" sem evidência.
