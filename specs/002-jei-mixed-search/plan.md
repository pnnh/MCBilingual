# Implementation Plan: JEI 中英与拼音搜索

**Branch**: `002-jei-mixed-search` | **Date**: 2026-10-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-jei-mixed-search/spec.md`

## Summary

JEI 搜索词同时包含物品的简体中文名、英文名、无声调连续全拼和首字母。拼音由 Apache-2.0 的 TinyPinyin 在资源加载时生成并缓存。打字只查询已经建好的索引。未安装 JEI 时不加载这条集成。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: `001-bilingual-tooltips` 的 `LanguageTexts`。JEI 1.21.1 NeoForge API 为 `compileOnly` 与开发期 `localRuntime`。拼音库 `com.github.promeg:tinypinyin:2.0.3`，随模组嵌入。

**Storage**: 内存索引，键为物品，值为中文名、英文名、全拼、首字母。资源重载时重建。不写入磁盘。

**Testing**: `PinyinTokens` 用 JUnit，不启动游戏。JEI 命中用 `./gradlew runClient`，见 [quickstart.md](quickstart.md)。

**Target Platform**: Minecraft 1.21.1 客户端

**Project Type**: 客户端模组

**Performance Goals**: 全表拼音在资源加载期间完成。JEI 打字不重新转换汉字，也不触发资源重载。

**Constraints**: 拼音不进入提示框和 Jade。多音字取第一读音。不依赖 Just Enough Characters、PolyglotTooltip 或 GPL 的 pinyin4j。JEI 可选。

**Scale/Scope**: JEI 物品名搜索。不含 EMI、AE2、创造栏、配方书和简繁转换。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| I. Client Only | 通过。索引和 JEI mixin 都在客户端。 |
| II. Translations Stay Outside | 通过。中文名从 `LanguageTexts` 读取，不新增语言文件。 |
| III. Own the Behavior | 通过。不依赖拼音模组。TinyPinyin 是计划允许的宽松许可证库，并保留其许可证声明。 |
| IV. Two Languages Until Expanded | 通过。搜索词只来自 `zh_cn` 和 `en_us`。 |
| V. Verify in a Client Session | 通过。拼音有单测；JEI 可见结果由 quickstart 覆盖。 |

Phase 1 之后复检：仍通过。嵌入 TinyPinyin 是宪法 Additional Constraints 里点名允许的库，不是原则例外。

## Project Structure

### Documentation (this feature)

```text
specs/002-jei-mixed-search/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── jei-query.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/
├── search/
│   ├── PinyinTokens.java
│   └── ItemSearchTerms.java
└── integration/jei/
    ├── JeiSearchPlugin.java
    └── mixin classes that append terms

src/main/resources/
└── mcbilingual.mixins.json   # JEI mixin 仅在加载了 jei 时应用

src/test/java/pnnh/mcbilingual/search/
└── PinyinTokensTest.java
```

**Structure Decision**: 拼音转换不引用 Minecraft 类型，方便单测。JEI 相关类和 mixin 放在 `integration.jei`，由 mixin 插件按模组是否加载决定是否应用。

## Complexity Tracking

无。
