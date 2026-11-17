# Haptics / QA plan

Owner: haptics_qa. Scope: haptics policy and Android adapter, haptic tests, Maestro flows, testing documentation. Other agents own UI/platform/integration; do not overwrite their work.

1. Agree explicit gesture-only policy API with UI. Preserve priority, rate limiting and end-stop deduplication.
2. Implement native view feedback using supported Android action constants and platform fallbacks. Never bypass the system touch-feedback preference; alarm vibration remains separate.
3. Unit test passive-source suppression, both winding directions, hour/five/end-stop priority, fast winding, gesture release and cancelled input.
4. Build semantic Maestro flows and manual physical-device checklist, with exact prerequisites and honest NOT RUN markers.
5. Run available policy checks after bootstrap; hand off file/API/validation status to integration. Root alone drives device until delegated.

Acceptance: no automatic countdown haptics; one strongest eligible effect per setting change; no replay queue; global 40ms floor; each reached end stop emits once until departing; no release click on idle tap or cancelled gesture; all results documented.
