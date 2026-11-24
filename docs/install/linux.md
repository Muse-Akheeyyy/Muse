# Install on Linux

## Requirements and build

Muse publishes Android packages only. Build the Linux package from source; local packaging targets AppImage for x64 and ARM64 on glibc-based distributions, and runs on Ubuntu 24.04. That does not establish compatibility with every older distribution. The x64 AppImage is a native executable that needs no Java; the ARM64 AppImage includes Java. Both include FFmpeg.

Choose x64 for `uname -m` output `x86_64`, or ARM64 for `aarch64`.

Use X11 or XWayland. Login needs GTK3/WebKitGTK 4.1. Saved credentials need Secret Service, libsecret, `secret-tool` and `dbus-send`. Audio needs ALSA integration with your audio server. See [Linux setup](../desktop.md#linux-login) and [audio setup](../desktop.md#linux-audio-setup). Startup rejects Omarchy when `os-release` identifies it or its `omarchy-version` tool exists in a standard install path or under `OMARCHY_PATH`, and links to [stopomarchy.org](https://stopomarchy.org/). Configuration directories, pacman repository entries and environment paths without the tool do not trigger rejection.

## Build and verify

Build the AppImage with `./scripts/build_appimage.sh`; it writes the result under `dist/`. Mark that file executable and run it:

```sh
chmod u+x -- dist/*.AppImage
dist/*.AppImage
```

Check playback, sign-in and retained credentials after restart.

If a tool such as Gear Lever already manages the launcher, Muse skips its own app-menu registration when launched with a nonempty `DESKTOPINTEGRATION` setting. To skip registration when launching manually, run:

```sh
DESKTOPINTEGRATION=1 dist/*.AppImage
```

This does not remove launchers already present.

No Muse package exists on the AUR, in a Homebrew tap, in nixpkgs or on Flathub. Community packages that carry another client's name install that client, not Muse.

## Update and uninstall

Back up, quit, then replace the AppImage with a verified build of the same architecture. Deleting an AppImage leaves profile data and music outside it. Sign out first to remove saved sessions, and preserve backups before clearing data.

## Common problems

For silent playback, check [ALSA routing](../desktop.md#linux-audio-setup). For blank login, check WebKitGTK and XWayland. For forgotten sign-in, check the unlocked Secret Service collection and [credential recovery](../desktop.md#credentials-and-settings-files). Use the [application log](../desktop.md#logs-and-profiling) to report remaining failures.

### Storage paths

Muse uses the default home directories when `XDG_DATA_HOME`, `XDG_CONFIG_HOME`, `XDG_CACHE_HOME`, or `XDG_STATE_HOME` is unset, empty, or relative. If an older setup stored files under a relative path, set that variable to the absolute path of the existing directory to keep using those files. Muse does not move them automatically.

### Sonos and DLNA with firewalld

Discovery sends SSDP searches to `239.255.255.250:1900`. Speakers reply from UDP port 1900 to a temporary local UDP port. Allowing only incoming destination port 1900 does not allow these replies. Muse serves cast audio on TCP port `57643` for the entire session, including song changes. This applies to Google Cast and FCast too. The app reports an error if it cannot bind this port or if the receiver has not requested the stream after 15 seconds of requested playback. A missing request can indicate a firewall, routing or receiver problem.

Check the Wi-Fi or Ethernet interface's zone with `firewall-cmd --get-active-zones`. For a speaker at `192.168.8.50` in the `public` zone, allow its discovery replies and audio requests:

```sh
sudo firewall-cmd --zone=public --add-rich-rule='rule family="ipv4" source address="192.168.8.50/32" source-port port="1900" protocol="udp" accept'
sudo firewall-cmd --zone=public --add-rich-rule='rule family="ipv4" source address="192.168.8.50/32" port port="57643" protocol="tcp" accept'
```

Replace the example IP and zone with your speaker and network. Repeat for each speaker, including members of a Sonos group that fetch audio. Reopen the cast picker after changing the rules. Once discovery and playback work, repeat the same commands with `--permanent` to keep the rules after a restart. See the [firewalld rich rule reference](https://firewalld.org/documentation/man-pages/firewalld.richlanguage.html) for address and port matching. mDNS alone does not discover DLNA or Sonos devices in Muse.
