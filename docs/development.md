# Development

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


## Verified environment

On 2026-10-23, `./gradlew --no-daemon check build nativeProbe` passed on an
Apple M4 Pro with OpenGL 4.1 Metal. The original scalar query overwrote the
adjacent canary with `0x1B02`; the transformed handler completed 10,000 query/restore
cycles without GL errors. This is a native driver check, not a full-pack playtest.

Release tooling also has five automated staging tests, run by `check`. They cover
matching artifacts and rejection of mismatched binaries, missing files, a wrong
tag version, and a checkout that does not match the tag. The workflow's Python
version is pinned to 3.13, and `.gitattributes` fixes tracked text to LF for
cross-platform source archive consistency.
