# Tasks: 中英主语言快捷切换

**Input**: Design documents from `/specs/003-language-hotkey/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/language-switch.md, quickstart.md

**Tests**: 计划要求 `LanguageCycle` 用 JUnit。快捷键和界面刷新用 `runClient` 抽查。

**Organization**: 按用户故事分组。语言循环和缓存表是按键的前置。

## Format: `[ID] [P?] [Story] Description`

## Phase 1: Setup

**Purpose**: 语言切换用的客户端访问器，与 JEI mixin 分开

- [x] T001 Add `src/main/resources/mcbilingual-language.mixins.json` and register it beside the JEI config in `src/main/templates/META-INF/neoforge.mods.toml`

---

## Phase 2: Foundational

**Purpose**: 纯语言循环，以及从 001 取出已经加载的文案。完成前不注册按键。

- [x] T002 [P] Write the cycle tests in `src/test/java/pnnh/mcbilingual/switchlang/LanguageCycleTest.java`
- [x] T003 Implement `zh_cn` / `en_us` cycling in `src/main/java/pnnh/mcbilingual/switchlang/LanguageCycle.java`
- [x] T004 Expose the cached table and the empty-table check in `src/main/java/pnnh/mcbilingual/language/LanguageTexts.java`

**Checkpoint**: 单测能判断下一次语言。空表或加载失败不算可用文案。

---

## Phase 3: User Story 1 - 一次按键换掉眼前的主语言 (Priority: P1)

**Goal**: 默认 F3+J 在中英之间切换当前界面文字，不出现资源重载画面，也不回到标题画面。

**Independent Test**: 中英语言包都已加载时，世界中按一次默认快捷键，再打开背包。物品名和界面文字已经是另一种语言。再按一次后恢复。

- [x] T005 [P] [US1] Add 21.1.251 accessors for installing a language and clearing cached text in `src/main/java/pnnh/mcbilingual/switchlang/mixin/`
- [x] T006 [US1] Install the cached `ClientLanguage`, refresh the open vanilla surfaces, and refuse an empty target in `src/main/java/pnnh/mcbilingual/switchlang/LanguageSwitch.java`
- [x] T007 [US1] Register an unbound key and accept F3+J only while it stays unbound in `src/main/java/pnnh/mcbilingual/switchlang/SwitchKeyHandler.java` and `src/main/java/pnnh/mcbilingual/MCBilingual.java`
- [x] T008 [P] [US1] Add the key name strings in `src/main/resources/assets/mcbilingual/lang/en_us.json` and `src/main/resources/assets/mcbilingual/lang/zh_cn.json`

**Checkpoint**: 切换不调用 `onResourceManagerReload` 或 `reloadResourcePacks`。聊天界面打开时按键被丢掉。

---

## Phase 4: User Story 2 - 启动时两套文案都已就绪 (Priority: P2)

**Goal**: 第一次按键使用 001 已经加载的两套表。搜索索引继续同时包含两种语言。

**Independent Test**: 冷启动后、按键前，语言表日志里两种苹果名都在。按键不重建 `ItemSearchIndex`。

- [x] T009 [US2] Leave `ItemSearchIndex.rebuild` only in `src/main/java/pnnh/mcbilingual/language/LanguageTexts.java`, and do not call it from `src/main/java/pnnh/mcbilingual/switchlang/LanguageSwitch.java`

**Checkpoint**: 切换不丢任何一种语言的搜索词。

---

## Phase 5: User Story 3 - 选择被记住，且只有这两种语言 (Priority: P3)

**Goal**: 成功切换写入原版语言选项。循环里不出现第三种语言。

**Independent Test**: `LanguageCycle` 连续十次只有 `zh_cn` 和 `en_us`。成功切换后 `options.languageCode` 被保存。

- [x] T010 [US3] Save `options.languageCode` from `src/main/java/pnnh/mcbilingual/switchlang/LanguageSwitch.java` after a successful install

**Checkpoint**: 按键前不改当前语言。第一次从其它语言进入 `zh_cn`。

---

## Phase 6: Polish

**Purpose**: 编译、单测，以及客户端能启动

- [x] T011 Run `./gradlew test` and `./gradlew compileJava`
- [x] T012 Boot `./gradlew runClient` far enough to log both language tables, without a resource-pack wipe

---

## Dependencies & Execution Order

- T001 先于 T005。T002 与 T003 可先后紧挨着做。T004 先于 T006。
- T006 依赖 T003、T004、T005。T007 依赖 T006。T008 可与 T007 并行。
- T009 与 T010 都落在 T006 的同一条切换路径上，放在按键注册之后核对。
- T011、T012 最后做。

## Parallel Example: User Story 1

```text
T005 accessors
T008 key name strings
```

## Implementation Strategy

先完成语言循环单测和缓存安装，再接按键。背包和暂停菜单的文字随当前语言对象更新；书、进度、告示牌、文本展示和聊天清掉已排版的缓存。不重载资源包。

## Notes

- 未在客户端里按下 F3+J 之前，quickstart 的按键步骤保持未验证。
