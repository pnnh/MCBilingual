# Data Model: 007-freeze-extended-translate

## LookupMode

| Field | Meaning |
| --- | --- |
| WORD | 默认。悬停显示英文词释义。 |
| UNIT | 对照段。有包显示翻译单元全文；无包则机翻单句。 |

定格会话内可变；结束定格可重置为 WORD。

## VisibleSpan

| Field | Meaning |
| --- | --- |
| text | 主语言可见字符串 |
| bounds | GUI 像素矩形（可拆成多块） |
| order | 绘制序号，重叠取最上 |
| unit | 可选：关联翻译组件解析出的对照全文 |
| source | PACK_UNIT / LITERAL / UNKNOWN |

无 `text` 时不得假装有词；提示「无法翻译」。

## WordHit

| Field | Meaning |
| --- | --- |
| word | 规范化英文词形（查找键） |
| bounds | 该词矩形 |
| span | 所属 VisibleSpan |

## DictionaryEntry

| Field | Meaning |
| --- | --- |
| lemma | 英文词 |
| gloss | 精简中文释义 |

只读；来自 jar 资源。

## MachineTranslationJob

| Field | Meaning |
| --- | --- |
| kind | WORD / SENTENCE |
| source | 源文本 |
| status | PENDING / OK / FAILED / TIMEOUT |
| result | 成功时的译文 |
| error | 失败文案键或原文 |

同 `kind+source` 缓存；UI 只展示当前悬停对应 job。

## ClientConfig

| Field | Meaning |
| --- | --- |
| extendedTranslation | 是否允许 Ollama；默认 true |

可扩展：超时毫秒、base URL（高级，默认本机）。
