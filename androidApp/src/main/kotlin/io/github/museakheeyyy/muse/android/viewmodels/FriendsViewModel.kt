package io.github.museakheeyyy.muse.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.museakheeyyy.muse.shared.viewmodels.FRIENDS_REFRESH_INTERVAL_MS
import io.github.museakheeyyy.muse.shared.viewmodels.FriendsViewModel

class FriendsViewModel : ViewModel() {
    val sharedViewModel =
        FriendsViewModel(viewModelScope)
    val uiState = sharedViewModel.uiState

    fun startPolling(intervalMs: Long = FRIENDS_REFRESH_INTERVAL_MS) = sharedViewModel.startPolling(intervalMs)

    fun stopPolling() = sharedViewModel.stopPolling()

    override fun onCleared() {
        sharedViewModel.stopPolling()
    }
}
