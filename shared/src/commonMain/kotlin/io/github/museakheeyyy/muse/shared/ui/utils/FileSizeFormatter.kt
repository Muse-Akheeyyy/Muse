package io.github.museakheeyyy.muse.shared.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.file_size_unit_byte
import io.github.museakheeyyy.muse.shared.generated.resources.file_size_unit_gigabyte
import io.github.museakheeyyy.muse.shared.generated.resources.file_size_unit_kilobyte
import io.github.museakheeyyy.muse.shared.generated.resources.file_size_unit_megabyte
import io.github.museakheeyyy.muse.shared.generated.resources.file_size_unit_terabyte
import io.github.museakheeyyy.muse.shared.utils.FileSizeUnits
import io.github.museakheeyyy.muse.shared.utils.formatFileSize
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberFileSizeFormatter(): (Long) -> String {
    val units =
        FileSizeUnits(
            byte = stringResource(Res.string.file_size_unit_byte),
            kilobyte = stringResource(Res.string.file_size_unit_kilobyte),
            megabyte = stringResource(Res.string.file_size_unit_megabyte),
            gigabyte = stringResource(Res.string.file_size_unit_gigabyte),
            terabyte = stringResource(Res.string.file_size_unit_terabyte),
        )
    return remember(units) { { bytes -> formatFileSize(bytes, units) } }
}
