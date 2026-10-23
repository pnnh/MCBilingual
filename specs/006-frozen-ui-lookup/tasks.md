# Tasks: 定格界面并查看另一种语言的整句

**Input**: Design documents from `/specs/006-frozen-ui-lookup/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/frozen-ui-lookup.md, quickstart.md

**Tests**: 计划要求用 JUnit 锁住纯判断，测试不得启动客户端，也不得导入 `net.minecraft`。规格里的界面步骤不写成自动化测试，留在 quickstart。

**Organization**: 按用户故事分阶段。句子判断、快捷键和 Mixin 插件是后面每个故事的前提。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可与同阶段其它标了 [P]、且其依赖已完成的任务并行（不同文件、没有未完成依赖）
- **[Story]**: 对应用户故事（US1 定格后读出整句对照语言，US2 定格期间输入不进游戏，US3 只提示仍带译文的游戏句子）

## Phase 1: Setup

**Purpose**: 给本模组自己的快捷键起名，并登记单独的 Mixin 配置。不新增依赖。

- [x] T001 [P] 在 `src/main/resources/assets/mcbilingual/lang/en_us.json` 增加 `"key.mcbilingual.freeze_ui": "Freeze interface and look up the other language"`。保留已有的 `key.mcbilingual.switch_language` 和类别名
- [x] T002 [P] 在 `src/main/resources/assets/mcbilingual/lang/zh_cn.json` 增加 `"key.mcbilingual.freeze_ui": "定格界面并查看另一种语言"`。保留已有条目
- [x] T003 [P] 在 `src/main/templates/META-INF/neoforge.mods.toml` 登记 `${mod_id}-freeze.mixins.json`。不新增模组依赖，不改 `neo_version` 和 `mod_license`，不把定格 Mixin 写进语言、JEI、AE2、集成终端或 GameTest 的配置

---

## Phase 2: Foundational

**Purpose**: 纯判断、定格状态和快捷键先就位。界面绘制和输入拦截都依赖它们。

**⚠️ CRITICAL**: 用户故事开始前必须完成本阶段。

- [x] T004 按 `specs/006-frozen-ui-lookup/data-model.md` 添加 `src/main/java/pnnh/mcbilingual/freeze/UiSentence.java`、`src/main/java/pnnh/mcbilingual/freeze/TextHit.java` 和 `src/main/java/pnnh/mcbilingual/freeze/FreezeSession.java`。`UiSentence` 保存对照整句和当前主语言全文。`TextHit` 用整数矩形、所属句子和绘制序号。`FreezeSession` 同时最多一个，保存是否定格、内存画面、已定格的命中列表，以及进入时是否放开了鼠标。这三个类型不导入 `net.minecraft`
- [x] T005 在 `src/main/java/pnnh/mcbilingual/freeze/SentenceResolver.java` 实现不启动游戏的整句判断。输入是当前语言代码、按键取模板的函数、一句只含翻译键或字面量的树，以及当前主语言全文。`en_us` 取 `zh_cn` 模板，`zh_cn` 取 `en_us` 模板。其它语言、模板缺失、模板等于翻译键、对照句与全文相同、或整棵树没有翻译键时返回空。字面量参数原样填入。只展开 `%s`、按序号的 `%s` 和 `%%`。嵌套翻译键用同一种对照语言展开。不读取成品字符串，不调用 `Language.inject` 或 `TranslatableContents.decompose`
- [x] T006 [P] 在 `src/test/java/pnnh/mcbilingual/freeze/SentenceResolverTest.java` 覆盖：英文主语言得到「苹果」、中文主语言得到 “Apple”、第三种语言为空、缺失或等于键为空、全文已等于对照句为空、`你好，%s` 加字面量 Steve 得到「你好，Steve」、序号占位和 `%%`、纯字面量为空。成品 “Hello, Steve” 不得当作键去查。测试不导入 `net.minecraft`
- [x] T007 [P] 在 `src/test/java/pnnh/mcbilingual/freeze/TextHitTest.java` 覆盖重叠时序号更大者命中、点在矩形外为空、宽或高为 0 的矩形不保留
- [x] T008 [P] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/FreezeMixinPlugin.java` 仿照 `LanguageMixinPlugin`：`onLoad` 时 `apply = !RivalMods.isBlocked()`，`shouldApplyMixin` 返回该标志。不修改 `src/main/java/pnnh/mcbilingual/compat/RivalMods.java`
- [x] T009 在 `src/main/resources/mcbilingual-freeze.mixins.json` 写客户端配置。包名 `pnnh.mcbilingual.freeze.mixin`，插件指向 T008，`compatibilityLevel` 为 `JAVA_21`，`required` 为 true，`defaultRequire` 为 1，`client` 先为空数组。后面的 Mixin 任务只往这个数组追加
- [x] T010 在 `src/main/java/pnnh/mcbilingual/freeze/FreezeKeyHandler.java` 注册 `key.mcbilingual.freeze_ui`，类别用 `SwitchKeyHandler.CATEGORY`，默认 `InputConstants.UNKNOWN`，`KeyConflictContext.UNIVERSAL`。未绑定时只认按住 F3 再按 K，并用已有的 `KeyboardHandlerAccessor.mcbilingual$setHandledDebugKey(true)` 吃掉调试键，避免松开 F3 打开调试画面。已绑定后只认新绑定。聊天界面或加载叠层打开时不触发。冲突模组已停用时直接返回。按下只在 `FreezeSession` 上请求切换，不调用 `LanguageSwitch`，不重载资源。在 `src/main/java/pnnh/mcbilingual/MCBilingual.java` 里于现有快捷键监听旁注册它。不复用 `SWITCH_LANGUAGE`

