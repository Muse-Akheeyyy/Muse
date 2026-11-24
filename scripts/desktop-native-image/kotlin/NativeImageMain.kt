package io.github.museakheeyyy.muse.desktop.experiment

import io.github.museakheeyyy.muse.desktop.state.playback.PlayerState
import io.github.museakheeyyy.muse.desktop.utils.DesktopUrlHandler
import io.github.museakheeyyy.muse.shared.model.SongItem
import java.io.File
import kotlin.concurrent.thread
import io.github.museakheeyyy.muse.desktop.main as desktopMain

fun main(args: Array<String>) {
    val media = System.getProperty("metrolist.experiment.media")
    if (media != null) {
        val trigger = File(requireNotNull(System.getProperty("metrolist.experiment.trigger")))
        thread(name = "native-image-playback-probe", isDaemon = true) {
            try {
                while (!trigger.isFile) Thread.sleep(50)
                if (DesktopUrlHandler.canHandle(media)) {
                    check(DesktopUrlHandler.handleUrl(media))
                } else {
                    val file = File(media)
                    require(file.isFile)
                    val song =
                        SongItem(
                            id = file.toURI().toString(),
                            title = file.name,
                            thumbnail = null,
                            artists = emptyList(),
                            album = null,
                            isLocal = true,
                        )
                    PlayerState.player.play(song, file.absolutePath)
                }
                while (true) {
                    val player = PlayerState.player
                    println(
                        "[NativeImagePlayback] positionMs=${player.currentPlaybackPositionMs()} " +
                            "durationMs=${player.duration.value} playing=${player.isPlaying.value} " +
                            "ready=${player.playbackReady} error=${player.loadError.value}",
                    )
                    Thread.sleep(1_000)
                }
            } catch (error: Throwable) {
                System.err.println("[NativeImagePlayback] failed=${error.javaClass.name}")
                error.printStackTrace()
            }
        }
    }
    desktopMain(args)
}
