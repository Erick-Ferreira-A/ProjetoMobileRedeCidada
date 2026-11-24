---
name: rede-cidada-validacao-mobile
description: Build, testes (app e :server), lint, servidor local, emulador e capturas do Rede Cidadã no Windows — JBR do Android Studio, workaround do erro "Unable to establish loopback connection", Pixel_8 a 420 dpi e fonte 1.0, temas claro/escuro, sem disparar SOS/ligações/relatos reais. Use ao validar qualquer mudança, rodar gradlew, abrir emulador, tirar screenshots ou quando o build falhar.
---

# Validação mobile

## Comandos (PowerShell, na raiz do repo)
```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint --console=plain
```
`test` inclui `:server:test`. Relatórios: `app/build/reports/lint-results-debug.txt`, `app/build/test-results/testDebugUnitTest/`, `server/build/test-results/test/`.

Servidor local para testar o app no emulador: `.\gradlew.bat :server:run` (porta 8080; o debug usa `10.0.2.2:8080`). Nunca testar fluxos de relato/SOS contra a produção no Railway.

## Se aparecer "java.io.IOException: Unable to establish loopback connection"
Erro de ambiente (UnixDomainSockets no Windows), não do app. Só no processo atual, preservando valor anterior:
```powershell
New-Item -ItemType Directory -Force 'C:\Temp\rede-cidada-gradle' | Out-Null
$env:JAVA_TOOL_OPTIONS = ("$env:JAVA_TOOL_OPTIONS -Djdk.net.unixdomain.tmpdir=C:\Temp\rede-cidada-gradle").Trim()
```
Reproduzido e resolvido assim em 20/11/2026. Não alterar configurações globais nem desativar segurança.

## Classificar falhas
- **App**: erro de compilação, teste falhando, lint *error*.
- **Ambiente**: JVM, loopback, SDK ausente, daemon incompatível.
- **Rede**: download de dependências, endpoints públicos fora do ar.

## Emulador e capturas
- SDK: `%LOCALAPPDATA%\Android\Sdk` (`platform-tools\adb.exe`, `emulator\emulator.exe`).
- Listar antes de agir: `adb devices`, `emulator -list-avds`. Não fechar emulador que já estava aberto.
- Padrão: Pixel_8, 1080×2400, 420 dpi, `font_scale 1.0`. Não reduzir densidade/fonte para caber tudo numa imagem; conteúdo abaixo da dobra é por rolagem.
- Capturar tema claro e escuro (`adb shell cmd uimode night yes|no`).
- Proibido nos testes: tocar em SOS real, ligar 190/193/199, publicar relato real, enviar notificação.
- **Antes de cada `adb shell input tap`:** conferir `dumpsys window | grep mCurrentFocus` (o app precisa estar em primeiro plano) e pegar as coordenadas de um `uiautomator dump` feito logo antes. Nunca tocar "às cegas" depois de uma transição. Em 23/11/2026 um toque no onboarding abriu o discador com 190 (link de emergência colado ao botão) e o toque seguinte caiu no botão "Call" do discador. No emulador a chamada vai para o modem simulado, mas o erro não pode se repetir. Use `MSYS_NO_PATHCONV=1` no Git Bash para caminhos `/sdcard`.

## Auditoria visual mínima
Contraste (selos verdes, textos azuis, ícones no escuro), alvos ≥48dp, TalkBack/contentDescription, fonte 1.3–2.0, teclado aberto, telas pequenas, insets do sistema, estados loading/erro/offline.

## Verificações antes de declarar pronto
Build + test + lint rodados nesta sessão, com contagem de warnings comparada à linha de base registrada em `docs/CONTEXTO_ATUAL.md`. Nunca dizer "sem erros" com base em execução antiga.
