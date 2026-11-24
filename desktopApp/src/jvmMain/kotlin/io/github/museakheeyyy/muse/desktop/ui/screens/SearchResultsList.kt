/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package io.github.museakheeyyy.muse.desktop.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.museakheeyyy.muse.desktop.state.app.AppState
import io.github.museakheeyyy.muse.desktop.state.content.ArtistState
import io.github.museakheeyyy.muse.desktop.state.content.PlaylistAlbumState
import io.github.museakheeyyy.muse.desktop.state.content.PodcastState
import io.github.museakheeyyy.muse.desktop.state.content.SearchState
import io.github.museakheeyyy.muse.desktop.state.playback.PlayerState
import io.github.museakheeyyy.muse.desktop.ui.components.AlbumContextMenu
import io.github.museakheeyyy.muse.desktop.ui.components.ArtistContextMenu
import io.github.museakheeyyy.muse.desktop.ui.components.EpisodeContextMenu
import io.github.museakheeyyy.muse.desktop.ui.components.PlaylistContextMenu
import io.github.museakheeyyy.muse.desktop.ui.components.PodcastContextMenu
import io.github.museakheeyyy.muse.desktop.ui.components.SongContextMenu
import io.github.museakheeyyy.muse.desktop.ui.components.SongSwipeActions
import io.github.museakheeyyy.muse.shared.generated.resources.album
import io.github.museakheeyyy.muse.shared.generated.resources.artist
import io.github.museakheeyyy.muse.shared.generated.resources.episode
import io.github.museakheeyyy.muse.shared.generated.resources.playlist
import io.github.museakheeyyy.muse.shared.generated.resources.podcast
import io.github.museakheeyyy.muse.shared.generated.resources.search
import io.github.museakheeyyy.muse.shared.generated.resources.songs
import io.github.museakheeyyy.muse.shared.model.AlbumItem
import io.github.museakheeyyy.muse.shared.model.ArtistItem
import io.github.museakheeyyy.muse.shared.model.EpisodeItem
import io.github.museakheeyyy.muse.shared.model.PlaylistItem
import io.github.museakheeyyy.muse.shared.model.PodcastItem
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.model.YTItem
import io.github.museakheeyyy.muse.shared.model.toSongItem
import io.github.museakheeyyy.muse.shared.state.GlobalYouTubeRepository
import io.github.museakheeyyy.muse.shared.ui.menu.contextMenuAnchor
import io.github.museakheeyyy.muse.shared.ui.menu.rememberCursorContextMenuState
import io.github.museakheeyyy.muse.shared.ui.screens.SearchScreen
import io.github.museakheeyyy.muse.shared.ui.screens.searchScreenText
import kotlinx.coroutines.launch

@Composable
fun SearchResultsList(
    colorScheme: ColorScheme,
    onBack: () -> Unit,
) {
    val uiState = SearchState.uiState
    var selectedItemForMenu by remember { mutableStateOf<YTItem?>(null) }
    val menuAnchor = rememberCursorContextMenuState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .contextMenuAnchor(menuAnchor),
    ) {
        SearchScreen(
            itemContainer = { item, content ->
                if (item is SongItem) SongSwipeActions(item, content = content) else content()
            },
            uiState = uiState,
            text = searchScreenText(),
            onQueryChange = { SearchState.onQueryChange(it) },
            onSearch = { SearchState.search(it) },
            onRemoveHistoryItem = SearchState::removeSearchQuery,
            onBack = onBack,
            onItemClick = { item ->
                openSearchItem(item, emptyList())
            },
            onItemLongClick = { item ->
                selectedItemForMenu = item
                menuAnchor.open()
            },
            onSongSave = { PlayerState.showAddToPlaylistSong = it },
            onAlbumPlay = { PlaylistAlbumState.playAlbum(it.id) },
            onHistoryItemClick = { query ->
                SearchState.search(query)
            },
            onHistoryItemInsert = { query ->
                SearchState.onQueryChange(query)
            },
            onClearHistory = {
                SearchState.clearSearchHistory()
            },
            onFilterSelected = { filter ->
                SearchState.updateSearchFilter(filter)
            },
            onRetryLoadMore = SearchState::retryLoadMoreFilteredResults,
            onLoadMore = {
                SearchState.loadMoreFilteredResults()
            },
            onArtistClick = { artistId ->
                ArtistState.fetchArtistData(artistId)
            },
            onAlbumClick = { albumId ->
                PlaylistAlbumState.fetchAlbumData(albumId)
            },
            onShuffleClick = { item ->
                if (item is ArtistItem) {
                    AppState.scope.launch {
                        GlobalYouTubeRepository.instance
                            .getArtistSongs(
                                item.id,
                            ).takeIf { it.isNotEmpty() }
                            ?.let {
                                PlayerState.shuffleQueue(it, songsOnly = true)
                            }
                    }
                }
            },
            onMixClick = { item ->
                if (item is ArtistItem) {
                    AppState.scope.launch {
                        GlobalYouTubeRepository.instance
                            .startArtistRadio(
                                item.id,
                            ).takeIf { it.isNotEmpty() }
                            ?.let {
                                PlayerState.playTrack(it.first(), it)
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            showSearchBar = false,
        )

        selectedItemForMenu?.let { item ->
            when (item) {
                is SongItem -> {
                    SongContextMenu(
                        song = item,
                        expanded = menuAnchor.expanded,
                        offset = menuAnchor.offset,
                        onDismiss = menuAnchor::dismiss,
                    )
                }

                is AlbumItem -> {
                    AlbumContextMenu(
                        album = item,
                        expanded = menuAnchor.expanded,
                        offset = menuAnchor.offset,
                        onDismiss = menuAnchor::dismiss,
                    )
                }

                is ArtistItem -> {
                    ArtistContextMenu(
                        artist = item,
                        expanded = menuAnchor.expanded,
                        offset = menuAnchor.offset,
                        onDismiss = menuAnchor::dismiss,
                    )
                }

                is PlaylistItem -> {
                    PlaylistContextMenu(
                        playlist = item,
                        expanded = menuAnchor.expanded,
                        offset = menuAnchor.offset,
                        onDismiss = menuAnchor::dismiss,
                    )
                }

                is PodcastItem -> {
                    PodcastContextMenu(
                        podcast = item,
                        expanded = menuAnchor.expanded,
                        offset = menuAnchor.offset,
                        onDismiss = menuAnchor::dismiss,
                    )
                }

                is EpisodeItem -> {
                    EpisodeContextMenu(
                        episode = item,
                        expanded = menuAnchor.expanded,
                        offset = menuAnchor.offset,
                        onDismiss = menuAnchor::dismiss,
                    )
                }

                else -> {
                    selectedItemForMenu = null
                }
            }
        }
    }
}

private fun openSearchItem(
    item: YTItem,
    songs: List<SongItem>,
) {
    when (item) {
        is SongItem -> PlayerState.playTrack(item, songs.ifEmpty { null })
        is EpisodeItem -> PlayerState.playTrack(item.toSongItem())
        is PodcastItem -> PodcastState.fetch(item.id)
        is AlbumItem -> PlaylistAlbumState.fetchAlbumData(item.id)
        is ArtistItem -> ArtistState.fetchArtistData(item)
        is PlaylistItem -> PlaylistAlbumState.fetchPlaylistData(item.id, item.thumbnail)
        else -> Unit
    }
}
