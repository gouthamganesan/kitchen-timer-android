# Platform plan

1. Bootstrap cached Gradle/Kotlin/Compose stack and native manifest. Acceptance: reproducible configuration, no Internet permission.
2. Build pure deadline engine and persistence. Acceptance: monotonic countdown, reboot recovery, generation-safe expiry; meaningful unit tests.
3. Connect AlarmManager, visual notification actions and ringing service. Acceptance: exact access checked, no countdown service, cleanup after cancel/stop.
4. Run focused tests/build and report contracts, limitations and device cases to integration.

Ownership: Gradle/config, core/, platform/, manifest, non-UI resources, core tests. Shared model and Activity belong to integration. Device operations belong to integration.
