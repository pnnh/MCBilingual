# Tasks: 物品与 Jade 双语名称

**Input**: Design documents from `/specs/001-bilingual-tooltips/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: 计划要求为纯策略写 JUnit。玩家可见行为用 `runClient` 抽查。

**Organization**: 按用户故事分组。语言表和策略是所有故事的前置。

## Format: `[ID] [P?] [Story] Description`

## Phase 1: Setup

**Purpose**: 测试运行器和可选的 Jade 编译依赖

- [x] T001 Add JUnit 5 and the Modrinth repository in `build.gradle`, and add `jade_version` in `gradle.properties`
- [x] T002 [P] Mark Jade as an optional client dependency and replace the scaffold description in `src/main/templates/META-INF/neoforge.mods.toml`

---

## Phase 2: Foundational

**Purpose**: 中英语言表和与界面无关的第二行策略。完成前不写提示框或 Jade。

- [x] T003 Add `zh_cn` and `en_us` constants in `src/main/java/pnnh/mcbilingual/language/LanguageCodes.java`
- [x] T004 [P] Write policy tests in `src/test/java/pnnh/mcbilingual/tooltip/SecondaryNamePolicyTest.java`
- [x] T005 Implement `SecondaryNameLine` and `SecondaryNamePolicy` in `src/main/java/pnnh/mcbilingual/tooltip/SecondaryNameLine.java` and `src/main/java/pnnh/mcbilingual/tooltip/SecondaryNamePolicy.java`
- [x] T006 Load both languages on client resource reload in `src/main/java/pnnh/mcbilingual/language/LanguageTexts.java` and register that listener from `src/main/java/pnnh/mcbilingual/MCBilingual.java`

**Checkpoint**: 策略测试通过。语言表在资源加载后可查询，且不修改当前游戏语言。

---

## Phase 3: User Story 1 - 物品名称下多一行另一种语言 (Priority: P1)

**Goal**: 物品提示框标题下出现灰色的另一种语言名称。

**Independent Test**: 不装 Jade，进入世界，悬停中英名称不同的原版物品。标题保持当前语言，下一行是另一种语言。

- [x] T007 [US1] Insert and keep the secondary line directly under the item title in `src/main/java/pnnh/mcbilingual/tooltip/ItemTooltipNames.java`

**Checkpoint**: 物品提示框单独可用。

---

## Phase 4: User Story 2 - Jade 瞄准名称同样多一行 (Priority: P2)

**Goal**: 安装 Jade 时，方块、流体方块和实体使用同一套第二行规则。

**Independent Test**: 装上 Jade 后瞄准方块和实体能看到第二行。卸下 Jade 后游戏仍能进入，物品提示框仍可用。

- [x] T008 [US2] Add the Jade plugin and block, fluid, and entity name lines in `src/main/java/pnnh/mcbilingual/integration/jade/McbilingualJadePlugin.java`, `src/main/java/pnnh/mcbilingual/integration/jade/JadeBlockNames.java`, and `src/main/java/pnnh/mcbilingual/integration/jade/JadeEntityNames.java`

**Checkpoint**: 主类不引用 Jade 类型。

---

## Phase 5: User Story 3 - 缺翻译时安静退让 (Priority: P3)

**Goal**: 没有译文时不加行，也不显示翻译键。模组不自带语言文件。

**Independent Test**: 策略测试覆盖「译文等于翻译键」和空白。`src/main/resources` 下没有 `lang` 文件。

- [x] T009 [US3] Treat a missing or key-equal translation as absent in `src/main/java/pnnh/mcbilingual/language/LanguageTexts.java`, and do not add language files under `src/main/resources`

**Checkpoint**: 缺中文语言包时查询结果为空行，而不是翻译键。

---

## Phase 6: Polish

**Purpose**: 去掉脚手架行为并完成可自动检查的验证

- [x] T010 Remove the Hello World login message from `src/main/java/pnnh/mcbilingual/MCBilingual.java`
- [x] T011 Run `./gradlew test` and `./gradlew compileJava`

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup 无前置。
- Foundational 依赖 Setup，并挡住所有用户故事。
- US1 依赖 Foundational。
- US2 依赖 Foundational 的语言表和策略，不依赖物品提示框类。
- US3 的省略规则在 Foundational 的策略和 `LanguageTexts` 中完成。
- Polish 在目标故事完成后执行。

### User Story Dependencies

- US1 是 MVP，不依赖 US2、US3 的界面代码。
- US2 可在 US1 之后单独验收。
- US3 不增加新界面，验收靠策略测试和资源目录。

### Parallel Opportunities

- T001 与 T002 触及不同文件，可并行。
- T003 与 T004 可并行。
- T007 与 T008 在 Foundational 完成后触及不同文件，可并行。

## Parallel Example: Foundational

```bash
# 常量与测试互不依赖：
Task: "T003 LanguageCodes.java"
Task: "T004 SecondaryNamePolicyTest.java"
```

## Implementation Strategy

1. 完成 Setup 与 Foundational，先让策略测试通过。
2. 交付 US1 物品提示框。
3. 加上 US2 Jade。
4. 用 US3 的缺词规则和「不打包语言文件」收口。
5. 去掉 Hello World，跑测试和编译。

## Notes

- 不复制 PolyglotTooltip 源文件。Jade 类不能被 `MCBilingual` 引用。
- 玩家可见的悬停和瞄准仍要在客户端里看。自动测试覆盖的是第二行选词规则。
- `assets/mcbilingual/lang` 只有 Jade 配置开关的两行本模组文案。物品翻译仍来自外部语言包。
