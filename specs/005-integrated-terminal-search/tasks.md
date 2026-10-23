# Tasks: 集成终端的中英与拼音搜索

**Input**: Design documents from `/specs/005-integrated-terminal-search/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: 规格没有要求新的测试。包含、大小写、空查询和无中文拼音沿用 `src/test/java/pnnh/mcbilingual/search/ItemSearchIndexMatchTest.java`。不新增 JUnit。客户端观看留在 quickstart。

**Organization**: 按用户故事分阶段。开发依赖和流体词表是后面每个故事的前提。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可与同阶段其它标了 [P]、且其依赖已完成的任务并行（不同文件、没有未完成依赖）
- **[Story]**: 对应用户故事（US1 物品名称搜索，US2 组合与前缀，US3 缺模组与冲突）

## Phase 1: Setup

**Purpose**: 把 Integrated Terminals 1.10.0 和打开存储终端所需的配套放进开发运行，不进入发布包。

- [x] T001 在 `gradle.properties` 加入 `integratedterminals_version=1.21.1-1.10.0`、`cyclopscore_version=2Z0ruVBB`（NeoForge 1.30.0；版本号坐标是 Forge jar）、`integrateddynamics_version=1.21.1-1.37.1`、`commoncapabilities_version=1.21.1-2.11.6`、`integratedtunnels_version=1.21.1-1.12.0`。保持 `neo_version=21.1.251` 和 `mod_license=All Rights Reserved`
- [x] T002 在 `build.gradle` 为 `maven.modrinth:integrated-terminals` 增加 `compileOnly` 和 `localRuntime`，并为 cyclops-core、integrated-dynamics、common-capabilities、integrated-tunnels 只增加 `localRuntime`。不 jarJar，不放进发布组件，不加入 Integrated Crafting 或单独的 Integrated Terminals Compat

---

## Phase 2: Foundational

**Purpose**: 流体搜索词与物品搜索词一样，只在语言表重载时构建。

**⚠️ CRITICAL**: 流体 Mixin 开始前必须完成本阶段。

- [x] T003 在 `src/main/java/pnnh/mcbilingual/search/FluidSearchIndex.java` 按已注册流体建立搜索词。键使用 NeoForge 21.1.251 映射里 `FluidStack` 悬停名实际使用的描述键，不猜 `fluid.` 或 `block.` 前缀。值用 `ItemSearchTerms.queries`，匹配用 `ItemSearchIndex.anyContains`。没有中文名时不生成拼音。`search` 包不导入 `org.cyclops`
- [x] T004 在 `src/main/java/pnnh/mcbilingual/language/LanguageTexts.java` 的现有 try 中，于 `ItemSearchIndex.rebuild()` 之外调用 `FluidSearchIndex.rebuild()`。不在 `src/main/java/pnnh/mcbilingual/switchlang/SwitchKeyHandler.java` 增加重建

**Checkpoint**: 语言快捷键不重建两张表。流体词表不依赖集成终端的类。

---

## Phase 3: User Story 1 - 在存储终端里用中文、英文或拼音找物品 (Priority: P1) 🎯 MVP

**Goal**: 存储终端和便携存储终端的普通名称搜索接受中文、英文、全拼和首字母，当前悬停名仍然命中，拼音不显示出来。流体页使用同一规则。

**Independent Test**: 装有集成终端 1.10.0 和中文文案时，对一件两种语言都有名称的物品，用另一种语言的名称、全拼和首字母都能找到它。名称仍是当前主语言。便携终端使用同一个过滤方法。流体两种语言都有名称时，四种词同样命中。

- [x] T005 [P] [US1] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalItemFilterMixin.java` 于 `IngredientComponentTerminalStorageHandlerItemStack.getInstanceFilterPredicate` 的 RETURN 注入，`remap = false`、`cancellable = true`。`SearchMode` 不是 `DEFAULT` 时保留原返回值。是 `DEFAULT` 时改为 `original.test(stack) || ItemSearchIndex.matches(stack.getItem().getDescriptionId(), query)`。先把 lambda 赋给 `Predicate<ItemStack>`。不替换掉原悬停名条件，不在主类导入这个 Mixin
- [x] T006 [P] [US1] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalFluidFilterMixin.java` 对 `IngredientComponentTerminalStorageHandlerFluidStack.getInstanceFilterPredicate` 做同样的 DEFAULT 包装，匹配改用 `FluidSearchIndex` 和 T003 的描述键
- [x] T007 [P] [US1] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/IntegratedTerminalsMixinPlugin.java` 仿照 `Ae2MixinPlugin`：仅当加载列表含 `integratedterminals` 且 `RivalMods.isBlocked()` 为假时应用 Mixin
- [x] T008 [P] [US1] 在 `src/main/resources/mcbilingual-integratedterminals.mixins.json` 写客户端配置，包名指向 `pnnh.mcbilingual.integration.integratedterminals.mixin`，插件指向 T007，`compatibilityLevel` 为 `JAVA_21`，`defaultRequire` 为 1，`required` 为 true，客户端数组包含两个 Mixin
- [x] T009 [P] [US1] 在 `src/main/templates/META-INF/neoforge.mods.toml` 把 `integratedterminals` 标成客户端可选，范围 `[1.10.0,1.11.0)`，顺序 `NONE`，并登记 `mcbilingual-integratedterminals.mixins.json`。不声明 cyclopscore、integrateddynamics、commoncapabilities、integratedtunnels

