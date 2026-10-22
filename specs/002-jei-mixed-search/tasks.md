# Tasks: JEI 中英与拼音搜索

**Input**: Design documents from `/specs/002-jei-mixed-search/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/jei-query.md, quickstart.md

**Tests**: 计划要求 `PinyinTokens` 用 JUnit。玩家可见的 JEI 命中用 `runClient` 抽查。

**Organization**: 按用户故事分组。拼音和搜索词是 JEI 接入的前置。

## Format: `[ID] [P?] [Story] Description`

## Phase 1: Setup

**Purpose**: 可选的 JEI 编译依赖，以及随模组嵌入的 TinyPinyin

- [x] T001 Add the BlameJared repository, `jei_version`, and JEI `compileOnly` / `localRuntime` in `build.gradle` and `gradle.properties`. Mark JEI optional in `src/main/templates/META-INF/neoforge.mods.toml`
- [x] T002 Add TinyPinyin 2.0.3 as a Jar-in-Jar dependency in `build.gradle`, and keep its Apache-2.0 text in `src/main/resources/META-INF/licenses/tinypinyin-LICENSE.txt` and `src/main/resources/META-INF/licenses/tinypinyin-NOTICE.txt`

---

## Phase 2: Foundational

**Purpose**: 与游戏和 JEI 无关的拼音、搜索词，以及资源重载时的物品索引。完成前不写 mixin。

- [x] T003 Write pinyin and search-term tests in `src/test/java/pnnh/mcbilingual/search/PinyinTokensTest.java` and `src/test/java/pnnh/mcbilingual/search/ItemSearchTermsTest.java`
- [x] T004 Implement per-character readings in `src/main/java/pnnh/mcbilingual/search/PinyinTokens.java`
- [x] T005 Implement Chinese, English, full pinyin, and initial queries in `src/main/java/pnnh/mcbilingual/search/ItemSearchTerms.java`
- [x] T006 Rebuild the description-id index on client resource reload in `src/main/java/pnnh/mcbilingual/search/ItemSearchIndex.java` and `src/main/java/pnnh/mcbilingual/language/LanguageTexts.java`

**Checkpoint**: 单测通过。索引只在语言表 `apply` 时重建，不读取当前主语言。

---

## Phase 3: User Story 1 - 用另一种语言的名称搜索 (Priority: P1)

**Goal**: JEI 物品搜索能命中中文名和英文名，与当前主语言无关。

**Independent Test**: 装上 JEI。中文主语言下用英文名能找到原版物品，英文主语言下用中文名也能找到。

- [x] T007 [US1] Append indexed terms to JEI's search name list in `src/main/java/pnnh/mcbilingual/integration/jei/mixin/JeiListElementInfoMixin.java`
- [x] T008 [US1] Apply that mixin only when JEI is loaded, via `src/main/java/pnnh/mcbilingual/integration/jei/JeiMixinPlugin.java`, `src/main/resources/mcbilingual.mixins.json`, and `src/main/templates/META-INF/neoforge.mods.toml`
- [x] T009 [US1] Register the JEI plugin marker in `src/main/java/pnnh/mcbilingual/integration/jei/JeiSearchPlugin.java`

**Checkpoint**: 搜索词进入 `ListElementInfo` 的名称列表。不调用会在提示框里显示别名的 JEI alias API。

---

## Phase 4: User Story 2 - 用全拼或首字母搜索 (Priority: P2)

**Goal**: 全拼和首字母能命中物品，并且不出现在提示框、Jade 或 JEI 展示名上。

**Independent Test**: `苹果` 的词包含 `pingguo` 和 `pg`。提示框与 Jade 代码不读取拼音。

- [x] T010 [US2] Keep pinyin inside the search term list from `src/main/java/pnnh/mcbilingual/search/ItemSearchTerms.java`, and do not read it from `src/main/java/pnnh/mcbilingual/tooltip/` or `src/main/java/pnnh/mcbilingual/integration/jade/`

**Checkpoint**: 拼音只存在于搜索词。展示名称仍走 001 的名称逻辑。

---

## Phase 5: User Story 3 - 没装 JEI 或没有中文时不妨碍游戏 (Priority: P3)

**Goal**: 没有 JEI 时客户端仍能进入。没有中文名时不产生拼音，英文名搜索保持可用。

**Independent Test**: 主类不引用 JEI。缺中文的搜索词测试不包含拼音。

- [x] T011 [US3] Leave `src/main/java/pnnh/mcbilingual/MCBilingual.java` free of JEI types, and omit pinyin when the Chinese name is missing in `src/main/java/pnnh/mcbilingual/search/ItemSearchTerms.java`

**Checkpoint**: 未加载 `jei` 时 mixin 不应用。缺中文不会造出空拼音词。

---

## Phase 6: Polish

**Purpose**: 编译和单测

- [x] T012 Run `./gradlew test` and `./gradlew compileJava`

---

## Dependencies & Execution Order

- Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6
- US1 依赖索引。US2 依赖 US1 把同一份词送进 JEI。US3 依赖 mixin 插件，不依赖拼音展示。
- 003 语言快捷键不在本功能内。索引不读取当前主语言，因此之后的切换不必重建拼音。

## Parallel Example: User Story 1

```text
T007 mixin 与 T009 插件可以并行编写，T008 把 mixin 登记进配置。
```

## Implementation Strategy

MVP 是 US1：另一种语言的名称能搜到。US2 把拼音放进同一份词。US3 保证 JEI 和中文都可缺席。

JEI 版本以本仓库能解析的 1.21.1 NeoForge 线 `19.64.0.455` 为准。默认后缀树和低内存搜索都读取 `ListElementInfo.getNames()`，所以只追加这个列表。别名 API 默认把别名画进 JEI 提示框，不能用来放拼音。
