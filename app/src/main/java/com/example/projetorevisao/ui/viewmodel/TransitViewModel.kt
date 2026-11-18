package com.example.projetorevisao.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetorevisao.data.remote.BusArrivals
import com.example.projetorevisao.data.remote.BusLine
import com.example.projetorevisao.data.remote.BusStop
import com.example.projetorevisao.data.remote.RailStatus
import com.example.projetorevisao.data.remote.TransitApi
import com.example.projetorevisao.data.remote.TransitData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Status dos trilhos e consulta de ônibus em tempo real. */
class TransitViewModel : ViewModel() {

    // --- Trilhos ---
    var railStatus by mutableStateOf<RailStatus?>(null)
        private set
    var railMessage by mutableStateOf<String?>(null)
        private set
    var railLoading by mutableStateOf(false)
        private set

    fun refreshRail() {
        if (railLoading) return
        viewModelScope.launch {
            railLoading = true
            when (val result = TransitApi.railStatus()) {
                is TransitData.Ready -> {
                    railStatus = result.data
                    railMessage = null
                }
                is TransitData.Message -> railMessage = result.text
            }
            railLoading = false
        }
    }

    // --- Ônibus: busca da linha → parada → previsão ---
    var busQuery by mutableStateOf("")
    var busLines by mutableStateOf<List<BusLine>>(emptyList())
        private set
    var selectedLine by mutableStateOf<BusLine?>(null)
        private set
    var stops by mutableStateOf<List<BusStop>>(emptyList())
        private set
    var selectedStop by mutableStateOf<BusStop?>(null)
        private set
    var arrivals by mutableStateOf<BusArrivals?>(null)
        private set
    var busMessage by mutableStateOf<String?>(null)
        private set
    var busLoading by mutableStateOf(false)
        private set

    private var arrivalsJob: Job? = null

    fun searchLines() {
        val query = busQuery.trim()
        if (query.length < 2) {
            busMessage = "Digite pelo menos 2 caracteres (ex.: 8000, Lapa)."
            return
        }
        resetSelection()
        load {
            when (val result = TransitApi.searchLines(query)) {
                is TransitData.Ready -> {
                    busLines = result.data
                    busMessage = if (result.data.isEmpty()) "Nenhuma linha encontrada para \"$query\"." else null
                }
                is TransitData.Message -> busMessage = result.text
            }
        }
    }

    fun selectLine(line: BusLine) {
        selectedLine = line
        selectedStop = null
        arrivals = null
        stopArrivalsUpdates()
        load {
            when (val result = TransitApi.stops(line.code)) {
                is TransitData.Ready -> {
                    stops = result.data
                    busMessage = if (result.data.isEmpty()) "Esta linha não tem paradas cadastradas." else null
                }
                is TransitData.Message -> busMessage = result.text
            }
        }
    }

    /** Abre a previsão da parada e atualiza a cada 30 s enquanto ela estiver aberta. */
    fun selectStop(stop: BusStop) {
        selectedStop = stop
        arrivals = null
        stopArrivalsUpdates()
        arrivalsJob = viewModelScope.launch {
            busLoading = true
            while (isActive) {
                when (val result = TransitApi.arrivals(stop.code)) {
                    is TransitData.Ready -> {
                        arrivals = result.data
                        busMessage = null
                    }
                    is TransitData.Message -> busMessage = result.text
                }
                busLoading = false
                delay(30_000)
            }
        }
    }

    fun backToStops() {
        stopArrivalsUpdates()
        selectedStop = null
        arrivals = null
    }

    fun backToLines() {
        stopArrivalsUpdates()
        selectedLine = null
        selectedStop = null
        stops = emptyList()
        arrivals = null
    }

    private fun resetSelection() {
        backToLines()
        busLines = emptyList()
        busMessage = null
    }

    private fun stopArrivalsUpdates() {
        arrivalsJob?.cancel()
        arrivalsJob = null
    }

    private fun load(block: suspend () -> Unit) {
        viewModelScope.launch {
            busLoading = true
            block()
            busLoading = false
        }
    }
}
