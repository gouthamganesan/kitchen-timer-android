# Public repository preparation plan

2026-11-17. Scope: prepare local source for a public repository and explain the design process in the README. The user selected MIT. The user subsequently authorized installing the switch fix and publishing a public GitHub repository.

1. Inspect source, assets and local-only data.
2. Preserve raw records locally; prepare portable public docs and curated design images.
3. Add MIT, third-party notices, Git exclusions, build workflow and contributor guidance.
4. Verify the exact Git candidate, documentation links and build from a clean source copy.
5. Record results and hand off the reviewable local repository.

Acceptance: no device identifiers, credentials, local build paths, APKs or raw diagnostic output in the publication candidate; honest design narrative and test claims; reproducible app build.

Publication completed: public GitHub repository created and version 0.1.2 installed on the phone. Build infrastructure explicitly installs Android SDK tools; hosted check history is available through the publication handoff.

## Download and website follow-up

- [x] Verify the SDK setup correction on GitHub.
- [x] Add successful-build APK artifacts and explain debug signing in the README.
- [x] Verify the final hosted workflow and publish the tested 0.1.2 APK with checksum.
- [x] Add the website project story and readable contribution dates together; test locally, push, and verify the rebuild.
