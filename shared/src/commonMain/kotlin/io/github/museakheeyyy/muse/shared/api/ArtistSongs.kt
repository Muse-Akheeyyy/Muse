package io.github.museakheeyyy.muse.shared.api

import io.github.museakheeyyy.muse.shared.model.ArtistPageResult
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.model.YTItem
import io.github.museakheeyyy.muse.shared.model.isPodcast
import io.github.museakheeyyy.muse.shared.model.isVideoSong
import io.github.museakheeyyy.muse.shared.settings.BlockedContent

internal suspend fun loadArtistSongs(
    page: ArtistPageResult,
    loadItems: suspend (String, String?) -> List<YTItem>,
): List<SongItem> {
    val section =
        page.topTracksSection
            ?: page.sections.entries
                .firstOrNull { (_, items) ->
                    items.filterIsInstance<SongItem>().any { !it.isVideoSong && !it.isPodcast && it.album != null }
                }?.key
            ?: page.sections.entries
                .firstOrNull { (_, items) ->
                    items.isNotEmpty() && items.all { it is SongItem && !it.isVideoSong && !it.isPodcast }
                }?.key
            ?: return emptyList()
    val loaded =
        page.sectionBrowseIds[section]
            ?.let { (id, params) ->
                id?.let { loadItems(it, params) }
            } ?: page.sections[section].orEmpty()
    val songs =
        loaded
            .filterIsInstance<SongItem>()
            .filterNot { it.isVideoSong || it.isPodcast }
            .distinctBy { it.id }
    return BlockedContent.filterSongs(songs)
}
