# Research: 集成终端的中英与拼音搜索

标签 `1.21.1-1.10.0` 的源码和 2026-10-23 的 Modrinth 发行版是下面决策的依据。不使用更新的 1.11 标签。

## 可行性

- **Decision**: 做成可选的客户端行为。开发会话能打开 1.10.0 的存储终端，发布包不要求玩家安装它。
- **Rationale**: 物品的中文、英文、全拼和首字母已经在语言表重载时放进 `ItemSearchIndex`。存储终端不读 JEI，也不读应用能源的搜索。缺口是它自己的名称条件只看当前悬停名。
- **Alternatives considered**: 只把 jar 放进开发环境、不改搜索。那样规格里的跨语言和拼音过不了。改 JEI 并指望终端跟着变。终端不读那份清单。

## 挂在哪一个方法上

- **Decision**: 只包住 1.10.0 里两个带 `@OnlyIn(Dist.CLIENT)` 的 `getInstanceFilterPredicate`。物品类是 `IngredientComponentTerminalStorageHandlerItemStack`，流体类是 `IngredientComponentTerminalStorageHandlerFluidStack`。`SearchMode` 不是 `DEFAULT` 时原样返回。是 `DEFAULT` 时，结果为「原条件成立，或本模组的搜索词包含这段查询」。
- **Rationale**: `IIngredientQuery.parse` 先按空格拆成「并且」，再按竖线拆成「或者」，最后才把一个词交给 `IngredientQueryLeaf`。空的普通名称不会调用过滤方法，直接匹配全部。`@`、`#`、`$` 分别变成 `MOD`、`TOOLTIP`、`TAG`。这三者和能量页因此不用本模组的词。物品和流体的 `DEFAULT` 分支现在都是 `getHoverName()` 的小写字符串。便携存储终端和方块上的存储终端都走 `TerminalStorageTabIngredientComponentClient` 里的同一次 `parse`，所以一个过滤方法覆盖两种外形。合成任务窗口用的是另一套容器，不调用这个方法。
- **Alternatives considered**: 包住 `IngredientQueryMatchers.containsQuery`。`@`、`#`、`$` 共用这个匹配器，包进去会让前缀也吃中文和拼音。替换掉原条件。铁砧改过的名字和终端自己的正则就会丢。改能量类。那个方法固定返回真，规格要求能量页保持原样。

## 无效正则

- **Decision**: 不另写一套正则。原匹配器对非法模式返回「永不命中」。本模组的判断是字面量包含，不编译模式，也不因空查询返回真。
- **Rationale**: `containsQuery` 把查询放进 `.*` + 查询 + `.*`。`PatternSyntaxException` 时已经不会崩溃，也不会匹配所有字符串。字面量包含最多再留下名称里真有这段字符的物品，不会因为一段非法输入列出全部。合法正则仍由原条件生效；本模组只额外做大小写不敏感的包含，与现有物品搜索一致。
- **Alternatives considered**: 非法模式时让本模组也返回假。那会挡住名称里恰好含有这些字符的物品，比规格更窄，也没有必要。自己重写空格、竖线和前缀。终端已经拆好了。

## 流体词表

- **Decision**: 新增 `FluidSearchIndex`，键是产生该流体悬停名的翻译键。构建时对每种已注册流体取一次这个键，用 `ItemSearchTerms.queries` 生成小写中文、英文、全拼和首字母，没有中文就不生成拼音。`LanguageTexts.apply` 在现有物品构建的同一次尝试里调用它。`search` 包不引用 Cyclops。匹配继续调用 `ItemSearchIndex.anyContains`。
- **Rationale**: 流体不在物品注册表里，不能塞进 `ItemSearchIndex`。终端比较的是悬停名，所以键必须是那条翻译，而不是另一个看起来像流体标识的字符串。实现时从 NeoForge 21.1.251 的映射源码读取 `FluidStack` 悬停名实际使用的描述键；计划不先写死 `fluid.` 或 `block.` 前缀。原版水若走方块翻译键，词表就用那个键。悬停名不是这条翻译时，只保留终端原来的当前名称条件，和铁砧改名的物品同一规则。
- **Alternatives considered**: 把流体描述键硬编码进计划。映射方法没在这份文档里打开过，写死会建错表。为流体再做一套拼音规则。现有规则已经规定缺中文时不生成拼音。

## 开发会话里装哪些模组

