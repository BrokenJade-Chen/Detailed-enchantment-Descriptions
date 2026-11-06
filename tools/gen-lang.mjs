// Generates assets/detailed_enchantment_descriptions/lang/{en_us,zh_cn}.json
// from the table below, so the two languages never drift apart.
import fs from 'node:fs';
import path from 'node:path';

const rows = [
  // id, zh, en, zhPlain, enPlain  (plain is only used by numeric lines; "%s" entries need "" for percent)
  ['sharpness', '增加 %s 点近战伤害。', 'Increases melee damage by %s.', '增加近战伤害。', 'Increases melee damage.'],
  ['smite', '对亡灵生物额外造成 %s 点伤害。', 'Deals %s extra damage to undead.', '对亡灵生物造成额外伤害。', 'Deals extra damage to undead.'],
  ['bane_of_arthropods', '对节肢生物额外造成 %s 点伤害。', 'Deals %s extra damage to arthropods.', '对节肢生物造成额外伤害。', 'Deals extra damage to arthropods.'],
  ['impaling', '对水生生物额外造成 %s 点伤害。', 'Deals %s extra damage to aquatic mobs.', '对水生生物造成额外伤害。', 'Deals extra damage to aquatic mobs.'],
  ['power', '箭矢伤害 +%s。', 'Increases arrow damage by %s.', '增加箭矢伤害。', 'Increases arrow damage.'],
  ['knockback', '击退距离 +%s。', 'Increases knockback by %s.', '增加击退距离。', 'Increases knockback.'],
  ['punch', '箭矢击退距离 +%s。', 'Increases arrow knockback by %s.', '增加箭矢击退距离。', 'Increases arrow knockback.'],
  ['piercing', '箭矢可穿透 %s 个目标。', 'Arrows pierce %s targets.', '箭矢可以穿透多个目标。', 'Arrows pierce multiple targets.'],
  ['density', '每下落 1 格，重锤伤害 +%s 点。', 'Adds %s damage per fallen block.', '下落时增加重锤伤害。', 'Adds damage per fallen block.'],
  ['breach', '无视目标 %s 的护甲。', "Ignores %s of the target's armor.", '无视目标的部分护甲。', "Ignores part of the target's armor."],
  ['protection', '受到的伤害减少 %s。', 'Reduces incoming damage by %s.', '减少受到的伤害。', 'Reduces incoming damage.'],
  ['fire_protection', '受到的火焰伤害减少 %s，燃烧时间减少 %s。', 'Reduces fire damage by %s and burn time by %s.', '减少受到的火焰伤害和燃烧时间。', 'Reduces fire damage and burn time.'],
  ['blast_protection', '受到的爆炸伤害减少 %s，爆炸击退减少 %s。', 'Reduces explosion damage by %s and explosion knockback by %s.', '减少受到的爆炸伤害和爆炸击退。', 'Reduces explosion damage and knockback.'],
  ['projectile_protection', '受到的弹射物伤害减少 %s。', 'Reduces projectile damage by %s.', '减少受到的弹射物伤害。', 'Reduces projectile damage.'],
  ['feather_falling', '受到的摔落伤害减少 %s。', 'Reduces fall damage by %s.', '减少受到的摔落伤害。', 'Reduces fall damage.'],
  ['efficiency', '挖掘速度 +%s（与工具本身速度相加）。', "Increases mining speed by %s (added to the tool's speed).", '增加挖掘速度。', 'Increases mining speed.'],
  ['aqua_affinity', '水下挖掘速度不再降低。', 'Removes the underwater mining speed penalty.', null, null],
  ['depth_strider', '水中移动速度提升 %s。', 'Increases underwater movement speed by %s.', '提高水中移动速度。', 'Increases underwater movement speed.'],
  ['respiration', '水下呼吸时间 +%s 秒。', 'Extends underwater breathing by %s seconds.', '延长水下呼吸时间。', 'Extends underwater breathing.'],
  ['swift_sneak', '潜行速度为行走速度的 %s。', 'Sneaking speed becomes %s of walking speed.', '提高潜行速度。', 'Increases sneaking speed.'],
  ['sweeping_edge', '横扫攻击造成 %s 的攻击伤害。', 'Sweeping attacks deal %s of attack damage.', '提高横扫攻击的伤害。', 'Increases sweeping attack damage.'],
  ['soul_speed', '在灵魂方块上移动速度 +%s，耐久消耗 +%s。', 'Increases movement speed on soul blocks by %s and durability use by %s.', '在灵魂方块上移动更快，但更耗耐久。', 'Move faster on soul blocks at the cost of durability.'],
  ['unbreaking', '有 %s 的概率不消耗耐久。', '%s chance to not consume durability.', '有概率不消耗耐久。', 'Chance to not consume durability.'],
  ['thorns', '有 %s 的概率反弹 1~5 点伤害。', '%s chance to deal 1-5 damage back.', '有概率反弹伤害。', 'Chance to deal damage back.'],
  ['fire_aspect', '点燃目标 %s 秒。', 'Sets the target on fire for %s seconds.', '点燃目标。', 'Sets the target on fire.'],
  ['flame', '点燃目标 5 秒。', 'Sets the target on fire for 5 seconds.', null, null],
  ['frost_walker', '在脚下生成半径 %s 格的霜冰。', 'Creates %s-block frost ice.', '在脚下生成霜冰。', 'Creates frost ice.'],
  ['wind_burst', '落地攻击产生风爆，击退倍率 %s。', 'Creates a wind burst on a falling attack, knockback x%s.', '落地攻击产生风爆。', 'Creates a wind burst on a falling attack.'],
  ['lure', '钓鱼等待时间减少 %s 秒。', 'Decreases fishing wait time by %s seconds.', '缩短钓鱼等待时间。', 'Decreases fishing wait time.'],
  ['luck_of_the_sea', '钓鱼时幸运 +%s。', 'Increases fishing luck by %s.', '提高钓鱼幸运。', 'Increases fishing luck.'],
  ['quick_charge', '弩的装填时间减少 %s 秒。', 'Reduces crossbow charge time by %s seconds.', '缩短弩的装填时间。', 'Reduces crossbow charge time.'],
  ['multishot', '一次射出 %s 支箭。', 'Fires %s arrows.', '一次射出多支箭。', 'Fires multiple arrows.'],
  ['loyalty', '三叉戟返回速度 +%s。', 'Increases trident return speed by %s.', '加快三叉戟返回。', 'Increases trident return speed.'],
  ['riptide', '冲刺撞击伤害 +%s。', 'Adds %s damage when riptiding.', '冲刺时造成额外伤害。', 'Adds damage when riptiding.'],
  ['mending', '每 1 点经验修复 2 点耐久。', 'Repairs 2 durability per 1 XP.', null, null],
  ['looting', '掉落数量增加 0~%s 个，装备掉落概率 +%s。', 'Increases drops by 0-%s, and equipment drop chance by %s.', '增加掉落数量。', 'Increases drop amounts.'],
  ['binding_curse', '装备后无法取下。', 'Cannot be removed once equipped.', null, null],
  ['vanishing_curse', '死亡时物品消失。', 'Disappears on death.', null, null],
  ['channeling', '雷暴天气下击中生物或避雷针时召唤闪电。', 'Summons lightning during thunderstorms when hitting a mob or a lightning rod.', null, null],
  ['fortune', '增加方块掉落数量与稀有掉落概率。', 'Increases block drops and the chance of rare drops.', null, null],
  ['infinity', '射箭不消耗箭矢（至少需要 1 支）。', 'Arrows are not consumed (requires at least one).', null, null],
  ['silk_touch', '方块掉落自身。', 'Blocks drop themselves.', null, null],
];

