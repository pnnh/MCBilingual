# Tasks: 三方模组兼容模式

**Input**: Design documents from `/specs/008-compat-mode-config/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: 计划要求用 JUnit 覆盖列表解析和门禁。规格没有要求先写失败测试。`CompatList` 与 `CompatGate` 的纯判断放在 `src/test/java/pnnh/mcbilingual/compat/`。这两类测试不引用 Jade、JEI、AE2、Cyclops 或 Minecraft 客户端类型。界面归属、配置界面和四种模式的画面核对留在 quickstart，完成代码之前不得把那些步骤勾成完成。

**Organization**: 按用户故事分阶段。共用的解析、配置、门禁、界面归属和失败记录在基础阶段，阻塞后面每个故事。阶段顺序按优先级：P1 是 US1、US2、US5，然后才是 P2 的 US3、US4。US5 在规格里排在最后，这里提前，是为了让初始化失败的隔离先于 Generic 和 Off。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可与同阶段其它标了 [P]、且其依赖已完成的任务并行（不同文件、没有未完成依赖）
- **[Story]**: 对应用户故事（US1 原版仍可用，US2 空列表保持专用兼容，US5 一条失败只跳过该模组，US3 Generic，US4 Off）

## Phase 1: Setup

**Purpose**: 确认这份配置不把任何第三方模组变成启动条件。

- [x] T001 核对 `src/main/templates/META-INF/neoforge.mods.toml` 的依赖只有 neoforge `[${neo_version},)` 和 minecraft `${minecraft_version_range}`。`build.gradle` 里 Jade、JEI、AE2、Integrated Terminals 保持 `compileOnly` 或 `localRuntime`。不把这四个模组写进依赖，不 jarJar

---

## Phase 2: Foundational

**Purpose**: 列表、快照、门禁、界面归属和一次性失败记录。后面每个故事都读这些类型。

**⚠️ CRITICAL**: 用户故事开始前必须完成本阶段。

- [x] T002 [P] 在 `src/main/java/pnnh/mcbilingual/compat/CompatMode.java` 定义 `FULL`、`GENERIC`、`OFF`。解析时去掉首尾空格并忽略大小写。无法识别时不抛异常。此类不导入 Minecraft、NeoForge 或第三方模组
- [x] T003 [P] 在 `src/main/java/pnnh/mcbilingual/compat/IntegrationGuard.java` 记录每个 mod id 每次启动至多一条失败。`fail(modId, throwable)` 第一次写出 mod id 和原因，之后同一 id 直接返回。日志走 `src/main/java/pnnh/mcbilingual/diagnostics/LoadReport.java`，记录器名保持 `pnnh.mcbilingual.MCBilingual`，中文在前、英文在后。`throwable` 为 null 时只打消息，不把 null 传给双参数的 `error`。不读写配置文件。此类不导入 Minecraft 客户端类，Mixin 插件可以调用
- [x] T004 [P] 在 `src/main/java/pnnh/mcbilingual/compat/UiOwner.java` 实现两层归属。绘制栈提供 `push`、`pop` 和 `beginFrame`（只清空绘制栈）。`screenOwner(Class)` 先认已知包名：`mezz.jei` → `jei`，`snownee.jade` → `jade`，`appeng` → `ae2`，`org.cyclops.integratedterminals` → `integratedterminals`。其余类用 `ModList.get().getModFiles()` 和 `IModFile.findResource` 按类文件路径查一次，结果按 `Class` 缓存。找不到或属于 `net.minecraft` 时当作原版。已知包名先于 jar 扫描，避免 jar-in-jar 漏掉这四个包。不导入 Jade、JEI、AE2、Cyclops 的类型
- [x] T005 [P] 在 `src/main/resources/assets/mcbilingual/lang/zh_cn.json` 增加 `config.mcbilingual.compat_modes`。说明列表默认为空，一行是 `modid=Full`、`modid=Generic` 或 `modid=Off`，并写出三个取值的含义。保留现有 Jade 与按键文案
- [x] T006 [P] 在 `src/main/resources/assets/mcbilingual/lang/en_us.json` 增加同一个键的英文说明。不新增第三种语言文件
- [x] T007 在 `src/main/java/pnnh/mcbilingual/compat/CompatList.java` 把字符串列表解析成不可变的 mod id → 模式快照，并带回警告。mod id 去首尾空格后整段小写，空 id 丢弃。同一 id 后写的一项生效，并产生一条重复警告。模式无法识别时该项按 `FULL`，并产生一条无效警告。查不到的 id 由调用方得到 `FULL`。不判断模组有没有安装，也不因此报错。不导入 Minecraft 或第三方类型
- [x] T008 在 `src/test/java/pnnh/mcbilingual/compat/CompatListTest.java` 覆盖空列表、`JEI` 与 `jei` 同一项、首尾空格、`jei=Sometimes` 按 Full 且有一条无效警告、两行 `jei` 以后一行为准且有一条重复警告、空 id 被丢弃、未出现的 id 为 Full。测试不启动游戏
- [x] T009 在 `src/main/java/pnnh/mcbilingual/compat/CompatConfig.java` 用 `ModConfig.Type.CLIENT` 和 `ModConfigSpec.Builder.defineListAllowEmpty` 定义默认为空的字符串列表。元素校验接受任意字符串，好让非法取值能保存。使用 `comment` 和 `translation("config.mcbilingual.compat_modes")`。监听 `net.neoforged.fml.event.config.ModConfigEvent.Loading` 与 `Reloading`，用 `CompatList` 整张替换快照，并把警告各记一条。重载回调捕获 `Exception` 与 `LinkageError`，失败时保留上一张快照。尚未加载时查找结果是 `FULL`。不使用 `defineList`
- [x] T010 在 `src/main/java/pnnh/mcbilingual/compat/CompatGate.java` 把判断写成不读 `ModList`、不读配置文件的纯方法，再加读取 `CompatConfig`、`IntegrationGuard`、`UiOwner`、`RivalMods` 的运行时包装。`dedicatedEnabled` 仅在模式为 `FULL`、该 id 未失败、且 `RivalMods` 未停用时为真。`sharedBlocked` 在 `RivalMods` 已停用，或当前 Screen 与绘制栈里任一 id 为 `OFF` 时为真。`GENERIC` 不让 `sharedBlocked` 为真。失败记录不关闭共用提示和冻结查看
- [x] T011 在 `src/test/java/pnnh/mcbilingual/compat/CompatGateTest.java` 只调用 T010 的纯方法。覆盖 Full 启用专用、Generic 与 Off 都不启用专用、未列出的 id 按 Full、失败的 Full 不再专用但仍不挡住共用路径、Generic 的界面归属不挡住共用路径、Off 的界面归属挡住共用路径、`RivalMods` 让专用为假且共用被挡住、改一个 id 不影响另一个 id
- [x] T012 在 `src/main/java/pnnh/mcbilingual/MCBilingual.java` 的构造器 try 里，于 `RivalMods.isBlocked()` 返回之前注册 T009 的客户端配置，文件名 `mcbilingual-client.toml`，并监听加载与重载。NeoForge 1.21 的构造器增加 `ModContainer` 参数来完成注册。冲突时仍在注册语言表和快捷键之前返回。配置列表不能把 `RivalMods` 的整体停用重新打开。配置注册本身失败仍走现有的 `LoadReport.modInitFailed`

**Checkpoint**: 空列表读成全部 Full。纯解析和门禁测试不依赖四个第三方模组的类。Mixin 插件还没有读取这份配置。

---

## Phase 3: User Story 1 - 只有原版时一切照常 (Priority: P1) 🎯 MVP

**Goal**: 只安装 Minecraft Java 1.21.1 和 NeoForge、配置保持空列表时，客户端能进入世界。原版物品提示、语言切换和冻结查看仍可用。

**Independent Test**: 不放入 Jade、JEI、应用能源、Integrated Terminals。空配置进入世界，看原版物品提示、按一次语言切换热键、对原版界面文字做一次冻结查看。

- [x] T013 [P] [US1] 在 `src/main/java/pnnh/mcbilingual/tooltip/ItemTooltipNames.java` 的 `onItemTooltip` 和 `onGatherComponents` 里，保留 `RivalMods.isBlocked()` 的返回，并在其后于 `CompatGate.sharedBlocked()` 为真时返回。空列表、没有 Screen、原版 `InventoryScreen` 时不得返回。不要因为某个已安装模组是 Generic 就返回
- [x] T014 [P] [US1] 在 `src/main/java/pnnh/mcbilingual/switchlang/LanguageSwitch.java` 确认 `trySwitch` 和 `refresh` 不调用 `CompatGate`、`CompatConfig` 或 `UiOwner`。语言切换不读取这份列表
- [x] T015 [P] [US1] 在 `src/main/java/pnnh/mcbilingual/freeze/FreezeCapture.java` 保持 `remember`、`rememberFrame` 和 `glyph` 对原版文字的记录。本阶段不因空列表或原版界面丢弃命中。Off 界面上的丢弃留到 US4

**Checkpoint**: 原版三项功能不依赖第三方模组，也不依赖列表里写了什么。到这里可以停下来做 MVP 核对。

---

## Phase 4: User Story 2 - 默认启用已有的专用兼容 (Priority: P1)

**Goal**: 配置为空，或某个已支持的 id 没有写入时，已安装的 Jade、JEI、应用能源和 Integrated Terminals 仍走今天的专用兼容。

**Independent Test**: 空配置下分别装上这四个模组之一，重复该模组现有的双语或搜索检查。

- [x] T016 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/integration/jei/JeiSearchAppender.java` 的 `append` 开头，`CompatGate.dedicatedEnabled("jei")` 为假时原样返回 `names`。空列表时仍执行现有的合并。不把 `append` 挪到 `JeiSearchTerms`。不改 `JeiListElementInfoMixin` 与 `JeiListElementInfoLegacyMixin` 的构造器描述符
- [x] T017 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/integration/ae2/mixin/Ae2RepoSearchMixin.java` 的 `mcbilingual$wrapNameSearch` 开头，`dedicatedEnabled("ae2")` 为假时直接 `list.add(predicate)`。为真时保持现有的名称谓词包装
- [x] T018 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalItemFilterMixin.java` 的 `mcbilingual$widenDefaultName` 开头，`dedicatedEnabled("integratedterminals")` 为假时保留原返回值。为真时仍只放宽 `SearchMode.DEFAULT`
- [x] T019 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalFluidFilterMixin.java` 做与 T018 相同的门禁。流体页仍使用现有的流体搜索词
- [x] T020 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/integration/jade/JadeSecondaryNames.java` 的 `add` 开头，`dedicatedEnabled("jade")` 为假时返回。`src/main/java/pnnh/mcbilingual/integration/jade/McbilingualJadePlugin.java` 的 `registerClient` 继续注册方块和实体组件，以便保存成 Full 后不用重启就能写入第二行

