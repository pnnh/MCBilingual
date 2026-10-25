# Data Model: 009 freeze word lexicon

## LexiconEntry

| 字段 | 说明 |
| --- | --- |
| `headword` | 规范词头（小写键） |
| `phonetic` | 可选音标 |
| `pos` | 词性串（如 `n. / v.`） |
| `exchange` | ECDICT 形变串或规范化列表 |
| `translation` | 中文释义（可含多义分隔） |

## LemmaResolution

| 字段 | 说明 |
| --- | --- |
| `surface` | 光标下原词 |
| `headword` | 命中词头 |
| `via` | EXACT \| EXCHANGE \| HEURISTIC |
| `entry` | LexiconEntry |

## WordCardText

由 entry + resolution 格式化为多行字符串，供 Hint.GLOSS。

## LookupModeHudHit

`left, top, width, height`（GUI 坐标）；`click` → toggle mode。