**Checkpoint**: `./gradlew test --tests pnnh.mcbilingual.freeze.*` 通过，且不启动客户端。快捷键尚未定格画面。

---

## Phase 3: User Story 1 - 定格后从提示框上读出整句对照语言 (Priority: P1) 🎯 MVP

**Goal**: 按下快捷键后留下已经看见的那一帧和仍带译文的整句位置。鼠标移到物品名上时，按当前主语言显示另一种语言的整句。

**Independent Test**: 主语言为英文，悬停中英名称不同的苹果，提示框还在时定格。鼠标移到 “Apple” 上看到整句「苹果」，移开后提示消失。再按一次后可以操作物品栏，没有资源重载。主语言改为简体中文后，移到「苹果」上看到整句 “Apple”。

- [x] T011 [US1] 添加 `src/main/java/pnnh/mcbilingual/freeze/FreezeCapture.java` 和 `src/main/java/pnnh/mcbilingual/freeze/mixin/ScreenHudCaptureMixin.java`。只在 `Screen.render` 与 `Gui.render` 期间打开采集。`LevelRenderer` 期间保持关闭，因此告示牌、头顶名字和漂浮文字不记。采集内容是草稿命中、视觉序列到原组件的标识映射、当前组件所有者栈和裁剪矩形栈。帧末只保留最新一份草稿，定格后不再改它
- [x] T012 [P] [US1] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/GuiGraphicsCaptureMixin.java` 于 `drawString(Font, Component, ...)` 和 `drawWordWrap` 的组件调用期间把该组件压入 T011 的所有者栈，返回时弹出。同步记录 `enableScissor` 与 `disableScissor`。`drawString(String)` 和只含视觉序列的重载不建立所有者
- [x] T013 [P] [US1] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/TooltipOwnerMixin.java` 包住 `ClientHooks.gatherTooltipComponentsFromElements` 的拆行。组件被拆成多条 `FormattedCharSequence` 后，按对象标识记回拆分前的那一个组件。不把提示框改道到 `GuiGraphics.drawString`
- [x] T014 [US1] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/FontCaptureMixin.java` 注入 `Font.drawInBatch(FormattedCharSequence, ...)`。仅当 T011 正在采集时记录。用矩阵把字形起点变到界面坐标，宽度用 `Font.width`，高度用字体行高，再与当前裁剪相交。空矩形丢掉。后画的序号更大。能指回翻译组件时才生成 `TextHit`。添加 `src/main/java/pnnh/mcbilingual/freeze/ComponentSentences.java`：用 `LanguageTexts.currentLanguage()` 和对应的 `ClientLanguage.getOrDefault` / `getComponent` 把组件交给 `SentenceResolver`，字面量参数保留。不临时更换全局语言，不把成品字符串反查成键
- [x] T015 [US1] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/GameRendererFreezeMixin.java` 于 `GameRenderer.render` 画出新世界之前处理 T010 的切换请求。进入时用 `Screenshot.takeScreenshot(minecraft.getMainRenderTarget())` 取得已经显示的那一帧，放进只存在于内存的 `DynamicTexture`，并复制上一份界面草稿。不调用 `Screenshot.grab`，不多等帧，不重载资源。定格后每帧跳过世界和界面的实时绘制，把该图铺满窗口，再按鼠标下序号最大的命中画对照整句。提示默认在光标右下方，贴边时翻到另一侧，长句换行并完整留在窗口内。提示绘制发生在采集关闭之后。没有对照句时不画，也不画翻译键。进入时若鼠标被抓住则 `releaseMouse` 并记下；离开时仅当进入前抓住过、当前没有界面且没有叠层才 `grabMouse`。离开时释放纹理和命中
- [x] T016 [US1] 把 `ScreenHudCaptureMixin`、`GuiGraphicsCaptureMixin`、`TooltipOwnerMixin`、`FontCaptureMixin`、`GameRendererFreezeMixin` 追加进 `src/main/resources/mcbilingual-freeze.mixins.json` 的 `client` 数组

