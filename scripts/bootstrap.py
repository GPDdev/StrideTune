"""Install the build tools locally; validate archives against publisher checksums."""
import concurrent.futures
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import urllib.request
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
TOOLS = ROOT / '.tools'
BASE = 'https://dl.google.com/android/repository/'


def fetch(url):
    return subprocess.check_output(['curl.exe', '-fsSL', '--retry', '3', '--connect-timeout', '20', '--max-time', '180', url])


def archive(url, filename, algorithm, checksum):
    path = TOOLS / filename
    if not path.exists() or hashlib.new(algorithm, path.read_bytes()).hexdigest() != checksum:
        print(f'Downloading {filename}', flush=True)
        partial = path.with_suffix('.part')
        subprocess.run(['curl.exe', '-fsSL', '--retry', '3', '--retry-all-errors', '--retry-delay', '1', '-C', '-', '--connect-timeout', '20', '--max-time', '600', url, '-o', str(partial)], check=True)
        actual = hashlib.new(algorithm, partial.read_bytes()).hexdigest()
        if actual != checksum:
            raise RuntimeError(f'Checksum mismatch: {filename}')
        partial.replace(path)
    return path


def sdk_package(package, dest):
    candidates = [p for p in repository.findall('remotePackage') if p.get('path') == package]
    p = next((p for p in candidates if '-ext' not in p.findtext('display-name', '').lower()), candidates[-1])
    a = next(a for a in p.findall('./archives/archive') if a.findtext('host-os') in (None, 'windows'))
    name = a.findtext('./complete/url')
    checksum = a.find('./complete/checksum')
    path = archive(BASE + name, name, checksum.get('type', 'sha1'), checksum.text)
    if not dest.exists():
        unpack = TOOLS / ('unpack-' + package.replace(';', '-'))
        with zipfile.ZipFile(path) as z:
            z.extractall(unpack)
        dirs = list(unpack.iterdir())
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(dirs[0]), str(dest))
        unpack.rmdir()
    print(f'Ready: {package}', flush=True)


def gradle():
    name = 'gradle-8.11.1-bin.zip'
    url = 'https://services.gradle.org/distributions/' + name
    checksum = fetch(url + '.sha256').decode().strip()
    path = archive(url, name, 'sha256', checksum)
    if not (TOOLS / 'gradle-8.11.1').exists():
        with zipfile.ZipFile(path) as z:
            z.extractall(TOOLS)
    print('Ready: Gradle 8.11.1', flush=True)


if __name__ == '__main__':
    TOOLS.mkdir(exist_ok=True)
    repository = ET.fromstring(fetch(BASE + 'repository2-1.xml'))
    sdk = TOOLS / 'android-sdk'
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        futures = [pool.submit(gradle)] + [pool.submit(sdk_package, package, sdk / dest) for package, dest in [
            ('platforms;android-35', 'platforms/android-35'),
            ('build-tools;35.0.0', 'build-tools/35.0.0'),
            ('platform-tools', 'platform-tools')]]
        for future in concurrent.futures.as_completed(futures):
            future.result()
    (ROOT / 'local.properties').write_text('sdk.dir=' + sdk.as_posix() + '\n', encoding='utf-8')
    print('Build tools ready. Run scripts/build.ps1.', flush=True)
