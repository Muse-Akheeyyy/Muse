/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package io.github.museakheeyyy.muse.android.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import io.github.museakheeyyy.muse.android.playback.LocalPlayerConnection
import io.github.museakheeyyy.muse.android.ui.menu.AlbumMenu
import io.github.museakheeyyy.muse.android.ui.menu.ArtistMenu
import io.github.museakheeyyy.muse.android.ui.menu.CollectionExportDialog
import io.github.museakheeyyy.muse.android.ui.menu.PlaylistMenu
import io.github.museakheeyyy.muse.android.ui.menu.PodcastMenu
import io.github.museakheeyyy.muse.android.ui.menu.SongDetailsSheet
import io.github.museakheeyyy.muse.android.ui.menu.SongMenu
import io.github.museakheeyyy.muse.android.ui.utils.playSong
import io.github.museakheeyyy.muse.android.utils.startActivitySafely
import io.github.museakheeyyy.muse.shared.api.musicvideo.MusicVideoResolver
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.db.refreshHistoryPlaylist
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.home_load_item_failed
import io.github.museakheeyyy.muse.shared.generated.resources.metadata_refreshed
import io.github.museakheeyyy.muse.shared.generated.resources.no_newer_metadata
import io.github.museakheeyyy.muse.shared.generated.resources.no_playable_items
import io.github.museakheeyyy.muse.shared.generated.resources.share_album
import io.github.museakheeyyy.muse.shared.generated.resources.share_artist
import io.github.museakheeyyy.muse.shared.generated.resources.share_playlist
import io.github.museakheeyyy.muse.shared.generated.resources.share_podcast
import io.github.museakheeyyy.muse.shared.generated.resources.share_song
import io.github.museakheeyyy.muse.shared.model.AlbumItem
import io.github.museakheeyyy.muse.shared.model.ArtistItem
import io.github.museakheeyyy.muse.shared.model.BuiltinPlaylist
import io.github.museakheeyyy.muse.shared.model.EpisodeItem
import io.github.museakheeyyy.muse.shared.model.PlaylistItem
import io.github.museakheeyyy.muse.shared.model.PodcastItem
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.model.YTItem
import io.github.museakheeyyy.muse.shared.model.refetchSongMetadata
import io.github.museakheeyyy.muse.shared.model.toSongItem
import io.github.museakheeyyy.muse.shared.state.SongLikeState
import io.github.museakheeyyy.muse.shared.ui.components.PopupContent
import io.github.museakheeyyy.muse.shared.ui.menu.BuiltinPlaylistMenu
import io.github.museakheeyyy.muse.shared.ui.menu.loadMediaItemSongs
import io.github.museakheeyyy.muse.shared.utils.ShareUrls
import io.github.museakheeyyy.muse.shared.utils.evictArtworkCache
import io.github.museakheeyyy.muse.shared.youtube.playback.PlaybackNetworkStateStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import io.github.museakheeyyy.muse.shared.db.entities.toSongItem as storedPlaylistSongToSongItem

