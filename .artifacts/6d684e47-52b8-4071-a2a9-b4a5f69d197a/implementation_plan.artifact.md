# Manifest Merger Failure Investigation and Fix

The project is experiencing a manifest merger failure: `Manifest merger failed with multiple errors, see logs`. This usually indicates conflicts between manifests (app vs. libraries) or missing required attributes/meta-data.

## User Review Required

> [!IMPORTANT]
> Since detailed merger logs are not immediately visible in the current build output, I will perform a series of targeted checks and "common fix" applications. If these do not resolve the issue, I will need to find a way to extract the full merger report.

## Proposed Changes

### 1. Permission and Exported Attribute Audit

*   **[MODIFY] [AndroidManifest.xml](file:///D:/My%20Project/BaseProject/app/src/main/AndroidManifest.xml)**
    *   Add `android.permission.RECEIVE_BOOT_COMPLETED` because `BootReceiver` uses `BOOT_COMPLETED`.
    *   Change `android:exported="true"` to `android:exported="false"` for Activities without intent filters (`MainActivity`, `AlertFullScreenActivity`).
    *   Ensure all components with intent filters have `android:exported`.

### 2. Facebook SDK and AdMob Audit

*   Check if Facebook SDK or AdMob Mediation requires specific meta-data that is missing.
*   Facebook SDK often requires `com.facebook.sdk.ApplicationId`.

### 3. Build Configuration Audit

*   Check `compileSdk` and `targetSdk` values for potential issues.
*   Verify `tools:replace` usage.

## Verification Plan

### Automated Tests
*   Run `./gradlew :app:processDebugMainManifest` after each set of changes to see if it passes.
*   Run a full build: `./gradlew :app:assembleDebug`.

### Manual Verification
*   N/A (Build issue)
