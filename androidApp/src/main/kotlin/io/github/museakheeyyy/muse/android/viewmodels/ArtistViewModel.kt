package io.github.museakheeyyy.muse.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.museakheeyyy.muse.shared.api.artwork.AnimatedArtworkService
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import kotlinx.coroutines.CoroutineScope
import io.github.museakheeyyy.muse.shared.viewmodels.ArtistViewModel as SharedArtistViewModel

class ArtistViewModel(
    artistId: String? = null,
    animatedArtworkService: AnimatedArtworkService,
    database: MusicDatabase,
    applicationScope: CoroutineScope,
) : ViewModel() {
    val shared = SharedArtistViewModel(artistId, viewModelScope, animatedArtworkService, database, applicationScope)
}
