# Implementation Plan: 中英主语言快捷切换

**Branch**: `003-language-hotkey` | **Date**: 2026-10-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-language-hotkey/spec.md`

## Summary

启动资源加载时已经由 `LanguageTexts` 持有中英两套文案。快捷键只切换游戏当前主语言，并把缓存的那一份语言装上，然后刷新依赖语言的界面。不调用完整资源重载。默认操作是按住 F3 再按 J，玩家可以在控制设置里改成单键。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: `001-bilingual-tooltips` 的 `LanguageTexts`。不依赖 Language Reload。原版按键系统没有 F3 修饰键，默认和弦单独处理。

**Storage**: 主语言写入原版选项的语言字段。不另建语言列表。

**Testing**: 切换目标语言的纯规则用 JUnit。可见切换用 `./gradlew runClient`，见 [quickstart.md](quickstart.md)。

**Target Platform**: Minecraft 1.21.1 客户端

**Project Type**: 客户端模组

**Performance Goals**: 按下后一秒内，已打开的背包或暂停菜单显示新语言。切换不重载资源包。

**Constraints**: 只在 `zh_cn` 与 `en_us` 之间交替。启动语言若在这对之外，第一次按键前不改它。聊天输入时不触发。缺语言包仍能进世界。

**Scale/Scope**: 跟随游戏语言的原版界面：菜单、按钮、标题、物品名、打开的书、进度、已加载的告示牌和文本展示。第三方界面保证关闭再打开后是新语言。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| I. Client Only | 通过。只改客户端语言和客户端界面缓存。 |
| II. Translations Stay Outside | 通过。切换的是已经加载的两套文案，不打包译文。 |
| III. Own the Behavior | 通过。快速切换的思路来自 Language Reload 1.21.1，实现用 NeoForge 和当前映射重写。不复制 Fabric mixin。 |
| IV. Two Languages Until Expanded | 通过。没有后备语言栈，没有第三种语言，没有三种显示模式。 |
| V. Verify in a Client Session | 通过。目标语言规则有单测；快捷键、界面刷新和重启保持由 quickstart 覆盖。 |

Phase 1 之后复检：仍通过。为安装缓存语言而访问语言管理器的私有状态，是实现手段，不增加用户可见范围。

## Project Structure

### Documentation (this feature)

```text
specs/003-language-hotkey/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── language-switch.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/
└── switchlang/
    ├── LanguageCycle.java
    ├── LanguageSwitch.java
    └── SwitchKeyHandler.java

src/test/java/pnnh/mcbilingual/switchlang/
└── LanguageCycleTest.java
```

**Structure Decision**: 按键和刷新放在 `switchlang`。语言表仍属于 001 的 `language` 包。需要把缓存语言设为当前语言时，用针对 21.1.251 源码核对过的访问器，不引用 Fabric 的类名。

## Complexity Tracking

无。
