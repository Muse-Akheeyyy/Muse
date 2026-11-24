package io.github.museakheeyyy.muse.android.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.museakheeyyy.muse.android.LocalMiniPlayerBottomInset
import io.github.museakheeyyy.muse.android.playback.LocalPlayerConnection
import io.github.museakheeyyy.muse.android.ui.menu.SongMenu
import io.github.museakheeyyy.muse.android.viewmodels.HistoryError
import io.github.museakheeyyy.muse.android.viewmodels.HistoryViewModel
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.delete
import io.github.museakheeyyy.muse.shared.generated.resources.delete_failed
import io.github.museakheeyyy.muse.shared.generated.resources.edit
import io.github.museakheeyyy.muse.shared.generated.resources.error_unknown
import io.github.museakheeyyy.muse.shared.model.BuiltinPlaylist
import io.github.museakheeyyy.muse.shared.model.HistoryEntryUi
import io.github.museakheeyyy.muse.shared.model.toSongItem
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.state.SongLikeState
import io.github.museakheeyyy.muse.shared.ui.components.PlayerMenuAction
import io.github.museakheeyyy.muse.shared.ui.components.PopupContent
import io.github.museakheeyyy.muse.shared.ui.icons.MetrolistIcons
import io.github.museakheeyyy.muse.shared.ui.menu.BuiltinPlaylistMenuButton
import io.github.museakheeyyy.muse.shared.ui.screens.DeleteHistoryDialog
import io.github.museakheeyyy.muse.shared.ui.screens.EditHistoryDialog
import io.github.museakheeyyy.muse.shared.ui.screens.HistoryContent
import io.github.museakheeyyy.muse.shared.utils.formatHistoryMonth
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    LaunchedEffect(
        Settings.shared.innerTubeCookie,
        Settings.shared.dataSyncId,
        Settings.shared.innerTubeAuthUser,
        Settings.shared.ytmSync,
        Settings.shared.pauseListenHistory,
    ) { viewModel.loadHistory() }
    val uiState by viewModel.uiState.collectAsState()
    val playerConnection = LocalPlayerConnection.current ?: return
    var menuEntry by remember { mutableStateOf<HistoryEntryUi?>(null) }
    var editEntry by remember { mutableStateOf<HistoryEntryUi?>(null) }
    var deleteEntry by remember { mutableStateOf<HistoryEntryUi?>(null) }
    val entries = uiState.uiEntries
    val displayedEntries = uiState.uiDisplayedEntries

    HistoryContent(
        headerActions = { BuiltinPlaylistMenuButton(BuiltinPlaylist.HISTORY, koinInject<MusicDatabase>()) },
        entries = entries,
        onLoadMore = if (uiState.hasMore) viewModel::loadMore else null,
        displayedEntries = displayedEntries,
        query = uiState.query,
        sortMode = uiState.sortMode,
        sortDirection = uiState.sortDirection,
        onSortDirectionChange = viewModel::setSortDirection,
        selectedIds = uiState.selectedIds.mapTo(mutableSetOf(), Long::toString),
        isLoading = uiState.isLoading,
        isDeleting = uiState.isDeleting,
        error =
            when (uiState.error) {
                HistoryError.DELETE -> stringResource(Res.string.delete_failed)
                HistoryError.LOAD -> stringResource(Res.string.error_unknown)
                null -> null
            },
        canDeleteRemote = uiState.canDeleteRemote,
        onQueryChange = viewModel::setQuery,
        onSortChange = viewModel::setSortMode,
        onRefresh = viewModel::retry,
        onPlay = { playerConnection.playSong(it.toSongItem(), url = "") },
        onSongMenu = { menuEntry = it },
        onToggleSelection = { viewModel.toggleSelection(it.toLong()) },
        onSelectAll = { viewModel.selectAll(it.mapTo(mutableSetOf(), String::toLong)) },
        onUpdate = { entry, title, artists, album ->
            viewModel.updateEntry(entry.id.toLong(), title, artists, album)
        },
        onDelete = { entry, alsoRemote ->
            viewModel.deleteEntry(entry.id.toLong(), deleteRemote = alsoRemote)
        },
        onDeleteSelected = { ids, alsoRemote ->
            viewModel.deleteEntries(ids.mapTo(mutableSetOf(), String::toLong), deleteRemote = alsoRemote)
        },
        formatMonth = { formatHistoryMonth(it) },
        onNavigateBack = onNavigateBack,
        showInlineEntryMenu = false,
        contentPadding = PaddingValues(bottom = LocalMiniPlayerBottomInset.current),
    )
    menuEntry?.let { entry ->
        val song = entry.toSongItem()
        SongMenu(
            song = song,
            isLiked = SongLikeState.isLiked(song.id),
            onDismiss = { menuEntry = null },
            onPlayNext = { playerConnection.addToQueue(song, playNext = true) },
            onAddToQueue = { playerConnection.addToQueue(song) },
            onLike = { SongLikeState.toggleLike(song) },
            showEditAction = false,
            leadingPrimaryActions =
                listOf(
                    PlayerMenuAction(MetrolistIcons.Outlined.Edit, stringResource(Res.string.edit)) {
                        menuEntry = null
                        editEntry = entry
                    },
                ),
            trailingPrimaryActions =
                listOf(
                    PlayerMenuAction(MetrolistIcons.Outlined.Delete, stringResource(Res.string.delete)) {
                        menuEntry = null
                        deleteEntry = entry
                    },
                ),
        )
    }
    PopupContent(editEntry) { entry ->
        EditHistoryDialog(
            entry = entry,
            onDismiss = { editEntry = null },
            onSave = { title, artists, album ->
                viewModel.updateEntry(entry.id.toLong(), title, artists, album)
                editEntry = null
            },
        )
    }
    PopupContent(deleteEntry) { entry ->
        DeleteHistoryDialog(
            entry = entry,
            canDeleteRemote = uiState.canDeleteRemote && !entry.remoteFeedbackToken.isNullOrBlank(),
            onDismiss = { deleteEntry = null },
            onDelete = { alsoRemote ->
                viewModel.deleteEntry(entry.id.toLong(), deleteRemote = alsoRemote)
                deleteEntry = null
            },
        )
    }
}
