# PolyglotTooltip 核心功能

参考来源：`thirdpart/PolyglotTooltip`，分支 `main`，模组 1.6.0。

- Minecraft 1.21.1
- NeoForge 21.1.219
- 仅客户端
- 许可证 MIT；简繁转换捆绑 Opencc4j（Apache-2.0）

它不改变游戏当前语言。它另建一份每种配置语言的客户端语言表，用来取第二名称和搜索词。

## 提示框

- 在物品名称下方，每种配置的附加语言插入一行名称。默认附加语言是 `en_us`，可以配成多份。
- 附加名称与当前游戏语言的名称相同时不显示，除非打开 `alwaysShow`（默认关）。
- 附加名称可用旧式格式码上色，默认 `&7`。Jade 另有一套格式码，默认也是 `&7`。
- 附魔行在原名称后用 `｜` 追加其他语言的附魔名，不单独占一行。
- 插入后会把第二名称重新排到标题紧下方，避免被其他提示组件挤走。
- 流体、Mekanism 化学品在对应集成里走同一套第二名称，不是原版流体提示框的通用覆盖。
- 个别模组的名称不是普通物品名，另有专门取名：Ars Nouveau 法术符文、Industrial Foregoing 插件、Productive Bees、Storage Drawers、探索者罗盘的结构名、自然罗盘的生物群系名。

## Jade

- 注册方块名和实体名两个客户端组件。
- 瞄准方块（含流体方块）或实体时，在原名称下插入第二语言名称。
- 已有第二名称标记时不重复插入。

## 搜索

搜索词来自当前名称、所有已加载附加语言的名称，以及这些名称的简繁变体。

- 简繁互换默认关闭（`enableChineseScriptMatching`）。打开后，JEI、EMI、AE2、RS2 及相关界面把简体和繁体当成同一搜索词。
- 拼音不是本模组的功能。
- 安装 Just Enough Characters 时，向 JEC 的索引补简繁变体，不替换 JEC 自己的拼音。未安装 JEC 时这条 mixin 不加载。

已接入搜索或第二名称的界面：

- JEI
- EMI
- AE2（含样板访问终端、Mek AE2 化学品）
- Refined Storage 2（物品、流体、Mekanism 化学品）
- Mekanism QIO 与化学品
- Integrated Terminals
- Occultism
- ProjectE
- Sophisticated Core
- Controlling 的按键搜索
- Apothic Enchanting 的附魔库

## 它没有的能力

- 不切换游戏主语言，也不预加载后用快捷键对调主语言。
- 不提供拼音、首字母或虚拟拼音语言。
- 不缩小第二语言字号，不做高分辨率整体缩字。
- 物品名不做单行中英拼接。单行追加只出现在附魔的 `｜`。
- 没有「仅主语言 / 仅副语言 / 双语」三种显示模式。
- 不覆盖普通菜单、聊天、地图、书本的双语行。

## 对本项目的含义

现阶段只借用三块行为：物品名第二行、Jade 瞄准名第二行、JEI 里同时用中文名和英文名搜索。拼音由 MCBilingual 自己做。其余集成留在这份清单里，等以后的规格再取。
