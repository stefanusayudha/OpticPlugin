package io.github.stefanusayudha.optic.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

context(CoroutineScope)
fun <T> MutableStateFlow<T>.safeUpdate(value: (T) -> T) {
    ensureActive()
    update(value)
}