# Install on iOS and iPadOS

## Requirements and download

Use iOS or iPadOS 17 or later. Muse publishes Android packages only, so build the app from source with Xcode — see [development](../development.md) — which produces an unsigned `Muse.ipa` you sign for your device.

## Sideloading

Follow your tool's setup and refresh instructions. Its OS support and signing limits can change:

- [AltStore Classic](https://faq.altstore.io/altstore-classic/how-to-install-altstore-windows), with [macOS setup](https://faq.altstore.io/altstore-classic/how-to-install-altstore-macos).
- [SideStore](https://docs.sidestore.io/docs/installation/prerequisites).
- [Sideloadly](https://sideloadly.io/faq.html).
- [TrollStore](https://github.com/opa334/TrollStore), only where its supported OS versions overlap this app's minimum.

Use the same signing account and resulting bundle ID for updates. Check the expiry shown by the tool and refresh before it expires. Refresh renews signing; build and install a new IPA separately to update Muse. Keep pairing files private.

Muse publishes no AltStore or SideStore source, so add the IPA to your tool yourself after each build.

## Signing and entitlements

The source bundle ID is `io.github.museakheeyyy.muse`. Tools may rewrite it. The Xcode project defines one app target without extensions. No app group is required.

`iosApp/Configuration/iosApp.entitlements` requests Associated Domains for `applinks:muse-akheeyyy.pages.dev`. `Info.plist` declares background audio and the app's URL schemes. A signing profile may omit Associated Domains; do not assume HTTPS invitations work after re-signing, especially since the site does not publish an Apple App Site Association document yet. See [invitation delivery](../listen-together.md#invitation-links).

For provisioning errors, record the entitlement named by the tool. Do not add app groups or remove frameworks to bypass signing failures.

## Verify, update and uninstall

Open the app, check its version, play audio, lock the device and test system controls. Verify sign-in after reopening. Check one manual signing refresh before relying on background refresh.

Export a [backup](README.md#before-updating-or-uninstalling), then install the newer verified IPA through the same tool/account/ID. Deleting the app removes local data. Follow the tool's uninstall instructions, including removal through TrollStore when applicable.

If installation succeeds but startup fails, report the app version, OS, device and signing tool. For expiry, pairing or provisioning failures, use the tool's support guide and retain its error log.
