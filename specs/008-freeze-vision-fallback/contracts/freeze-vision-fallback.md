# Contract: 定格视觉兜底与分管道状态

## 范围

客户端行为契约。无对外 HTTP API（除玩家本机 Ollama）。

## 触发条件（视觉）

```text
GIVEN FreezeSession.active()
  AND LookupMode == UNIT
  AND extendedTranslation == true
  AND cursor 下无可用 VisibleSpan / 主语言绘制串
  AND RivalMods 未禁用 freeze
WHEN 悬停持续
THEN 发起至多一个 VISION Job（同 cacheKey 复用）
  AND 提示文案管道 == IMAGE
```

```text
GIVEN LookupMode == WORD
  AND 无 VisibleSpan
THEN 不发起 VISION
  AND 可显示 UNAVAILABLE（无法翻译）
```

## Ollama 视觉请求（本机）

- Endpoint: 配置的 base URL + Ollama 图像生成接口（实现写死路径）
- Model: **`qwen2.5vl:3b`** 仅
- Body: 含 ROI 图像（base64）与「识别并译为简体中文」的固定提示词（扩展翻译暂定英→中）
- 禁止：向 `qwen2.5:1.5b` / `qwen2.5:3b` 送图

## 悬停文案契约

| 条件 | 必须出现的语言键（或等价） | 禁止出现 |
| --- | --- | --- |
| TEXT_MT PENDING | `…status.text_recognizing` | image_recognizing |
| VISION_MT PENDING | `…status.image_recognizing` | text_recognizing |
| 词典/包即时命中 | 结果文本 | 任一 recognizing |
| 扩展关且无即时结果 | 无法翻译或不提示（与 007 对齐） | 发起 HTTP |
| VISION FAILED/TIMEOUT | 图片管道失败/超时键 | text_recognizing |
| 鼠标在模式浮窗 | 不抢焦点显示 recognizing | — |

## 缓存

- 同 `Kind.VISION` + 同 `cacheKey` → 同一 Job
- 新定格世代 → 旧 VISION 缓存条目可清除或忽略

## 非目标

- jar 内 OCR 库
- WORD 模式视觉取词
- 云端推理 endpoint
