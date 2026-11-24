@file:Suppress("FunctionName")

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.museakheeyyy.muse.shared.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.museakheeyyy.muse.shared.auth.AuthManager
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.db.preserveKnownPlaylistEditability
import io.github.museakheeyyy.muse.shared.db.selectablePlaylistDetails
import io.github.museakheeyyy.muse.shared.db.syncRemotePlaylists
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.add_anyway
import io.github.museakheeyyy.muse.shared.generated.resources.android_auto_youtube_playlist
import io.github.museakheeyyy.muse.shared.generated.resources.cancel
import io.github.museakheeyyy.muse.shared.generated.resources.create_playlist
import io.github.museakheeyyy.muse.shared.generated.resources.duplicates
import io.github.museakheeyyy.muse.shared.generated.resources.local_playlist
import io.github.museakheeyyy.muse.shared.generated.resources.playlist_name
import io.github.museakheeyyy.muse.shared.generated.resources.playlist_update_failed
import io.github.museakheeyyy.muse.shared.generated.resources.save
import io.github.museakheeyyy.muse.shared.generated.resources.skip
import io.github.museakheeyyy.muse.shared.generated.resources.song_already_in_named_playlist
import io.github.museakheeyyy.muse.shared.model.PlaylistItem
import io.github.museakheeyyy.muse.shared.playlist.LibraryBookmarkMutations
import io.github.museakheeyyy.muse.shared.playlist.storedPlaylistMembership
import io.github.museakheeyyy.muse.shared.settings.HiddenLibraryItems
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.state.GlobalYouTubeRepository
import io.github.museakheeyyy.muse.shared.ui.components.AlertDialog
import io.github.museakheeyyy.muse.shared.ui.components.ModalBottomSheet
import io.github.museakheeyyy.muse.shared.ui.components.PlaylistChooser
import io.github.museakheeyyy.muse.shared.ui.components.PopupContent
import io.github.museakheeyyy.muse.shared.ui.components.PopupVisibility
import io.github.museakheeyyy.muse.shared.ui.components.rememberModalSheetState
import io.github.museakheeyyy.muse.shared.ui.theme.ComponentShapes
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobilePlaylistActions(
    workflow: MobilePlaylistWorkflow,
    database: MusicDatabase,
) {
    if (workflow.song == null && !workflow.creating && workflow.duplicate == null) return
    val userPlaylists by database.dao().editablePlaylistsByCreateDateAsc().collectAsState(initial = null)
    val signedIn = AuthManager().isLoggedIn
    val hiddenVersion by HiddenLibraryItems.version.collectAsState()
    val editablePlaylists =
        remember(userPlaylists, signedIn, hiddenVersion, Settings.shared.hiddenLibraryPlaylistsJson) {
            userPlaylists.orEmpty().selectablePlaylistDetails(signedIn, HiddenLibraryItems.keys())
        }
    val songIds = workflow.songs.mapTo(mutableSetOf()) { it.id }
    var playtimes by remember(editablePlaylists) { mutableStateOf(emptyMap<String, Long?>()) }
    LaunchedEffect(database, editablePlaylists) {
        playtimes = database.dao().playlistLibraryStats(editablePlaylists.map { it.id }).associate { it.playlistId to it.playtime }
    }
    var membership by remember(songIds, editablePlaylists) { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(database, songIds, editablePlaylists) {
        storedPlaylistMembership(
            database,
            GlobalYouTubeRepository.instance,
            editablePlaylists.map { it.playlist },
            songIds,
        ).collect { membership = it }
    }
    LaunchedEffect(workflow.song != null, signedIn) {
        if (workflow.song != null && signedIn && GlobalYouTubeRepository.instance.canManagePlaylists()) {
            try {
                val bookmarkSnapshot = LibraryBookmarkMutations.capture()
                val result = GlobalYouTubeRepository.instance.getUserPlaylistsResult()
                val playlists =
                    database
                        .preserveKnownPlaylistEditability(result.items, result.unresolvedPlaylistEditabilityIds)
                        .filterIsInstance<PlaylistItem>()
                LibraryBookmarkMutations.withLock {
                    database.syncRemotePlaylists(playlists, LibraryBookmarkMutations.newerChoices(bookmarkSnapshot).keys)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
            }
        }
    }
    PopupContent(workflow.song.takeIf { userPlaylists != null }) { _ ->
        ModalBottomSheet(
            onDismissRequest = { workflow.dismissSong() },
            sheetState =
                rememberModalSheetState(
                    enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
                    confirmValueChange = { !workflow.busy || it != SheetValue.Hidden },
                ),
            shape = ComponentShapes.BottomSheetShape,
            dragHandle = {
                Box(
                    Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
                )
            },
        ) {
            // Full height from the first frame, so playlists that load or sync later cannot leave the sheet half open.
            PlaylistChooser(
                playlists = editablePlaylists,
                playtimes = playtimes,
                membership = membership,
                onDismiss = { workflow.dismissSong() },
                onChoosePlaylist = { workflow.togglePlaylist(it.playlist) },
                selectedIds = workflow.selectedPlaylists.mapTo(mutableSetOf()) { it.id },
                completedIds = workflow.completedIds,
                failedIds = workflow.failedIds,
                onAdd = { workflow.addSelected() },
                onCreatePlaylist = { workflow.showCreate() },
                busy = workflow.busy,
                failed = workflow.failed,
                sheet = true,
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }

    workflow.duplicate?.let { playlist ->
        PopupContent(workflow.song) { song ->
            AlertDialog(
                onDismissRequest = { workflow.skipDuplicate() },
                title = { Text(stringResource(Res.string.duplicates)) },
                text = { Text(stringResource(Res.string.song_already_in_named_playlist, song.title, playlist.name)) },
                confirmButton = {
                    TextButton(
                        enabled = !workflow.busy,
                        onClick = {
                            workflow.confirmDuplicate()
                        },
                        shapes = ButtonDefaults.shapes(),
                    ) { Text(stringResource(Res.string.add_anyway)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { workflow.skipDuplicate() },
                        shapes = ButtonDefaults.shapes(),
                    ) { Text(stringResource(Res.string.skip)) }
                },
            )
        }
    }

    PopupVisibility(visible = workflow.creating) {
        AlertDialog(
            onDismissRequest = {
                workflow.dismissCreate()
            },
            title = { Text(stringResource(Res.string.create_playlist)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = workflow.name,
                        onValueChange = { workflow.name = it },
                        label = { Text(stringResource(Res.string.playlist_name)) },
                        singleLine = true,
                        enabled = !workflow.busy,
                    )
                    if (workflow.remoteCreationAvailable) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth().toggleable(
                                    value = workflow.createLocally,
                                    role = Role.Checkbox,
                                    enabled = !workflow.busy,
                                    onValueChange = { workflow.createLocally = it },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = workflow.createLocally,
                                onCheckedChange = null,
                                enabled = !workflow.busy,
                            )
                            Text(stringResource(Res.string.local_playlist))
                        }
                    }
                    if (!workflow.remoteCreationAvailable || !workflow.createLocally) {
                        Text(
                            stringResource(
                                if (workflow.createLocally) Res.string.local_playlist else Res.string.android_auto_youtube_playlist,
                            ),
                        )
                    }
                    if (workflow.busy) LinearWavyProgressIndicator(Modifier.fillMaxWidth())
                    if (workflow.failed) {
                        Text(stringResource(Res.string.playlist_update_failed), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = workflow.name.isNotBlank() && !workflow.busy,
                    onClick = {
                        workflow.createPlaylist()
                    },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        workflow.dismissCreate()
                    },
                    enabled = !workflow.busy,
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}
