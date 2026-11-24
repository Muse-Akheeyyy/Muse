<div align="center">

<a href="https://github.com/Muse-Akheeyyy/Muse/releases/latest"><img src="assets/hero.svg" alt="Muse — music without the noise" width="100%" /></a>

<br/>

<a href="https://github.com/Muse-Akheeyyy/Muse/releases"><img src="https://img.shields.io/github/v/release/Muse-Akheeyyy/Muse?style=flat-square&labelColor=1b1424&color=8e6fc1" alt="Latest release" /></a>
<a href="https://github.com/Muse-Akheeyyy/Muse/blob/main/LICENSE"><img src="https://img.shields.io/github/license/Muse-Akheeyyy/Muse?style=flat-square&labelColor=1b1424&color=8e6fc1" alt="License" /></a>
<a href="https://github.com/Muse-Akheeyyy/Muse/releases"><img src="https://img.shields.io/github/downloads/Muse-Akheeyyy/Muse/total?style=flat-square&labelColor=1b1424&color=8e6fc1" alt="Downloads" /></a>
<a href="https://muse-akheeyyy.pages.dev"><img src="https://img.shields.io/badge/site-muse--akheeyyy-8e6fc1?style=flat-square&labelColor=1b1424" alt="Muse site" /></a>
<a href="https://discord.gg/FW6EX7Wgz"><img src="https://img.shields.io/badge/discord-join-5865F2?style=flat-square&labelColor=1b1424" alt="Discord" /></a>

<br/><br/>

<a href="https://github.com/Muse-Akheeyyy/Muse/releases/latest/download/Muse.apk"><img src="assets/download-button.svg" width="260" alt="Download the latest APK" /></a>

<br/>

<sub>Android 7+ · every build is signed with the same key, so updates install straight over the old one</sub>

</div>

<br/>

> [!WARNING]
> <details>
>   <summary>The use of Muse to access YouTube Music may violate YouTube's Terms of Service.</summary>
>   <p>You use Muse at your own risk and are solely responsible for the consequences. Muse and its contributors are not liable for damages, claims or actions against your account.</p>
> </details>
>
> YouTube Music is region-restricted. If it is unavailable where you are, use a [VPN or proxy in a supported region](https://one.one.one.one/).

## Features

<table>
  <tr>
    <td width="33%" valign="top"><b>🎧 Playback</b><br/><sub>YouTube Music with or without signing in. No ads.</sub></td>
    <td width="33%" valign="top"><b>🎛 Material 3</b><br/><sub>Player, miniplayer and background styles, custom fonts.</sub></td>
    <td width="33%" valign="top"><b>🪄 Listen Together</b><br/><sub>Shared queues and synced playback with friends.</sub></td>
  </tr>
  <tr>
    <td valign="top"><b>📺 Casting</b><br/><sub>Google Cast, F-Cast, SONOS and DLNA.</sub></td>
    <td valign="top"><b>🎚 Lyrics</b><br/><sub>Synced, with translation and romanization.</sub></td>
    <td valign="top"><b>🔄 Imports</b><br/><sub>Backups from 30+ clients and 6 services.</sub></td>
  </tr>
  <tr>
    <td valign="top"><b>🧠 Recognition</b><br/><sub>Shazam, Discord Rich Presence, Last.fm and ListenBrainz.</sub></td>
    <td valign="top"><b>🌐 Everywhere</b><br/><sub>Android, iOS, Windows, macOS and Linux.</sub></td>
    <td valign="top">&nbsp;</td>
  </tr>
</table>

## Install

1. Tap the download button above, or grab `Muse.apk` from [Releases](https://github.com/Muse-Akheeyyy/Muse/releases/latest).
2. Allow installing from this source when Android asks.
3. Open it. Later releases install as normal updates, and the app checks for them from the Updates settings.

<details>
<summary>Desktop and iOS</summary>

This fork publishes Android builds only; the desktop and iOS sources build from source. Guides: [Windows](docs/install/windows.md) · [macOS](docs/install/macos.md) · [Linux](docs/install/linux.md) · [iOS](docs/install/ios.md).

</details>

## Build

```powershell
.\gradle.ps1 :androidApp:assembleDebug     # APK -> androidApp\build\outputs\apk\debug\
.\gradle.ps1 :desktopApp:run               # run the desktop app
```

Releases come from GitHub Actions: a `v*` tag builds, signs and publishes `Muse.apk`. See the [developer docs](docs/README.md) and the [release guide](docs/releasing.md).

<br/>

<div align="center">
<sub>
Muse is <a href="https://www.gnu.org/licenses/gpl-3.0.html">GPL-3.0</a> free software, maintained by <a href="https://muse-akheeyyy.pages.dev">Akhil TG</a>.<br/>
Not affiliated with YouTube or Google LLC.
</sub>
</div>
