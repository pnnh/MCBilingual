# Contract: freeze word lexicon

## Dictionary lookup

```text
lookup(surface) →
  normalize → exact entry?
  else exchangeIndex(surface)?
  else heuristicLemmas(surface) first hit?
  else empty
```

## Word card (non-empty entry)

MUST include headword line.  
SHOULD include pos / exchange / multi-sense translation when present.  
MUST NOT be only `surface` Latin letters equal to source with no Chinese.

## MT echo rejection

```text
WHEN Kind.WORD AND result.equalsIgnoreCase(source)
THEN status FAILED (not OK)
```

## Mode button

```text
GIVEN freeze active
WHEN left mouse press on LookupModeHud bounds (GUI space)
THEN toggle LookupMode AND consume click (no look/drag)
```
