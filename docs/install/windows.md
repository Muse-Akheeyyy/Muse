# Install on Windows

## Requirements and build

Use Windows 10 or later on x64. Muse publishes Android packages only, so build the desktop app from source and package it locally with the installer or portable ZIP tasks. The x64 package is a native executable that needs no Java; the ARM64 package includes Java. Login requires Microsoft Edge WebView2 Runtime.

## Install and verify

1. Run the installer, or extract the complete portable ZIP to a writable directory.
2. Launch Muse and check playback and login.
3. Restart and confirm that saved credentials return.

Keep the extracted runtime and helper files with the executable. A portable ZIP does not register an installed application or Start Menu entry.

## Update and uninstall

Back up first. Registered installations can use the stable installer update in settings. Portable installations replace their extracted directory after quitting. See [desktop updates](../desktop.md#updates-and-restore).

Uninstall a registered copy through Windows Settings, Apps. For portable copies, delete the extracted directory and shortcuts. Profile data and music outside it can remain. Sign out first if removing saved credentials.

## Common problems

Confirm the source and checksum before allowing a blocked download. Do not disable Windows security globally. For failed login, check WebView2 Runtime and the bundled helper. Include the [desktop log](../desktop.md#logs-and-profiling) in a failure report.
