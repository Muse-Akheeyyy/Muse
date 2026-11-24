# Install Muse

Choose your platform: [Android](android.md), [iOS and iPadOS](ios.md), [Windows](windows.md), [macOS](macos.md), or [Linux](linux.md).

## Get the package

Muse publishes [Android releases](https://github.com/Muse-Akheeyyy/Muse/releases) only. Each release attaches `Muse.apk`; desktop and iOS are built from source with the local packaging tasks described in the [desktop reference](../desktop.md#native-packages) and the [iOS guide](ios.md).

| Platform | Package |
| --- | --- |
| Android | `Muse.apk` from [Releases](https://github.com/Muse-Akheeyyy/Muse/releases/latest) |
| iOS and iPadOS | Unsigned `Muse.ipa` built locally; see [iOS](ios.md) |
| Windows, macOS, Linux | Built locally; `Muse` installer, DMG or AppImage |

## Verify a download

Releases do not attach a checksum file. To verify a transfer, calculate the package's SHA-256 digest before and after copying it and compare the two values. Enter the full path when prompted.

On Linux:

```sh
printf 'Downloaded file path: '
read -r package
sha256sum -- "$package"
```

On macOS:

```sh
printf 'Downloaded file path: '
read -r package
shasum -a 256 -- "$package"
```

On Windows, use PowerShell:

```powershell
$package = Read-Host 'Downloaded file path'
Get-FileHash -Algorithm SHA256 -LiteralPath $package
```

Compare all 64 hexadecimal characters with the earlier value. Letter case does not matter.

A mismatch means you should download the file again. Do not install a mismatched file. A checksum confirms that the file matches your earlier copy; it is not a code-signing certificate.

For Android or iOS, you can verify the APK or IPA on a computer before transferring it unchanged to the device. After installation, check the version in Muse's settings, open a song and confirm audible playback. Platform guides add checks for login and signing.

## Before updating or uninstalling

Export a backup from Muse's settings and keep it outside the installation directory or mobile app storage. Keep downloaded music separately if you need it. Do not assume a backup includes every downloaded audio file.

Use the same platform, architecture and installation method for updates. Avoid downgrading a database created by a newer version; see [downgrade safety](../crash-recovery.md). Uninstalling a mobile app deletes its local data. Removing a desktop application can leave settings, credentials, caches and downloaded music behind.

If installation or startup still fails, include the package filename, app version, OS version, CPU architecture and exact error in a report to the [issue tracker](https://github.com/Muse-Akheeyyy/Muse/issues). For desktop failures, collect the [application log](../desktop.md#logs-and-profiling). Remove account details and tokens before sharing logs.
