> **AI 声明**：本模组是在 AI（DeepSeek Harness）辅助下完成的，作者本人只在游戏内实测了每个版本。

# 详细附魔描述 · Detailed Enchantment Descriptions

在物品提示框里，为每个附魔名字下方加一行简短描述；数值直接读取附魔自身的数据，**当前等级是多少就显示多少**，与游戏实际行为保持一致。

支持附魔书，尊重原版"隐藏附魔"标志，并且支持用 JSON 自定义任何附魔（包括其它模组的附魔）。

## 支持版本

| 加载器 | Minecraft |
|---|---|
| Fabric | 1.21.1、1.21.4、1.21.8、1.21.11 |
| NeoForge | 1.21.1 |

仅客户端模组。Fabric 需要 **Fabric API**；Mod Menu 可选（提供设置入口）。

## 功能

* **覆盖 42 个原版附魔**，每个都有一行当前等级的说明。
* **数值来自游戏本身**，不是手写表格：模组读取的正是游戏使用的附魔数据，所以数值不会和实际效果脱节，数据包改动也会自动生效。
* **支持使用标准效果组件的模组附魔**；任何附魔也都可以通过 JSON 文件自定义描述。
* **设置界面** —— 用 `/ded`、`/dedconfig`、可重绑定的按键，或 Fabric 上的 Mod Menu 打开：
  * 开关描述、数值、附魔书；
  * 颜色选择器：HSV 色盘、12 个预设色、`#RRGGBB` 输入，分别设置描述颜色与数值颜色。
* **按住才显示** —— 可选只在按住某个按键时显示描述。
* **JSON 覆盖** —— 把文件放进资源包（`assets/<命名空间>/enchantment_descriptions/*.json`）或配置目录（`config/detailed_enchantment_descriptions/enchantments/*.json`）。模式：`replace` / `append` / `disable`。命令：`/ded reload`、`/ded dump <id>`、`/ded export <id>`。
* 中英双语，数字不带多余的 `.0`。
* 尊重原版"隐藏附魔"标志。

## 显示效果示例

* 锋利 V → *增加 3 点近战伤害。*
* 保护 IV → *受到的伤害减少 16%。*
* 耐久 III → *有 75% 的概率不消耗耐久。*（盔甲为 30%）
* 灵魂疾行 III → *在灵魂方块上移动速度 +61.5%，耐久消耗 +12%。*

## 说明

* 请移除其它附魔描述类模组（例如 Enchantment Descriptions），避免重复显示。
* 本模组只负责显示信息，不会改变附魔的实际效果。
* 截图里的圆角提示框和更平滑的字体来自其它客户端模组，不是本模组提供的。

## 致谢

灵感来自 Ronidas 的 [Exact Enchantments](https://modrinth.com/mod/exact-enchantments)（MIT）。本项目是独立重构，未复制其代码。

## 许可证

MIT。源码与问题反馈：[github.com/BrokenJade-Chen/Detailed-enchantment-Descriptions](https://github.com/BrokenJade-Chen/Detailed-enchantment-Descriptions)
