package io.github.museakheeyyy.muse.shared.backup

import io.github.museakheeyyy.muse.shared.constants.ArtistNameAliasesJsonKey
import io.github.museakheeyyy.muse.shared.settings.ArtistNameAliases
import io.github.museakheeyyy.muse.shared.settings.PortableSetting
import io.github.museakheeyyy.muse.shared.settings.PortableSettings
import io.github.museakheeyyy.muse.shared.settings.SettingType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun PortableSettings?.withLegacyArtistNameAliases(serialized: String): PortableSettings {
    val aliases = ArtistNameAliases.deserialize(serialized)
    val settings = this ?: PortableSettings(entries = emptyList())
    return settings.copy(
        entries =
            settings.entries.filterNot { it.key == ArtistNameAliasesJsonKey } +
                PortableSetting(ArtistNameAliasesJsonKey, SettingType.STRING, Json.encodeToString(aliases)),
    )
}
