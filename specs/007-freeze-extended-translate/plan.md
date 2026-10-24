# Implementation Plan: 定格扩展取词与对照段 / 机翻

**Branch**: `007-freeze-extended-translate` | **Date**: 2026-10-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/007-freeze-extended-translate/spec.md`

## Summary

在现有 F3+K 定格（006）上增加两种悬停模式：默认**取词**（英→中，ECDICT 频次截断离线词典，未命中可异步调用本机 Ollama `qwen2.5:1.5b`）；**对照段**模式优先显示语言包翻译单元全文，无对照则按句末标点切单句并用 `qwen2.5:3b`。扩大文字采集以覆盖字面量与 UGC。新增 NeoForge 客户端配置（扩展翻译默认开）。不做 OCR。有对照时不按标点切开语言包段落。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: 现有 `freeze` 包、`LanguageTexts`、`RivalMods`；JDK `HttpClient` 调本机 Ollama；构建期脚本生成 ECDICT 截断资源；NeoForge `ModConfigSpec` 客户端配置。无新的模组硬依赖。

**Storage**: 词典为 jar 内只读资源；机翻结果与模式状态仅内存；配置写入 `config/mcbilingual-client.toml`（名称以实现为准）。

**Testing**: 词典查找、断句、模式优先级用 JUnit（不启游戏）。定格取词/对照段、配置开关、Ollama 有/无用 `./gradlew runClient`（见 quickstart）。

**Target Platform**: Minecraft 1.21.1 客户端。

**Project Type**: 客户端模组

**Performance Goals**: 词典命中悬停提示 < 1s；机翻不阻塞渲染线程；同文本缓存避免重复请求。

**Constraints**: 仅本机 HTTP；固定模型 id；默认开扩展翻译但加载不依赖 Ollama；禁止 OCR；禁止成品字符串反查键；世界投影文字不采集；Rival 冲突模组时整组 freeze 停用。

**Scale/Scope**: 定格 UI 内取词 + 对照段 + 配置 + 离线词典 + 可选 Ollama。不做独立词典 HTTP、不做中→英取词、不合并多键段落。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| Purpose / 学习辅助 | 通过。定格取词与可选本机机翻服务沉浸学习。 |
| I. Client Only | 通过。配置、词典、HTTP 均在客户端。 |
| II. Game packs outside; learner aids may ship | 通过。语言包仍外置；附带 ECDICT 衍生词典；可选本机 Ollama；无 OCR。 |
| III. Own the Behavior | 通过。不依赖 Polyglot/Language Reload。 |
| IV. Two Languages Until Expanded | 通过。仅 zh_cn/en_us；取词仅英→中。 |
| V. Verify in a Client Session | 通过。quickstart 含 Ollama 有/无；词典逻辑有单测。 |

Phase 1 之后复检：仍通过。Complexity Tracking 无例外（GPL/AGPL 词典许可按约束附带 NOTICE）。

## Project Structure

### Documentation (this feature)

```text
specs/007-freeze-extended-translate/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── freeze-extended-translate.md
├── checklists/
│   └── requirements.md
└── tasks.md                 # /speckit-tasks 时再生成
```

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/
├── config/                  # ModConfigSpec 客户端配置
├── dictionary/              # 离线词典加载与查询
├── translate/               # Ollama 客户端、缓存、请求状态
└── freeze/                  # 扩展：模式、字面量采集、词矩形、提示 UI

src/main/resources/
├── data/mcbilingual/dictionary/   # 截断词表 + LICENSE NOTICE
└── assets/mcbilingual/lang/       # 配置与快捷键文案

tools/dictionary/            # 可选：从 ECDICT 生成截断表的构建脚本

src/test/java/pnnh/mcbilingual/
├── dictionary/
├── translate/               # 断句、缓存纯逻辑
└── freeze/
```

**Structure Decision**: 词典与 Ollama 与 `freeze` 解耦，便于单测与配置开关；定格只负责命中与展示。

## Complexity Tracking

无。
