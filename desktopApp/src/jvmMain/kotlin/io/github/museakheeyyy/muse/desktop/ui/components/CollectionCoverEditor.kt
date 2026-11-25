package io.github.museakheeyyy.muse.desktop.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.museakheeyyy.muse.desktop.utils.pickDesktopFile
import io.github.museakheeyyy.muse.desktop.utils.playlistCoverExtensions
import io.github.museakheeyyy.muse.desktop.utils.readPlaylistCover
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.edit_playlist_cover
import io.github.museakheeyyy.muse.shared.playlist.storeCollectionCover
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.ui.components.CollectionCoverDialog
import io.github.museakheeyyy.muse.shared.utils.XdgUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.io.File

@Composable
fun CollectionCoverEditor(
    type: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var busy by remember(type) { mutableStateOf(false) }
    var failed by remember(type) { mutableStateOf(false) }
    val directory = remember { File(XdgUtils.getXdgDataHome(), "metrolist/playlist_covers") }
    val title = stringResource(Res.string.edit_playlist_cover)

    fun save(source: File?) {
        scope.launch {
            busy = true
            failed = false
            try {
                storeCollectionCover(type, directory, source?.let { { readPlaylistCover(it) } })
                onDismiss()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                failed = true
            } finally {
                busy = false
            }
        }
    }

    CollectionCoverDialog(
        hasCover = Settings.shared.collectionCovers[type] != null,
        busy = busy,
        failed = failed,
        onChoose = {
            scope.launch {
                val file =
                    try {
                        pickDesktopFile(title, playlistCoverExtensions.toList())
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        failed = true
                        return@launch
                    }
                file?.let(::save)
            }
        },
        onRemove = { save(null) },
        onDismiss = onDismiss,
    )
}