**Checkpoint**: 物品页和流体页的普通名称搜索可在客户端里单独核对。`@`、`#`、`$` 已在 Mixin 里保持原样。

---

## Phase 4: User Story 2 - 搜索框原有的组合和前缀保持不变 (Priority: P1)

**Goal**: 空格、竖线、`@`、`#`、`$`、空搜索和非法正则保持终端原来的含义。能量页不增加搜索词。

**Independent Test**: 空格只留两段都命中的物品，竖线留任意一段命中的物品。前缀结果不因中文、英文或拼音变多。空搜索仍列出原来的物品。非法正则不崩溃，也不列出全部物品。能量页与未改之前相同。

- [x] T010 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalItemFilterMixin.java` 保持非 `DEFAULT` 的原谓词。不编译正则。查询为空白时不得在这个方法里变成全部命中。不为 `IngredientQueryMatchers` 或 `IIngredientQuery` 添加 Mixin
- [x] T011 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalFluidFilterMixin.java` 保持同样的非 `DEFAULT` 行为，并且不新增 `IngredientComponentTerminalStorageHandlerEnergy` 的 Mixin

**Checkpoint**: 组合、前缀、空搜索、非法输入和能量页不再依赖新的查询类。

---

## Phase 5: User Story 3 - 没装这个模组或遇到同类语言模组时不添乱 (Priority: P2)

**Goal**: 没装集成终端时现有功能仍在。Polyglot Tooltip 或 Language Reload 继续停用本模组，存储终端搜索也不被扩展。

**Independent Test**: 拿掉集成终端后仍能进入世界，提示、JEI 搜索和语言快捷键可用。装上两个冲突模组之一时，终端搜索不被扩展，已有的一条日志和一条聊天仍出现，语言选项不改。

- [x] T012 [P] [US3] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/IntegratedTerminalsMixinPlugin.java` 确认缺 `integratedterminals` 或 `RivalMods.isBlocked()` 时不应用 Mixin。不修改 `src/main/java/pnnh/mcbilingual/compat/RivalNotice.java`，也不往 `src/main/java/pnnh/mcbilingual/compat/RivalMods.java` 增加标识
- [x] T013 [P] [US3] 在 `src/main/java/pnnh/mcbilingual/MCBilingual.java` 确认没有 `org.cyclops` 导入，并且冲突时仍在注册语言表和快捷键之前返回

**Checkpoint**: 缺模组和冲突都不依赖新的聊天路径。

---

## Phase 6: Polish

**Purpose**: 编译和已有单元测试通过，再按 quickstart 做客户端核对。

- [x] T014 运行 `./gradlew test compileJava`。已有测试必须通过。确认 `gradle.properties` 里的 `neo_version` 和 `mod_license` 未被改掉
- [ ] T015 按 `specs/005-integrated-terminal-search/quickstart.md` 在客户端里核对物品页、流体页、能量页、便携终端、前缀、空搜索、缺模组和冲突。没有进入世界并操作搜索框之前，不得把这些步骤记为完成

---

## Dependencies

- Phase 1 阻塞所有需要编译 `SearchMode` 或启动终端的任务。
- Phase 2 阻塞 T006。T005 和 T007 只依赖 Phase 1。
- T008 依赖 T005、T006、T007 的类已经存在。T009 可与 T008 并行。
- US2 依赖 US1 的两个 Mixin 文件。
- US3 依赖 T007 的插件类。T013 只读取现有主类，可与 T012 并行。
- 空格和竖线仍由终端的 `IIngredientQuery.parse` 处理，没有单独的实现任务。

## Parallel example

- T005、T006、T007 可同时进行（T006 须等 T004 完成）。
- T008 与 T009 可同时进行。
- T010 与 T011 可同时进行。
- T012 与 T013 可同时进行。

## Implementation strategy

先完成开发依赖和流体词表，再写物品过滤 Mixin。MVP 是 User Story 1：物品页的四种搜索词、当前名称仍命中、流体页同一规则、可选 Mixin 从第一次写入就检查冲突。User Story 2 只收紧前缀、空搜索、非法输入和能量页，不重写终端的拆词。User Story 3 验收缺模组和已有停用，不新增说明。quickstart 由玩家在客户端里看，编译通过不等于这些步骤已经完成。
