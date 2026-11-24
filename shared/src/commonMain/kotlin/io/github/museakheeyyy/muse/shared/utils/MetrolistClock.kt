package io.github.museakheeyyy.muse.shared.utils

import kotlin.time.Clock

object MetrolistClock {
    fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()
}
