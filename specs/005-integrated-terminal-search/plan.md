# Implementation Plan: 集成终端的中英与拼音搜索

**Branch**: `005-integrated-terminal-search` | **Date**: 2026-10-23 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/005-integrated-terminal-search/spec.md`

## Summary

开发用的客户端额外启动 Integrated Terminals 1.10.0，以及打开存储终端所需要的 Cyclops Core、Integrated Dynamics、Common Capabilities。箱子里的物品若要出现在终端中，同一会话再带上 Integrated Tunnels。这些 jar 只在开发运行时出现，不打进发布包。本模组的 NeoForge 仍是 21.1.251，许可证保持 All Rights Reserved。中文仍来自各模组自带语言文件和已有的外部资源包。

存储终端物品页和流体页的普通名称条件，在终端自己的当前名称匹配之外，再接受已有的中文、英文、全拼和首字母。`@`、`#`、`$`、能量页、空搜索，以及空格和竖线的组合方式保持终端原样。便携存储终端使用同一个条件。没装这个模组时 Mixin 不应用。Polyglot Tooltip 或 Language Reload 仍让本模组整体停用。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: 已有的 `LanguageTexts`、`ItemSearchIndex`、`ItemSearchTerms`。开发运行时另放入下面五份 Modrinth 发行版。只有 Integrated Terminals 进入编译类路径，供客户端 Mixin 引用 `SearchMode`。其余四份只进 `localRuntime`。都不发布，也不 jarJar。

| 坐标 | 用途 |
| --- | --- |
| `maven.modrinth:integrated-terminals:1.21.1-1.10.0` | 存储终端。`compileOnly` 与 `localRuntime` |
| `maven.modrinth:cyclops-core:2Z0ruVBB` | 终端声明的前置，NeoForge 1.30.0。版本号坐标会解析成 Forge jar。仅 `localRuntime` |
| `maven.modrinth:integrated-dynamics:1.21.1-1.37.1` | 终端声明的前置。仅 `localRuntime` |
| `maven.modrinth:common-capabilities:1.21.1-2.11.6` | 终端声明的前置。仅 `localRuntime` |
| `maven.modrinth:integrated-tunnels:1.21.1-1.12.0` | 让箱子里的物品能进入终端网络。仅 `localRuntime` |

**Storage**: 不新增配置，不把汉化文件写入仓库。物品搜索词仍在语言表重载时构建。流体搜索词用另一张表，同一时刻构建。语言快捷键不构建这两张表。

**Testing**: 「一段查询被某条搜索词包含」仍用现有 JUnit，流体复用同一判断。终端里的中文、英文、拼音、空格、竖线、前缀、空搜索和流体页用 `./gradlew runClient`，见 [quickstart.md](quickstart.md)。

**Target Platform**: Minecraft 1.21.1 客户端。集成服务器会带上这些开发模组。专用服务器不需要本模组。

**Project Type**: 客户端模组

**Performance Goals**: 一次按键仍由终端自己重绘列表。本模组只替换普通名称条件，不在匹配时重建搜索词。语言切换后，搜索框里已有的词仍然有效，且不打开资源重载画面。

**Constraints**: 只放宽 `SearchMode.DEFAULT`。不改 `@`、`#`、`$`，不改能量页，不改合成任务列表。拼音不出现在名称、提示框或搜索框。缺中文、缺集成终端时仍能进世界。`neo_version` 保持 21.1.251。可选依赖范围只写 `[1.10.0,1.11.0)`，不声称支持 1.11。

**Scale/Scope**: 一个存储终端搜索框，包括使用同一过滤方法的便携存储终端。物品页和流体页的普通名称。能量页和合成任务列表不在范围内。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| I. Client Only | 通过。过滤方法带有客户端标记，Mixin 写在客户端数组。不改协议，不要求专用服务器安装本模组。 |
| II. Translations Stay Outside | 通过。不新增语言文件。中文来自模组 jar 或未提交的资源包。集成终端不进入发布组件。许可证不改。 |
| III. Own the Behavior | 通过。不搬入 Cyclops 的查询类，只在公开的过滤方法返回后保留原条件并加上自己的名称判断。Polyglot Tooltip 与 Language Reload 仍让全部 Mixin 停用。 |
| IV. Two Languages Until Expanded | 通过。仍是 `zh_cn` 与 `en_us`。本规格只纳入存储终端搜索框，不把集成动力的其它界面放进来。 |
| V. Verify in a Client Session | 通过。包含判断沿用已有单测。搜索框、前缀、空查询和流体页写在 quickstart 里，编译通过不算这些步骤已经看见。 |

Phase 1 之后复检：仍通过。集成终端是可选客户端集成。另外四份配套不进入编译或发布依赖。没有需要写入 Complexity Tracking 的例外。

## Project Structure

### Documentation (this feature)

```text
specs/005-integrated-terminal-search/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── integrated-terminal-search.md
└── tasks.md
```

`tasks.md` 只在要求实现时生成。

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/
├── language/
│   └── LanguageTexts.java
├── search/
│   ├── ItemSearchIndex.java
│   ├── ItemSearchTerms.java
│   └── FluidSearchIndex.java
├── gametest/mixin/
│   └── GameTestHooksMixin.java
└── integration/integratedterminals/
    ├── IntegratedTerminalsMixinPlugin.java
    └── mixin/
        ├── TerminalItemFilterMixin.java
        └── TerminalFluidFilterMixin.java

src/main/resources/
├── mcbilingual-gametest.mixins.json
└── mcbilingual-integratedterminals.mixins.json

src/main/templates/META-INF/neoforge.mods.toml
gradle.properties
build.gradle
```

**Structure Decision**: 流体词表放在 `search`，不引用 Cyclops 类型，构建入口仍是 `LanguageTexts.apply`。终端类只出现在 `integration/integratedterminals`。插件在模组列表里有 `integratedterminals` 且 `RivalMods` 未停用时才应用 Mixin。主类不引用 `org.cyclops`。`mods.toml` 把 `integratedterminals` 标成客户端可选，范围 `[1.10.0,1.11.0)`。五份开发 jar 的版本写在 `gradle.properties`。

## Complexity Tracking

无。
