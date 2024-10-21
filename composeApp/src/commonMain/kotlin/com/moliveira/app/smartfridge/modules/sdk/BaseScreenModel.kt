package com.moliveira.app.smartfridge.modules.sdk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseScreenModel<S : Any, E : Any>(initialState: S) : ViewModel() {
    protected val viewModelScope = CoroutineScope(Job())

    val uiStateFlow: StateFlow<S> by lazy {
        uiStateProvider
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2000),
                initialValue = initialState
            )
    }

    private val _uiEffects = MutableSharedFlow<E>()
    val uiEffects: SharedFlow<E> = _uiEffects

    protected abstract val uiStateProvider: Flow<S>

    fun sendUiEffectAsync(uiEffect: E) {
        viewModelScope.launch {
            _uiEffects.emit(uiEffect)
        }
    }

    suspend fun sendUiEffect(effect: E) {
        _uiEffects.emit(effect)
    }

//    override fun onDispose() {
//        viewModelScope.cancel()
//        super.onDispose()
//    }
}

@Composable
fun <S : Any, E : Any> BaseScreenModel<S, E>.ObserveUiEffect(block: (E) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(key1 = this) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            this@ObserveUiEffect.uiEffects.onEach { effect ->
                block(effect)
            }.launchIn(this)
        }
    }
}