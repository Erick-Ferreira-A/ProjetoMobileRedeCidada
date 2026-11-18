package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.data.remote.RailLine
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.TransitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Seções com dados reais: status dos trilhos (ARTESP) e ônibus (SPTrans Olho Vivo). */
fun LazyListScope.transitLiveSection(vm: TransitViewModel) {
    item(key = "rail-header") {
        SectionHeader(
            title = "Status dos trilhos",
            subtitle = "Linhas concedidas, em tempo real",
            loading = vm.railLoading,
            onRefresh = vm::refreshRail
        )
    }
    item(key = "rail-body") { RailStatusCard(vm) }

    item(key = "bus-header") {
        SectionHeader(title = "Ônibus municipais", subtitle = "Previsão de chegada (SPTrans Olho Vivo)", loading = vm.busLoading)
    }
    item(key = "bus-search") { BusSearchCard(vm) }

    val line = vm.selectedLine
    val stop = vm.selectedStop
    when {
        line == null -> items(vm.busLines, key = { "line-${it.code}" }) { bus ->
            ListRow(title = bus.sign, subtitle = "Sentido ${bus.destination}") { vm.selectLine(bus) }
        }
        stop == null -> {
            item(key = "stops-head") {
                BackRow("Linha ${line.sign} → ${line.destination}. Escolha a parada:", "Trocar linha", vm::backToLines)
            }
            items(vm.stops, key = { "stop-${it.code}" }) { s ->
                ListRow(title = s.name.ifBlank { "Parada ${s.code}" }, subtitle = s.address) { vm.selectStop(s) }
            }
        }
        else -> {
            item(key = "arrivals-head") {
                BackRow(stop.name.ifBlank { "Parada ${stop.code}" }, "Trocar parada", vm::backToStops)
            }
            item(key = "arrivals") { ArrivalsCard(vm) }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, loading: Boolean, onRefresh: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else if (onRefresh != null) TextButton(onClick = onRefresh) { Text("Atualizar") }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) { Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() } }
}

@Composable
private fun RailStatusCard(vm: TransitViewModel) {
    SectionCard {
        val status = vm.railStatus
        vm.railMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (status == null && vm.railMessage == null) {
            Text("Carregando…", style = MaterialTheme.typography.bodyMedium)
        }
        status?.let {
            it.lines.forEachIndexed { index, line ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                RailLineRow(line)
            }
            val time = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(it.fetchedAtEpochMs))
            Text("Atualizado às $time. ${it.coverageNote}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RailLineRow(line: RailLine) {
    val color = when (line.classification) {
        "operacional" -> if (line.normal) CitizenStatusColors.safe else CitizenStatusColors.warning
        "problema" -> CitizenStatusColors.critical
        else -> MaterialTheme.colorScheme.outline
    }
    Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.padding(top = 5.dp).size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("${line.line} • ${line.operator}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(line.situation, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.SemiBold)
            if (line.description.isNotBlank()) {
                Text(line.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BusSearchCard(vm: TransitViewModel) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = vm.busQuery,
                onValueChange = { vm.busQuery = it.take(60) },
                label = { Text("Número ou nome da linha") },
                placeholder = { Text("Ex.: 8000, Lapa, 7021") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.searchLines() }),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = vm::searchLines, enabled = !vm.busLoading) { Text("Buscar") }
        }
        vm.busMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun ListRow(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BackRow(text: String, action: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        TextButton(onClick = onClick) { Text(action) }
    }
}

@Composable
private fun ArrivalsCard(vm: TransitViewModel) {
    SectionCard {
        val arrivals = vm.arrivals
        if (arrivals == null) {
            Text(if (vm.busLoading) "Consultando a SPTrans…" else "Sem previsão no momento.", style = MaterialTheme.typography.bodyMedium)
            return@SectionCard
        }
        if (arrivals.lines.isEmpty()) {
            Text("Nenhum ônibus a caminho desta parada agora.", style = MaterialTheme.typography.bodyMedium)
        }
        arrivals.lines.forEachIndexed { index, line ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("${line.sign} → ${line.destination}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            line.vehicles.take(4).forEach { v ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(v.arrivalTime ?: "--:--", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = CitizenStatusColors.safe)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Ônibus ${v.prefix}" + if (v.accessible) " • acessível ♿" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Text(
            "Previsão da SPTrans às ${arrivals.referenceTime}. Atualiza sozinha a cada 30 s.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}
