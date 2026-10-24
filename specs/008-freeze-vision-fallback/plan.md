# Implementation Plan: 定格视觉兜底与分管道状态提示

**Branch**: `008-freeze-vision-fallback` | **Date**: 2026-10-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/008-freeze-vision-fallback/spec.md`

## Summary

在 007 定格扩展翻译之上：当对照段模式（`LookupMode.UNIT`）下光标处**无绘制管线可见字符串**时，裁剪定格帧 ROI，异步调用本机 Ollama **`qwen2.5vl:3b`**，一次得到**短段落简体中文**译文并缓存（扩展翻译暂定英→中）。取词模式无串不走视觉。悬停提示按管道区分「文本识别中…」「图片识别中…」「无法翻译」及文本/图片失败超时。模组 jar **不**捆绑 OCR。

**粒度锁定（实现选型）**：单次视觉调用 = 光标附近固定像素 ROI 内的整块画面识别+翻译（短段落语义）；不做 OCR 后再断句，也不做视觉取词。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: 现有 `freeze` / `translate` / `config`（007）；JDK `HttpClient` 调 Ollama `/api/generate`（或 chat）并附 base64 图像；定格帧缓冲（006 `FreezeFrames`）。无新模组硬依赖、无原生 OCR。

**Storage**: 视觉结果内存缓存（ROI 哈希或坐标+帧世代键）；配置沿用 `extendedTranslation`（可选独立 timeout 覆盖视觉更长等待）。

**Testing**: ROI 键/状态机/语言键映射用 JUnit（不启游戏）。视觉有/无、文本 vs 图片文案用 `./gradlew runClient`（quickstart）。

**Target Platform**: Minecraft 1.21.1 客户端。

**Project Type**: 客户端模组

**Performance Goals**: 文本路径状态立即切换为「文本识别中」；视觉路径立即「图片识别中」；视觉超时默认 ≥ 文本超时（建议默认 60s 可配置或共用 timeout 但文案标明图片）；渲染线程只读 Job 状态。

**Constraints**: 仅本机；固定 `qwen2.5vl:3b`；禁用时不发视觉请求；WORD 模式无视觉；扩展翻译暂定英→中；世界投影不采不视觉与 Rival 停用继承 006（008 不单独立项回归）；发布包无 OCR 原生库。

**Scale/Scope**: UNIT 无串视觉兜底 + 全管道悬停状态文案。不做视觉取词、不做 Tessseract、不开放任意 VL 模型。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| Purpose / 学习辅助 | 通过。视觉兜底服务无绘制串 UI，仍为学习辅助。 |
| I. Client Only | 通过。ROI 裁剪与 Ollama 均在客户端。 |
| II. Learner aids / VL / 禁 OCR / 管道状态 | 通过。宪法 **v2.1.0** 已允许本机 VL；禁止 jar OCR；要求文本/图片状态区分。本 feature 落实该条。 |
| III. Own the Behavior | 通过。自有实现，无 Polyglot/Language Reload 依赖。 |
| IV. Two Languages | 通过。界面双语仍限 zh_cn/en_us；扩展 MT/VL 暂定英→中。 |
| V. Verify in Client Session | 通过。quickstart 含 VL 有/无与文案区分。 |

Phase 1 之后复检：仍通过。无 Complexity Tracking 例外。

## Project Structure

### Documentation (this feature)

```text
specs/008-freeze-vision-fallback/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── freeze-vision-fallback.md
├── checklists/
│   └── requirements.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/
├── translate/
│   ├── OllamaClient.java              # VISION kind + 图像 payload；英→中 prompt
│   ├── MachineTranslationCache.java   # Kind.VISION
│   └── VisionRoi.java                 # ROI 几何与缓存键（纯逻辑）
├── freeze/
│   ├── FreezeFrames.java              # 定格像素副本；WORD/UNIT 提示；浮窗早退
│   ├── FreezeVisionCapture.java       # UNIT 无串 → 裁 ROI → requestVision
│   ├── UnitLookup.java                # 有串：包对照 / 文本句机翻（不发视觉）
│   ├── WordLookup.java / LookupStatusKeys.java
│   └── LookupModeHud.java             # 模式浮窗（悬停不抢识别提示）
└── config/ClientConfig.java           # visionTimeoutMs 等

src/main/resources/assets/mcbilingual/lang/
├── en_us.json   # text recognizing / image recognizing / pipeline errors
└── zh_cn.json

src/test/java/pnnh/mcbilingual/
├── translate/VisionRoiTest.java
└── freeze/LookupStatusMappingTest.java   # 管道 → 语言键
```

**Structure Decision**: 视觉请求放在 `translate`（与文本 Ollama 同客户端），定格只负责「何时裁 ROI、展示哪条状态」。

## Complexity Tracking

无。
