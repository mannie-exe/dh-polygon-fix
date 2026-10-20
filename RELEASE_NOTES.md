DH Polygon Fix 1.0.1

Maintenance release of the unofficial DH 3.3.3 polygon-query backport.

- Move the Java package and build group to `design.inherent`; mod ID remains
  `dh_polygon_fix`. Replace the 1.0.0 JAR, do not install both versions.
- Keep the runtime fix unchanged: exactly one client query uses a two-integer
  buffer and retains the first value. No OS-specific branches or new runtime dependencies.
- Add real Mixin negative tests for missing and duplicate target queries.
- Prevent CI checkout credentials from persisting in build/release workspaces.
- Retain pinned DH artifact verification, matching tag/checkout checks,
  cross-platform artifact equality, and draft-first release publication.

Requires DH **3.3.3**, Minecraft **1.21.1**, NeoForge **21.1.263+ within 21.1.x**,
and Java **21**. Install `dh-polygon-fix-1.0.1-mc1.21.1-neoforge.jar` on clients.
Dedicated servers do not need this mod. Remove it before upgrading DH.

Local verification: `clean check build nativeProbe` passed on Apple M4 Pro /
OpenGL 4.1, including 10,000 query/restore cycles. CI gates publication on
Linux/Windows/macOS checks and Linux Mesa native testing. Full-pack gameplay
is not established by these checks. This patch does not fix shader sampler limits.

The sources JAR and complete project source ZIP are for development, not installation.
Checksums are supplied in `SHA256SUMS` and `SHA512SUMS`.
Licensed LGPL-3.0-only. This is an unofficial compatibility patch, not a DH release.
