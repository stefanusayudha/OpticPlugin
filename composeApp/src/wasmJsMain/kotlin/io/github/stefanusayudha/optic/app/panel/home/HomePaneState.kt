package io.github.stefanusayudha.optic.app.panel.home

import io.github.stefanusayudha.optic.app.business.entity.News
import io.github.stefanusayudha.optic.app.panel.home.HomePaneState.NewsSection
import io.github.stefanusayudha.optic.GenerateLens
import io.github.stefanusayudha.optic.Lens

data class HomePaneState(
    @GenerateLens
    val newsSection: NewsSection = NewsSection(),
) {

    data class NewsSection(
        val news: List<News> = emptyList(),
        @GenerateLens
        val showLoading: Boolean = false,
        val errorMessage: String? = null,
    )
}

// #BEGIN Auto Generated Lenses, Do not Modify!
// Powered by: Singularity Indonesia

object  HomePaneStateLens {
    val NewsSectionLens = object : Lens< HomePaneState, NewsSection> {
        override fun modify(
            parent:  HomePaneState,
            children: (NewsSection) -> NewsSection
        ):  HomePaneState = parent.copy(newsSection = children.invoke(parent.newsSection))
    
        override fun get(parent:  HomePaneState): NewsSection = parent.newsSection
    }
}

object  NewsSectionLens {
    val ShowLoadingLens = object : Lens< NewsSection, Boolean> {
        override fun modify(
            parent:  NewsSection,
            children: (Boolean) -> Boolean
        ):  NewsSection = parent.copy(showLoading = children.invoke(parent.showLoading))
    
        override fun get(parent:  NewsSection): Boolean = parent.showLoading
    }
}
// #END Auto Generated Lenses, Do not Modify!