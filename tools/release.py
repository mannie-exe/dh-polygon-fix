#!/usr/bin/env python3
"""Publish only the identical, tested binaries from this tag's three CI jobs."""
import hashlib
import pathlib
import shutil
import subprocess
import sys

root = pathlib.Path(__file__).resolve().parents[1]
version = next(
    line.split("=", 1)[1]
    for line in (root / "gradle.properties").read_text().splitlines()
    if line.startswith("mod_version=")
)
tag = sys.argv[1]
if tag != f"v{version}":
    raise SystemExit("Tag must equal mod_version")
head = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip()
tag_head = subprocess.check_output(["git", "rev-parse", f"{tag}^{{commit}}"], cwd=root, text=True).strip()
if head != tag_head:
    raise SystemExit("Release checkout does not match tag")

destination = root / "build/release"
destination.mkdir(parents=True, exist_ok=False)
for filename in [
    f"dh-polygon-fix-{version}-mc1.21.1-neoforge.jar",
    f"dh-polygon-fix-{version}-sources.jar",
]:
    builds = [
        root / "build/ci" / f"build-{runner}" / "libs" / filename
        for runner in ["ubuntu-24.04", "windows-2025", "macos-15"]
    ]
    hashes = {hashlib.sha256(path.read_bytes()).hexdigest() for path in builds}
    if len(hashes) != 1:
        raise SystemExit(f"Cross-platform build mismatch: {filename}")
    shutil.copyfile(builds[0], destination / filename)

subprocess.run([
    "git", "archive", "--format=zip", f"--prefix=dh-polygon-fix-{version}/",
    f"--output={destination / f'dh-polygon-fix-{version}-source.zip'}", head,
], cwd=root, check=True)
assets = sorted(destination.iterdir())
for algorithm in ["sha256", "sha512"]:
    manifest = destination / f"{algorithm.upper()}SUMS"
    manifest.write_text("".join(
        f"{hashlib.new(algorithm, path.read_bytes()).hexdigest()}  {path.name}\n"
        for path in assets
    ), encoding="utf-8")
subprocess.run([
    "gh", "release", "create", tag,
    *map(str, sorted(destination.iterdir())),
    "--verify-tag", "--draft", "--title", f"DH Polygon Fix {version}",
    "--notes-file", "RELEASE_NOTES.md",
], cwd=root, check=True)
subprocess.run(["gh", "release", "edit", tag, "--draft=false"], cwd=root, check=True)