**Checkpoint**: 物品栏里两种主语言都能定格并读到整句对照语言。点击是否穿透留到 User Story 2。

---

## Phase 4: User Story 2 - 定格期间游戏不跟着鼠标走 (Priority: P2)

**Goal**: 定格之后只有鼠标移动和定格快捷键生效。点击、滚轮、移动、转身和语言切换都不进入游戏。

**Independent Test**: 在打开的背包上定格。点击格子、滚动滚轮、按住前进和按调试键加 J。背包内容、角色位置、视角和主语言都不变。只有再次按下定格键才恢复，恢复后的下一次点击生效。

- [x] T017 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/KeyboardInputFreezeMixin.java` 于 `KeyboardInput.tick` 开头在定格时把 `forwardImpulse` 和 `leftImpulse` 置 0 并取消方法体，避免已经按住的前进键继续走动
- [x] T018 [P] [US2] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/MouseLookFreezeMixin.java` 于定格时让 `MouseHandler.turnPlayer` 直接返回，并清掉累计的视角增量，避免结束定格时镜头跳转。`onPress` 与 `onScroll` 在定格时直接返回，使点击、拖拽和滚轮到不了当前界面
- [x] T019 [US2] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/KeyboardHandlerFreezeMixin.java` 于 `KeyboardHandler.keyPress` 开头处理定格。不是定格键时直接返回，使 `screen.keyPressed`、`KeyMapping.click`、暂停、调试画面和隐藏界面都不运行。是定格键时跳过上述界面处理，仍把这一下交给 `FreezeKeyHandler`，并继续用 `KeyboardHandlerAccessor` 标记调试键已处理。`charTyped` 在定格中直接返回
- [x] T020 [US2] 在 `src/main/java/pnnh/mcbilingual/switchlang/SwitchKeyHandler.java` 的按键和鼠标处理开头，若 `FreezeSession` 已定格则返回，避免调试键加 J 在定格中切换主语言。把 T017、T018、T019 的三个 Mixin 追加进 `src/main/resources/mcbilingual-freeze.mixins.json`

**Checkpoint**: 定格画面不因点击、滚轮、移动或语言键改变。结束定格后输入恢复。

---

## Phase 5: User Story 3 - 只提示仍带译文的游戏句子 (Priority: P3)

**Goal**: 按钮和系统聊天在仍能对上译文时显示整句对照语言。玩家聊天、改名、手写说明和已经拼好的成品文字不提示。

**Independent Test**: 同一次定格中依次指向原版按钮、一条可翻译的系统聊天、玩家刚发出的 “Apple”、铁砧改名后的物品。前两段有对照语言时显示整句；后两段没有提示，也没有翻译键。

- [x] T021 [US3] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/ChatOwnerMixin.java` 于 `ChatComponent.addMessageToDisplayQueue` 调用 `ComponentRenderUtils.wrapComponents` 之后，把每条视觉序列按对象标识记回 `GuiMessage` 原来的组件。玩家聊天仍是字面量，因此 T014 不产生命中。系统句仍是翻译组件时，鼠标放在任意换行碎片上都回到整句。把 `ChatOwnerMixin` 追加进 `src/main/resources/mcbilingual-freeze.mixins.json`
- [x] T022 [US3] 在 `src/main/java/pnnh/mcbilingual/freeze/mixin/FontCaptureMixin.java` 和 `src/main/java/pnnh/mcbilingual/freeze/ComponentSentences.java` 确认没有成品字符串索引。`drawString(String)`、纯字面量提示行和铁砧改名都不产生 `TextHit`。按钮继续只靠 T012 的组件所有者，不为 `AbstractWidget` 另写反查。世界文字仍只靠 T011 的采集开关排除

