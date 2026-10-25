# Data Model: JourneyMap 路径点拼音

## WaypointNameMatch

| 输入 | 说明 |
| --- | --- |
| `query` | 搜索框文本，比较前 `strip` + lower |
| `name` | 路径点显示名原文 |

| 规则 | 命中条件 |
| --- | --- |
| 原文 | `name.lower()` 包含 `query` |
| 全拼 | `PinyinTokens.from(name).full` 包含 `query`（仅当有汉字） |
| 首字母 | `initials` 包含 `query` |

`query` 为空：视为不过滤（由 JM 决定；我们的 matcher 对空 query 返回 true）。

## PinyinReadingCache（可选）

| 字段 | 说明 |
| --- | --- |
| key | 路径点名字符串 |
| reading | `PinyinTokens.Reading` |

会话内存；名称变更时下次查找未命中则重算。
