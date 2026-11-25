package io.github.museakheeyyy.muse.desktop.state.playback

import io.github.museakheeyyy.muse.shared.cast.CastState
import io.github.museakheeyyy.muse.shared.model.RadioQueuePage
import io.github.museakheeyyy.muse.shared.model.SongItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

internal data class DesktopRadioBootstrap(
    val page: RadioQueuePage,
    val classic: Boolean,
)

internal class DesktopRadioPlayback(
    private val scope: CoroutineScope,
    private val beginTransition: (preserveCurrent: Boolean) -> Long,
    private val replaceQueue: (SongItem, Long) -> Unit,
    private val play: suspend (SongItem, Long, Boolean, Long) -> Unit,
    private val recommendations: suspend (SongItem, Long) -> DesktopRadioBootstrap?,
    private val appendRecommendations: (DesktopRadioBootstrap?, Long) -> Unit,
) {
    fun start(
        song: SongItem,
        localPrepared: Boolean,
        remote: CastState,
        startPlaying: Boolean = true,
    ): Job {
        val preserveCurrent = localPrepared && !remote.isConnected
        val preserveCastPosition = remote.isConnected && remote.queueEntry?.song?.id == song.id
        val generation = beginTransition(preserveCurrent)
        replaceQueue(song, generation)
        return scope.launch {
            val next =
                async(Dispatchers.IO) {
                    try {
                        recommendations(song, generation)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        null
                    }
                }
            if (!preserveCurrent) {
                play(
                    song,
                    generation,
                    if (preserveCastPosition) remote.isPlaying else startPlaying,
                    if (preserveCastPosition) remote.positionMs else 0L,
                )
            }
            appendRecommendations(next.await(), generation)
        }
    }
}
