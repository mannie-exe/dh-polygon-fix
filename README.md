# Distant Horizons Polygon Fix

Fixes an unsafe OpenGL query in **Distant Horizons 3.3.3**. The fault was reproduced
on macOS, where Apple's OpenGL driver writes two polygon-mode values into space
reserved for one. That can corrupt adjacent native memory and cause delayed crashes.

## Installation

Use **Minecraft 1.21.1, NeoForge 21.1.263 or later in the 21.1 series, and Java 21**.
Install the
[release mod JAR](https://github.com/mannie-exe/dh-polygon-fix/releases) alongside
Distant Horizons 3.3.3 on the client. Choose the file ending in
`-mc1.21.1-neoforge.jar`, not the sources JAR. No configuration is needed.

This is primarily useful for the reproduced macOS fault, but the corrected query
is safe on other client platforms too. It is not needed on dedicated servers.
Other DH versions are intentionally rejected; remove this patch when upgrading DH.

## Behavior

This is an unofficial backport of [DH !126](https://gitlab.com/distant-horizons-team/distant-horizons/-/merge_requests/126).
It gives the query room for both values and preserves upstream's use of the first
value. It does not modify or bundle the DH JAR, change rendering settings, or fix
unrelated causes of crashes.

## Development

Build with JDK 21 and `./gradlew --no-daemon check build`. The development target is
NeoForge 21.1.263; metadata supports later 21.1.x versions. See
[development and verification](docs/development.md) and
[publishing](docs/publishing.md) for test coverage and release instructions.

## License

LGPL-3.0-only. See [NOTICE](NOTICE), [LGPLv3](LICENSE.LESSER.txt), and
[GPLv3](LICENSE.txt). This project is not affiliated with or endorsed by the
Distant Horizons team.
