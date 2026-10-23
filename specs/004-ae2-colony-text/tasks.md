# Tasks: 应用能源终端与殖民地物品的语言

**Input**: Design documents from `/specs/004-ae2-colony-text/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: 计划要求为搜索词包含和冲突标识各写一个不启动游戏的 JUnit。客户端观看留在 quickstart，不写成自动任务。

**Organization**: 按用户故事分阶段。冲突停用是后面每个故事的前提。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可与同阶段其它未完成任务并行（不同文件、没有未完成依赖）
- **[Story]**: 对应用户故事（US1 冲突停用，US2 终端搜索，US3 殖民地名称，US4 终端提示与切换）

## Phase 1: Setup

**Purpose**: 把开发期模组装进运行类路径，不进入发布包。

- [x] T001 在 `gradle.properties` 和 `build.gradle` 加入应用能源 19.2.18、GuideME 21.1.1，以及殖民地 1.1.1405 和 Curse 文件声明的前置，使用 `localRuntime` 或仅用于编译的 `compileOnly`。I18nUpdateMod 3.7.0 被 NeoForge 21.1.251 跳过，改为下载简体中文资源包到 `run/resourcepacks`

---

## Phase 2: Foundational

**Purpose**: 冲突判断和搜索词匹配，后续故事都依赖它们。

- [x] T002 在 `src/main/java/pnnh/mcbilingual/compat/RivalMods.java` 判断 `polyglottooltip` 与 `languagereload`，并生成中英说明
- [x] T003 [P] 在 `src/test/java/pnnh/mcbilingual/compat/RivalModsTest.java` 覆盖冲突标识、文案模组不冲突、空列表
- [x] T004 [P] 在 `src/main/java/pnnh/mcbilingual/search/ItemSearchIndex.java` 增加一段查询是否被已有搜索词包含的判断
- [x] T005 [P] 在 `src/test/java/pnnh/mcbilingual/search/ItemSearchIndexMatchTest.java` 覆盖包含、大小写、空查询和无中文词

**Checkpoint**: 纯规则可测，且不读取加载中的模组列表。

---

## Phase 3: User Story 1 - 遇到同类语言模组时停用自己 (Priority: P1)

**Goal**: 冲突时不注入、不改提示和语言，日志和聊天各留一条说明。

**Independent Test**: 加载列表含 `polyglottooltip` 或 `languagereload` 时，Mixin 不应用；进入世界后聊天一条、日志一条；不含这两个标识时行为与现在相同。

- [x] T006 [US1] 在 `src/main/java/pnnh/mcbilingual/switchlang/LanguageMixinPlugin.java` 和 `src/main/resources/mcbilingual-language.mixins.json` 于冲突时跳过语言 Mixin
- [x] T007 [US1] 在 `src/main/java/pnnh/mcbilingual/integration/jei/JeiMixinPlugin.java` 于冲突时跳过 JEI Mixin
- [x] T008 [US1] 在 `src/main/java/pnnh/mcbilingual/MCBilingual.java`、`src/main/java/pnnh/mcbilingual/tooltip/ItemTooltipNames.java`、`src/main/java/pnnh/mcbilingual/switchlang/SwitchKeyHandler.java` 和 `src/main/java/pnnh/mcbilingual/integration/jade/McbilingualJadePlugin.java` 于冲突时不注册、不修改
- [x] T009 [US1] 在 `src/main/java/pnnh/mcbilingual/compat/RivalNotice.java` 于进入世界时把说明写入聊天一次

---

## Phase 4: User Story 2 - 应用能源终端搜索 (Priority: P1)

**Goal**: 物品终端的普通搜索词同时接受中文、英文、全拼和首字母。

**Independent Test**: 没有冲突且安装了应用能源时，物品键的搜索词包含查询则命中；流体和带前缀的条件仍走终端原来的判断。

- [x] T010 [US2] 在 `src/main/java/pnnh/mcbilingual/integration/ae2/Ae2ItemQuery.java` 只对物品键使用 `ItemSearchIndex`
- [x] T011 [US2] 在 `src/main/java/pnnh/mcbilingual/integration/ae2/mixin/Ae2NameSearchAccessor.java` 和 `src/main/java/pnnh/mcbilingual/integration/ae2/mixin/Ae2RepoSearchMixin.java` 保留原名称条件并加上物品搜索词
- [x] T012 [US2] 在 `src/main/java/pnnh/mcbilingual/integration/ae2/Ae2MixinPlugin.java`、`src/main/resources/mcbilingual-ae2.mixins.json` 和 `src/main/templates/META-INF/neoforge.mods.toml` 把应用能源标成可选，并在缺失或冲突时不应用 Mixin

---

## Phase 5: User Story 3 - 殖民地物品与工作台 (Priority: P1)

**Goal**: 殖民地物品沿用现有提示和语言切换，不把殖民地编译进本模组。

**Independent Test**: `mods.toml` 不声明殖民地依赖；开发运行类路径含 1.1.1405 及其前置。物品提示和快捷键不新增殖民地类型。

- [x] T013 [US3] 确认 `build.gradle` 的殖民地依赖只有 `localRuntime`，且 `src/main/templates/META-INF/neoforge.mods.toml` 不声明 `minecolonies`

---

## Phase 6: User Story 4 - 终端提示与切换后的名称 (Priority: P2)

**Goal**: 终端格子提示在另一种语言有文案时显示第二行。切换仍不重载资源。

**Independent Test**: 提示收集事件在第二行缺失时补上，已存在时不重复。冲突时不补。

- [x] T014 [US4] 在 `src/main/java/pnnh/mcbilingual/tooltip/ItemTooltipNames.java` 的提示收集里补上尚未存在的第二行

---

## Phase 7: Polish

**Purpose**: 编译和单元测试通过。

- [x] T015 运行 `./gradlew test compileJava --offline`；若开发依赖尚未缓存，去掉 `--offline` 再运行一次

---

## Dependencies

- Phase 1 不阻塞单元测试，但阻塞带应用能源和殖民地的客户端。
- Phase 2 阻塞 US1、US2、US4。
- US1 阻塞「冲突时 US2 和 US4 不得生效」的验收。
- US3 只依赖 Phase 1 的运行时 jar，不依赖应用能源 Mixin。
- US4 依赖现有提示收集，不依赖 US2 的搜索 Mixin。

## Parallel example

- T003、T004、T005 可同时进行。
- T006 与 T007 可同时进行，之后再改共同入口 T008。

## Implementation strategy

先完成冲突停用和两个纯规则测试，再接应用能源搜索。殖民地不新增类型。终端第二行补在已有提示收集里。quickstart 的创造模式观看由玩家在客户端里做。
