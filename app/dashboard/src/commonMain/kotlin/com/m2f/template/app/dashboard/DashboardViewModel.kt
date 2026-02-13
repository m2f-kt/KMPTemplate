package com.m2f.template.app.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel providing static mock data for the dashboard screen.
 *
 * Simulates a brief loading delay on init, then exposes a [DashboardState]
 * with pre-defined mock metrics, processes, activity, and deployment data.
 */
class DashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(300)
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun selectNavItem(item: String) {
        _state.update { it.copy(selectedNavItem = item) }
    }
}