**Checkpoint**: 来源区分不依赖聊天或提示框的外形。缺对照译文时定格仍能开关且不崩溃。

---

## Phase 6: Polish

**Purpose**: 单测和编译通过，再按 quickstart 做两种主语言的客户端核对。

- [x] T023 运行 `./gradlew test --tests pnnh.mcbilingual.freeze.* compileJava`。确认 `gradle.properties` 的 `neo_version` 仍是 `21.1.251`、`mod_license` 仍是 `All Rights Reserved`，且 `RivalMods` 仍只有 `polyglottooltip` 和 `languagereload`
- [ ] T024 按 `specs/006-frozen-ui-lookup/quickstart.md` 在客户端里核对两种主语言、系统聊天、玩家聊天、铁砧改名、告示牌、定格中的移动和语言键、聊天输入框，以及改键后旧组合失效。没有进入世界并完成这些步骤之前，不得把本任务记为完成

---

## Dependencies

- Phase 1 不阻塞编译，但 T010 的键名依赖 T001 和 T002 的语言条目。T016 及以后的配置依赖 T003 和 T009。
- Phase 2 阻塞全部用户故事。T006 依赖 T005。T007 依赖 T004。T009 依赖 T008。T010 依赖 T004。
- US1 依赖 Phase 2。T012 与 T013 依赖 T011，并可彼此并行。T014 依赖 T005、T012、T013。T015 依赖 T010 与 T014。T016 依赖 T011 至 T015 的类已经存在。
- US2 依赖 `FreezeSession` 能进入定格，因此依赖 T010 与 T015。T017、T018、T019 彼此并行。T020 依赖这三个 Mixin，并修改现有的 `SwitchKeyHandler`。
- US3 依赖 T011 的标识映射和 T014 的记录规则。T022 只收紧 T014 已有文件，不新增反查。
- 现有提示第二行、搜索和调试键加 J 的切换没有单独任务，不得在本功能里改写。

## Parallel example

- T001、T002、T003 可同时进行。
- T006、T007、T008 可同时进行（T006 须等 T005，T007 须等 T004）。
- T012 与 T013 可同时进行。
- T017、T018、T019 可同时进行。

## Implementation strategy

先完成纯判断、快捷键和空的 Mixin 配置。MVP 是 User Story 1：按下时留下已经看见的画面，物品提示框里两种主语言都能读到整句对照语言，成品文字不反查。User Story 2 再挡住点击、滚轮、移动、转身和语言键。User Story 3 把系统聊天指回原组件，并确认玩家文字和世界文字没有提示。quickstart 由玩家在客户端里看，编译或单测通过不等于这些步骤已经完成。
