package io.github.museakheeyyy.muse.shared.api

import com.metrolist.innertubex.models.YouTubeDataPage
import io.github.museakheeyyy.muse.shared.model.BrowsePageData
import io.github.museakheeyyy.muse.shared.model.HomeSection
import io.github.museakheeyyy.muse.shared.model.YTItem

suspend fun loadBrowsePage(
    current: BrowsePageData,
    seenTokens: Set<String>,
    loadSections: suspend (String) -> Pair<List<HomeSection>, String?>,
    loadShelf: suspend (String) -> YouTubeDataPage<YTItem>,
    songsLast: Boolean = false,
): BrowsePageData {
    val shelfIndex = current.sections.indexOfFirst { it.continuation != null }
    val token = if (shelfIndex >= 0) current.sections[shelfIndex].continuation!! else current.continuation ?: return current

    fun next(token: String?) =
        token?.takeUnless {
            it in seenTokens ||
                it == (if (shelfIndex >= 0) current.sections[shelfIndex].continuation else current.continuation)
        }
    val updated =
        if (shelfIndex >= 0) {
            val page = loadShelf(token)
            current.copy(
                sections =
                    current.sections.mapIndexed { index, section ->
                        if (index ==
                            shelfIndex
                        ) {
                            section.copy(items = section.items + page.items, continuation = next(page.continuation))
                        } else {
                            section
                        }
                    },
            )
        } else {
            val (sections, nextToken) = loadSections(token)
            current.copy(sections = (current.sections + sections).take(1_000), continuation = next(nextToken))
        }
    return if (songsLast) updated.copy(sections = updated.sections.moveSongSectionsLast()) else updated
}
