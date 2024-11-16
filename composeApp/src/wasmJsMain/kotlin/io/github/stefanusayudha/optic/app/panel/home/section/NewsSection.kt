package io.github.stefanusayudha.optic.app.panel.home.section

import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import io.github.stefanusayudha.optic.app.panel.home.HomePaneIntent
import io.github.stefanusayudha.optic.app.panel.home.HomePaneState


@Composable
fun NewsSection(
    newsState: HomePaneState.NewsSection,
    onIntent: (HomePaneIntent) -> Unit
) {
    Column {
        Text("News")

        if (newsState.errorMessage != null)
            Text(newsState.errorMessage)

        if (newsState.showLoading)
            CircularProgressIndicator(
                color = MaterialTheme.colors.primary
            )
    }
}