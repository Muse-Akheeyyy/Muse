package io.github.museakheeyyy.muse.shared.api

import io.github.museakheeyyy.muse.shared.model.HomeSection
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.model.isVideoSong

fun isMoodsAndGenresBrowseId(browseId: String?): Boolean = browseId?.startsWith("FEmusic_moods_and_genres") == true

/**
 * Mood and genre pages list their song shelf under a title that YouTube localizes, so the shelf
 * is detected by item type. Music-video shelves also parse as [SongItem], so video items are
 * excluded; only the audio "Songs" shelf moves to the end. Every other section keeps server order.
 */
fun List<HomeSection>.moveSongSectionsLast(): List<HomeSection> {
    if (none { it.isSongShelf() }) return this
    val (songs, others) = partition { it.isSongShelf() }
    return others + songs
}

private fun HomeSection.isSongShelf(): Boolean = items.isNotEmpty() && items.all { it is SongItem && !it.isVideoSong }
