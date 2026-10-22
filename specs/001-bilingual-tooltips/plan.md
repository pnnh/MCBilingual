# Implementation Plan: 物品与 Jade 双语名称

**Branch**: `001-bilingual-tooltips` | **Date**: 2026-10-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-bilingual-tooltips/spec.md`

## Summary

在不改游戏语言的前提下，物品提示框标题下增加一行另一种语言的名称，并在安装 Jade 时用同一规则处理瞄准名称。中英文本在客户端资源加载时各建一份，缺译文就省略。本功能提供后面两个功能共用的语言对存储。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: NeoForge 客户端事件。Jade 仅 `compileOnly`，运行时可选。不依赖 PolyglotTooltip、Language Reload、I18nUpdateMod3、Minecraft-Mod-Language-Package。

**Storage**: 内存中的两份语言表。不写翻译文件，不写额外配置。

**Testing**: 纯判断逻辑用 JUnit。提示框和 Jade 用 `./gradlew runClient`，步骤见 [quickstart.md](quickstart.md)。

**Target Platform**: Minecraft 1.21.1 客户端

**Project Type**: 客户端模组

**Performance Goals**: 每次提示框只做映射查找和至多一次插入。语言表在资源加载时构建，不在渲染时读语言文件。

**Constraints**: 只处理 `zh_cn` 与 `en_us`。不自带译文。不切换主语言。Jade 缺失时仍能进世界。查看提示框不触发资源重载。

**Scale/Scope**: 原版与语言包覆盖到的物品名；Jade 的方块、流体方块和实体名。不含附魔行、菜单双语、字号和显示模式。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| I. Client Only | 通过。只注册客户端事件和客户端 Jade 插件。 |
| II. Translations Stay Outside | 通过。只读资源管理器里已有的语言文件。缺译文省略，不显示翻译键。 |
| III. Own the Behavior | 通过。行为参考 PolyglotTooltip，代码写在 `pnnh.mcbilingual`。不声明该模组依赖。 |
| IV. Two Languages Until Expanded | 通过。规格外的集成不进入本计划。 |
| V. Verify in a Client Session | 通过。纯策略有单测；可见行为由 quickstart 的客户端会话覆盖。 |

Phase 1 之后复检：仍通过。没有需要写入 Complexity Tracking 的例外。

## Project Structure

### Documentation (this feature)

```text
specs/001-bilingual-tooltips/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── tooltip-name.md
│   └── jade-name.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/
├── MCBilingual.java
├── language/
│   ├── LanguageCodes.java
│   └── LanguageTexts.java
├── tooltip/
│   ├── SecondaryNamePolicy.java
│   └── ItemTooltipNames.java
└── integration/jade/
    ├── McbilingualJadePlugin.java
    ├── JadeBlockNames.java
    └── JadeEntityNames.java

src/test/java/pnnh/mcbilingual/tooltip/
└── SecondaryNamePolicyTest.java
```

**Structure Decision**: 单模块 NeoForge 模组。语言对存储放在 `language`，因为 002 和 003 也要读它。Jade 类不由主类引用，避免未安装 Jade 时加载 Jade 类型。

## Complexity Tracking

无。
