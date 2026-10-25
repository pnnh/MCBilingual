# Implementation Plan: 定格取词词典增强与模式按钮点击

**Branch**: `009-freeze-word-lexicon` | **Date**: 2026-10-25 | **Spec**: [spec.md](spec.md)

## Summary

修复定格取词「常见词/变形无释义或回显英文」：导入 ECDICT 频次截断丰富条目、词形还原/形变索引、词典卡片式提示；机翻结果若等于源词视为失败。修复 `MouseLookFreezeMixin` 吞掉点击导致 Word/Unit 按钮无效：在定格 `onPress` 路径显式处理 HUD 点击后再取消其余鼠标。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: 现有 `dictionary` / `freeze` / `translate`；ECDICT 衍生资源；可选构建脚本扩表。

**Storage**: jar 内词典资源（TSV 或分段：word + pos + exchange + translation）；内存索引 surface→lemma。

**Testing**: JUnit：lemma 还原、条目格式化、回显拒绝；按钮逻辑可用纯坐标 hit 测。runClient 验按钮与取词卡片。

**Target Platform**: 客户端模组

**Constraints**: 英→中；无 COCA；不破坏 008 视觉；定格仍隔离输入。

**Scale/Scope**: 取词路径 + HUD 点击；不重做对照段/视觉。

## Constitution Check

| 原则 | 结果 |
| --- | --- |
| I Client Only | 通过 |
| II Learner aids / ECDICT / 无 OCR | 通过。丰富词典属学习辅助；附 MIT 声明 |
| III Own Behavior | 通过 |
| IV Two languages / 英→中 | 通过 |
| V Client verify | quickstart 含按钮点击与变形取词 |

## Project Structure

```text
specs/009-freeze-word-lexicon/
├── plan.md, research.md, data-model.md, quickstart.md
├── contracts/freeze-word-lexicon.md
└── tasks.md

src/main/java/pnnh/mcbilingual/
├── dictionary/   # LexiconEntry, lemma index, richer load
├── freeze/       # WordLookup card format; MouseLookFreezeMixin HUD click
└── translate/    # reject echo results for WORD

src/main/resources/data/mcbilingual/dictionary/
tools/dictionary/ # ECDICT → rich TSV generator (expand)
```

## Complexity Tracking

无。
