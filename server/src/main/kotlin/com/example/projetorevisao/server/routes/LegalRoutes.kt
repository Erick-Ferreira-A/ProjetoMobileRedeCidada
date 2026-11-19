package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.repository.AuthRepository
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * Páginas públicas exigidas pela Play Store: política de privacidade, termos de uso e exclusão de conta.
 * Os textos ficam em resources/legal (um .html por página); aqui só entra o layout comum.
 */
fun Route.legalRoutes(authRepository: AuthRepository) {
    get("/") { call.respondRedirect("/privacidade") }
    get("/privacidade") { call.respondPage("Política de privacidade", fragment("privacidade")) }
    get("/termos") { call.respondPage("Termos de uso", fragment("termos")) }
    get("/excluir-conta") { call.respondDeletionPage(DELETE_FORM) }

    rateLimit(AUTH_RATE_LIMIT) {
        post("/excluir-conta") {
            val params = call.receiveParameters()
            val email = params["email"].orEmpty().trim()
            val password = params["password"].orEmpty()
            val result = when {
                email.isBlank() || password.isBlank() ->
                    resultBox(false, "Preencha o e-mail e a senha.") + DELETE_FORM
                authRepository.deleteAccountByEmail(email, password) ->
                    resultBox(true, "Conta excluída. Seus dados foram apagados.")
                else ->
                    resultBox(false, "E-mail ou senha incorretos. Se não lembra a senha, peça a exclusão por e-mail (opção 3).") + DELETE_FORM
            }
            call.respondDeletionPage(result)
        }
    }
}

private val DELETE_FORM = """
<form method="post" action="/excluir-conta">
  <label for="email">E-mail da conta</label>
  <input id="email" name="email" type="email" autocomplete="email" required>
  <label for="password">Senha</label>
  <input id="password" name="password" type="password" autocomplete="current-password" required>
  <button type="submit">Excluir minha conta de vez</button>
  <p class="meta">A exclusão é imediata e não pode ser desfeita.</p>
</form>
""".trimIndent()

private fun resultBox(ok: Boolean, message: String) =
    """<div class="${if (ok) "ok" else "erro"}" role="status">$message</div>"""

private fun fragment(name: String): String =
    object {}.javaClass.getResource("/legal/$name.html")?.readText(Charsets.UTF_8)
        ?: error("Página legal não encontrada: $name")

private suspend fun ApplicationCall.respondDeletionPage(formArea: String) =
    respondPage("Excluir conta", fragment("excluir-conta").replace("{{FORM}}", formArea))

private suspend fun ApplicationCall.respondPage(title: String, body: String) {
    response.headers.append("Cache-Control", "no-store")
    response.headers.append("X-Content-Type-Options", "nosniff")
    response.headers.append("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; form-action 'self'")
    respondText(layout(title, body), ContentType.Text.Html.withParameter("charset", "utf-8"), HttpStatusCode.OK)
}

private fun layout(title: String, body: String) = """
<!doctype html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>$title · Rede Cidadã</title>
<style>
  :root { --azul:#1F4E9C; --laranja:#E8772E; --texto:#1d2330; --suave:#5b6474; --fundo:#f6f7fb; --cartao:#fff; --borda:#dde2ec; }
  @media (prefers-color-scheme: dark) {
    :root { --azul:#8fb4ff; --laranja:#ffab6e; --texto:#e8ebf2; --suave:#a5adbd; --fundo:#11151d; --cartao:#1a202b; --borda:#2c3442; }
  }
  * { box-sizing: border-box; }
  body { margin:0; background:var(--fundo); color:var(--texto); font:16px/1.6 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif; }
  header { background:var(--azul); color:#fff; padding:14px 16px; }
  @media (prefers-color-scheme: dark) { header { background:#1a2a48; } }
  header a { color:#fff; text-decoration:none; font-weight:800; }
  nav { display:flex; flex-wrap:wrap; gap:6px 16px; padding:10px 16px; border-bottom:1px solid var(--borda); background:var(--cartao); }
  nav a { color:var(--azul); font-weight:600; text-decoration:none; }
  main { max-width:760px; margin:0 auto; padding:16px; }
  h1 { font-size:1.6rem; line-height:1.25; margin:.6em 0 .2em; }
  h2 { font-size:1.15rem; margin:1.6em 0 .4em; }
  a { color:var(--azul); }
  .meta { color:var(--suave); font-size:.9rem; }
  .tabela { overflow-x:auto; }
  table { border-collapse:collapse; width:100%; font-size:.93rem; background:var(--cartao); }
  th, td { border:1px solid var(--borda); padding:8px; text-align:left; vertical-align:top; }
  th { background:var(--fundo); }
  .alerta { border-left:4px solid var(--laranja); background:var(--cartao); padding:12px 14px; margin:16px 0; }
  form { background:var(--cartao); border:1px solid var(--borda); border-radius:12px; padding:16px; display:grid; gap:8px; max-width:420px; }
  label { font-weight:600; }
  input { font:inherit; padding:10px; border:1px solid var(--borda); border-radius:8px; background:var(--fundo); color:var(--texto); }
  button { font:inherit; font-weight:700; padding:12px; border:0; border-radius:8px; background:#C62828; color:#fff; cursor:pointer; margin-top:6px; }
  .ok, .erro { padding:12px 14px; border-radius:8px; margin:8px 0 12px; font-weight:600; }
  .ok { background:#e3f4e6; color:#1b5e20; }
  .erro { background:#fde7e7; color:#8e1c1c; }
  footer { max-width:760px; margin:0 auto; padding:24px 16px 40px; color:var(--suave); font-size:.9rem; }
</style>
</head>
<body>
<header><a href="/privacidade">Rede Cidadã</a></header>
<nav><a href="/privacidade">Privacidade</a><a href="/termos">Termos de uso</a><a href="/excluir-conta">Excluir conta</a></nav>
<main>
$body
</main>
<footer>Contato: <a href="mailto:suporte.redecidada@gmail.com">suporte.redecidada@gmail.com</a></footer>
</body>
</html>
""".trimIndent()
