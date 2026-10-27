import hashlib
import json
import os
import sys
import re
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path


def inputs_hash(root):
    paths = sorted([root / "buildScripts/setup.ant.xml",
                    *root.glob("buildScripts/ivy*.xml"),
                    *root.glob("buildScripts/ivy-repo/*.xml")])
    digest = hashlib.sha256()
    for path in paths:
        digest.update(str(path.relative_to(root)).encode() + b"\0" + path.read_bytes() + b"\0")
    return digest.hexdigest()


def mirror(lock_file, directory):
    lock = json.loads(lock_file.read_text())
    if inputs_hash(Path(".")) != lock["buildScriptsHash"]:
        raise SystemExit("Nix dependency lock is stale. Run: "
                         "nix develop -c python3 nix/dependencies.py update")
    urls = {item["url"] for item in lock["downloads"]}
    prefix = directory.as_uri()
    timestamp = int(os.environ["SOURCE_DATE_EPOCH"]) * 1000

    def replace(match):
        attribute, url = match.groups()
        if attribute == "root" or url in urls:
            return f'{attribute}="{prefix}/{url.replace("://", "/", 1)}"'
        return match.group()

    for path in Path("buildScripts").rglob("*.xml"):
        text = re.sub(r'\b(root|src|url)="(https?://[^"]+)"', replace, path.read_text())
        # Outer archive timestamps do not normalize embedded JARs.
        text = re.sub(r'<(jar|zip)(?=[\s>])',
                      rf'<\1 modificationtime="{timestamp}"', text)
        path.write_text(text)


def collect(build, log):
    downloads = {}

    def add(url, path):
        if url.startswith(("https://", "http://")):
            digest = hashlib.sha256(Path(path).read_bytes()).hexdigest()
            if url in downloads and downloads[url] != digest:
                raise ValueError(f"Inconsistent download: {url}")
            downloads[url] = digest

    url = None
    for line in log.splitlines():
        if "[get] Getting: " in line:
            url = line.split("[get] Getting: ", 1)[1].strip()
        elif "[get] To: " in line and url:
            add(url, line.split("[get] To: ", 1)[1].strip())
            url = None

    cache = build / "ivyCache"
    # Parent/BOM POMs are needed offline but absent from resolved-artifact reports.
    for path in cache.rglob("ivydata-*.properties"):
        revision = path.name.removeprefix("ivydata-").removesuffix(".properties")
        for line in path.read_text().splitlines():
            line = re.sub(r"\\(.)", r"\1", line)
            if "#pom.original#" in line and ".location=" in line:
                add(line.split(".location=", 1)[1], path.parent / f"ivy-{revision}.xml.original")

    reports = list(cache.glob("*-*.xml"))
    if not reports:
        raise ValueError("No Ivy resolution reports")
    for report in reports:
        for artifact in ET.parse(report).findall(".//artifact"):
            origin = artifact.find("origin-location")
            if origin is not None:
                add(origin.attrib["location"], artifact.attrib["location"])
    if not downloads:
        raise ValueError("No downloads found")
    return [{"url": url, "sha256": digest} for url, digest in sorted(downloads.items())]


def update():
    root = Path(__file__).resolve().parent.parent
    with tempfile.TemporaryDirectory(prefix="lombok-dependencies-") as directory:
        build = Path(directory)
        paths = subprocess.check_output(["git", "ls-files", "-z"], cwd=root).decode().split("\0")
        for name in filter(None, paths):
            source = root / name
            if source.is_file():
                (build / name).parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(source, build / name)
        fingerprint = inputs_hash(build)
        result = subprocess.run(["ant", "-verbose", "deps"], cwd=build,
                                stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        if result.returncode:
            raise SystemExit(result.stdout)
        downloads = collect(build, result.stdout)
    (root / "nix/dependencies.json").write_text(json.dumps({"buildScriptsHash": fingerprint, "downloads": downloads}, indent=2) + "\n")
    print(f"Locked {len(downloads)} downloads")


if __name__ == "__main__":
    if sys.argv[1:] == ["update"]:
        update()
    elif len(sys.argv) == 4 and sys.argv[1] == "mirror":
        mirror(Path(sys.argv[2]), Path(sys.argv[3]))
    else:
        raise SystemExit("Usage: python3 nix/dependencies.py update")
