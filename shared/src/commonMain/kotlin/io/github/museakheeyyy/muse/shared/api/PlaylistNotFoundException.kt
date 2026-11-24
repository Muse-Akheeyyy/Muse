package io.github.museakheeyyy.muse.shared.api

class PlaylistNotFoundException(
    val playlistId: String,
) : Exception()
