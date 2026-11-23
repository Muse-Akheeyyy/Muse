package io.github.museakheeyyy.muse.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.museakheeyyy.muse.shared.model.HomeSection
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.utils.withArtworkCacheRevision
import io.github.museakheeyyy.muse.shared.viewmodels.BrowseViewModel

class HomeSectionViewModel(
    section: HomeSection,
) : ViewModel() {
    val shared = BrowseViewModel(section.browseId, section.params, section.title, viewModelScope, initialSection = section)
    private var artworkCacheRevision = System.nanoTime()

    fun updateSongMetadata(song: SongItem) {
        shared.updateSongMetadata(song.copy(thumbnail = song.thumbnail.withArtworkCacheRevision(++artworkCacheRevision)))
    }
}
