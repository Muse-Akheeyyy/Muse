package io.github.museakheeyyy.muse.desktop.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.museakheeyyy.muse.desktop.state.history.HistoryScreenState
import io.github.museakheeyyy.muse.desktop.state.playback.PlayerState
import io.github.museakheeyyy.muse.desktop.ui.components.SongContextMenu
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.delete_failed
import io.github.museakheeyyy.muse.shared.generated.resources.error_occurred
import io.github.museakheeyyy.muse.shared.model.BuiltinPlaylist
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.model.defaultDirection
import io.github.museakheeyyy.muse.shared.model.toSongItem
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.ui.menu.BuiltinPlaylistMenuButton
import io.github.museakheeyyy.muse.shared.ui.menu.cursorContextMenu
import io.github.museakheeyyy.muse.shared.ui.menu.rememberCursorContextMenuState
import io.github.museakheeyyy.muse.shared.ui.screens.HistoryContent
import io.github.museakheeyyy.muse.shared.utils.formatHistoryMonth
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HistoryScreen() {
    val state = remember { HistoryScreenState() }
    val scope = rememberCoroutineScope()
    val songMenu = rememberCursorContextMenuState()
    var menuSong by remember { mutableStateOf<SongItem?>(null) }

    LaunchedEffect(state.query) {
        delay(300.milliseconds)
        state.debouncedQuery = state.query
    }
    LaunchedEffect(
        state.refreshKey,
        state.debouncedQuery,
        state.sortMode,
        state.sortDirection,
        Settings.shared.innerTubeCookie,
        Settings.shared.dataSyncId,
        Settings.shared.innerTubeAuthUser,
        Settings.shared.ytmSync,
        Settings.shared.pauseListenHistory,
    ) { state.loadHistory(scope) }

    LaunchedEffect(state.entries) { state.preparePresentation() }
    LaunchedEffect(state.selectedIds, state.entries) { state.prepareSelection() }

    HistoryContent(
        headerActions = { BuiltinPlaylistMenuButton(BuiltinPlaylist.HISTORY, GlobalContext.get().get<MusicDatabase>()) },
        modifier = Modifier.cursorContextMenu(songMenu, openOnSecondaryClick = false),
        entries = state.uiEntries,
        displayedEntries = state.displayedEntries,
        query = state.query,
        onLoadMore = if (state.hasMore) ({ state.loadMore(scope) }) else null,
        sortMode = state.sortMode,
        sortDirection = state.sortDirection,
        onSortDirectionChange = { state.sortDirection = it },
        selectedIds = state.selectedIds.mapTo(mutableSetOf(), Long::toString),
        isLoading = state.isLoading,
        isDeleting = state.isDeleting,
        error =
            when {
                state.deletionFailed -> stringResource(Res.string.delete_failed)
                state.loadFailed -> stringResource(Res.string.error_occurred)
                else -> null
            },
        canDeleteRemote = state.canDeleteRemote,
        selectedHasRemoteEntries = state.selectedHasRemoteEntries,
        onQueryChange = { state.query = it },
        onSortChange = {
            state.sortMode = it
            state.sortDirection = it.defaultDirection
        },
        onRefresh = { state.refreshKey++ },
        onPlay = { PlayerState.playFromId(it.songId) },
        onSongMenu = {
            menuSong = it.toSongItem()
            songMenu.open()
        },
        onToggleSelection = { state.toggleSelection(it.toLong()) },
        onSelectAll = { state.selectAll(it.mapTo(mutableSetOf(), String::toLong), scope) },
        onUpdate = { entry, title, artists, album ->
            state.entries.firstOrNull { it.id.toString() == entry.id }?.let {
                state.updateEntry(it, title, artists, album, scope)
            }
        },
        onDelete = { entry, alsoRemote ->
            state.entries.firstOrNull { it.id.toString() == entry.id }?.let {
                state.deleteEntry(it, alsoRemote, scope)
            }
        },
        onDeleteSelected = { ids, alsoRemote ->
            state.deleteEntries(ids.mapTo(mutableSetOf(), String::toLong), alsoRemote, scope)
        },
        formatMonth = { formatHistoryMonth(it) },
        selectionBottomPadding = 0.dp,
        contentPadding = PaddingValues(bottom = 100.dp),
    )
    menuSong?.let { song ->
        SongContextMenu(song = song, expanded = songMenu.expanded, offset = songMenu.offset, onDismiss = {
            songMenu.dismiss()
        })
    }
}
