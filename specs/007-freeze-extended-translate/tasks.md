# Tasks: 定格扩展取词与对照段 / 机翻

**Input**: Design documents from `/specs/007-freeze-extended-translate/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/freeze-extended-translate.md, quickstart.md

**Tests**: 计划与宪法要求词典查找、断句、模式优先级用 JUnit（不启游戏、尽量不导入 `net.minecraft`）。界面与 Ollama 有/无步骤留在 quickstart，不写成自动化 UI 测试。

**Organization**: 按用户故事分阶段。配置、词典、Ollama 客户端与可见串模型是故事前提。继承 006 定格栈，不重做截帧与输入隔离。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可与同阶段其它标了 [P]、且其依赖已完成的任务并行
- **[Story]**: US1 默认取词，US2 对照段/单句机翻与模式切换，US3 配置关与无法翻译

## Phase 1: Setup

**Purpose**: 包结构、语言键、词典资源与许可占位。不新增模组硬依赖，不改 `neo_version`。

- [x] T001 按 plan 建包目录：`src/main/java/pnnh/mcbilingual/config/`、`dictionary/`、`translate/`；测试目录 `src/test/java/pnnh/mcbilingual/dictionary/`、`translate/`。不删除现有 `freeze/`
- [x] T002 [P] 在 `src/main/resources/assets/mcbilingual/lang/en_us.json` 增加扩展翻译相关键：定格模式切换键名、取词/对照段浮窗文案、加载中、超时、失败、无法翻译、扩展翻译配置标题（英文）。保留既有键
- [x] T003 [P] 在 `src/main/resources/assets/mcbilingual/lang/zh_cn.json` 增加与 T002 对应的中文条目
- [x] T004 [P] 添加 `src/main/resources/data/mcbilingual/dictionary/LICENSE-ECDICT.txt`（MIT 声明）与空的或最小样例 `entries.tsv`（或约定二进制格式说明文件）。正式截断表可在后续任务填入
- [x] T005 添加 `tools/dictionary/README.md`：说明从 ECDICT 按频次截断生成 `entries` 的步骤与禁止打包商业 COCA。可选随后添加生成脚本，本任务至少文档化

---

## Phase 2: Foundational

**Purpose**: 客户端配置、LookupMode、词典查询、Ollama 异步客户端、断句与取词纯逻辑。用户故事接线前必须完成。

**⚠️ CRITICAL**: 用户故事开始前必须完成本阶段。

- [x] T006 在 `src/main/java/pnnh/mcbilingual/config/ClientConfig.java`（及必要时 `ConfigEvents`）用 NeoForge `ModConfigSpec` 注册 **CLIENT** 配置：至少 `extendedTranslation` 布尔默认 `true`；可选 `requestTimeoutMs` 与本机 base URL（默认 `http://127.0.0.1:11434`）。在 `MCBilingual.java` 注册配置。关闭时不得发起机翻。加载不探测 Ollama
- [x] T007 [P] 在 `src/main/java/pnnh/mcbilingual/freeze/LookupMode.java` 定义 `WORD` / `UNIT`。扩展 `FreezeSession.java`：保存当前模式，默认 `WORD`；提供切换与读取；结束定格重置为 `WORD`（除非规格外选择记住——本任务按重置实现）
- [x] T008 [P] 在 `src/main/java/pnnh/mcbilingual/dictionary/LearnerDictionary.java`（及 `DictionaryEntry` 若需）加载 jar 内词典资源；`lookup(String englishWord)` 大小写不敏感；未命中返回 empty。不导入 `net.minecraft`（资源加载可经薄封装或测试用 classpath 流）
- [x] T009 [P] 在 `src/test/java/pnnh/mcbilingual/dictionary/LearnerDictionaryTest.java` 用夹具覆盖命中、未命中、大小写。不启游戏
- [x] T010 [P] 在 `src/main/java/pnnh/mcbilingual/translate/SentenceBounds.java`（或同名）按中英句末标点切出光标偏移所在**单句**；对 `1.21` 与常见缩写做保守规则。不导入 `net.minecraft`
- [x] T011 [P] 在 `src/test/java/pnnh/mcbilingual/translate/SentenceBoundsTest.java` 覆盖多句取中间句、无标点整段、小数版本号不误切的基本用例
- [x] T012 [P] 在 `src/main/java/pnnh/mcbilingual/translate/EnglishWords.java` 从可见英文字符串按空白/标点分词，并在给定字体宽度回调下计算词起止（纯逻辑接口：宽度由调用方注入，便于单测）
- [x] T013 [P] 在 `src/test/java/pnnh/mcbilingual/translate/EnglishWordsTest.java` 覆盖多分词与光标落在第二词
- [x] T014 在 `src/main/java/pnnh/mcbilingual/translate/OllamaClient.java` 与 `MachineTranslationCache.java`：仅本机 HTTP；单词模型固定 `qwen2.5:1.5b`、整句 `qwen2.5:3b`；异步完成；超时读配置；按 kind+source 缓存；`ClientConfig.extendedTranslation==false` 时拒绝发起。状态 PENDING/OK/FAILED/TIMEOUT。不在类加载时连接 Ollama
- [x] T015 [P] 在 `src/test/java/pnnh/mcbilingual/translate/MachineTranslationCacheTest.java` 覆盖同键命中缓存与不同 kind 不串用（可不打真实 HTTP）

