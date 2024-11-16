package io.github.stefanusayudha.optic.app.panel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.stefanusayudha.optic.app.business.PostBusiness
import io.github.stefanusayudha.optic.app.business.PostBusinessImpl
import io.github.stefanusayudha.optic.core.util.safeUpdate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.coroutineContext

class HomePaneViewModel(
    private val postRepository: PostBusiness = PostBusinessImpl()
) : ViewModel() {
    companion object {
        const val FIVE_MINUTE_MILLIS = 5 * 60 * 1000L
    }

    private val _uiState = MutableStateFlow(HomePaneState())
    val uiState = _uiState.asStateFlow()
        .onStart {
            onIntent(HomePaneIntent.Reload)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(FIVE_MINUTE_MILLIS),
            _uiState.value
        )

    fun onIntent(intent: HomePaneIntent) {
        viewModelScope.launch {
            when (intent) {
                HomePaneIntent.Reload -> onReload()
            }
        }
    }

    // Intents
    private var onReloadSubIntentSuperVisor: CompletableJob? = null
    private suspend fun onReload() = withContext(coroutineContext) {
        onReloadSubIntentSuperVisor?.cancel()
        onReloadSubIntentSuperVisor = SupervisorJob()

        loadNews(onReloadSubIntentSuperVisor!!)
    }

    // Sub Intents
    private fun loadNews(supervisorJob: CompletableJob): Job = viewModelScope.launch(supervisorJob) {
        val showLoadingLens = HomePaneStateLens.NewsSectionLens + NewsSectionLens.ShowLoadingLens
        _uiState.safeUpdate {
            showLoadingLens.modify(it) { true }
        }

        val result = postRepository.getNews()
        val news = result.getOrElse { exception -> emptyList() }
        val errorMessage = result.exceptionOrNull()?.message

        _uiState.safeUpdate {
            HomePaneStateLens.NewsSectionLens.modify(it) { newsSection ->
                newsSection.copy(
                    news = news,
                    errorMessage = errorMessage,
                    showLoading = false
                )
            }
        }
    }

}