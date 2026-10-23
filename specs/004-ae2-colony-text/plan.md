# Implementation Plan: 应用能源终端与殖民地物品的语言

**Branch**: `004-ae2-colony-text` | **Date**: 2026-10-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-ae2-colony-text/spec.md`

## Summary

开发用的客户端额外启动应用能源 19.2.18、殖民地 1.1.1405，以及它们缺了就进不了世界的配套，再用外部汉化提供中文。这些 jar 只在开发运行时出现，不打进发布包，许可证保持 All Rights Reserved。配套的元数据必须接受 Minecraft 1.21.1 和 NeoForge 21.1.251。

同时安装 Polyglot Tooltip 或 Language Reload 时，本模组不应用自己的 Mixin，也不注册提示、搜索和语言切换。启动时写日志，进入世界时在聊天写一次。

应用能源物品终端的名称搜索只看当前显示名。没有冲突时，客户端把这个名称条件扩展成已有的中文、英文、全拼和首字母。终端格子的提示走物品堆的提示事件，缺第二行时由现有的提示收集补上。殖民地物品提示和注视名称沿用现有第二行；工作台文字沿用现有的主语言切换。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: 已有的 `LanguageTexts`、`ItemSearchIndex`、`SecondaryNamePolicy`、语言快捷键。开发运行时另外放入应用能源、殖民地及其启动配套，并下载简体中文资源包到 `run/resourcepacks`。这些都不是发布依赖。应用能源只在编译可选集成时出现在编译类路径。I18nUpdateMod 3.7.0 在 NeoForge 21.1.251 上不会被加载。

**Storage**: 不新增配置，不把汉化文件写入仓库。搜索词仍在语言表重载时构建。

**Testing**: 名称词是否包含一次查询的纯规则用 JUnit。终端、提示框和工作台用 `./gradlew runClient`，见 [quickstart.md](quickstart.md)。

**Target Platform**: Minecraft 1.21.1 客户端。集成服务器会带上这些开发模组，专用服务器不需要本模组。

**Project Type**: 客户端模组

**Performance Goals**: 终端里一次按键搜索仍由应用能源自己过滤列表。语言切换后一秒内更新会跟着当前语言重绘的名称；记住旧句子的窗口关闭再打开后正确。切换不重载资源包，也不重建搜索词。

**Constraints**: 只处理物品。不改样板访问、流体和无线终端。工作台窗口不加整页第二行。拼音不出现在任何可见名称上。缺中文、缺应用能源或殖民地时仍能进世界。

**Scale/Scope**: 一个物品终端搜索框（合成终端共用该框时同样生效），殖民地特有物品的提示和注视名称，以及一个能打开的工作台界面。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| I. Client Only | 通过。搜索和名称都在客户端。不改协议，不要求专用服务器安装本模组。 |
| II. Translations Stay Outside | 通过。中文来自各模组自带语言文件和未提交的资源包。不复制 I18nUpdateMod3 源码，也不把它放进编译或发布类路径。许可证不改。 |
| III. Own the Behavior | 通过。终端搜索按 19.2.18 的公开类重写，不搬入 PolyglotTooltip 或 Language Reload 的源码。二者同时安装时本模组停用自己的行为。 |
| IV. Two Languages Until Expanded | 通过。仍是 `zh_cn` 与 `en_us`。本规格明确纳入应用能源物品终端和殖民地的指定界面，没有把整个兼容目录放进来。 |
| V. Verify in a Client Session | 通过。匹配规则有单测。终端搜索、第二行和工作台切换由 quickstart 在客户端里看。 |

Phase 1 之后复检：仍通过。应用能源用可选客户端集成；殖民地不进入本模组的编译或发布依赖。

## Project Structure

### Documentation (this feature)

```text
specs/004-ae2-colony-text/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── ae2-terminal-search.md
│   ├── colony-names.md
│   └── rival-standdown.md
└── tasks.md
```

`tasks.md` 只在要求实现时生成。

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/
├── compat/
│   └── RivalMods.java
├── search/
│   └── ItemSearchIndex.java
└── integration/ae2/
    ├── Ae2ItemQuery.java
    ├── Ae2MixinPlugin.java
    └── mixin/
        ├── Ae2NameSearchAccessor.java
        └── Ae2RepoSearchMixin.java

src/main/resources/
└── mcbilingual-ae2.mixins.json

src/test/java/pnnh/mcbilingual/
├── compat/RivalModsTest.java
└── search/ItemSearchIndexMatchTest.java
```

**Structure Decision**: 冲突判断放在 `compat`，Mixin 插件和事件入口都先问它。应用能源类只出现在 `integration/ae2`。主类不引用 `appeng`。三份 Mixin 配置在冲突时全部跳过。殖民地不新增包。开发模组写在 `localRuntime`，不进发布组件。

## Complexity Tracking

无。
