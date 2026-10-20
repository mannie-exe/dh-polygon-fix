# Distant Horizons Polygon Fix

Unofficial client-only backport of [DH !126](https://gitlab.com/distant-horizons-team/distant-horizons/-/merge_requests/126)
for **Distant Horizons 3.3.3, Minecraft 1.21.1, NeoForge 21.1.263+ within 21.1.x,
and Java 21**.

Install the release's `*-mc1.21.1-neoforge.jar` alongside the original DH 3.3.3
JAR on clients. Do not install the sources JAR. The patch is unnecessary on a
dedicated server. Remove this patch when upgrading DH; other DH versions are
intentionally rejected on clients.

## What changes

DH's `GLState.saveState()` calls the scalar `glGetInteger(GL_POLYGON_MODE)`
helper, whose native buffer only has room for one integer. A driver can write
two polygon modes. On the tested Apple OpenGL 4.1 driver, the second write
overwrites the adjacent word. This can cause delayed native crashes whose
stack points at unrelated code.

One required Mixin redirect replaces that query with the upstream `int[2]`
query and returns its first value. It leaves state restoration, projection
matrices, shaders, networking, and all other methods unchanged. The original DH
artifact is not modified or bundled.

This is applied on every client platform, not gated on an OS name. Room for two
integers is safe for drivers returning either one or two values. First-value
semantics match upstream. This does not add support for independently restoring
different front/back polygon modes in compatibility contexts.

The injection must match exactly once. Unexpected bytecode or a conflicting
redirect should fail during loading rather than silently retain the unsafe call.

## Build and verification

Use JDK 21 and the checked-in Gradle wrapper:

```sh
./gradlew --no-daemon check build
./gradlew --no-daemon nativeProbe
./gradlew --no-daemon runClient
```

`check` verifies the DH artifact's SHA-256, applies the real Mixin transformer to
its released class, checks that exactly one scalar query changed, and checks
unrelated methods for unchanged bytecode. It executes the transformed handler
with one-value and two-value driver simulations, including differing front/back
values and an invalid-query negative case. Separate JVM probes mutate the target
to remove or duplicate the query and verify that Mixin rejects both cases.

`nativeProbe` needs a graphical session and OpenGL 4.1. It extracts the handler
from the real transformed class and runs 10,000 native query/restore cycles.
It also reproduces the original scalar overwrite using an explicitly allocated
adjacent canary. It does not start Minecraft or prove full-pack compatibility.

CI builds/tests on Linux, Windows, and macOS; Linux also runs the native probe
against Mesa under Xvfb. Physical Windows GPUs are not tested by CI.

The build downloads the pinned official DH binary as a development dependency.
Output is in `build/libs`. No accounts, worlds, game files, or DH binary belong
in this repository.

## Release

Update `mod_version`, release notes, and verification evidence; commit and push
a matching `v<version>` tag. The release workflow requires all three CI jobs,
checks that their binaries are identical, and publishes the binary, sources JAR,
complete project source ZIP, and SHA-256/SHA-512 manifests. A draft becomes public
only after every asset upload succeeds.

Modpacks can pin the public GitHub release asset URL and its digest. Mark this
dependency client-required and server-unsupported. Do not use a moving `latest`
URL or overwrite published release assets.

## License and provenance

LGPL-3.0-only. See [NOTICE](NOTICE), [LGPLv3](LICENSE.LESSER.txt), and
[GPLv3](LICENSE.txt). Upstream fix:
[`51bf1f31`](https://gitlab.com/distant-horizons-team/distant-horizons/-/commit/51bf1f319b56e233ac8e0e8189cd7350444da0f6).
The tested DH source revision is `575517f261dfd04e302a37719f1459e9b5633d9f`.
This project is not affiliated with or endorsed by the Distant Horizons team.
