# Publication progress

2026-11-17: MIT selected; public GitHub publication explicitly authorized. Added the design-process README, current screenshots, original concept archive, banner, portable docs, MIT/third-party notices, pinned CI and a publication guard. Raw evidence and prior docs are retained locally in ignored folders.

Version 0.1.2 removes the rocker row ripple. Local build, 31 tests and lint passed; emulator checked both rocker positions, four appearances, picker and Settings. Installed 0.1.2 / code 3 on the connected phone and verified its package version.

Publication scan passed. A clean source copy built successfully with all 56 Gradle tasks executed, 31 tests passing and zero lint errors. GitHub Markdown rendering and publication checks passed. The public repository is live at https://github.com/gouthamganesan/kitchen-timer-android. Its banner and screenshots were inspected in the published README. The initial hosted check found sdkmanager missing; a pinned Android SDK setup action now makes that dependency explicit. Actual run results remain in GitHub Actions. No complete security-audit or physical-feel claim is made.
