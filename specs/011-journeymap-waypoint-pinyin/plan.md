# Implementation Plan: JourneyMap 路径点拼音搜索

**Branch**: `011-journeymap-waypoint-pinyin` | **Date**: 2026-10-25 | **Spec**: [spec.md](spec.md)

## Summary

在 JourneyMap「管理路径点」列表过滤处注入拼音匹配：对路径点名称用现有 `PinyinTokens` 生成无调全拼与首字母；查询命中原名子串 **或** 全拼/首字母子串即保留。JourneyMap 为 `compileOnly` + `localRuntime` 软依赖；Mixin 放入独立 `mcbilingual-journeymap.mixins.json`（`required: false` 或仅当 JM 存在时加载）。

## Technical Context

**Language/Version**: Java 21，MC 1.21.1，NeoForge 21.1.251  

**Primary Dependencies**: 现有 `PinyinTokens` / TinyPinyin；JourneyMap（可选，版本在 `gradle.properties` 钉死，优先 Modrinth/Curse Maven）。  

**Storage**: 可选内存缓存 `name → Reading`（会话级）；不写 JM 存档。  

**Testing**: `PinyinTokens` / 新 `WaypointNameMatch` 纯 JUnit；UI 用 runClient + JM 手测。  

**Constraints**: 拼音不上 UI 文案；无 JM 不崩溃；不修改路径点数据。

## Constitution Check

| 原则 | 结果 |
| --- | --- |
| I Client Only | 通过 |
| III Own Behavior | 通过；不依赖 Polyglot；JM 可选 |
| IV Spec 开集成 | 通过；本 feature 明确把 JM 路径点搜索列入范围 |
| Pinyin search-only | 通过；FR-004 |
| V Client verify | quickstart 含 JM 有/无 |

## Project Structure

```text
specs/011-journeymap-waypoint-pinyin/
├── plan.md, research.md, data-model.md, quickstart.md
├── contracts/journeymap-waypoint-pinyin.md
└── tasks.md

src/main/java/pnnh/mcbilingual/
├── search/WaypointNameMatch.java     # 纯逻辑：query vs name (+ pinyin)
└── integration/journeymap/
    └── mixin/…                       # 挂钩 JM 过滤（类名 research 钉死）

src/main/resources/
└── mcbilingual-journeymap.mixins.json

src/test/java/pnnh/mcbilingual/search/WaypointNameMatchTest.java
```

## Integration approach

1. Dev 依赖引入 JourneyMap jar，反编译/映射定位「搜索框文本过滤路径点列表」的方法。  
2. Mixin：在「名称是否匹配 query」判断处 OR 上 `WaypointNameMatch.matches(query, name)`。  
3. 若过滤是 `String.contains` 内联多处，优先抽到单一 helper 的调用点；否则注入最上层 filter。  
4. `neoforge.mods.toml`：`journeymap` optional CLIENT。
