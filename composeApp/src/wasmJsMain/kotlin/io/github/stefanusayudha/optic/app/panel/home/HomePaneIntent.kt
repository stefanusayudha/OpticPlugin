package io.github.stefanusayudha.optic.app.panel.home

sealed class HomePaneIntent {
    data object Reload : HomePaneIntent()
}
