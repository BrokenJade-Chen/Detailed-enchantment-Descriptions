// Builds the mod for one or more Minecraft versions.
// Usage: node tools/build-version.mjs 1.21.4 1.21.1 1.21.11
// Each version is built in its own working copy (build-work/<version>), with the matching
// overlays/<version> files copied on top of src/, so version-specific classes replace the defaults.
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';

const root = process.cwd();
const gradleBat = process.env.GRADLE_BAT || 'gradle';
const versions = process.argv.slice(2);
if (versions.length === 0) versions.push('1.21.8');

function copyDir(from, to) {
  fs.mkdirSync(to, { recursive: true });
  for (const entry of fs.readdirSync(from, { withFileTypes: true })) {
    const src = path.join(from, entry.name);
    const dst = path.join(to, entry.name);
    if (entry.isDirectory()) copyDir(src, dst);
    else fs.copyFileSync(src, dst);
  }
}

const results = [];
for (const v of versions) {
  const dir = path.join(root, 'build-work', v);
  fs.rmSync(dir, { recursive: true, force: true });
  fs.mkdirSync(dir, { recursive: true });

  copyDir(path.join(root, 'src'), path.join(dir, 'src'));
  const overlay = path.join(root, 'overlays', v);
  const overlayUsed = fs.existsSync(overlay);
  if (overlayUsed) copyDir(overlay, dir);

  for (const f of ['build.gradle', 'settings.gradle', 'gradle.properties']) {
    fs.copyFileSync(path.join(root, f), path.join(dir, f));
  }
  fs.mkdirSync(path.join(dir, 'versions', v), { recursive: true });
  fs.copyFileSync(path.join(root, 'versions', v, 'gradle.properties'),
                  path.join(dir, 'versions', v, 'gradle.properties'));

  console.log(`\n=== ${v} (overlay: ${overlayUsed ? 'yes' : 'no'}) ===`);
  execFileSync(gradleBat, ['-p', dir, 'build', `-Pmcver=${v}`, '--console=plain'],
      { stdio: 'inherit', env: process.env, shell: true });

  const libs = path.join(dir, 'build', 'libs');
  const jars = fs.readdirSync(libs).filter(f => f.endsWith('.jar') && !f.includes('sources'));
  if (jars.length !== 1) throw new Error(`expected one jar in ${libs}, found ${jars.length}`);
  const jar = path.join(libs, jars[0]);
  execFileSync(process.execPath, [path.join(root, 'tools', 'check-jar.mjs'), jar], { stdio: 'inherit' });

  const outDir = path.join(root, '..', 'dist');
  fs.mkdirSync(outDir, { recursive: true });
  fs.copyFileSync(jar, path.join(outDir, jars[0]));
  const sources = fs.readdirSync(libs).find(f => f.endsWith('-sources.jar'));
  if (sources) fs.copyFileSync(path.join(libs, sources), path.join(outDir, sources));
  results.push(jars[0]);
}

console.log('\nbuilt:');
for (const r of results) console.log(' -', r);


