# Research: 009 freeze word lexicon

## 根因确认

- **Decision**: 将「样例表过小 + 无 lemma + 机翻回显 + mixin 吞点击」四条写入 spec Problem Analysis，作为本 feature 输入。
- **Rationale**: 对照 `entries.tsv`、`LearnerDictionary.lookup`、`OllamaClient` WORD prompt、`MouseLookFreezeMixin.onPress` 与截图一致。
- **Alternatives**: 仅怪罪 Ollama——无法解释按钮问题与表内缺失。

## 词典格式

- **Decision**: 扩展为多列表：`word \t phonetic? \t pos \t exchange \t translation`（缺列可空）。`translation` 可含 ECDICT 式多义（换行或分号）。构建期从 ECDICT CSV 生成；仓库至少提交「高频子集」使 `into`/`tuck` 等可测。
- **Rationale**: ECDICT 已有 pos/exchange/translation；避免自创 schema。
- **Alternatives**: 仅放大单列 gloss——无法满足词性/形变 UI。

## 词形还原

- **Decision**: （1）精确匹配；（2）exchange 反向索引（`d:tucked`→`tuck`）；（3）保守启发式：去 `ies→y`、`es`、`s`、`ing`、`ed` 再查。记录 `resolvedFrom` 供 UI。
- **Rationale**: 无 NLP 依赖；exchange 覆盖不规则；启发式补规则变形。
- **Alternatives**: 嵌入 Snowball stemmer——额外依赖，收益有限。

## 机翻回显

- **Decision**: WORD 结果 `equalsIgnoreCase(source)` → `FAILED`（或专用状态），不画成 GLOSS。
- **Rationale**: 直接消灭截图「显示 into/tucked」的成功假象。
- **Alternatives**: 加大模型——成本高且仍可能回显。

## 按钮点击

- **Decision**: 在 `MouseLookFreezeMixin.mcbilingual$stopButtons` 定格分支中，先换算 GUI 坐标并调用 `LookupModeHud.click`；命中则 toggle 并 cancel；未命中再按原逻辑吞掉点击。可保留 NeoForge Pre 作双保险，但 mixin 为必经路径。
- **Rationale**: Pre 事件可能因 HEAD cancel 从未发出。
- **Alternatives**: 去掉 cancel——会破坏定格隔离。

## 卡片排版

- **Decision**: 纯文本多行：`word` / `pos` / `exchanges` / senses；`FreezeFrames` 已有 `font.split` 换行。
- **Rationale**: 无新 GUI 控件；与现有 hint 一致。
