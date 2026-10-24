# Research: 定格视觉兜底与分管道状态提示

## 粒度：短段落 ROI vs 视觉后再断句

- **Decision**: 一次 VL 调用处理光标附近**固定大小 ROI**（短段落语义）；模型直接返回译文。不做「先 OCR 出字再 SentenceBounds」。
- **Rationale**: 实现更简单；无串场景往往是整块自绘字；再断句需可靠 OCR 文本，与「禁 jar OCR」冲突。
- **Alternatives**: 视觉取词——误识率高、规格禁止；多 ROI 拼接——复杂且慢。

## 何时触发视觉

- **Decision**: 仅 `LookupMode.UNIT` 且鼠标下无 `VisibleSpan`/可用主语言串，且 `extendedTranslation` 开。`WORD` 无串 → 「无法翻译」，不发视觉。
- **Rationale**: 规格 Edge Cases；取词模式不应变成慢速识词。
- **Alternatives**: 两模式都视觉——等待更长且与「不做视觉取词」冲突。

## Ollama VL API

- **Decision**: 使用本机 Ollama 图像能力（`/api/generate` 或 chat，images 字段 base64 PNG/JPEG）；模型固定 `qwen2.5vl:3b`；与文本请求共用 `HttpClient` 线程池或独立单线程队列，避免阻塞渲染。
- **Rationale**: 玩家已接受 Ollama；qwen2.5 **文本**模无视觉；须用 vl 变体。
- **Alternatives**: 系统 OCR → 文本模型——违反宪法 II / 分发约束。

## ROI 与缓存键

- **Decision**: 以光标为中心裁矩形（plan 实现定默认边长，如 256–384px，钳制在帧内）；缓存键 = 定格世代 id + 量化后的 ROI 坐标（或 ROI 像素哈希）。同键复用 Job。
- **Rationale**: 悬停微抖不重复打满模型；换定格帧必须失效。
- **Alternatives**: 仅坐标键无世代——二次定格可能串结果。

## 状态文案映射

- **Decision**:
  - 文本 MT `PENDING` → `mcbilingual.freeze.status.text_recognizing`
  - 视觉 MT `PENDING` → `mcbilingual.freeze.status.image_recognizing`
  - 无结果路径 → `cannot_translate`（与 007 对齐或复用）
  - FAILED/TIMEOUT → 带管道后缀的失败/超时键（或同一键 + 参数）
  - 词典/语言包命中 → 直接结果，无 PENDING 文案
- **Rationale**: 宪法 II.1 心理预期；文本与视觉耗时差大。
- **Alternatives**: 统一「加载中」——规格明确拒绝。

## 超时

- **Decision**: 视觉请求可复用 `requestTimeoutMs`，若实测不足则加 `visionTimeoutMs`（默认更长，如 60000）。失败每次悬停显示错误直至关扩展翻译。
- **Rationale**: VL 明显慢于 1.5b/3b 文本。
- **Alternatives**: 无限等——卡死预期。

## 与 007 状态骨架

- **Decision**: 扩展现有 `MachineTranslationCache.Job` / `HintKind`，将笼统 LOADING 拆成 TEXT vs IMAGE（或 Hint 携带 pipeline）。
- **Rationale**: 少造平行系统。
- **Alternatives**: 全新状态机——重复劳动。
