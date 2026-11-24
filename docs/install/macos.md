# Install on macOS

## Download

Muse publishes Android packages only. Build the desktop app from source and package it locally; choose the ARM64 package for Apple Silicon or x64 for Intel. Packaging requires macOS 15; compatibility with older systems needs platform verification. The package includes Java.

## Install and verify

1. Open the DMG and drag Muse to Applications.
2. Eject the image and open Muse.
3. If macOS blocks the unsigned app, use its per-app Open Anyway option only after verifying the build. Follow [Apple's instructions](https://support.apple.com/en-us/102445).
4. Check playback, login and credential restoration after restart.

Packages are unsigned and unnotarized. Do not disable Gatekeeper globally.

## Update and uninstall

Back up and quit before replacing the app in Applications. Deleting the app leaves profile data, Keychain credentials and music outside it. Sign out before removing credentials and preserve backups before clearing data.

## Common problems

For architecture or launch failure, confirm the package matches the processor and collect the [desktop log](../desktop.md#logs-and-profiling). For credential problems, check [Keychain recovery](../desktop.md#credentials-and-settings-files) before deleting files.
