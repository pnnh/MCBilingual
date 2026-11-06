# Implementation Plan: 三方模组兼容模式

**Branch**: `008-compat-mode-config` | **Date**: 2026-11-06 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/008-compat-mode-config/spec.md`

## Summary

客户端配置是一份默认为空的字符串列表，每一项是 `modid=Full|Generic|Off`。没写上的 mod id 按 Full。玩家在模组配置界面保存后，下一帧就按新列表运行，不用重启。

Full 继续跑已经写好的 Jade、JEI、应用能源和 Integrated Terminals 专用兼容。Generic 和 Off 都不跑这些专用代码。Off 另外在该模组自己的界面或覆盖层上停用双语提示和冻结查看。语言切换热键不看这份列表。某一条兼容初始化失败时只跳过那个 mod id，写一条客户端日志，不改已保存的配置。Polyglot Tooltip 和 Language Reload 仍让整个模组停用。

## Technical Context

**Language/Version**: Java 21，Minecraft 1.21.1，NeoForge 21.1.251。发布范围保持 `[21.1.251,)`。

**Primary Dependencies**: 已有的 `LanguageTexts`、物品和流体搜索词、Jade / JEI / AE2 / Integrated Terminals 集成、`RivalMods`、`LoadReport`。配置用 NeoForge `ModConfigSpec.defineListAllowEmpty`，类型 `ModConfig.Type.CLIENT`。不新增第三方依赖，也不把 Jade、JEI、AE2、Integrated Terminals 写进 `neoforge.mods.toml`。

**Storage**: `config/mcbilingual-client.toml` 里的一个字符串列表。内存里是一张 mod id 到模式的快照，加上每个 mod id 至多一条的初始化失败记录。

**Testing**: 列表解析、重复项、无效值和模式判断用 JUnit，不启动游戏。界面归属、配置界面和四个模组的 Full / Generic / Off 用 `./gradlew runClient`，见 [quickstart.md](quickstart.md)。

**Target Platform**: Minecraft 1.21.1 客户端。专用服务器不加载本模组。

**Project Type**: 客户端模组

**Performance Goals**: 每一帧只做几次模式查找和界面归属查询。归属按类缓存。失败日志每个 mod id 每次启动一条。

**Constraints**: Mixin 插件在配置加载之前运行，不能用配置决定 Mixin 是否应用。专用兼容在已经应用的入口里读当前模式。第三方 Mixin 配置改为非必需，避免一条签名失配拖垮客户端。原版提示、语言切换和冻结查看在没有第三方模组时保持可用。

**Scale/Scope**: 四个已有专用集成，外加原版提示和冻结查看的 Off 门禁。列表可以写下其它 mod id，没有对应专用代码时只影响“这一页是不是该模组的界面”。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 结果 |
| --- | --- |
| I. Client Only | 通过。配置类型是客户端。不改协议，不要求专用服务器安装本模组。 |
| II. Game Language Packs Outside; Learner Aids May Ship | 通过。不新增语言包，不把第三方模组打进发布包。只给配置项加本模组自己的两行说明。 |
| III. Own the Behavior | 通过。不复制 Jade、JEI、AE2 或 Cyclops 的实现。Polyglot Tooltip 与 Language Reload 仍让全部功能停用，配置列表不能把它们重新打开。 |
| IV. Two Languages Until Expanded | 通过。仍是 `zh_cn` 与 `en_us`。本规格只给已有集成加门禁，不新增殖民地或其它模组的专用兼容。 |
| V. Verify in a Client Session | 通过。解析和门禁用单测。配置界面、原版背包与 JEI 同屏、四个模组的三种模式写在 quickstart 里。单测通过不算这些画面已经看见。 |
| VI. Vanilla First, Third-Party Optional | 通过。`mods.toml` 仍只有 Minecraft 和 NeoForge。某一条集成初始化失败时跳过并记日志。原版路径不读第三方类型。 |

Phase 1 之后复检：仍通过。运行时门禁和 `required: false` 是为了满足“失败不拖垮客户端”和“保存后不用重启”。没有需要写入 Complexity Tracking 的例外。

## Project Structure

### Documentation (this feature)

```text
specs/008-compat-mode-config/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── compat-config.md
│   └── compat-gates.md
└── tasks.md
```

`tasks.md` 已生成。实现按该文件执行。

### Source Code (repository root)

```text
src/main/java/pnnh/mcbilingual/
├── MCBilingual.java
├── compat/
│   ├── CompatMode.java
│   ├── CompatList.java
│   ├── CompatConfig.java
│   ├── CompatGate.java
│   ├── UiOwner.java
│   └── IntegrationGuard.java
├── diagnostics/LoadReport.java
├── tooltip/ItemTooltipNames.java
├── freeze/FreezeCapture.java
└── integration/
    ├── jade/
    ├── jei/
    ├── ae2/
    └── integratedterminals/

src/main/resources/
├── assets/mcbilingual/lang/zh_cn.json
├── assets/mcbilingual/lang/en_us.json
├── mcbilingual.mixins.json
├── mcbilingual-ae2.mixins.json
└── mcbilingual-integratedterminals.mixins.json

src/test/java/pnnh/mcbilingual/compat/
├── CompatListTest.java
└── CompatGateTest.java
```

**Structure Decision**: 解析、模式和门禁放在 `compat`，不引用 Jade、JEI、AE2 或 Cyclops。第三方类只留在原来的 `integration` 包。提示和冻结查看调用 `CompatGate`，不反向依赖某个集成。配置在主类注册，同类语言模组导致停用时也注册，这样玩家仍能看见列表，但门禁先看 `RivalMods`。

## Complexity Tracking

无。
