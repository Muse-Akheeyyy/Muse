# Releasing

Muse publishes Android APKs only, and always from GitHub Actions. Change `appVersion`
only with release authorization.

Desktop, iOS, nightly prereleases, IzzyOnDroid, WinGet, Homebrew and Flatpak publication
are not part of this fork's release flow. `.github/workflows/release.yml` has a single
`android` job; nothing else runs on a tag.

## Stable releases

`release.yml` runs on `v*.*.*` tags. The tag must be exactly `vMAJOR.MINOR.PATCH` and equal
the suffix-free `appVersion`.

1. Bump `appVersion` in `gradle.properties` and raise `versionCode` in
   `androidApp/build.gradle.kts` above the previous release (`versionName` follows
   `appVersion`). Muse 1.0.1 ships `appVersion=1.0.1` with `versionCode = 2`.
2. Add a `---vX.Y.Z` entry to [changelog](changelog.md). `.github/scripts/parse_changelog.sh`
   extracts it up to the next marker and rejects missing or empty entries and TODO markers.
3. Commit and push `main`, then push an annotated tag:
   `git tag -a vX.Y.Z -m "Muse X.Y.Z"; git push origin vX.Y.Z`.
4. The workflow resolves the version, stages the signing keystore, runs
   `:androidApp:assembleRelease` (skipping `lintVitalRelease`), collects `dist/Muse.apk`,
   checks it with `apksigner`, and calls `gh release create` with the changelog entry.

The published asset is always named `Muse.apk` at
`https://github.com/Muse-Akheeyyy/Muse/releases/latest/download/Muse.apk`. The website and
the in-app updater read `releases/latest`, so they need no edit per release.

## Signing

The workflow needs the repository secrets `KEYSTORE_BASE64` (base64 of
`muse-release.jks`), `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`.
`.github/actions/prepare-android-signing` writes the keystore for the build, and
"Require signing" fails the run when they are missing.

The release identity is `CN=Muse`, SHA-256
`d20fabd4249aa38e3bc3182ffc2da2e734c1156f89af3e3e8fcde2a48c9b6418`. Every published
build must use it: changing the key breaks in-place updates. The keystore is backed up
encrypted in the private repository `Muse-Akheeyyy/muse-signing`. After changing any
secret, verify a built APK with `apksigner verify --print-certs` before publishing.

## Builds run in CI

Do not build on the local machine. The tag workflow is the only build that produces the
released APK, and PR CI is the only verification. To install an unreleased build on a
device, dispatch the **Build Android** workflow with variant `debug` (or `release`) and
install its artifact with `adb install -r Muse-debug.apk`. Never upload a locally built APK: the CI
build is the artifact users install.

## Retries

A rerun republishes from the same tag. `gh release create` fails while the release already
exists, so delete it first and keep the tag:

```sh
gh release delete vX.Y.Z --yes
gh run rerun <run-id>
```

Never delete or move a published tag. Raise `versionCode` for a new release instead of
reusing one.
