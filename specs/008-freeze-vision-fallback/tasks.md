# Tasks: 定格视觉兜底与分管道状态提示

**Input**: Design documents from `/specs/008-freeze-vision-fallback/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/freeze-vision-fallback.md, quickstart.md；007 定格扩展翻译已落地。

**Tests**: ROI 键、状态→语言键映射用 JUnit（不启游戏）。视觉/文案用 runClient + quickstart，不写自动化 UI 测试。

**Organization**: US1 视觉兜底；US2 文本路径「文本识别中」；US3 错误区分与边界。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可并行（不同文件、依赖已满足）
- **[Story]**: US1 视觉，US2 文本状态，US3 错误/边界

## Phase 1: Setup

**Purpose**: 语言键与类型占位；不改 NeoForge 版本。

- [x] T001 [P] 在 `src/main/resources/assets/mcbilingual/lang/en_us.json` 增加：`mcbilingual.freeze.status.text_recognizing`、`image_recognizing`、文本/图片失败与超时键（可复用 007 失败键并加管道后缀）。保留既有键
- [x] T002 [P] 在 `zh_cn.json` 增加对应中文：「文本识别中…」「图片识别中…」及管道错误文案
- [x] T003 [P] 在 `MachineTranslationCache.Kind` 增加 `VISION`；确认 `Job` 仍支持 PENDING/OK/FAILED/TIMEOUT（`src/main/java/pnnh/mcbilingual/translate/MachineTranslationCache.java`）

---

## Phase 2: Foundational

**Purpose**: ROI 几何、视觉请求能力、状态映射纯逻辑。故事接线前完成。

**⚠️ CRITICAL**: 用户故事实现前完成本阶段。

- [x] T004 实现 `src/main/java/pnnh/mcbilingual/translate/VisionRoi.java`：以光标为中心计算钳制矩形；生成 `cacheKey(frameGeneration, roi)`。不导入 Minecraft 或仅用基本几何（便于单测）
- [x] T005 [P] `src/test/java/pnnh/mcbilingual/translate/VisionRoiTest.java`：边界钳制、同世代同 ROI 同键、不同世代不同键
- [x] T006 扩展 `OllamaClient`：`VISION_MODEL = "qwen2.5vl:3b"`；`requestVision(cacheKey, imageBytes)` 走本机 HTTP + images；**禁止**对 WORD/SENTENCE 模型送图；尊重 `extendedTranslation`；超时用 `visionTimeoutMs`（若配置未加则先复用 `requestTimeoutMs` 并在注释标明后续可拆）
- [x] T007 可选：`ClientConfig` 增加 `visionTimeoutMs`（默认 ≥ 文本超时，如 60000）。关 `extendedTranslation` 时 `requestVision` 直接 empty
- [x] T008 [P] `src/test/java/pnnh/mcbilingual/freeze/LookupStatusMappingTest.java`（或 translate 包）：给定 pipeline+Job.status → 期望语言键 id 字符串常量；覆盖 TEXT_PENDING / VISION_PENDING / 失败管道区分

**Checkpoint**: `./gradlew test --tests pnnh.mcbilingual.translate.VisionRoiTest --tests pnnh.mcbilingual.freeze.LookupStatusMappingTest`（及既有 translate 测试）通过。

---

## Phase 3: User Story 1 - 无绘制串时图片识别翻译 (Priority: P1) 🎯 MVP

**Goal**: UNIT + 无 VisibleSpan + 扩展开 → ROI → VL → 图片识别中 → 译文或错误。

**Independent Test**: 自绘无串 UI 定格对照段悬停，见图片识别中再出结果；WORD 无串不发视觉。

- [x] T009 [US1] 从定格帧缓冲裁 ROI 为 PNG/JPEG bytes（实现于 `FreezeFrames` 或新 `FreezeVisionCapture.java`），仅客户端、失败返回 empty 不崩溃
- [x] T010 [US1] 在 `UnitLookup`（或等价 UNIT 解析）无串分支：调用视觉请求；返回 Hint 带 VISION pipeline；WORD 路径明确不调用
- [x] T011 [US1] `FreezeFrames` 绘制：VISION PENDING 用 image_recognizing 键；OK 显示译文；不在模式浮窗上触发视觉

**Checkpoint**: quickstart 用例 A 可手动验证。

---

## Phase 4: User Story 2 - 文本路径「文本识别中」 (Priority: P1)

**Goal**: 文本 MT PENDING 显示文本识别中，不用图片文案；即时路径无识别中。

**Independent Test**: 取词未命中等文本模型 → 仅文本识别中；有词典/包 → 无识别中。

- [x] T012 [US2] 改 `WordLookup` / UNIT 有串 MT 路径：`HintKind` 或等价区分 TEXT vs IMAGE；LOADING 拆分或带 pipeline
- [x] T013 [US2] `FreezeFrames`（及文案解析）映射 TEXT_PENDING → text_recognizing；确认词典命中与包对照不走 PENDING 文案

**Checkpoint**: quickstart 用例 B。

---

## Phase 5: User Story 3 - 无法翻译与管道错误 (Priority: P2)

**Goal**: 文本/图片失败可区分；WORD 无串无法翻译；关扩展无视觉；浮窗不抢提示。

**Independent Test**: 停 Ollama 分别触发两路径见不同错误；WORD 空白无 HTTP。

- [x] T014 [US3] FAILED/TIMEOUT 文案按 Kind 选择文本或图片错误键；每次悬停可见直至关扩展翻译
- [x] T015 [US3] WORD + 无串 → 无法翻译，断言不调用 `requestVision`（可用测试替身或包内标志单测）
- [x] T016 [US3] 鼠标在 `LookupModeHud` 区域时不显示 recognizing / 无法翻译抢焦点（与 007 行为对齐并回归）

**Checkpoint**: quickstart 用例 C/D。

---

## Phase 6: Polish

- [x] T017 [P] 核对 `docs/plan/Ollama集成.md` 与本 feature quickstart 键名、模型 id、触发条件一致
- [ ] T018 按 quickstart 在 `./gradlew runClient` 走通 A–D；确认发布依赖树无 OCR 原生库（本机 Gradle/插件仓库 SSL 失败，需维护者本地验证）
- [x] T019 [P] 更新本目录 `spec.md` Status → Code complete — client verify pending（T018 通过后再改为 Implemented）
- [x] T020 [P] 分析 remediation：`feature.json`→008；Assumptions 锁定英→中；data-model IDLE/UNAVAILABLE；plan 类名与 006 继承说明；`docs/plan/Ollama集成.md` 同步。注意：若 shell 中 `SPECIFY_FEATURE_DIRECTORY`/`SPECIFY_FEATURE` 仍指向 007，会覆盖 `feature.json`——会话内应改为 008

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup → Foundational → US1 → US2 → US3 → Polish  
- Foundational **阻塞**所有故事  
- US2 可与 US1 绘制层部分并行，但建议先 US1 MVP（视觉）再统一 Hint 映射（若 T012 改公共 Hint，可先做 T012 再 T011）

### Suggested order note

若 `Hint` 类型需先扩展：将 T012 提前到 Phase 2 末或 US1 前，再实现 T011/T013。

### User Story Dependencies

- **US1**: T004–T007 + 帧裁剪  
- **US2**: 依赖 Hint/pipeline 类型  
- **US3**: 依赖 US1/US2 文案路径

### Parallel Opportunities

- T001/T002/T003；T005 与 T008；T017/T019

### Parallel Example: Foundational

```text
T004 VisionRoi
T005 VisionRoiTest
T008 LookupStatusMappingTest
# then T006/T007 Ollama vision + config
```

---

## Implementation Strategy

### MVP

1. Phase 1–2  
2. Phase 3（US1）→ runClient 无串 UI  
3. Phase 4–5 补齐文案与边界  

### Incremental

- US1：产品最大缺口（自绘字）  
- US2：修正 007 笼统「加载中」预期  
- US3：错误可懂、无误触发  

### Stop criteria

`tasks.md` 全部勾选且 quickstart A–D 通过（环境 SSL 导致无法 runClient 时在 T018 注明由维护者本地补验）。