**Checkpoint**: `./gradlew test --tests pnnh.mcbilingual.dictionary.* --tests pnnh.mcbilingual.translate.*` 通过。定格提示行为尚未改。

---

## Phase 3: User Story 1 - 定格默认取词看英文释义 (Priority: P1) 🎯 MVP

**Goal**: 定格默认取词；词典命中立即显示中文释义；未命中且扩展翻译开则走单词 Ollama（加载/失败文案）；有语言包整段时也不自动弹整段。

**Independent Test**: 定格后不切换模式，悬停词典内英文词见释义；扩展翻译关时未收录词不再请求机翻错误；有整段汉化处仍只显示词义。

- [x] T016 [US1] 扩展 `FreezeCapture` / `FontCaptureMixin` / 相关所有者：在定格草稿中记录**可见主语言字符串**与矩形（含能拿到的字面量路径），供取词；世界投影绘制期仍不采集。语言包对照仍只来自翻译组件，禁止成品反查键。可新增 `VisibleSpan` 记录类型于 `src/main/java/pnnh/mcbilingual/freeze/VisibleSpan.java`
- [x] T017 [US1] 在 `src/main/java/pnnh/mcbilingual/freeze/WordLookup.java`（或等价）结合 `EnglishWords` + `LearnerDictionary` + 可选 `OllamaClient`：给定鼠标下 span 与模式 `WORD`，返回词典释义、PENDING、失败文案或 empty。取词模式**忽略**翻译单元对照全文
- [x] T018 [US1] 修改 `FreezeFrames.java`（及必要绘制辅助）：定格且 `LookupMode.WORD` 时按 T017 在光标旁画提示；词典命中无加载态；机翻中显示加载；失败每次悬停显示错误（扩展翻译开时）。无命中词且无错误需求时可不画
- [x] T019 [P] [US1] 用构建脚本或手工将 ECDICT 频次截断表填入 `src/main/resources/data/mcbilingual/dictionary/`（规模以计划包体为准，至少覆盖常见 UI 词便于 quickstart）。更新 NOTICE。禁止 COCA 商业表

**Checkpoint**: 仅取词路径可在 runClient 按 quickstart「词典取词」验证。模式切换留给 US2。

---

## Phase 4: User Story 2 - 切换到对照段 / 单句机翻 (Priority: P1)

**Goal**: 快捷键 + 浮窗按钮切换取词/对照段；对照段优先整段语言包对照；无对照则单句 Ollama；UGC 可采集则走机翻。

**Independent Test**: 对照段下悬停多句任务文案见整段对照；悬停玩家聊天一句在 Ollama 可用时得单句机翻；热键与按钮状态一致。

- [x] T020 [P] [US2] 在 `FreezeKeyHandler.java`（或新 `LookupModeKeyHandler.java`）注册定格内模式切换键（默认未绑定或独立组合，不得占用 F3+J / F3+K）；仅 `FreezeSession.active()` 时生效。更新 en_us/zh_cn 键名（若 T002 未写全则补全）。在 `MCBilingual.java` 注册
- [x] T021 [US2] 在定格提示层增加浮窗切换按钮（绘制于定格图之上、不进入文字采集）：显示当前模式，点击切换，与 T020 状态同步。实现可放在 `FreezeFrames.java` 或 `src/main/java/pnnh/mcbilingual/freeze/LookupModeHud.java`
- [x] T022 [US2] 实现对照段解析：`LookupMode.UNIT` 且 span 关联翻译单元对照非空 → 显示**全文**对照，不按标点截断。复用/扩展 `ComponentSentences` / `UiSentence`，不反查成品键
- [x] T023 [US2] `LookupMode.UNIT` 且无对照：用 `SentenceBounds` 取光标所在单句；扩展翻译开则 `OllamaClient` 整句模型；UGC/字面量可见串同样可走此路径。加载/失败行为同 US1
- [x] T024 [US2] 将 T022/T023 接到 `FreezeFrames` 绘制：按当前 `LookupMode` 分支；确保 WORD 路径仍不弹整段对照

