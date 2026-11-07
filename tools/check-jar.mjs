// Verifies a built jar really contains what we expect. Exit code 1 on any problem.
import fs from 'node:fs';
import zlib from 'node:zlib';

const file = process.argv[2];
if (!file) { console.error('usage: node check-jar.mjs <jar>'); process.exit(2); }
const buf = fs.readFileSync(file);

// Minimal zip reader (central directory scan)
function entries(b) {
  let eocd = -1;
  for (let i = b.length - 22; i >= 0 && i > b.length - 65557; i--) {
    if (b.readUInt32LE(i) === 0x06054b50) { eocd = i; break; }
  }
  if (eocd < 0) throw new Error('no end-of-central-directory');
  const count = b.readUInt16LE(eocd + 10);
  let off = b.readUInt32LE(eocd + 16);
  const out = new Map();
  for (let i = 0; i < count; i++) {
    if (b.readUInt32LE(off) !== 0x02014b50) throw new Error('bad central header');
    const nameLen = b.readUInt16LE(off + 28);
    const extraLen = b.readUInt16LE(off + 30);
    const commentLen = b.readUInt16LE(off + 32);
    const localOff = b.readUInt32LE(off + 42);
    const name = b.subarray(off + 46, off + 46 + nameLen).toString('utf8');
    const compSize = b.readUInt32LE(off + 20);
    const method = b.readUInt16LE(off + 10);
    out.set(name, { localOff, compSize, method });
    off += 46 + nameLen + extraLen + commentLen;
  }
  return out;
}
function read(b, entry) {
  const o = entry.localOff;
  const nameLen = b.readUInt16LE(o + 26);
  const extraLen = b.readUInt16LE(o + 28);
  const start = o + 30 + nameLen + extraLen;
  const data = b.subarray(start, start + entry.compSize);
  return entry.method === 0 ? data : zlib.inflateRawSync(data);
}

const list = entries(buf);
const problems = [];
const classes = [...list.keys()].filter(n => n.endsWith('.class'));
if (classes.length === 0) problems.push('no .class entries');
if (!list.has('fabric.mod.json')) problems.push('missing fabric.mod.json');
else {
  const meta = JSON.parse(read(buf, list.get('fabric.mod.json')).toString('utf8'));
  console.log('version:', meta.version, '| id:', meta.id, '| classes:', classes.length, '| entries:', list.size);
}
const lang = [...list.keys()].filter(n => n.endsWith('lang/zh_cn.json'));
if (lang.length === 0) problems.push('missing zh_cn lang');
else {
  const json = JSON.parse(read(buf, list.get(lang[0])).toString('utf8'));
  if (!json['key.detailed_enchantment_descriptions.show_descriptions']) problems.push('missing show_descriptions key');
}
if (![...list.keys()].some(n => n.endsWith('color_wheel.png'))) problems.push('missing color_wheel.png');
if (buf.length < 20000) problems.push('jar looks empty (' + buf.length + ' bytes)');

if (problems.length) { console.error('PROBLEMS: ' + problems.join('; ')); process.exit(1); }
console.log('OK');
