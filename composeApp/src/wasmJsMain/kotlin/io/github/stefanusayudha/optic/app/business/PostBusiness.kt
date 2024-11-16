package io.github.stefanusayudha.optic.app.business

import io.github.stefanusayudha.optic.app.business.entity.News
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

interface PostBusiness {
    suspend fun getNews(): Result<List<News>>
}

class PostBusinessImpl : PostBusiness {
    override suspend fun getNews(): Result<List<News>> = withContext(Dispatchers.Default) {
        delay(3000)
        runCatching { TODO() }
    }
}