<!--
Sync Impact Report
- Version change: 2.0.0 → 2.1.0
- Modified principles:
  - II. Game Language Packs Outside; Learner Aids May Ship
    (allows optional local Ollama vision fallback; forbids bundling OCR engines;
     requires distinct player-visible status for text vs image pipelines when specified)
  - Purpose → mentions optional vision fallback
  - IV → points at 007/008 plan notes
- Added sections: none
- Removed sections: none
- Follow-up TODOs:
  - Feature 008: freeze vision fallback + status hints (Spec Kit)
-->

# MCBilingual Constitution

## Purpose

MCBilingual is a **client-side bilingual learning aid** for Minecraft.
Players SHOULD be able to play with a non-native primary language, meet
high-frequency text in context, and get help with item and UI proper names
in a second language.

Core display still prefers game and mod language packs already on the client.
The mod MAY add learner aids that go beyond pack lookup: a bundled general
dictionary, freeze-mode word and sentence lookup, optional local machine
translation for text that has no pack counterpart (including player UGC), and
— when a feature specification says so — an optional local **vision-model**
fallback for UI that never exposes draw-path strings.

## Core Principles

### I. Client Only

MCBilingual MUST run only on the Minecraft client. A dedicated server MUST NOT
need this mod installed. The mod MUST NOT change server rules, world data, or
the network protocol.

Rationale: display, search, language switching, dictionary lookup, and local
model calls are presentation and client tooling. Players install the mod
locally, including any optional Ollama setup on their machine.

### II. Game Language Packs Outside; Learner Aids May Ship

**Game and mod UI language packs** (for example content from I18nUpdateMod3
and Minecraft-Mod-Language-Package) MUST NOT be shipped inside this mod. The
mod MUST read those files when resource packs or other mods already supply
them. Those projects MAY be present at development time or at runtime. They
MUST NOT be compile or publication dependencies.

Source code from I18nUpdateMod3 MUST NOT be copied into this repository.
Translation packs remain external data sources, not a vendored code base.

**Learner aids MAY ship with the mod**, when a feature specification says so:

- A general-purpose bilingual learner dictionary (for example an ECDICT-derived
  list truncated by frequency), with license notices in the distribution.
- Freeze-mode lookup that shows pack counterparts for a whole **translation
  unit** (one translatable component / key, which MAY contain several
  linguistic sentences).
- Optional **local** machine translation (Ollama on the player machine) for
  text with no pack counterpart, including player chat, renamed items, and
  handwritten book pages, under rules set by the feature specification.
- Optional **local vision-model** fallback (Ollama VL on the player machine)
  when the client draw path yields no usable primary-language string. The mod
  MAY crop and send a freeze-frame region; the model MAY read and translate
  that region. Vision fallback MUST stay sentence- or short-paragraph-scoped
  as defined by the feature specification, MUST NOT require a native OCR
  engine inside the published jar, and MUST remain disableable with other
  extended-translation settings.

Missing pack text MUST degrade to the available language, a dictionary or
model result, an explicit in-progress status, or an explicit player-visible
failure such as “无法翻译”. It MUST NOT crash the client. Item and tooltip
secondary lines MUST NOT surface a raw translation key as the second language.

When extended lookup is in progress or fails, the freeze UI MUST tell the
player **which pipeline** is running or failed (for example text recognition
versus image recognition versus unavailable), so wait-time expectations match
the path. Exact strings are set by the feature specification.

A feature specification that enables vision fallback supersedes the older rule
that forbade guessing from pixels. Until that feature exists, draw-path misses
MUST NOT call a vision model.

Rationale: pack text stays maintainable outside the mod; learner dictionary
and optional local MT/VL support immersion without shipping language packs or
OS-specific OCR binaries in the CurseForge/Modrinth jar.

### III. Own the Behavior

MCBilingual MUST NOT require PolyglotTooltip or Language Reload to load or to
deliver its features.

PolyglotTooltip `main` is a NeoForge 1.21.1 reference (NeoForge 21.1.219).
Its behavior MAY be reimplemented against this project's NeoForge 21.1.251
toolchain. Its sources MUST NOT be vendored into the published mod, and the
mod MUST NOT declare it as a dependency.

