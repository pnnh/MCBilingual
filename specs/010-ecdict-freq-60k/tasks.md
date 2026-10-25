# Tasks: ECDICT 词频前 6 万

- [x] T001 编写 `tools/dictionary/build_ecdict_tsv.py`（按 frq/bnc 排序取 top N）
- [x] T002 更新 `tools/dictionary/README.md` 构建步骤与许可
- [x] T003 运行脚本生成 `entries.tsv`（实际约 57k 单词头；rank 1..50000 段）
- [x] T004 [P] JUnit：`BundledLexiconScaleTest` size ≥ 50000；`into`/`tucked`/`the`
- [x] T005 确认 `LearnerDictionary` 流式加载无需改 API；`.cache/` gitignore
- [x] T006 更新 spec Status；Ollama 备忘指向 010

## Notes

硬截 `frq≤60000` 时源表单 token 仅约 42k；改为「有词频的单 token 按 rank 排序取前 60000」，得到 **56992** 条（ECDICT 单 token 有 rank 的上限附近）。
