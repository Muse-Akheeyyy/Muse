package io.github.museakheeyyy.muse.desktop.deviceconnect

import io.github.museakheeyyy.muse.shared.deviceconnect.DeviceConnectDeviceType
import io.github.museakheeyyy.muse.shared.deviceconnect.DeviceConnectManager

object DesktopDeviceConnect {
    val manager = DeviceConnectManager(localDeviceType = DeviceConnectDeviceType.DESKTOP)
}
