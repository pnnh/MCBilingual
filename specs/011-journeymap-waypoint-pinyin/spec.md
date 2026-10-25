# Feature Specification: JourneyMap 路径点拼音搜索

**Feature Branch**: `011-journeymap-waypoint-pinyin`

**Created**: 2026-10-25

**Status**: Code complete / Implemented

**Input**: JourneyMap「管理路径点」搜索框支持对用户自填中文路径点名的拼音/首字母匹配；名称非 i18n，可能是中文、英文或混合。软依赖 JourneyMap；未安装时模组照常运行。

## Clarifications

### Session 2026-10-25

- Q: 名称来源？ → A: 玩家自填字符串，**不**经语言包。
- Q: 拼音库？ → A: 复用现有 TinyPinyin / `PinyinTokens`（无调全拼 + 首字母；多音字第一读音）。
- Q: 接入方式？ → A: JourneyMap 无公开搜索钩子 → 可选依赖 + Mixin 扩展过滤（或等价注入）；官方 API 仅作辅助（列举路径点），不能替代 UI 过滤挂钩。
- Q: 分组名？ → A: 本特征仅路径点**名称**；分组名搜索为非目标（可后续 feature）。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 用拼音找到中文路径点 (Priority: P1)

玩家打开 JourneyMap 管理路径点界面，在搜索框输入全拼或首字母（如 `zhituta` / `ztt` 对应「制图塔」），列表筛出该中文路径点；输入路径点中的汉字原文仍按 JourneyMap 原逻辑命中。

**Why this priority**: 用户截图场景的核心价值。

**Independent Test**: 建名为「制图塔」的路径点，搜 `ztt` 或全拼可见；未装 JM 时游戏可进世界。

**Acceptance Scenarios**:

1. **Given** JM 已装且存在中文名路径点，**When** 输入其无调全拼子串，**Then** 该点出现在过滤结果中。
2. **Given** 同上，**When** 输入其拼音首字母串，**Then** 该点出现在过滤结果中。
3. **Given** 同上，**When** 输入名称中的汉字，**Then** 仍能命中（不破坏原搜索）。

---

### User Story 2 - 英文名与无 JM 时不坏事 (Priority: P1)

纯英文路径点行为与 JM 原版一致；未安装 JourneyMap 时 MCBilingual 不报错、不加载 JM Mixin。

**Acceptance Scenarios**:

1. **Given** 路径点名为英文，**When** 按英文子串搜索，**Then** 与未装本特征时一致。
2. **Given** 未安装 JourneyMap，**When** 启动客户端，**Then** 模组正常，无 JM 相关崩溃。

---

### User Story 3 - 拼音不出现在地图 UI 文案 (Priority: P2)

拼音仅用于搜索匹配，不改路径点显示名称、不写入路径点数据文件。

**Acceptance Scenarios**:

1. **Given** 中文路径点，**When** 浏览列表（无搜索或搜索后），**Then** 显示名仍为玩家填写的原文，不见拼音串。

---

### Edge Cases

- 中英混合名：仅对其中汉字生成拼音；英文部分仍靠子串。
- 无汉字名称：不生成拼音；仅原串匹配。
- 大小写：查询按小写比较（与物品搜索一致）。
- JourneyMap 大版本改包名/方法：Mixin 可能失效 → 应安全跳过（不崩溃），并在任务/文档标明目标 JM 版本。
- 死亡点、各分组内路径点：凡进入同一搜索列表的名称均适用。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 当 JourneyMap 管理路径点搜索过滤路径点名时，MUST 在原有名称子串匹配之外，支持对该名称的无调全拼与首字母匹配。
- **FR-002**: 拼音 MUST 由路径点显示名称字符串即时或缓存生成，MUST NOT 要求 i18n key。
- **FR-003**: JourneyMap MUST 为可选依赖；缺失时 MUST NOT 加载依赖 JM 类型的代码路径（Mixin 配置分离或等价）。
- **FR-004**: 拼音 MUST NOT 写入路径点存档，MUST NOT 替换列表中显示的名称。
- **FR-005**: 匹配规则 MUST 与现有 `PinyinTokens` / 物品搜索语义一致（无调、首字母、第一读音）。
- **FR-006**: Mixin 或注入失败时 MUST 降级为 JM 原过滤，MUST NOT 因此崩溃客户端。

### Key Entities

- **WaypointName**: 玩家可见名称字符串
- **PinyinReading**: full + initials（来自 `PinyinTokens`）
- **WaypointFilterQuery**: 搜索框当前文本（小写）

## Success Criteria *(mandatory)*

- **SC-001**: 至少一个中文路径点可用首字母或全拼在管理界面搜到。
- **SC-002**: 汉字原文搜索仍可用。
- **SC-003**: 无 JourneyMap 时客户端可正常进世界。
- **SC-004**: 列表显示名无拼音泄漏。

## Assumptions

- 目标：Minecraft 1.21.1 + 当前模组使用的 NeoForge；JourneyMap 版本在 plan 中钉死（dev `localRuntime`）。
- 复用 `pnnh.mcbilingual.search.PinyinTokens`，不新增拼音库。
- 不实现分组名拼音、全屏地图其它搜索框（除非同一过滤函数天然覆盖）。
