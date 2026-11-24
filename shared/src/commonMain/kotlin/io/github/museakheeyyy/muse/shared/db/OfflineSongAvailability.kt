package io.github.museakheeyyy.muse.shared.db

fun interface OfflineSongAvailability {
    suspend fun songIds(): Set<String>
}
