# Tasks: 定格取词词典增强与模式按钮点击

**Input**: `/specs/009-freeze-word-lexicon/`

## Phase 1: Setup

- [x] T001 [P] 扩展词典资源格式说明于 `tools/dictionary/README.md`（多列 ECDICT 字段）
- [x] T002 [P] 更新/生成 `entries.tsv`：至少含 `into`、`tuck`(+exchange)、若干高频词；保留 LICENSE

## Phase 2: Foundational

- [x] T003 实现 `LexiconEntry` + 丰富加载于 `dictionary/`（兼容旧两列 TSV）
- [x] T004 [P] `EnglishLemmas`（或同类）：exchange 索引 + 启发式还原；JUnit
- [x] T005 `LearnerDictionary.lookupRich(surface)` → Optional resolution+entry
- [x] T006 [P] `WordCardFormatter` 多行卡片；JUnit
- [x] T007 WORD 机翻结果回显源词 → FAILED（`OllamaClient` 或 cache put 处）

## Phase 3: US1+US2 取词

- [x] T008 [US1] `WordLookup` 改用 rich lookup + 卡片文本；无中文/回显不进 GLOSS
- [x] T009 [US2] `FreezeFrames` 确认多行 hint 换行可用（必要时调 maxWidth）

## Phase 4: US3 按钮

- [x] T010 [US3] `MouseLookFreezeMixin.onPress`：定格时先 `LookupModeHud.click`（GUI 坐标），命中则 toggle+cancel；未命中再吞点击
- [x] T011 [P] [US3] 回归：`LookupModeKeyHandler` 热键仍可用；mixin 为点击主路径（Pre 保留作双保险）

## Phase 5: Polish

- [x] T012 [P] quickstart / Ollama 备忘一句指向 009
- [ ] T013 runClient：按钮点击 + into/tucked 卡片（环境允许时；本机 Gradle SSL 可能阻塞）

## Dependencies

Setup → Foundational → US1/2 → US3 → Polish. T010 可与词典并行。