@Composable
internal fun HomeItemMenu(
    selectedItem: YTItem?,
    onDismiss: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToPodcast: (String) -> Unit,
    onSongMetadataUpdated: (SongItem) -> Unit,
    active: Boolean = true,
) {
    val shareSongText = stringResource(Res.string.share_song)
    val sharePlaylistText = stringResource(Res.string.share_playlist)
    val shareAlbumText = stringResource(Res.string.share_album)
    val shareArtistText = stringResource(Res.string.share_artist)
    val sharePodcastText = stringResource(Res.string.share_podcast)
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current
    val database: MusicDatabase = koinInject()
    val musicVideoResolver: MusicVideoResolver = koinInject()
    val imageLoader: ImageLoader = koinInject()
    val scope = rememberCoroutineScope()
    var detailsSong by remember { mutableStateOf<SongItem?>(null) }
    var exportItem by remember { mutableStateOf<YTItem?>(null) }

    fun share(
        url: String,
        chooserTitle: String,
    ) {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
            }
        context.startActivitySafely(
            Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    fun withCollectionSongs(
        item: YTItem,
        action: (List<SongItem>) -> Unit,
    ) {
        scope.launch {
            val songs =
                runCatching {
                    if (item is PlaylistItem && item.isLocal && PlaybackNetworkStateStore.current.connected) {
                        database.refreshHistoryPlaylist(item.id)
                        database
                            .dao()
                            .playlistSongs(item.id)
                            .first()
                            .map { it.storedPlaylistSongToSongItem() }
                    } else {
                        loadMediaItemSongs(item)
                    }
                }.getOrElse {
                    Toast
                        .makeText(
                            context,
                            getString(Res.string.home_load_item_failed, item.title),
                            Toast.LENGTH_SHORT,
                        ).show()
                    return@launch
                }
            if (songs.isEmpty()) {
                Toast.makeText(context, getString(Res.string.no_playable_items), Toast.LENGTH_SHORT).show()
            } else {
                action(songs)
            }
        }
    }

    fun queueCollection(
        item: YTItem,
        playNext: Boolean,
    ) {
        withCollectionSongs(item) { songs ->
            playerConnection?.addToQueue(songs, playNext)
        }
    }

    PopupContent(selectedItem?.takeIf { active }) { item ->
        when (item) {
            is SongItem, is EpisodeItem -> {
                val song = item as? SongItem ?: (item as EpisodeItem).toSongItem()
                SongMenu(
                    song = song,
                    isLiked = SongLikeState.isLiked(song.id),
                    onDismiss = { onDismiss() },
                    onPlay = { playSong(song, playerConnection, scope, startVideo = false) },
                    onStartRadio = { playerConnection?.startRadio(song) },
                    onPlayNext = { playerConnection?.addToQueue(song, playNext = true) },
                    onAddToQueue = { playerConnection?.addToQueue(song) },
                    onLike = { SongLikeState.toggleLike(song) },
                    onShare = {
                        share(
                            ShareUrls.song(song.id),
                            shareSongText,
                        )
                    },
                    onGoToAlbum =
                        if (item is EpisodeItem) {
                            item.podcastId?.let { podcastId -> { onNavigateToPodcast(podcastId) } }
                        } else {
                            song.album?.id?.let { albumId -> { onNavigateToAlbum(albumId) } }
                        },
                    onGoToArtist = onNavigateToArtist,
                    onShowInfo = { detailsSong = song },
                    onRefetch = {
                        scope.launch {
                            val refreshed = refetchSongMetadata(database, musicVideoResolver, song)
                            if (refreshed != null) {
                                imageLoader.evictArtworkCache(song.thumbnail)
                                imageLoader.evictArtworkCache(refreshed.thumbnail)
                                onSongMetadataUpdated(refreshed)
                            }
                            Toast
                                .makeText(
                                    context,
                                    getString(if (refreshed != null) Res.string.metadata_refreshed else Res.string.no_newer_metadata),
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    },
                )
            }

            is PlaylistItem -> {
                if (item.id.startsWith("auto:") && BuiltinPlaylist.fromId(item.id) != null) {
                    BuiltinPlaylistMenu(item, database) { onDismiss() }
                } else {
                    PlaylistMenu(
                        playlist = item,
                        onDismiss = { onDismiss() },
                        onPlay = { playerConnection?.playPlaylist(item.id, item.title) },
                        onShuffle = {
                            playerConnection?.playPlaylist(item.id, item.title, shuffle = true)
                        },
                        onShare = {
                            share(
                                ShareUrls.playlist(item.id),
                                sharePlaylistText,
                            )
                        },
                        onPlayNext = { queueCollection(item, playNext = true) },
                        onAddToQueue = { queueCollection(item, playNext = false) },
                        onExport = { exportItem = item },
                    )
                }
            }

            is AlbumItem -> {
                AlbumMenu(
                    album = item,
                    onDismiss = { onDismiss() },
                    onPlay = { playerConnection?.playAlbum(item.id, item.title) },
                    onShuffle = {
                        playerConnection?.playAlbum(item.id, item.title, shuffle = true)
                    },
                    onShare = {
                        share(
                            ShareUrls.album(item.id),
                            shareAlbumText,
                        )
                    },
                    onPlayNext = { queueCollection(item, playNext = true) },
                    onAddToQueue = { queueCollection(item, playNext = false) },
                    onGoToArtist = onNavigateToArtist,
                    onExport = { exportItem = item },
                )
            }

            is ArtistItem -> {
                ArtistMenu(
                    artist = item,
                    onDismiss = { onDismiss() },
                    onShuffle = {
                        withCollectionSongs(item) {
                            playerConnection?.shuffleQueue(it, queueTitle = item.title)
                        }
                    },
                    onShare = {
                        share(
                            ShareUrls.artist(item.id),
                            shareArtistText,
                        )
                    },
                )
            }

            is PodcastItem -> {
                PodcastMenu(
                    podcast = item,
                    onDismiss = { onDismiss() },
                    onPlay = { withCollectionSongs(item) { playerConnection?.playQueue(it, queueTitle = item.title) } },
                    onShuffle = {
                        withCollectionSongs(item) {
                            playerConnection?.shuffleQueue(it, queueTitle = item.title)
                        }
                    },
                    onShare = {
                        share(
                            ShareUrls.podcast(item.id),
                            sharePodcastText,
                        )
                    },
                    onPlayNext = { queueCollection(item, playNext = true) },
                    onAddToQueue = { queueCollection(item, playNext = false) },
                )
            }

            else -> {
                onDismiss()
            }
        }
    }

    detailsSong?.takeIf { active }?.let { song ->
        SongDetailsSheet(song = song, onDismiss = { detailsSong = null })
    }

    exportItem?.takeIf { active }?.let { item ->
        CollectionExportDialog(
            title = item.title,
            onDismiss = { exportItem = null },
            loadSongs = { loadMediaItemSongs(item) },
        )
    }
}
