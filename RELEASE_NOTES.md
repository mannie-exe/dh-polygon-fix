DH Polygon Fix 1.0.2

Maintenance release for Minecraft 1.21.1 / NeoForge and Distant Horizons 3.3.3.
The runtime polygon-query fix is unchanged from 1.0.1.

- Clarify installation and the native-memory fault reproduced on macOS
- Separate player instructions, technical verification and publishing guidance
- Add non-publishing release staging and tests for invalid release inputs
- Pin Python in CI for the release-tool checks

Install `dh-polygon-fix-1.0.2-mc1.21.1-neoforge.jar` on clients, replacing the
previous patch JAR. Dedicated servers do not need it. Remove the patch before
upgrading Distant Horizons; it requires exactly DH 3.3.3.

Verification includes transformation and target-guard tests, release staging
checks and 10,000 native OpenGL query/restore cycles on Apple M4 Pro. Publication
is gated on Linux, Windows and macOS builds, matching artifacts, and Linux/Mesa
native checks. These checks do not establish full-pack gameplay compatibility.

Source archives and SHA-256/SHA-512 manifests accompany the release.
LGPL-3.0-only; this is an unofficial backport of DH !126.
