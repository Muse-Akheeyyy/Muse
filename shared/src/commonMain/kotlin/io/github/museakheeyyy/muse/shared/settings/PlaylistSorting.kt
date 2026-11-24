package io.github.museakheeyyy.muse.shared.settings

import io.github.museakheeyyy.muse.shared.model.CollectionSort
import io.github.museakheeyyy.muse.shared.model.PlaylistSongSort
import io.github.museakheeyyy.muse.shared.model.SortDirection
import kotlinx.serialization.Serializable

@Serializable
data class SavedPlaylistSongSorting(
    val sort: PlaylistSongSort,
    val direction: SortDirection,
)

@Serializable
data class SavedPlaylistCollectionSorting(
    val sort: CollectionSort,
    val direction: SortDirection,
)
