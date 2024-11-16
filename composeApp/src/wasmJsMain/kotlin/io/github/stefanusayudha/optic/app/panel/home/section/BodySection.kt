package io.github.stefanusayudha.optic.app.panel.home.section

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.stefanusayudha.optic.app.panel.home.HomePaneIntent
import io.github.stefanusayudha.optic.app.panel.home.HomePaneState

@Composable
fun BodySection(
    modifier: Modifier = Modifier,
    newsState: HomePaneState.NewsSection,
    onIntent: (HomePaneIntent) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text("Home")

        NewsSection(
            newsState,
            onIntent
        )
    }
}