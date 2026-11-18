package com.example.projetorevisao.ui.components

import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.projetorevisao.data.location.LocationProvider
import com.example.projetorevisao.data.location.hasLocationPermission
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

/** Nome curto da região (bairro, cidade) a partir das coordenadas. Null se o aparelho não tiver geocodificador. */
private suspend fun Context.areaLabelFor(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
    if (!Geocoder.isPresent()) return@withContext null
    runCatching {
        @Suppress("DEPRECATION") // a versão assíncrona só existe a partir do Android 13
        val address = Geocoder(this@areaLabelFor, Locale.forLanguageTag("pt-BR"))
            .getFromLocation(latitude, longitude, 1)
            ?.firstOrNull()
        listOfNotNull(address?.subLocality, address?.locality ?: address?.subAdminArea)
            .distinct()
            .joinToString(", ")
            .ifBlank { null }
    }.getOrNull()
}

/**
 * Lê o GPS uma vez e entrega a posição ao ViewModel (o mapa centraliza nela).
 * Devolve a função para pedir uma nova leitura (ex.: botão "minha localização").
 */
@Composable
fun rememberUserLocationSync(viewModel: CitizenViewModel): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val provider = remember(context) { LocationProvider(context.applicationContext) }

    val refresh: () -> Unit = remember(provider, viewModel) {
        {
            if (context.hasLocationPermission()) {
                scope.launch {
                    // Logo após liberar a permissão o GPS pode ainda não ter sinal: tenta mais algumas vezes.
                    var location = withTimeoutOrNull(10_000) { provider.currentLocation() }
                    var attempts = 0
                    while (location == null && attempts < 3) {
                        delay(4_000)
                        location = withTimeoutOrNull(10_000) { provider.currentLocation() }
                        attempts++
                    }
                    if (location == null) return@launch
                    val label = context.areaLabelFor(location.latitude, location.longitude)
                    viewModel.updateUserLocation(location.latitude, location.longitude, label)
                }
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }
    return refresh
}
