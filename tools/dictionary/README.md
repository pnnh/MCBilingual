# Learner dictionary build

Bundled file: `src/main/resources/data/mcbilingual/dictionary/entries.tsv`

## Row format

`word<TAB>phonetic<TAB>pos<TAB>exchange<TAB>translation` (UTF-8)

- `exchange`: ECDICT-style (`d:tucked/i:tucking/...`)
- `translation`: Simplified Chinese; `\n` escapes for multiple senses
- Lines starting with `#` are comments

Legacy two-column `word<TAB>gloss` rows still load.

## Generate top-N by frequency (010)

Source: [ECDICT](https://github.com/skywind3000/ECDICT) MIT — `ecdict.csv`.

```bash
# Downloads ~66MB CSV into tools/dictionary/.cache/ (gitignored), then writes entries.tsv
python tools/dictionary/build_ecdict_tsv.py --download --limit 60000

# Or reuse a local CSV
python tools/dictionary/build_ecdict_tsv.py --csv /path/to/ecdict.csv --limit 60000
```

Selection rules:

1. Prefer **`frq`** (contemporary rank; smaller = more common). If `frq` empty/0, use **`bnc`**.
2. Require non-empty Chinese `translation`.
3. Keep **single-token** headwords only (no spaces) for freeze word mode.
4. Dedupe case-insensitively; keep the better (lower) rank.
5. Sort by rank ascending; take the first **N** (default 60000).

After filtering, ECDICT may yield slightly under 60k single-token ranked lemmas; the script warns if below 50000.

## Do not ship

Commercial **COCA** lists from wordfrequency.info.

## Runtime

Lookup: exact → exchange index → heuristics → optional Ollama. Keep `LICENSE-ECDICT.txt` next to the TSV.