**Checkpoint**: quickstart「对照段」与模式切换可手动验证。

---

## Phase 5: User Story 3 - 配置开关与无法取词 (Priority: P2)

**Goal**: 配置关闭后不再机翻报错；无可见字符串时提示无法翻译；确认无 OCR 代码路径。

**Independent Test**: 关扩展翻译后未收录词/无对照 UGC 不再出现 Ollama 错误；无字符串区域显示无法翻译。

- [x] T025 [US3] 确认并补强 `ClientConfig.extendedTranslation==false` 时：取词未命中与对照段无包路径均不调用 `OllamaClient`、不显示机翻错误；词典命中与语言包对照仍可用。必要时在 `WordLookup` / UNIT 路径加显式分支与注释
- [x] T026 [US3] 定格悬停时若鼠标下无任何 `VisibleSpan`/词命中：绘制「无法翻译」类语言键文案。不添加 OCR 依赖或截图像素识别
- [x] T027 [P] [US3] 在 `src/test/java/pnnh/mcbilingual/freeze/LookupPriorityTest.java`（纯逻辑）覆盖：WORD 优先词义不返回整段对照；UNIT 有对照返回全文；扩展翻译关时未命中不产生 MT 请求标志。不启游戏

**Checkpoint**: quickstart「Ollama 有/无」与「无法翻译」可走通。

---

## Phase 6: Polish

**Purpose**: 文档与交叉检查。

- [x] T028 [P] 更新 `docs/plan/Ollama集成.md` 与 `specs/007-freeze-extended-translate/quickstart.md` 中与最终键名/配置项名一致的说明
- [ ] T029 按 `quickstart.md` 在 `./gradlew runClient` 走通：词典取词、对照段整段、模式切换、扩展翻译开关、Ollama 停用错误提示。编译通过不算完成（本机 Gradle/插件仓库 SSL 失败，需你本地验证）
- [x] T030 [P] 核对 `META-INF/licenses` 或 dictionary NOTICE 已随资源分发；`neoforge.mods.toml` 无需为 Ollama 加依赖

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup → Foundational → US1 → US2 → US3 → Polish**
- Foundational **阻塞**所有用户故事
- US1 与 US2 均为 P1：建议先 US1（MVP 取词）再 US2（模式与对照段）；二者都依赖 T016 可见串采集
- US3 依赖配置（T006）与绘制路径（US1/US2）

### User Story Dependencies

- **US1**: Foundational 完成后即可；MVP
- **US2**: 依赖 US1 的可见串采集（T016）与提示绘制骨架（T018）；模式切换可并行于词查找打磨
- **US3**: 依赖 T006 与悬停绘制已存在

### Parallel Opportunities

- T002/T003/T004；T007–T013、T015 在 Foundational 内多可并行
- T019 可与 T017/T018 并行（不同关注点）
- T020 与 T021 可并行（键 vs HUD），再与 T022/T023 汇合到 T024

### Parallel Example: Foundational

```text
T007 LookupMode + FreezeSession
T008/T009 LearnerDictionary + test
T010/T011 SentenceBounds + test
T012/T013 EnglishWords + test
T015 cache test
# then T006 config + T014 OllamaClient（读配置）
```

---

## Implementation Strategy

### MVP

1. Phase 1 + 2  
2. Phase 3（US1 取词）→ runClient 验证词典命中  
3. 再开 US2 / US3  

### Incremental

- US1：离线学习价值最高  
- US2：整段对照 + UGC 机翻  
- US3：可关噪音 + 无法翻译边界  

---

## Notes

- 不重做 006 定格截帧与输入隔离；冲突模组停用逻辑保持  
- 固定模型 id，不向玩家暴露任意模型常规选项  
- 每完成一阶段可单独提交；实现前以本 tasks 与 spec 为准  