const ui = {
  zh_cn: {
    'ded.config.title': '详细附魔描述 · 设置',
    'ded.config.category.general': '常规',
    'ded.config.enabled.tooltip': '总开关，关闭后完全恢复原版 tooltip。',
    'ded.config.numbers.tooltip': '在描述里显示精确数值。',
    'ded.config.enabled': '启用',
    'ded.config.descriptions': '显示描述',
    'ded.config.numbers': '显示数值',
    'ded.config.modded': '包含模组附魔',
    'ded.config.books': '附魔书',
    'ded.config.number_color': '数值颜色',
    'ded.config.description_color': '描述颜色',
    'ded.color.brightness': '亮度：%s%%',
    'ded.color.hex': '颜色代码',
    'ded.color.apply': '应用',
    'ded.color.preview.description': '锋利 V：增加 %s 点近战伤害。',
    'ded.color.preview.value': '耐久 III：有 %s 的概率不消耗耐久。',

    'ded.color.dark_gray': '深灰',
    'ded.value.on': '开',
    'ded.value.off': '关',
    'ded.color.dark_green': '深绿',
    'ded.color.green': '绿',
    'ded.color.aqua': '青',
    'ded.color.blue': '蓝',
    'ded.color.gold': '金',
    'ded.color.gray': '灰',
    'ded.color.yellow': '黄',
    'key.categories.detailed_enchantment_descriptions': '详细附魔描述',
    'key.detailed_enchantment_descriptions.open_config': '打开设置',
  },
  en_us: {
    'ded.config.title': 'Detailed Enchantment Descriptions · Settings',
    'ded.config.category.general': 'General',
    'ded.config.enabled.tooltip': 'Master switch; turning this off restores the vanilla tooltip.',
    'ded.config.numbers.tooltip': 'Show the exact numbers inside the description.',
    'ded.config.enabled': 'Enabled',
    'ded.config.descriptions': 'Descriptions',
    'ded.config.numbers': 'Numbers',
    'ded.config.modded': 'Modded enchantments',
    'ded.config.books': 'Enchanted books',
    'ded.config.number_color': 'Number colour',
    'ded.config.description_color': 'Description colour',
    'ded.color.brightness': 'Brightness: %s%%',
    'ded.color.hex': 'Hex colour',
    'ded.color.apply': 'Apply',
    'ded.color.preview.description': 'Sharpness V: Increases melee damage by %s.',
    'ded.color.preview.value': 'Unbreaking III: %s chance to not consume durability.',

    'ded.color.dark_gray': 'dark gray',
    'ded.value.on': 'on',
    'ded.value.off': 'off',
    'ded.color.dark_green': 'dark green',
    'ded.color.green': 'green',
    'ded.color.aqua': 'aqua',
    'ded.color.blue': 'blue',
    'ded.color.gold': 'gold',
    'ded.color.gray': 'gray',
    'ded.color.yellow': 'yellow',
    'key.categories.detailed_enchantment_descriptions': 'Detailed Enchantment Descriptions',
    'key.detailed_enchantment_descriptions.open_config': 'Open settings',
  },
};

const outDir = path.resolve('src/main/resources/assets/detailed_enchantment_descriptions/lang');
fs.mkdirSync(outDir, { recursive: true });

for (const lang of ['en_us', 'zh_cn']) {
  const map = { ...ui[lang] };
  const zi = lang === 'zh_cn' ? 1 : 2;
  const pi = lang === 'zh_cn' ? 3 : 4;
  for (const row of rows) {
    const id = row[0];
    map[`ded.desc.${id}`] = row[zi];
    if (row[pi] != null) map[`ded.desc.${id}.plain`] = row[pi];
  }
  const sorted = Object.fromEntries(Object.keys(map).sort().map((key) => [key, map[key]]));
  fs.writeFileSync(path.join(outDir, `${lang}.json`), JSON.stringify(sorted, null, 2) + '\n', 'utf8');
  console.log(lang, Object.keys(sorted).length, 'keys');
}
