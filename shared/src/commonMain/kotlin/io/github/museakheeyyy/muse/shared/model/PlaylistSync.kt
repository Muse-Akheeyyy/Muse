package io.github.museakheeyyy.muse.shared.model

fun String.isSyncablePlaylistId(): Boolean {
    val id = removePrefix("VL")
    return !id.startsWith("auto:") && (id.isEditablePlaylistId() || id.startsWith("OLAK") || id.startsWith("RDCL"))
}