**Checkpoint**: 四个专用入口都读同一张快照。空列表下的行为与加配置之前一致。Mixin 是否应用仍不看 Full、Generic、Off。

---

## Phase 5: User Story 5 - 某一个第三方初始化失败时跳过它 (Priority: P1)

**Goal**: 某一条专用兼容在初始化或入口里失败时，客户端继续启动。日志有该 mod id 和原因。原版功能和其它未失败的 Full 兼容继续可用。已保存的配置文字不变。

**Independent Test**: 用一个会让某一条兼容初始化失败的第三方版本启动，进入世界，读最新日志，再检查原版提示和另一条未失败的兼容。配置文件里的该项仍是玩家写下的值。

- [x] T021 [P] [US5] 把 `src/main/resources/mcbilingual.mixins.json` 的 `required` 改为 `false`。`defaultRequire` 保持 1。不改 `src/main/resources/mcbilingual-language.mixins.json` 和 `src/main/resources/mcbilingual-freeze.mixins.json`，二者保持 `required: true`
- [x] T022 [P] [US5] 把 `src/main/resources/mcbilingual-ae2.mixins.json` 的 `required` 改为 `false`。`defaultRequire` 保持 1
- [x] T023 [P] [US5] 把 `src/main/resources/mcbilingual-integratedterminals.mixins.json` 的 `required` 改为 `false`。`defaultRequire` 保持 1
- [x] T024 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/jei/JeiMixinPlugin.java` 的 `onLoad` 外包一层 `Exception | LinkageError`。捕获后 `enabled` 为假，调用 `IntegrationGuard.fail("jei", exception)`，不改写配置。保留现有的 19.56 构造器选择和类文件核对。`shouldApplyMixin` 仍不读 `CompatConfig`
- [x] T025 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/ae2/Ae2MixinPlugin.java` 的 `onLoad` 捕获 `Exception | LinkageError`。在 “ae2 已加载且未被 `RivalMods` 停用” 之外，用模组文件核对 `appeng/client/gui/me/search/RepoSearch.class` 是否仍有 `getPredicates`。没有该方法时不应用，并 `IntegrationGuard.fail("ae2", reason)`。不因配置模式跳过 Mixin
- [x] T026 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/IntegratedTerminalsMixinPlugin.java` 做同样的捕获。分别核对物品与流体两个 handler 类上的 `getInstanceFilterPredicate`。缺一个方法时只跳过对应的那个 Mixin，另一个仍可应用。跳过时 `IntegrationGuard.fail("integratedterminals", reason)` 只记一次。不读配置模式
- [x] T027 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/ae2/mixin/Ae2RepoSearchMixin.java` 保留 T017 的门禁。包装过程捕获 `Exception | LinkageError` 后 `IntegrationGuard.fail("ae2", exception)`，并退回 `list.add(predicate)`。同一次启动只通过 `IntegrationGuard` 记一条
- [x] T028 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalItemFilterMixin.java` 保留 T018 的门禁。放宽过程捕获 `Exception | LinkageError` 后 `IntegrationGuard.fail("integratedterminals", exception)`，并保留原返回值
- [x] T029 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalFluidFilterMixin.java` 对流体入口做与 T028 相同的捕获和回退
- [x] T030 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/jei/JeiSearchAppender.java` 保留 T016 的门禁和现有的 `Exception | LinkageError` 捕获。捕获时改为 `IntegrationGuard.fail("jei", exception)` 并返回原来的 `names`。失败后下一次 `append` 由 `dedicatedEnabled` 直接返回，不再按物品重复记日志，也不再调用会周期性加计数的 `LoadReport.jeiSearchFailed`
- [x] T031 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/jade/McbilingualJadePlugin.java` 的 `registerClient` 外包 `Exception | LinkageError`。失败时 `IntegrationGuard.fail("jade", exception)` 并返回，不把异常抛出以中止客户端。成功时仍注册两个组件
- [x] T032 [P] [US5] 在 `src/main/java/pnnh/mcbilingual/integration/jade/JadeSecondaryNames.java` 保留 T020 的门禁。写入第二行时捕获 `Exception | LinkageError`，调用 `IntegrationGuard.fail("jade", exception)` 后返回。失败记录不修改 `config/mcbilingual-client.toml`

