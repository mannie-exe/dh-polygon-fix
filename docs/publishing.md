# Publishing

## Release

Update `mod_version`, release notes, and verification evidence; commit and push
a matching `v<version>` tag. The release workflow requires all three CI jobs,
checks that their binaries are identical, and publishes the binary, sources JAR,
complete project source ZIP, and SHA-256/SHA-512 manifests.
`python3 tools/release.py v<version> --stage-only` validates/stages downloaded CI
artifacts without publishing; five fixture tests exercise its rejection guards. A draft becomes public
only after every asset upload succeeds.

Modpacks can pin the public GitHub release asset URL and its digest. Mark this
dependency client-required and server-unsupported. Do not use a moving `latest`
URL or overwrite published release assets.


## Modrinth project

Create the project under **mannie-exe**. The public profile currently lists no
projects; GitHub already hosts [dh-polygon-fix](https://github.com/mannie-exe/dh-polygon-fix).

| Field | Selection |
| --- | --- |
| Project type | Mod |
| Name | Distant Horizons Polygon Fix |
| Slug | `dh-polygon-fix` |
| Summary | Fixes an unsafe OpenGL query in Distant Horizons 3.3.3, reproduced on macOS |
| Categories | Utility; Optimization if a secondary category is desired |
| Environment | Client-side only (`client_only`) |
| License | LGPL-3.0-only |
| Source | `https://github.com/mannie-exe/dh-polygon-fix` |
| Issues | `https://github.com/mannie-exe/dh-polygon-fix/issues` |
| Description | Installation and behavior sections from the README |

For version 1.0.2, select **Release**, **Minecraft 1.21.1**, and **NeoForge** only.
Upload `dh-polygon-fix-1.0.2-mc1.21.1-neoforge.jar` as the primary file. Add
Distant Horizons **3.3.3 for 1.21.1**, version ID `9w34y8ai`, as a required
version dependency. A project-only dependency could install an unsupported DH
version. Do not upload the sources JAR as an installable mod.

Use the published GitHub v1.0.2 binary and verify it against that release's checksum
manifest. Do not rebuild or replace an existing public release for documentation
changes. Keep the corresponding source archive and license notices accessible
from the GitHub release, as required by this project's distribution license.

The project page should explain the macOS driver reproduction, without describing
this as a general performance optimization or a fix for every DH crash. The patch
also safely applies on other supported client platforms.

## Upload process

1. Create a draft project at Modrinth while signed in as mannie-exe.
2. Set the project fields above and use the README's player-facing text for the
   description. Add a human-created icon if desired; do not reuse DH branding.
3. Upload the verified binary and release notes. Select the exact required
   dependency version, loader and game version before publishing the version.
4. Review the draft page and submit the project for moderation when ready.
5. Confirm that the downloaded primary file has the release's SHA-512 digest.

The GitHub workflow does not upload to Modrinth. A later automated uploader should
use a repository secret containing a scoped Modrinth token, resolve the project ID,
and upload the tested GitHub artifact with exact dependency IDs. Do not store a
token in Gradle properties or documentation. No Modrinth token is needed to build.

References: [project creation](https://docs.modrinth.com/api/operations/createproject/),
[version uploads](https://docs.modrinth.com/api/operations/createversion/),
[environment selections](https://modrinth.com/news/article/new-environments/).

## Content disclosures and publication eligibility

Select **AI-generated code** and **AI-generated text** for the contributions
produced in this authoring workflow. There is no runtime AI functionality or
AI-generated artwork in these projects. DH Polygon Fix also attributes its
upstream backport in NOTICE and the README; its derivative-code disclosure should
identify that origin.

The [current Modrinth rules](https://modrinth.com/legal/rules) prohibit public
projects whose contents are primarily or entirely AI-generated. Disclosure alone
does not make such a project eligible. Sky Village Neighbors' current code was
written in this agent workflow and must not be presented as ready for public
Modrinth submission under that rule. DH Polygon Fix combines an attributed
upstream fix with agent-authored integration/tests; eligibility is not established
by its passing tests or GitHub release. Resolve eligibility with Modrinth before
submitting either project; do not misstate authorship or treat superficial edits
as changing it.

These fields and upload instructions are a prepared publishing reference, not a
claim of moderation approval. See also the
[disclosure guide](https://support.modrinth.com/en/articles/16567675-content-disclosures).