- **Decision**: 版本钉死为 Modrinth 上不带构建号的发行版，坐标见 [plan.md](plan.md)。2026-10-23 能解析到的文件是 `integratedterminals-1.21.1-neoforge-1.10.0.jar`、`cyclopscore-1.21.1-neoforge-1.30.0.jar`、`integrateddynamics-1.21.1-neoforge-1.37.1.jar`、`commoncapabilities-1.21.1-neoforge-2.11.6.jar`、`integratedtunnels-1.21.1-neoforge-1.12.0.jar`。Cyclops Core 在 Maven 上用版本号 `1.21.1-1.30.0` 会下到 Forge jar，开发坐标改用 NeoForge 文件的版本标识 `2Z0ruVBB`。Integrated Terminals Compat 已打在终端 jar 里，不另加一份。也不另加 Integrated Crafting。开发客户端会扫描所有带游戏测试注解的类；其中引用了未安装模组的类在链接时失败。本模组在 `GameTestHooks.addGameTestMethods` 入口跳过这种类，让客户端能打开。搜索行为不依赖这些测试。
- **Rationale**: 1.10.0 的 `neoforge.mods.toml` 要求 `cyclopscore`、`integrateddynamics`、`commoncapabilities`，范围向下封在 `1.30.0`、`1.37.1`、`2.11.6`，向上打开。所选发行版满足这些下限。Integrated Dynamics 1.37.1 还接受 Common Capabilities `2.9.11` 起的版本，因此 `2.11.6` 可用。Cyclops Core 1.30.0 高于 Common Capabilities 编译时用过的 `1.25.1`。存储终端通过物品接口读取网络里的物品；Integrated Tunnels 用来在接口单独不够时把箱子接进网络。后两份兼容模组不在 1.10.0 的必需依赖里。
- **Alternatives considered**: 跟到 1.11.0 或 1.11.1。玩家点名的是 1.10.0。使用带构建号的 beta。发行版已经存在。把五份 jar 都放进 `compileOnly`。主类和搜索包不需要另外四个模组的类型。

## 版本线

- **Decision**: 本模组 `neo_version` 保持 21.1.251。五份模组的 NeoForge 下限都不高于这个构建：终端和 Integrated Dynamics、Integrated Tunnels 是 `[21.1.228,)`，Common Capabilities 是 `[21.1.21,)`，Cyclops Core 是 `[21.1.2,)`。`mods.toml` 里本模组对 `integratedterminals` 的可选范围写成 `[1.10.0,1.11.0)`，顺序 `NONE`，侧别 `CLIENT`。
- **Rationale**: 21.1.251 落在这五段范围里。可选范围停在 1.11 之前，是因为这一版只读过 1.10.0 的方法签名。玩家装了 1.11 时，这个可选依赖不匹配，本模组不会带着未验证的 Mixin 去挂 1.11。
- **Alternatives considered**: 把本模组的 NeoForge 升到 21.1.228 以上的更新构建。宪法把工具链定在 21.1.251。把可选范围向上打开。方法若改名，`defaultRequire` 为 1 会在装了终端时崩溃。

## 可选加载和冲突

- **Decision**: 新配置 `mcbilingual-integratedterminals.mixins.json` 只列两个客户端 Mixin，插件只在加载列表里有 `integratedterminals` 且 `RivalMods.isBlocked()` 为假时应用。`defaultRequire` 为 1，注入点 `remap` 为 false。不新增聊天或日志。冲突时仍用现有的一条启动日志和进入世界后的一条聊天。
- **Rationale**: 与应用能源、JEI 的可选 Mixin 相同。缺模组时不应转换这两份类。冲突标识仍只有 `polyglottooltip` 和 `languagereload`。`i18nupdatemod`、语言包、JEI、Jade、应用能源、殖民地和集成终端都不是冲突对象。
- **Alternatives considered**: 放进现有的应用能源配置。没装应用能源时会把终端搜索一起跳过。冲突时只跳过这一份配置、其它行为继续。规格要求本模组整体停用。

## 中文和许可证

- **Decision**: 不往仓库添加 `zh_cn.json`。1.10.0 的源码树里没有中文语言文件。开发会话继续使用已经会下载的外部资源包，以及各个 jar 自带的语言文件。许可证保持 All Rights Reserved。
- **Rationale**: 集成终端是 MIT，独立运行，不打进发布包。不复制 I18nUpdateMod3，也不把它放回类路径。没有中文时按现有规则只保留英文和当前名称，不生成拼音，不把翻译键显示出来。
- **Alternatives considered**: 把终端物品的中文抄进本模组。那是在发布译文。

## 尚未在客户端证实

这些判断可以指导实现，但不能写成已经看见：

- 没装集成终端时，引用了 `SearchMode` 的 Mixin 类不会被加载。
- `defaultRequire` 为 1 会在 1.10.0 的方法签名对不上时阻止客户端启动。
- 上面五条 Modrinth 坐标能被现有仓库解析。接口记录只说明文件存在。
- 流体悬停名的翻译键与 `FluidSearchIndex` 使用的键相同。
- 存储终端、便携终端、流体页、前缀和语言快捷键的玩家可见结果。004 的菜单启动也不能代替这些步骤。