**Checkpoint**: 一条集成失败后，该 id 的专用兼容不再执行。原版提示仍走 US1 的路径。另一个仍为 Full 且未失败的 id 不受影响。

---

## Phase 6: User Story 3 - 关掉某一个模组的专用兼容 (Priority: P2)

**Goal**: 某一个 mod id 为 Generic 时，不再执行专为它写的兼容。原版提示保持双语。其它仍为 Full 的 id 不变。不保证该模组内部一定有或一定没有双语。

**Independent Test**: 只把 `jei` 设为 Generic。JEI 不再接受另一种语言和拼音，原版背包里的物品提示仍然双语。对其余三个 id 各做一次同样的单点检查。

- [x] T033 [P] [US3] 核对 `src/main/java/pnnh/mcbilingual/integration/jei/JeiSearchAppender.java`：模式不是 `FULL` 时 `append` 在合并搜索词之前返回。Generic 与 Off 都走 T016 这一条返回。缺了这条返回时补上，并保留 T030 的捕获
- [x] T034 [P] [US3] 核对 `src/main/java/pnnh/mcbilingual/integration/ae2/mixin/Ae2RepoSearchMixin.java`：`ae2` 为 Generic 时不包装名称谓词。保留 T027 的捕获
- [x] T035 [P] [US3] 核对 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalItemFilterMixin.java`：`integratedterminals` 为 Generic 时不放宽物品名称条件。保留 T028 的捕获
- [x] T036 [P] [US3] 核对 `src/main/java/pnnh/mcbilingual/integration/integratedterminals/mixin/TerminalFluidFilterMixin.java`：同一 id 为 Generic 时不放宽流体名称条件。保留 T029 的捕获
- [x] T037 [P] [US3] 核对 `src/main/java/pnnh/mcbilingual/integration/jade/JadeSecondaryNames.java`：`jade` 为 Generic 时 `add` 不写入第二行。组件注册仍留在 `McbilingualJadePlugin.java`。保留 T032 的捕获
- [x] T038 [P] [US3] 核对 `src/main/java/pnnh/mcbilingual/tooltip/ItemTooltipNames.java`：Generic 不调用提前返回。只有 `RivalMods` 或 `sharedBlocked()`（当前归属里有 Off）才返回。原版背包格子在旁边某个模组为 Generic 时仍添加双语第二行

**Checkpoint**: Generic 只撤专用代码。原版文本路径和语言切换仍在。另一个 id 保持 Full 时，它的专用入口仍会执行。

---

## Phase 7: User Story 4 - 在某个模组自己的界面上完全停用 (Priority: P2)

**Goal**: 某一个 mod id 为 Off 时，本模组不在那个模组自己的界面和覆盖层上做双语提示、专用搜索或冻结查看。同一画面里的原版格子和原版文字保持可用。语言切换仍是全局的。

**Independent Test**: `jei` 为 Off 时，带 JEI 列表的原版背包里，列表、搜索框和配方界面没有本模组功能，原版格子的提示仍有双语。`jade` 为 Off 时只检查 Jade 浮层。`ae2` 与 `integratedterminals` 为 Off 时终端整页没有本模组功能，回到原版背包后提示仍在。

- [x] T039 [P] [US4] 新增 `src/main/java/pnnh/mcbilingual/integration/jei/mixin/JeiOverlayOwnerMixin.java`。目标是 `mezz.jei.gui.overlay.IngredientListOverlay` 的 `drawScreen`、`drawForeground`、`drawTooltips`，`remap = false`。每个方法在 HEAD 对 `UiOwner` 压入 `jei`，在 RETURN 弹出。开发针是 JEI 19.57.0.446 里这三个方法。不按 Full、Generic、Off 决定这个 Mixin 是否应用
- [x] T040 [P] [US4] 在 `src/main/java/pnnh/mcbilingual/integration/jei/JeiMixinPlugin.java` 保留 T024 的捕获和 19.56 构造器选择。`shouldApplyMixin` 对 `JeiOverlayOwnerMixin` 在 JEI 存在且未被 `RivalMods` 停用时返回 true，不看构造器线路，也不读配置。另外两个搜索 Mixin 仍按 `JeiLineChoice`
- [x] T041 [P] [US4] 在 `src/main/resources/mcbilingual.mixins.json` 的 client 数组加入 `JeiOverlayOwnerMixin`。`required` 保持 T021 写下的 `false`
- [x] T042 [P] [US4] 新增 `src/main/java/pnnh/mcbilingual/integration/jade/mixin/JadeOverlayOwnerMixin.java`、`src/main/java/pnnh/mcbilingual/integration/jade/JadeMixinPlugin.java` 和 `src/main/resources/mcbilingual-jade.mixins.json`。在 `src/main/templates/META-INF/neoforge.mods.toml` 只增加一条 `[[mixins]]`，不增加 Jade 依赖。配置 `required` 为 `false`，包名指向 `pnnh.mcbilingual.integration.jade.mixin`。插件仅在 `jade` 已加载且未被 `RivalMods` 停用时应用，`onLoad` 捕获后 `IntegrationGuard.fail("jade", exception)`。目标类是 `snownee.jade.overlay.OverlayRenderer`。Jade 15.10.5 里 `renderOverlay478757(GuiGraphics, float)` 会调用 `renderOverlay(BoxElement, GuiGraphics)`，绘制发生在内层。两个方法都做一对 `jade` 的 push/pop。不另起一个 jar 里没有的方法名
- [x] T043 [P] [US4] 在 `src/main/java/pnnh/mcbilingual/freeze/FreezeCapture.java` 的 `beginDraw` 里，仅当绘制深度从 0 变为 1 时调用 `UiOwner.beginFrame()`。嵌套的 `beginDraw` 不清空外层已经压入的 mod id。`remember`、`rememberFrame` 和 `glyph` 在 `CompatGate.sharedBlocked()` 为真时不记录这一次命中。Generic 不丢弃。原版文字在归属不是 Off 时仍记录
- [x] T044 [P] [US4] 核对 `src/main/java/pnnh/mcbilingual/compat/UiOwner.java` 的 Screen 归属已经覆盖 `mezz.jei.gui.recipes.RecipesGui`、`appeng.client.gui` 和 `org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminal`。原版背包的 Screen 不是 `jei`。不为 AE2 或 Integrated Terminals 的整页终端再写绘制栈 Mixin。`LanguageSwitch` 继续不读归属

**Checkpoint**: Off 只盖住该模组自己画出的界面和覆盖层。JEI 列表旁边的原版格子仍有双语。保存配置并回到游戏后下一帧生效，不重启客户端。

---

## Phase 8: Polish

**Purpose**: 单测通过，并留下必须由玩家在客户端里看的核对项。

- [x] T045 运行 `./gradlew test --offline`。`CompatListTest` 与 `CompatGateTest` 通过，已有的 `JeiSearchTermsTest` 仍通过。单测通过不等于 quickstart 的画面已经看见
- [ ] T046 按 `specs/008-compat-mode-config/quickstart.md` 做客户端核对：只有原版、空列表等于 Full、Generic 与 Off、坏取值、重复项、未安装的 id、一条初始化失败、Polyglot Tooltip 或 Language Reload 仍让整个模组停用。没有进入世界并操作对应界面之前，不得把本任务勾成完成
- [x] T047 [P] 再核对 `src/main/templates/META-INF/neoforge.mods.toml`：依赖仍只有 NeoForge 和 Minecraft。T042 的 Jade Mixin 配置只是 `[[mixins]]` 条目
- [x] T048 [P] 不修改 `.specify/memory/constitution.md`，也不修改 `specs/001` 到 `specs/007` 的规格。不新增殖民地兼容。不改 `src/main/java/pnnh/mcbilingual/compat/RivalMods.java` 里的冲突模组名单

---

## Dependencies

- Phase 1 阻塞 Phase 2。Phase 2 阻塞全部用户故事。
- T007 依赖 T002。T008 依赖 T007。T009 依赖 T007。T010 依赖 T003、T004、T007、T009 的类型。T011 依赖 T010。T012 依赖 T009。
- T005 与 T006 只依赖已经写死的翻译键 `config.mcbilingual.compat_modes`，可与 T002、T003、T004 同时进行。
- US1 只依赖 Phase 2。它和 US2 改的是不同文件，基础阶段完成后可以同时进行。
- US2 只依赖 Phase 2。T016 到 T020 彼此文件不同。
- US5 的 T027 到 T030、T032 要包住 US2 已经写上的门禁，所以排在 US2 之后。T021 到 T026 和 T031 不改这些门禁，但同属这一阶段。
- US3 核对 US2 与 US5 碰过的同一批入口，排在二者之后。T038 依赖 US1 的 `sharedBlocked` 返回。
- US4 的 T041 依赖 T021 已经把 JEI Mixin 配置改成非必需。T040 依赖 T024 的 `onLoad` 捕获还在。T043 使用 T004 的 `beginFrame`。T013 的提示返回在归属变为 Off 之后自动生效，US4 不再改 `ItemTooltipNames`。
- Polish 依赖打算交付的那些故事。只做 MVP 时，T045 可以在 US1 之后先跑一轮，T046 里只核对原版一节。

## Parallel example

- T002、T003、T004、T005、T006 可同时进行。
- T008 与 T009 可在 T007 之后同时进行。
- T013、T014、T015 可同时进行。
- T016、T017、T018、T019、T020 可同时进行。
- T021 到 T032 可同时进行（US2 已完成）。
- T033 到 T038 可同时进行。
- T039 到 T044 可同时进行（US5 已把三个旧 Mixin 配置改成非必需）。
- US1 与 US2 可由两人在基础阶段之后同时进行。

## Implementation strategy

先完成列表、配置注册和门禁。MVP 停在 User Story 1：空配置、只有 Minecraft 和 NeoForge 时，原版提示、语言切换和冻结查看仍在。玩家能感知的默认行为还要加上 User Story 2，空列表才继续启用四个已经存在的专用兼容。User Story 5 让一条初始化失败停在那个 mod id 上，并满足宪法里的启动前提。Generic 与 Off 放在后面。Off 用 Screen 类加上绘制栈，不把模式写进 Mixin 插件的 `shouldApplyMixin`，否则保存配置后必须重启。

`./gradlew test --offline` 只证明解析和门禁。quickstart 里的客户端画面由玩家看，代码合并或单测通过都不表示那些步骤已经完成。

## Notes

- 四个现有 id 是 `jade`、`jei`、`ae2`、`integratedterminals`。列表里的其它 id 可以保存，没有专用入口时没有额外效果。
- 保存配置并关闭配置界面后，下一次绘制使用新快照。不要求退出世界或重启客户端。
- JEI 19.70 的 `refreshIngredientAliases` 会在后来丢掉追加的搜索词。那不是本规格的范围，不为它再写第三个搜索 Mixin。
- 开发用的 JEI 针保持 `jei_version=19.57.0.446`。构造器分界仍是 19.56.0：更早用三参数，从 19.56.0 起用四参数。
- 每个任务或每一组相关任务完成后再提交。本文件生成时不表示代码已经实现。
