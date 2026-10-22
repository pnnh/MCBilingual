<!--
Sync Impact Report
- Version change: unversioned template → 1.0.0
- Modified principles:
  - [PRINCIPLE_1_NAME] → I. Client Only
  - [PRINCIPLE_2_NAME] → II. Translations Stay Outside
  - [PRINCIPLE_3_NAME] → III. Own the Behavior
  - [PRINCIPLE_4_NAME] → IV. Two Languages Until Expanded
  - [PRINCIPLE_5_NAME] → V. Verify in a Client Session
- Added sections: Additional Constraints, Development Workflow
- Removed sections: none (template placeholders replaced)
- Follow-up TODOs: none
-->

# MCBilingual Constitution

## Core Principles

### I. Client Only

MCBilingual MUST run only on the Minecraft client. A dedicated server MUST NOT
need this mod installed. The mod MUST NOT change server rules, world data, or
the network protocol.

Rationale: bilingual display, search text, and language switching are
presentation. Players on a server install the mod locally.

### II. Translations Stay Outside

MCBilingual MUST NOT ship Chinese or English translation text. It MUST read
language files already supplied by resource packs and by other mods, including
I18nUpdateMod3 and Minecraft-Mod-Language-Package. Those mods MAY be present
at development time or at runtime. They MUST NOT be compile or publication
dependencies.

Missing text MUST degrade to the available language or be omitted. It MUST NOT
crash the client and MUST NOT surface a raw translation key as a second
display line.

Source from I18nUpdateMod3 MUST NOT be copied into this repository. That
project is AGPL-3.0. Translation packs are data sources, not a code base.

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
support only Simplified Chinese (`zh_cn`) and English (`en_us`).

A feature MUST stay inside its specification. Menu-wide bilingual lines, chat
translation, maps, font scaling, display-mode cycling, EMI, AE2, and the rest
of the PolyglotTooltip integration catalog are out of scope until a spec adds
them.

### V. Verify in a Client Session

A feature that changes what the player sees is not complete until that flow
has been exercised in a client session. `./gradlew runClient` is the default
check. A packaged jar in a full modpack is the later compatibility check, not
a substitute for the daily client check.

Pure text transforms, such as pinyin, MUST have an automated test that does
not start the game. Player-visible claims MUST NOT be marked done from a
compile alone.

## Additional Constraints

- Minecraft 1.21.1, NeoForge 21.1.251, Java 21.
- Mod id `mcbilingual`, package `pnnh.mcbilingual`, client dist only.
- Optional integrations (JEI, Jade) MUST load as optional. The mod MUST still
  reach a world when they are absent.
- Pinyin is search text only. It MUST NOT appear in tooltips or Jade.
- Switching the displayed primary language MUST NOT run a full resource-pack
  reload.
- Reference reading is allowed. Drop-in reuse is not, except a permissively
  licensed pinyin library chosen in the search feature plan. GPL pinyin4j is
  rejected because a known Minecraft pinyin mod treats that library as GPL.

## Development Workflow

Work follows Spec Kit, one feature at a time:

1. Specify the player-visible behavior.
2. Plan the design (`research.md`, `data-model.md`, `contracts/`,
   `quickstart.md`).
3. Break the plan into tasks only when implementation is requested.
4. Implement only the active feature's tasks.

`docs/MCBilingual.md` is background. `docs/plan/` is the reference catalog of
PolyglotTooltip and Language Reload. A feature specification overrides both
when they disagree. Do not implement the background document's later stages
(font scale, three display modes, extra mod integrations) under a stage-1
spec.

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

**Version**: 1.0.0 | **Ratified**: 2026-10-22 | **Last Amended**: 2026-10-22
