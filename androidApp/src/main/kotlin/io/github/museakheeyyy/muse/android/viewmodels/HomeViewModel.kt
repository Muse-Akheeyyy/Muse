package io.github.museakheeyyy.muse.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.viewmodels.HomeViewModel as SharedHomeViewModel

class HomeViewModel(
    database: MusicDatabase,
    likedSongsName: String,
) : ViewModel() {
    val shared = SharedHomeViewModel(database, viewModelScope, likedSongsName = likedSongsName)
}
