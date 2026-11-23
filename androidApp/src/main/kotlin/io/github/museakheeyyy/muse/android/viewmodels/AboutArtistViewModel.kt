package io.github.museakheeyyy.muse.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.museakheeyyy.muse.shared.viewmodels.AboutArtistViewModel as SharedAboutArtistViewModel

class AboutArtistViewModel(
    artistId: String,
    artistName: String,
) : ViewModel() {
    val shared = SharedAboutArtistViewModel(artistId, artistName, viewModelScope)
}
