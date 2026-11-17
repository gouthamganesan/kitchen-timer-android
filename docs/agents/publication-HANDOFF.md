# Publication handoff

Scope: public source repository under MIT, README explaining ideation and visual taste, and the sound-switch feedback correction. The user explicitly authorized publishing and installing the update.

Current app: 0.1.2 / code 3. Local build and 31 tests pass with no lint errors; emulator visuals/toggle flow passed. Updated phone installation succeeded and version was verified. No unrelated phone interaction occurred.

Public source includes app, tests, Maestro flows, design prototypes, editable icon assets, agent docs, curated images and CI. Raw logs, device identifiers, build output, local SDK configuration and temporary authoring utilities are excluded. `scripts/check_publication.py` checks Git's candidate files and local Markdown links; it is a focused guard, not a full security audit.

Published publicly at [gouthamganesan/kitchen-timer-android](https://github.com/gouthamganesan/kitchen-timer-android) under MIT. The README banner and screenshots were inspected on the live page. Clean source build passed with all 56 Gradle tasks executed; Markdown rendering and publication checks passed. Hosted results live in [Actions](https://github.com/gouthamganesan/kitchen-timer-android/actions/workflows/android.yml). The initial runner lacked sdkmanager, so the workflow now explicitly sets up Android SDK tools. No store release or release signing is included.

Download follow-up: the SDK correction passed GitHub run 44130232919. Successful checks now upload an installable debug APK for 30 days. Publish v0.1.2 with the saved verified APK and SHA256 after verifying the updated workflow. README distinguishes that development preview from ephemeral runner debug builds. Website work also adds a Kitchen Timer story about learning to build with AI with taste and readable contribution dates.
