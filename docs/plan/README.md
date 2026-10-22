# 参考功能与现阶段范围

本目录记录两个参考项目的核心功能，供以后扩功能时查阅。现阶段规格在 `specs/`，不在这里。

核对日期：2026-10-22。本地 `thirdpart/` 已换成用户更新后的 remote，结论以当前检出分支的构建文件为准。

## 加载器核对

| 项目 | Remote | 当前检出 | Minecraft | 加载器 | 对本项目的用法 |
| --- | --- | --- | --- | --- | --- |
| PolyglotTooltip | `https://github.com/StarskyXIII/PolyglotTooltip.git` | `main`（没有名为 `1.21.1` 的分支） | 1.21.1 | NeoForge **21.1.219** | 同为 NeoForge 1.21.1。可以按它的源码改写进 MCBilingual。不能把该仓库当依赖，也不能假设 21.1.219 的代码在本项目的 NeoForge 21.1.251 上原样编译通过。 |
| Language Reload | `https://github.com/Jerozgen/LanguageReload.git` | `1.21.1` | 1.21.1 | **Fabric**（Yarn、Fabric Loom、`fabric.mod.json`，模组 1.7.6） | 不支持 NeoForge。只能按行为重新实现。禁止把 Fabric mixin 搬进来。 |

PolyglotTooltip 许可证 MIT，并捆绑 Apache-2.0 的 Opencc4j。Language Reload 许可证 MIT。

## 现阶段四件事

可行性结论写在各规格的假设里，这里只保留边界：

1. 物品提示框和 Jade 瞄准名称增加第二语言行，不因此改变当前主语言。见 `specs/001-bilingual-tooltips/`。
2. JEI 搜索同时接受中文、英文、全拼和首字母。拼音不显示。见 `specs/002-jei-mixed-search/`。
3. 启动时加载中文和英文。一个全局快捷键切换整套界面的主语言，不走完整资源重载。见 `specs/003-language-hotkey/`。这里的「主语言」按菜单、按钮、标题和物品名一起切换来规划；提示框的另一行仍是剩下的那种语言。
4. 翻译文案留在 I18nUpdateMod3、Minecraft-Mod-Language-Package 或其他资源包里。这是宪法约束，不是单独的功能规格。

本阶段明确不做：主语言 / 副语言 / 双语三种显示模式、字号缩放、高分辨率收缩、单行拼接、EMI、AE2、简繁互换，以及下面两份清单里其余集成。

## 文件

- [polyglottooltip.md](polyglottooltip.md)
- [languagereload.md](languagereload.md)
