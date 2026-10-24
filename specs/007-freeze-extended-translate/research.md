# Research: 定格扩展取词与对照段 / 机翻

## 与 006 的关系

- **Decision**: 保留 006 的定格截帧、输入隔离、翻译组件对照解析与世界文字排除。007 扩展：（1）字面量/UGC 也产生可命中可见串；（2）LookupMode；（3）词典与可选 Ollama。冲突的 006 FR（禁机翻、禁 UGC 提示）以 007 为准。
- **Rationale**: 产品定位改为学习辅助；宪法 v2.0.0 已允许学习者词典与本机机翻。
- **Alternatives**: 全新定格栈——成本高且无必要。

## 有语言包时整段 vs 单句

- **Decision**: 对照段模式对 `TranslatableContents` 显示对照语言**翻译单元全文**。标点切句仅用于无对照的机翻。
- **Rationale**: 任务说明等多句共一键；切句会破坏对照且比现有 `ComponentSentences` 更难。
- **Alternatives**: 始终切单句——伤害有包文案体验。

## 词典数据

- **Decision**: 构建时从 ECDICT（MIT）按 `frq`/`bnc` 截断高频词，打包精简 `word → translation`；附 LICENSE。禁止商业 COCA 再分发。
- **Rationale**: 可再分发、含释义与频次；包体可控。
- **Alternatives**: 运行时 HTTP 词典——增加部署；完整 ECDICT 过大。

## 取词命中

- **Decision**: 在可见英文字符串上按空白/标点分词，用 `Font.width` 累计得到词矩形；仅英→中。
- **Rationale**: 无 OCR 时必须有字符串；英文分词足够 MVP。
- **Alternatives**: 整行当一词——学习价值低。

## Ollama 集成

- **Decision**: JDK `HttpClient` 异步访问 `http://127.0.0.1:11434`；单词 `qwen2.5:1.5b`，句子 `qwen2.5:3b`；配置默认开；失败每次悬停报错直至关闭；渲染线程只读缓存/状态。
- **Rationale**: 固定模型便于提示词；可选符合宪法；用户已接受错误噪音策略。
- **Alternatives**: 默认关——与产品「扩展默认可用」不符；开放任意模型——难调优。

## 客户端配置

- **Decision**: NeoForge `ModConfigSpec` + `CLIENT` 类型；至少 `extendedTranslation`（或同名）布尔，默认 `true`。
- **Rationale**: 生态标准，无需 Cloth Config 硬依赖。
- **Alternatives**: 仅热键开关——无法持久关闭错误提示。

## 采集范围

- **Decision**: 在现有 Font 采集上增加字面量/`String` 路径的可见串与矩形，供 UGC 与无键文本；仍排除世界投影绘制阶段。
- **Rationale**: 规格要求 UGC 可机翻且禁止 OCR。
- **Alternatives**: OCR——本特征明确排除。
