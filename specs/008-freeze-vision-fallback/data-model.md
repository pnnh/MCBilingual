# Data Model: 定格视觉兜底与分管道状态提示

## LookupPipeline

| 值 | 含义 |
| --- | --- |
| `PACK` | 语言包对照（即时） |
| `DICT` | 词典命中（即时） |
| `TEXT_MT` | 文本 Ollama（词或句） |
| `VISION_MT` | 视觉 Ollama（ROI） |
| `NONE` | 无可用结果 |

## HoverStatus

| 值 | 玩家可见（概念） | 典型来源 |
| --- | --- | --- |
| `IDLE` | 无提示（不抢焦点） | 鼠标在模式浮窗上 |
| `READY` | 译文/释义 | PACK / DICT / MT OK |
| `TEXT_PENDING` | 文本识别中… | TEXT_MT PENDING |
| `VISION_PENDING` | 图片识别中… | VISION_MT PENDING |
| `UNAVAILABLE` | 无法翻译 | WORD 无串；UNIT 不可视觉或关扩展且无即时结果 |
| `TEXT_FAILED` / `TEXT_TIMEOUT` | 文本失败/超时 | TEXT_MT |
| `VISION_FAILED` / `VISION_TIMEOUT` | 图片失败/超时 | VISION_MT |

## VisionRequest

| 字段 | 说明 |
| --- | --- |
| `frameGeneration` | 本次定格世代，换帧即失效 |
| `roiX/Y/W/H` | 钳制后的像素矩形 |
| `cacheKey` | generation + 量化 ROI（或内容哈希） |
| `imageBytes` | PNG/JPEG，仅请求时持有，不长期占内存 |

## MachineTranslationCache 扩展

现有 `Kind`: `WORD` | `SENTENCE`。新增：

| Kind | 模型 | source 键 |
| --- | --- | --- |
| `VISION` | `qwen2.5vl:3b` | `cacheKey`（非自然语言原文） |

`Job.status`: 仍用 `PENDING` | `OK` | `FAILED` | `TIMEOUT`；调用方按 `Kind` 映射到 HoverStatus。

## 配置

| 项 | 默认 | 说明 |
| --- | --- | --- |
| `extendedTranslation` | `true` | 关则不发 TEXT_MT / VISION_MT |
| `requestTimeoutMs` | 既有 | 文本；可选兼视觉 |
| `visionTimeoutMs` | 可选，更长 | 若实现独立超时 |
| `ollamaBaseUrl` | `http://127.0.0.1:11434` | 本机 |

## 状态转换（UNIT 无串）

```text
悬停无 VisibleSpan
  → extended off → UNAVAILABLE
  → extended on → VISION_PENDING → READY | VISION_FAILED | VISION_TIMEOUT
```

## 状态转换（有串文本 MT）

```text
词典/包未命中且需 MT
  → TEXT_PENDING → READY | TEXT_FAILED | TEXT_TIMEOUT
```
