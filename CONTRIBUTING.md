# Contributing

Kitchen Timer explores a specific physical design. Read the [design principles](DESIGN.md) and [requirements](docs/REQUIREMENTS.md) before changing the interface.

For a visual change, explain the interaction it improves and include before/after screenshots. Check light, backlit and focus modes, compact portrait, landscape and increased font size. Keep the lighting consistent and reserve decoration for things that communicate material, depth or state.

Run `./scripts/check.sh` before submitting code. Use the [Maestro flows](maestro/README.md) on a test device when interactions change. Never run them over a timer someone is relying on. Physical haptics and speaker output need a real device; emulator results cannot establish how they feel.

Keep personal paths, device identifiers, logs, signing material and build outputs out of Git. `python3 scripts/check_publication.py` checks the publication candidate for common leaks and broken local documentation links; it is a focused guard, not a comprehensive security audit.

Work in a focused branch. Update the relevant requirements, test record and handoff when behavior changes. Contributions are covered by the project's MIT license; bundled third-party material retains its own terms.
