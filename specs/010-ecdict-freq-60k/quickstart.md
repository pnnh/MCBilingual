# Quickstart: 010

1. 确认 `entries.tsv` 约 5–6 万行（非注释）。
2. `./gradlew test --tests pnnh.mcbilingual.dictionary.BundledLexiconScaleTest`
3. `runClient`：定格取词悬停常见词应**即时**出卡片（不必等 Ollama）。
4. 重建词表：`python tools/dictionary/build_ecdict_tsv.py --download --limit 60000`
