# Publication progress

2026-11-17: MIT selected; public GitHub publication explicitly authorized. Added the design-process README, current screenshots, original concept archive, banner, portable docs, MIT/third-party notices, pinned CI and a publication guard. Raw evidence and prior docs are retained locally in ignored folders.

Version 0.1.2 removes the rocker row ripple. Local build, 31 tests and lint passed; emulator checked both rocker positions, four appearances, picker and Settings. Installed 0.1.2 / code 3 on the connected phone and verified its package version.

Publication scan passed. A clean source copy built successfully with all 56 Gradle tasks executed, 31 tests passing and zero lint errors. GitHub Markdown rendering and publication checks passed. The public repository is live at https://github.com/gouthamganesan/kitchen-timer-android. Its banner and screenshots were inspected in the published README. The initial hosted check found sdkmanager missing; a pinned Android SDK setup action now makes that dependency explicit. Actual run results remain in GitHub Actions. No complete security-audit or physical-feel claim is made.

Hosted setup follow-up: explicitly request platform-tools so the pinned SDK action does not request the retired tools package. Application source and the installed APK are unchanged.

The corrected SDK setup passed hosted build, unit tests, lint and publication checks in run 44130232919. Added an APK artifact after successful checks (30-day retention) and a public release download entry. The release uses the saved, locally verified 0.1.2 APK; runner-built debug APKs have temporary signing keys. Hosted run 44131768665 passed and its APK artifact was confirmed.

Public download completed: [v0.1.2](https://github.com/gouthamganesan/kitchen-timer-android/releases/tag/v0.1.2) includes the locally tested APK and checksum. An unauthenticated download matched SHA256 `44e74f47003cc111b181dd86be16ccc6078f23e0e57e70b166b03910c0249a7a`.

Website follow-up completed: [Kitchen Timer](https://gouthamg.com/Projects/kitchen-timer) tells the experiment in building with AI with taste, with real app screenshots and a public download. The Projects shelf includes the icon and story; contribution tooltips read “1 contribution on 9 August 2026”. Local website type/format checks, 163 unit tests, 9 deployment tests and 49 build/preservation checks passed. The date regression covers three time zones. Website code and content were pushed together as 454c198f and 37bcaaf; both hosted workflows passed. The rebuilt live Projects page, tooltip and story were verified. Unrelated local website edits and writing were left untouched.
