# Implementation Plan: ECDICT 词频前 6 万

**Branch**: `010-ecdict-freq-60k` | **Date**: 2026-10-25 | **Spec**: [spec.md](spec.md)

## Summary

用构建脚本从 ECDICT `ecdict.csv` 按 `frq` 选取 `1..60000` 的 lemma，导出 rich `entries.tsv`（含 exchange），替换样例词表；加载路径沿用 `LearnerDictionary`；更新 README 与许可。

## Technical Context

**Language**: Python 3 构建脚本 + 现有 Java 加载  
**Source**: ECDICT MIT CSV（~66MB）  
**Output**: `src/main/resources/data/mcbilingual/dictionary/entries.tsv`  
**Testing**: 脚本后断言行数；JUnit 抽查 `into`/`tuck` 仍可解析；可选加载 size ≥ 50000  

## Constitution Check

通过：学习者词典可附带；MIT NOTICE；Client only；查找顺序不变。

## Structure

```text
tools/dictionary/build_ecdict_tsv.py
tools/dictionary/README.md
src/main/resources/data/mcbilingual/dictionary/entries.tsv
src/main/resources/data/mcbilingual/dictionary/LICENSE-ECDICT.txt
```

## Selection rule

```text
rank = frq if frq>0 else bnc if bnc>0 else skip
single-token headword + non-blank translation
dedupe by lower(word), keep lowest rank
sort by rank ascending
write first 60000 rows
```

ECDICT 在「单 token + 有 rank」约束下可能略少于 60000；以生成日志为准（010 实测约 57k）。
