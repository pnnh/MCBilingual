# Feature Specification: 定格取词词典增强与模式按钮点击

**Feature Branch**: `009-freeze-word-lexicon`

**Created**: 2026-10-25

**Status**: Code complete — client verify pending

**Input**: User description: "定格取词时常见词/变形常无释义或回显英文；整句模式正常。取词模式应像词典一样显示词性、形变与多义释义。右上角 Word/Unit 按钮点击无效，须可点击切换。新 Speckit feature。"

## Problem Analysis (observed)

定格取词失败或「显示原词」的主要原因（已对照代码与资源，非猜测）：

1. **捆绑词典过小**：`entries.tsv` 仅为百余条 UI 样例词，未导入 ECDICT 频次截断表。`into`、`tucked` 等不在表中 → 词典未命中。
2. **无词形还原**：查找仅做大小写归一，不处理 `-ed` / `-ing` / `-s` 等。即便日后有 lemma `tuck`，`tucked` 仍会 miss。
3. **机翻回显原文**：词典未命中后走 `qwen2.5:1.5b`，小模型对功能词/变形常直接回传英文；UI 把结果当「释义」画出，看起来像「取到了原词」。整句用更大文本模型且上下文更完整，故截图 3 正常。
4. **模式按钮**：定格时 `MouseLookFreezeMixin` 在 `MouseHandler.onPress` HEAD 取消全部按键，NeoForge 鼠标 Pre 事件/HUD `click` 路径吃不到点击；快捷键不经该路径故仍可用。

## Clarifications

### Session 2026-10-25

- Q: 词典数据？ → A: 用 ECDICT（MIT）频次截断丰富字段；保留许可；禁止 COCA。
- Q: 取词展示？ → A: 词典软件风格：词头、词性、形变、多义释义（可多行）。
- Q: 按钮？ → A: 必须支持鼠标点击切换 Word/Unit。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 变形与常见词能查到词典条目 (Priority: P1)

玩家定格后取词模式悬停 `tucked`、`citizens`、`into` 等常见或规则变形词。若词表含对应 lemma 或原形，提示显示中文多义释义（及词性/形变），而不是英文原词回显，也不是无故「无法翻译」。

**Why this priority**: 直接修复截图中的学习断层。

**Independent Test**: 悬停 `tucked`（若表含 `tuck`）见中文条目；悬停表内原形词见释义。

**Acceptance Scenarios**:

1. **Given** 词典含 lemma `tuck` 及形变索引，**When** 悬停 `tucked`，**Then** 显示基于 `tuck` 的词典条目（标明查询形或原形），中文释义非英文回显。
2. **Given** 词典含 `into`，**When** 悬停 `into`，**Then** 显示中文多义/词性信息，不回显 `into`。
3. **Given** 词典与还原后均未命中且扩展翻译开，**When** 机翻结果与源词相同（忽略大小写），**Then** 不得当作成功释义展示（失败或重试策略由 plan 定）。

---

### User Story 2 - 取词提示像简明词典卡片 (Priority: P1)

取词命中时，光标旁提示包含：词头（及必要时「由 xxx 还原」）、词性、主要形变、分义项释义。多行可读，不遮死整屏。

**Why this priority**: 用户明确要求丰富内容。

**Independent Test**: 命中多义词时可见至少词性 + 两条以上义项或分隔清晰的多义文本。

**Acceptance Scenarios**:

1. **Given** 条目含词性与多义，**When** 取词命中，**Then** 提示同时呈现词性与多义（格式可读）。
2. **Given** 条目含形变信息，**When** 取词命中，**Then** 提示展示主要形变或以简明方式列出。
3. **Given** 仅有单行旧式 gloss，**When** 命中，**Then** 仍可显示（降级），不崩溃。

---

### User Story 3 - 点击 Word/Unit 按钮可切换 (Priority: P1)

定格后点击右上角模式按钮，模式在取词/对照段间切换，与快捷键效果一致；点击不穿透到被冻结界面。

**Why this priority**: 快捷键可用但按钮死，属明显缺陷。

**Independent Test**: 仅用鼠标点击按钮两次，标签在 Word↔Unit 间变化。

**Acceptance Scenarios**:

1. **Given** 定格且按钮可见，**When** 左键点击按钮，**Then** 模式切换且标签更新。
2. **Given** 定格，**When** 点击按钮区域，**Then** 不触发底层 GUI 按钮/聊天焦点异常。
3. **Given** 定格，**When** 点击按钮外区域，**Then** 仍不拖动视角/不误触（保持定格输入隔离）。

---

### Edge Cases

- 不规则复数/过去式：优先依赖 ECDICT exchange/索引；启发式还原为辅，失败则机翻或无法翻译。
- 专有名词、模组 id：允许未命中。
- 超长释义：提示框限宽换行，可截断过长行并保留可读性。
- 对照段模式行为不变（含视觉兜底）。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 取词查找 MUST 在大小写归一之外，尝试词形还原或形变索引，使规则变形能命中 lemma 条目。
- **FR-002**: 发布词典 MUST 基于 ECDICT（或同等可再分发来源）频次截断，覆盖高频通用词；MUST 附许可声明；MUST NOT 分发 COCA。
- **FR-003**: 词典条目 MUST 能承载词性、形变、多义释义（字段可由 plan 定）；取词 UI MUST 展示这些信息（有则显示）。
- **FR-004**: 取词成功展示 MUST NOT 仅为与源词相同的英文字符串（忽略大小写）。
- **FR-005**: 定格模式下点击 Word/Unit 浮层按钮 MUST 切换 `LookupMode`，效果同模式热键。
- **FR-006**: 定格鼠标隔离 MUST 保留；仅模式按钮（及既有允许的交互）可消费点击。
- **FR-007**: 对照段/视觉路径 MUST NOT 因本特征回归而破坏既有行为。

### Key Entities

- **LexiconEntry**: headword, partOfSpeech, exchanges/inflections, senseTexts (ZH)
- **LemmaResolution**: surface form → headword + optional note
- **LookupModeHud**: clickable hit box while frozen

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 对词表内 lemma 的常见规则过去式/复数/进行时，取词命中率显著高于「仅精确匹配」（以夹具用例为准，至少覆盖 `-ed`/`-ing`/`-s` 各一例）。
- **SC-002**: 取词命中卡片可见词性或形变或分义（至少一类元数据；有数据时三类尽量都有）。
- **SC-003**: 仅用鼠标点击模式按钮即可完成 Word↔Unit 切换，无需热键。
- **SC-004**: 机翻若回显源词，玩家看不到「英文当释义」的成功态。

## Assumptions

- 继承 007/008 定格、扩展翻译开关、英→中方向。
- ECDICT 包体控制在可接受范围（plan 写截断规模）；构建脚本可生成 TSV/JSON。
- 「像词典」为简明卡片，非完整电子辞典 UI（无发音音频、无联网查词）。
