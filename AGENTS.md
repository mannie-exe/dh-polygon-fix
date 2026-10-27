# DH Polygon Fix

Maintain the narrow client backport for Distant Horizons' polygon-mode query.
README owns installation and the upstream removal condition; `docs/development.md`
owns implementation, exact input verification and runtime evidence. Read the
pinned dependency and its released bytecode before changing a guard or hook.

Preserve the two-integer query buffer, first-value semantics and exactly one
required injection. Do not broaden this into projection, shader or worldgen fixes.
Keep the server free of client GL dependencies. Do not bundle the upstream JAR or
decompiled source; retain required license and attribution notices.

Run `./gradlew check build` after code/build changes. Transformer and driver
simulation checks do not establish native rendering. Use `nativeProbe` for changes
to the GL boundary and a disposable client for affected game behavior; request
desktop focus before interactive checks. Record artifacts, commands and limits.

Preserve unrelated work and player data. Remove disposable inspection output after
recording useful evidence in the owning docs. Write direct prose without marketing
or per-edit ledgers. Do not commit, push or release without authorization.
Use conventional commits with proportional, short `- [tag]: message` body bullets.
