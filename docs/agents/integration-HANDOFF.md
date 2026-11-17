# Integration handoff

Read `docs/HANDOFF.md` first. Canonical project: `the repository root`. Delivery: `artifacts/releases/Kitchen-Timer-0.1.1-debug.apk`; build, 31 tests and lint passed. Sources, assets, evidence and docs stay together in this project.

Integration owns shared contracts, MainActivity, top-level docs, final manifest/icon/version wiring and device execution. Other ownership is in `docs/ARCHITECTURE.md`; no active coding assignments remain.

The user resumed installation after reconnecting the Pixel. Version 0.1.1 / code 2 is now installed and verified over USB with existing app data retained. See `artifacts/phone-install-2026-11-17.md`. Wireless pairing remains deferred. Use Maestro for UI tests and adb for installation/diagnostics; coordinate before driving their phone.

Icon is editable SVG paths with its generated origin preserved. It is a semantic vector reconstruction, not a pixel-perfect trace. Reproduction and verification details: `docs/agents/icon-HANDOFF.md`.

Latest evidence: `artifacts/final-landscape-build.log`, `final-smoke.log`, `final-winding.log` and `final-compact-fit.log`. Final compact portrait and landscape screenshots were inspected, including the full sound switch. Remaining physical, reboot, Doze, permission and accessibility validation is explicit in the test matrix.
