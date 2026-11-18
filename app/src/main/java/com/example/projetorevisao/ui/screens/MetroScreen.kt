package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.TransitViewModel
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

/** Mobilidade: só dados oficiais (ARTESP para trilhos concedidos, SPTrans para ônibus). */
@Composable
fun CitizenMetroScreen(
    @Suppress("UNUSED_PARAMETER") viewModel: CitizenViewModel = rememberCitizenViewModel(),
    transitViewModel: TransitViewModel = remember { TransitViewModel() }
) {
    LaunchedEffect(Unit) { transitViewModel.refreshRail() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        transitLiveSection(transitViewModel)

    }
}
