package io.github.stefanusayudha.optic.app.panel.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.stefanusayudha.optic.app.panel.home.section.BodySection

@Composable
fun HomePane() {
    val viewModel: HomePaneViewModel = viewModel { HomePaneViewModel() }
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    HomePane(
        state = state.value,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun HomePane(
    state: HomePaneState,
    onIntent: (HomePaneIntent) -> Unit
) {
    Scaffold(
        bottomBar = {
            Row {
                Button(
                    onClick = {
                        onIntent(HomePaneIntent.Reload)
                    }
                ) {
                    Text("Reload")
                }
            }
        }
    ) { paddingValues ->
        BodySection(
            modifier = Modifier.padding(paddingValues),
            newsState = state.newsSection,
            onIntent = onIntent
        )
    }
}
