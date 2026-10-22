# Research: JEI 中英与拼音搜索

## 拼音库

- **Decision**: 使用 TinyPinyin 2.0.3（`com.github.promeg:tinypinyin`，Apache-2.0）。用 Jar-in-Jar 或等价的 NeoForge 嵌入方式打进模组，并在模组附带其许可证声明。不加载可选的城市词库。
- **Rationale**: 库约 80KB，输出无声调拼音，默认给出单字的一个读音，符合「第一个读音」。Apache-2.0 可以随模组分发。
- **Alternatives considered**: pinyin4j。Just Enough Characters 的作者把原库视为 GPL，并因此把自己的模组也改为 GPL。Maven 上的 belerweb 分支标注 BSD，两套说法冲突。本项目不使用它。不把 Just Enough Characters 当依赖，因为它的许可证和在线词库都超出本规格。

## 词怎么生成

- **Decision**: 对中文名里连续的汉字转成小写无空格全拼；每个汉字再取全拼的首字母连成首字母串。非汉字原样留在中文搜索词中，不进入拼音。英文名另存一份小写文本。缺中文名时不生成拼音。
- **Rationale**: 规格要求 `pingguo` 与 `pg`，大小写不敏感，空格全拼不是必须输入。
- **Alternatives considered**: 同时索引带空格的全拼。查询若是包含匹配，带空格的键对无空格输入没有帮助。多音词库可以以后再加，本阶段规格明确不做。

## 何时建索引

- **Decision**: `LanguageTexts` 就绪后，遍历物品注册表生成 `ItemSearchTerms`。仅在客户端资源重载时重建。主语言切换不重建。
- **Rationale**: 四种词都不依赖当前主语言。切换时重建会违反「打字和切换都不重算整表」。
- **Alternatives considered**: 第一次打开 JEI 时懒构建。冷启动后的第一次搜索会卡住，也不利于测试「打开搜索框之前已经就绪」。

## 如何放进 JEI

- **Decision**: 使用本仓库能解析的 JEI `19.64.0.455`（`mezz.jei:jei-1.21.1-neoforge`，BlameJared）。在 `ListElementInfo` 构造结束时，把已经生成的搜索词追加到它的名称列表。默认后缀树和低内存搜索都通过 `getNames()` 读取这份列表。mixin 仅当模组 `jei` 已加载时应用。`neoforge.mods.toml` 将 JEI 标为客户端可选依赖。
- **Rationale**: 19.64 的构造函数比 PolyglotTooltip 所针对的 19.43 多了一个过滤配置参数，不能照搬旧 mixin。两条索引路径都读同一份名称列表，查询时做包含匹配。名称列表不参与物品绘制。1.21.1 的开发运行不会从 classes 目录加载 Jar-in-Jar，所以 TinyPinyin 还要放进 `additionalRuntimeClasspath`。
- **Alternatives considered**: `IModPlugin.registerIngredientAliases`。19.64 提供了这个公开 API，但 `searchIngredientAliases` 默认为真，JEI 会把别名写进物品提示框，拼音会露出来，违反 FR-006。正式依赖 JEI 会使未安装时客户端起不来。运行时反射整个索引更难对照版本。

## 拼音库的调用方式

- **Decision**: 只对每个汉字调用 `Pinyin.toPinyin(char)`，再转成小写。不调用按词切分的 `toPinyin(String)`，也不嵌入 `org.ahocorasick:ahocorasick`。
- **Rationale**: 单字接口给出表里的第一个无声调读音。`行` 在「银行」和「行走」里都是 `xing`。按词切分才会用到运行时词典依赖。实测 `苹果` 为 `pingguo` / `pg`。

## Just Enough Characters

- **Decision**: 本阶段不写 JEC mixin。quickstart 在未安装 JEC 的环境里验证。
- **Rationale**: JEC 会替换 JEI 的树，使我们的 JEI mixin 失效。规格的成功标准没有要求与 JEC 共存。PolyglotTooltip 有一条兼容 mixin，留在 `docs/plan/polyglottooltip.md`，以后需要再单开规格。
- **Alternatives considered**: 现在就移植那条 mixin。它扩大本阶段范围，而且 JEC 自己已经提供拼音。

## 可见文本

- **Decision**: 搜索词只进入 JEI 索引。提示框、Jade 和 JEI 绘制的名称继续走 001 的名称契约，不读取拼音字段。
- **Rationale**: FR-006。
- **Alternatives considered**: 在调试提示里显示拼音。玩家可见，违反规格。
