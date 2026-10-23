# Implementation Plan: 定格界面并查看另一种语言的整句

**Branch**: `006-frozen-ui-lookup` | **Date**: 2026-10-23 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-frozen-ui-lookup/spec.md`

## Summary

玩家按下调试键加 K 时，留下已经画完、玩家正在看的那一帧，以及这一帧里仍能对应到译文的整句位置。鼠标只挑选最上面的一句。提示是当前主语言之外的那一种整句：主语言为 `en_us` 时用 `zh_cn`，主语言为 `zh_cn` 时用 `en_us`。成品文字不反查键值。没有对照译文、玩家自己写的字，以及世界里的告示牌和头顶名字，都不提示。再按一次恢复操作。不切换主语言，不重载资源，不做机器翻译。

对照译文仍从已经加载的两份语言表读取。默认快捷键沿用现有的未绑定按键加调试组合，避免占用中英切换的调试键加 J。Polyglot Tooltip 或 Language Reload 存在时，这组 Mixin 不应用。规格允许按下后数帧内文字还在的画面，本计划采用按下时已经显示的那一帧，因为多等几帧不能把成品文字还原成译文。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251

**Primary Dependencies**: 已有的 `LanguageTexts`、`LanguageCodes`、`RivalMods` 和 `SwitchKeyHandler` 的按键方式。不新增模组依赖，不把汉化包打进发布 jar。

**Storage**: 定格只活在内存里。结束或退出世界时丢掉画面和文字位置。快捷键名称写进本模组自己的 `en_us` 和 `zh_cn`，不新增别的语言，也不写入外部资源包。

**Testing**: 对照语言方向、模板参数、缺译文、成品文字不反查、玩家文字和重叠时取最上面一条，用 JUnit 测纯判断。定格、两种主语言下的提示、聊天、点击和恢复操作用 `./gradlew runClient`，见 [quickstart.md](quickstart.md)。编译通过不算玩家已经看见。

**Target Platform**: Minecraft 1.21.1 客户端。专用服务器不需要本模组。

**Project Type**: 客户端模组

**Performance Goals**: 平时每帧只保留界面上刚画过的文字矩形，帧末丢掉上一帧。定格后不再下载画面。对照整句在句子第一次被记下时算好，鼠标移动时只做矩形判断。

**Constraints**: 只处理界面和抬头显示里仍带着翻译组件的整句。普通字符串和纯字面量不做反向查找，模板拼好后的成品也不做。世界投影文字不记录。定格期间鼠标键、滚轮、视角和移动输入都不进入游戏。当前主语言不是 `zh_cn` 或 `en_us` 时可以定格，但不弹出提示。`neo_version` 保持 21.1.251。拼音不出现在提示里。

**Scale/Scope**: 一个定格开关，覆盖当前画面上的菜单、物品提示、任务或书籍页面，以及聊天栏里的系统句。不覆盖玩家打字、改名、手写书页，也不覆盖世界中的告示牌和名字。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| I. Client Only | 通过。记录、定格和提示都在客户端绘制和输入里。不改协议，不要求专用服务器安装。 |
| II. Translations Stay Outside | 通过。对照句来自已经加载的 `zh_cn` 与 `en_us`。缺失时不提示，也不显示翻译键。不复制外部汉化模组的源码。 |
| III. Own the Behavior | 通过。不依赖 Polyglot Tooltip 或 Language Reload。二者存在时 Mixin 不应用。 |
| IV. Two Languages Until Expanded | 通过。本规格仍只使用 `zh_cn` 和 `en_us`，方向跟随当前主语言。世界文字、玩家产生的文字、机器翻译、第三种语言和字体缩放不在其中。 |
| V. Verify in a Client Session | 通过。方向、模板和成品文字不反查有单测。两种主语言下的提示框、系统聊天、玩家文字和输入隔离写在 quickstart 里。 |

Phase 1 之后复检：仍通过。没有新的发布依赖。没有需要写入 Complexity Tracking 的例外。

## Project Structure

### Documentation (this feature)

```text
specs/006-frozen-ui-lookup/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── frozen-ui-lookup.md
└── tasks.md
```

`tasks.md` 只在要求实现时生成。

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/freeze/
├── FreezeKeyHandler.java
├── FreezeSession.java
├── UiSentence.java
├── SentenceResolver.java
├── TextHit.java
└── mixin/
    ├── FreezeMixinPlugin.java
    ├── GuiGraphicsCaptureMixin.java
    ├── FontCaptureMixin.java
    ├── TooltipOwnerMixin.java
    ├── ChatOwnerMixin.java
    ├── GameRendererFreezeMixin.java
    ├── KeyboardInputFreezeMixin.java
    └── MouseLookFreezeMixin.java

src/main/resources/mcbilingual-freeze.mixins.json
src/main/resources/assets/mcbilingual/lang/en_us.json
src/main/resources/assets/mcbilingual/lang/zh_cn.json
src/main/templates/META-INF/neoforge.mods.toml
src/test/java/pnnh/mcbilingual/freeze/
├── SentenceResolverTest.java
└── TextHitTest.java
```

**Structure Decision**: 定格相关类型放在 `freeze`，不放进 `switchlang`。对照语言只读 `LanguageTexts` 里已经加载的两份表，不在绘制中途改全局语言。快捷键注册仍走客户端事件，类别继续用 `key.categories.mcbilingual`。Mixin 使用单独配置，插件只在 `RivalMods` 未停用时应用。主类不因为这个功能引用第三方模组。

## Complexity Tracking

无。
