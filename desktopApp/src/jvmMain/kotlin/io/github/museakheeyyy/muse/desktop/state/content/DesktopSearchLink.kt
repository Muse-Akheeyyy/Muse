package io.github.museakheeyyy.muse.desktop.state.content

import com.metrolist.innertubex.utils.YouTubeUrlParser
import io.github.museakheeyyy.muse.shared.utils.parseYouTubePodcastId
import io.github.museakheeyyy.muse.shared.utils.parseYouTubeUrl

internal sealed interface DesktopSearchLink {
    data class Podcast(
        val id: String,
    ) : DesktopSearchLink

    data class Media(
        val destination: YouTubeUrlParser.ParsedUrl,
    ) : DesktopSearchLink
}

internal fun resolveDesktopSearchLink(query: String): DesktopSearchLink? =
    parseYouTubePodcastId(query)?.let(DesktopSearchLink::Podcast)
        ?: parseYouTubeUrl(query)?.let(DesktopSearchLink::Media)
