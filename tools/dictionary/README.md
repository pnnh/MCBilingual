# Learner dictionary build

Bundled file: `src/main/resources/data/mcbilingual/dictionary/entries.tsv`

Format: one entry per line, `english_word<TAB>chinese_gloss`, UTF-8. Lines starting with `#` are comments.

## Recommended source

Use [ECDICT](https://github.com/skywind3000/ECDICT) (MIT). Sort or filter by `frq` / `bnc` and keep the top ~50k–60k lemmas with a short `translation` field.

Example outline:

1. Download `ecdict.csv` from ECDICT releases.
2. Keep rows with a numeric frequency rank under your cutoff.
3. Write `word` and a single-line Chinese gloss into `entries.tsv`.
4. Keep `LICENSE-ECDICT.txt` next to the TSV.

## Do not ship

Commercial **COCA** word-frequency products from wordfrequency.info — their license forbids redistributing the lists outside the purchasing organisation.

## Current repo

`entries.tsv` starts as a curated common-UI sample so clients can verify word mode without a full ECDICT import. Replace it with a full truncated ECDICT export before a wide release.
