# Learner dictionary build

Bundled file: `src/main/resources/data/mcbilingual/dictionary/entries.tsv`

## Row formats

1. **Legacy**: `english_word<TAB>chinese_gloss`
2. **Rich (preferred)**: `word<TAB>phonetic<TAB>pos<TAB>exchange<TAB>translation`

- `exchange`: ECDICT-style, e.g. `d:tucked/p:tucked/i:tucking/3:tucks`
- `translation`: Simplified Chinese; use `\n` in the field for multiple senses
- Lines starting with `#` are comments
- Encoding: UTF-8

## Recommended source

Use [ECDICT](https://github.com/skywind3000/ECDICT) (MIT). Filter by `frq` / `bnc`, keep top ~50k–60k lemmas, map columns to the rich TSV. Keep `LICENSE-ECDICT.txt`.

## Do not ship

Commercial **COCA** lists from wordfrequency.info.

## Runtime behaviour (009)

Lookup order: exact headword → exchange reverse index → conservative heuristics (`-ed`/`-ing`/`-s`…). UI shows a compact card (headword, pos, forms, senses).