Language Reload branch `1.21.1` is Fabric-only. Its ideas MAY be
reimplemented on NeoForge. Fabric mixins, Yarn names, and Fabric API calls
MUST NOT be copied in as a port.

`thirdpart/` is a local reference checkout. It MUST stay gitignored.

### IV. Two Languages Until Expanded

Until a later feature specification explicitly expands the set, the mod MUST
support only Simplified Chinese (`zh_cn`) and English (`en_us`) for primary
display, pack counterparts, learner dictionary direction, and extended
translation (including vision fallback output).

A feature MUST stay inside its specification. Wider bilingual surfaces, font
scaling, display-mode cycling, and extra mod integrations remain out of
scope until a spec adds them. Freeze-mode word lookup, sentence or unit
lookup, bundled dictionary, client config, optional Ollama text models, and
optional Ollama vision fallback are in scope only when specified (see
`docs/plan/Ollama集成.md` and `specs/007-*` / `specs/008-*`).

### V. Verify in a Client Session

A feature that changes what the player sees is not complete until that flow
has been exercised in a client session. `./gradlew runClient` is the default
check. A packaged jar in a full modpack is the later compatibility check, not
a substitute for the daily client check.

Pure text transforms, such as pinyin or dictionary lookup over fixtures, MUST
have an automated test that does not start the game. Player-visible claims
MUST NOT be marked done from a compile alone. Optional Ollama text and vision
paths MUST each be checked at least once with the service present and once
with it absent when that feature is implemented.

## Additional Constraints

- Minecraft 1.21.1, NeoForge 21.1.251, Java 21.
- Mod id `mcbilingual`, package `pnnh.mcbilingual`, client dist only.
- Optional integrations (JEI, Jade) MUST load as optional. The mod MUST still
  reach a world when they are absent.
- Pinyin is search text only. It MUST NOT appear in tooltips or Jade.
- Switching the displayed primary language MUST NOT run a full resource-pack
  reload.
- Client-tunable behavior (including enabling or disabling optional Ollama
  extended translation and vision fallback) MUST use a NeoForge client config
  when that feature is implemented.
- GPL and AGPL libraries and data MAY be used when a feature plan records the
  license and ships required notices. The published mod MAY adopt GPL or AGPL
  to stay compatible. Permissive licenses remain acceptable where they fit.
- Reference reading of other mods is allowed. Drop-in reuse of their sources
  is not, except libraries and data chosen and licensed in a feature plan.
- Bundled learner dictionary data MUST be redistributable under its upstream
  license (ECDICT MIT truncation is the planned default). Proprietary lists
  such as commercial COCA frequency dumps MUST NOT be redistributed in the
  mod jar.
- Optional Ollama, when specified, MUST call a local endpoint only, use fixed
  default model ids chosen in that specification (separate ids for text word,
  text sentence, and vision when applicable), and MUST remain disableable in
  client config. It MUST NOT be a hard load-time dependency of the mod.
- The published mod jar MUST NOT bundle a native OCR engine or OS-specific OCR
  runtime. Pixel reading, when specified, MUST go through the player's local
  Ollama vision model (or an equally local, player-installed tool named in the
  feature plan).

## Development Workflow

Work follows Spec Kit, one feature at a time:

1. Specify the player-visible behavior.
2. Plan the design (`research.md`, `data-model.md`, `contracts/`,
   `quickstart.md`).
3. Break the plan into tasks only when implementation is requested.
4. Implement only the active feature's tasks.

`docs/MCBilingual.md` is background. `docs/plan/` holds reference notes
(PolyglotTooltip, Language Reload) and the extended-translation design note
`Ollama集成.md`. A feature specification overrides those docs when they
disagree. Do not implement background later stages under an unrelated spec.

## Governance

This constitution overrides conflicting local practice. Feature plans MUST
pass the Constitution Check before implementation. A violation needs an entry
in that plan's Complexity Tracking.

Amendments change this file and bump the version:

- MAJOR: remove or redefine a principle.
- MINOR: add a principle or a constraint.
- PATCH: wording that does not change a rule.

The ratification date stays 2026-10-22. The amended date changes on every
edit. Specs and plans are reviewed against the current version before
implementation starts.

**Version**: 2.1.0 | **Ratified**: 2026-10-22 | **Last Amended**: 2026-10-24
