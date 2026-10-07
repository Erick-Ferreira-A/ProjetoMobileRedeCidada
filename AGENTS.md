# AGENTS.md — Regras compartilhadas para agentes de IA

Vale para Codex, Claude Code, Antigravity e Gemini (Android Studio).

## Projeto
- App Android nativo em Kotlin com Jetpack Compose (Material 3).
- Pacote: `com.example.projetorevisao` — minSdk 24, targetSdk 37.
- IDE principal: Android Studio (emulador, Gradle, debug).
- Design: Figma via MCP. Consulte o design antes de criar telas.
- Repositório: GitHub (use `gh` para branches e PRs).

## Convenções
- Kotlin idiomático, UI 100% Compose, arquitetura MVVM.
- Composables em PascalCase, um arquivo por tela, em `ui/`.
- Tema em `ui/theme/` (Color.kt, Theme.kt, Type.kt): reaproveite, não duplique cores.
- Não faça commit de segredos (tokens, `local.properties`, keystores, `.mcp.json`).

## Comandos (Windows)
- Build: `.\gradlew.bat assembleDebug`
- Testes: `.\gradlew.bat test`
- Lint: `.\gradlew.bat lint`

## Fluxo com Git
- Um branch por agente/tarefa: `agent/<nome>-<tarefa>`.
- Nunca dois agentes editando o mesmo arquivo ao mesmo tempo.
- Abra PR com `gh pr create`; não faça push direto na `main`.
