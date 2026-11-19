package com.example.projetorevisao.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.data.remote.BackendHttp

/** Links para as páginas públicas de privacidade, termos e exclusão de conta (servidas pelo backend). */
@Composable
fun LegalLinks(modifier: Modifier = Modifier, showDeletion: Boolean = false) {
    val uriHandler = LocalUriHandler.current
    val links = buildList {
        add("Termos de uso" to "termos")
        add("Política de privacidade" to "privacidade")
        if (showDeletion) add("Excluir conta pela web" to "excluir-conta")
    }
    FlowRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        links.forEach { (label, path) ->
            TextButton(onClick = { runCatching { uriHandler.openUri("${BackendHttp.siteUrl}/$path") } }) {
                Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
