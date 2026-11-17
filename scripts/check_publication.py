#!/usr/bin/env python3
"""Check Git's publishable files, not local build output. No network access."""
from pathlib import Path
import re
import subprocess
import sys
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
raw = subprocess.check_output(['git','ls-files','-z','--cached','--others','--exclude-standard'],cwd=ROOT)
files = sorted(set(raw.decode().strip('\0').split('\0')) - {''})
manifest = set(files)
errors = []
for required in ['README.md','LICENSE','THIRD_PARTY_NOTICES.md','docs/banner.png','gradlew','gradlew.bat','gradle/wrapper/gradle-wrapper.jar']:
    if required not in manifest: errors.append(f'Missing public file: {required}')
private_parts = {'.local','.gradle','.kotlin','.idea','node_modules','build','artifacts'}
private_names = {'local.properties','key.properties','keystore.properties','signing.properties'}
private_suffixes = {'.apk','.aab','.jks','.keystore','.pem','.key','.p12','.pfx','.hprof','.log'}
rules = {
    'personal absolute path': re.compile(r'/(?:Users|home)/[A-Za-z0-9._-]+/'),
    'temporary macOS path': re.compile(r'/var/folders/[A-Za-z0-9/_.-]+'),
    'private key': re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----'),
    'GitHub credential': re.compile(r'\b(?:gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{30,})'),
    'AWS access key': re.compile(r'\b(?:AKIA|ASIA)[A-Z0-9]{16}\b'),
    'API credential': re.compile(r'\bsk-(?:proj-)?[A-Za-z0-9_-]{30,}'),
}
links = 0
for name in files:
    path=ROOT/name
    parts=set(Path(name).parts)
    if parts & private_parts or Path(name).name in private_names or Path(name).suffix in private_suffixes or Path(name).name.startswith('.env'):
        errors.append(f'Private/local file in candidate: {name}')
        continue
    if name.startswith('artwork/') and 'tmp' in parts:
        errors.append(f'Temporary artwork in candidate: {name}')
        continue
    if path.is_symlink():
        errors.append(f'Symlink needs review: {name}')
        continue
    if not path.is_file():
        errors.append(f'Missing candidate file: {name}')
        continue
    if path.stat().st_size > 10*1024*1024:
        errors.append(f'File exceeds 10 MiB review threshold: {name}')
    try: text=path.read_text()
    except UnicodeError: continue
    for label,rule in rules.items():
        if rule.search(text): errors.append(f'{label}: {name}')
    if path.suffix.lower()!='.md': continue
    targets=re.findall(r'\]\(([^\s)]+)(?:\s+"[^"]*")?\)',text)
    targets+=re.findall(r'<(?:img|a)\b[^>]*(?:src|href)=["\']([^"\']+)',text)
    for target in targets:
        url=urlsplit(target.strip('<>'))
        if url.scheme or url.netloc or not url.path: continue
        links+=1
        destination=(path.parent/unquote(url.path)).resolve()
        try: relative=destination.relative_to(ROOT).as_posix()
        except ValueError:
            errors.append(f'Document link leaves repo: {name} -> {target}');continue
        if relative not in manifest and not any(f.startswith(relative.rstrip('/')+'/') for f in files):
            errors.append(f'Link is missing or excluded: {name} -> {target}')
if errors:
    for error in errors: print(error,file=sys.stderr)
    raise SystemExit(1)
print(f'Publication check passed: {len(files)} files, {links} local documentation links; no flagged paths or credential patterns.')
print('This is a focused publication guard, not a comprehensive security audit.')
