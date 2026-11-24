package io.github.museakheeyyy.muse.shared.settings

import io.github.museakheeyyy.muse.shared.model.SortDirection
import kotlinx.serialization.Serializable

@Serializable
data class LibrarySorting(
    val sort: LibraryContentSort,
    val direction: SortDirection,
)
