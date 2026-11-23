package io.github.museakheeyyy.muse.shared.ui.menu

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.db.refreshStoredPlaylist
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.error_occurred
import io.github.museakheeyyy.muse.shared.generated.resources.ok
import io.github.museakheeyyy.muse.shared.generated.resources.sync_playlist
import io.github.museakheeyyy.muse.shared.model.PlaylistItem
import io.github.museakheeyyy.muse.shared.model.isSyncablePlaylistId
import io.github.museakheeyyy.muse.shared.state.GlobalYouTubeRepository
import io.github.museakheeyyy.muse.shared.ui.components.AlertDialog
import io.github.museakheeyyy.muse.shared.ui.components.PlayerMenuAction
import io.github.museakheeyyy.muse.shared.ui.components.PopupVisibility
import io.github.museakheeyyy.muse.shared.ui.icons.MetrolistIcons
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun rememberPlaylistSyncAction(
    playlist: PlaylistItem,
    onDismiss: () -> Unit,
): PlayerMenuAction? {
    if (playlist.isLocal || !playlist.id.isSyncablePlaylistId()) return null
    val scope = rememberCoroutineScope()
    var syncing by remember(playlist.id) { mutableStateOf(false) }
    var failed by remember(playlist.id) { mutableStateOf(false) }
    val label = stringResource(Res.string.sync_playlist)
    PopupVisibility(failed) {
        AlertDialog(
            onDismissRequest = { failed = false },
            title = { Text(label) },
            text = { Text(stringResource(Res.string.error_occurred)) },
            confirmButton = {
                TextButton(onClick = { failed = false }, shapes = ButtonDefaults.shapes()) {
                    Text(stringResource(Res.string.ok))
                }
            },
        )
    }
    return PlayerMenuAction(
        icon = MetrolistIcons.Outlined.Sync,
        title = label,
        isLoading = syncing,
        enabled = !syncing,
        onClick = {
            if (!syncing) {
                syncing = true
                scope.launch {
                    try {
                        withContext(Dispatchers.Default) {
                            KoinPlatform.getKoin().get<MusicDatabase>().refreshStoredPlaylist(GlobalYouTubeRepository.instance, playlist.id)
                        }
                        onDismiss()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        failed = true
                    } finally {
                        syncing = false
                    }
                }
            }
        },
    )
}
