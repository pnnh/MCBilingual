# Research: 应用能源终端与殖民地物品的语言

## 可行性

- **Decision**: 这件事可以做，而且应当做成可选的客户端行为，而不是把两个模组编进发布包。
- **Rationale**: 物品提示和注视名称已经按翻译键取两种语言。语言快捷键已经换上缓存的语言表。缺口主要是应用能源终端不用物品清单的搜索，以及开发会话里还没有这些模组和中文。
- **Alternatives considered**: 只把两个 jar 放进开发环境、不改搜索。那样终端仍只能搜当前显示名，规格里的跨语言和拼音过不了。

## 开发会话里装哪些模组

- **Decision**: `localRuntime` 放入应用能源 `19.2.18`（NeoForge，Minecraft 1.21.1）和殖民地 `1.1.1405-1.21.1-snapshot`。解析到文件后读取殖民地 jar 里的依赖范围，再把缺了就无法进入世界的配套也放进 `localRuntime`。已知的一组是 Structurize、BlockUI、Multi-Piston、Domum Ornamentum。不安装旅程地图、动态树或殖民地附属。应用能源若在元数据里要求 GuideME 且 jar 未自带，再把 GuideME 放进 `localRuntime`。
- **Rationale**: 1.1.1403 的启动记录已经因为缺少 Structurize `1.0.832-1.21.1-snapshot` 而无法加载。1405 的下限以那个 jar 的元数据为准，不凭更旧版本猜死。这些依赖只为了开发客户端能打开，不写进本模组的发布依赖。
- **Alternatives considered**: 只放玩家点名的两个 jar。殖民地会在预加载时直接失败。把配套写成本模组的强制依赖。玩家不装殖民地也会被挡住，违反客户端可选。

## 中文从哪来

- **Decision**: 不把 I18nUpdateMod 3.7.0 放进运行类路径。NeoForge 21.1.251 看到 jar 里的 `fabric.mod.json`、又没有 `neoforge.mods.toml`，会跳过该 jar，ModLauncher 服务也不会启动。应用能源 19.2.18 和殖民地 1.1.1405 的 jar 自己带有 `zh_cn.json`。另外在 `runClient` 前把 CFPA 的 `Minecraft-Mod-Language-Modpack-1-21.zip` 下载到 `run/resourcepacks`，把其中的 `pack_format` 从 33 改成 34 后再写进资源包选项。原包声明支持 34 到 37，但主格式是 33，Minecraft 1.21.1 会把它丢掉。资源包不提交。
- **Rationale**: 这两个模组的物品中文不依赖自动汉化。自动汉化是 AGPL，发布版的合一 jar 在这条 NeoForge 上加载不了；不复制它的源码，也不把许可证改成 AGPL。资源包协议是 CC BY-NC-SA，只留在被忽略的 `run/` 里。
- **Alternatives considered**: 继续把 3.7.0 放在 `localRuntime`。启动日志会每次警告跳过，资源包仍不会被下载。把译文放进本模组 `assets`。那就是在发布译文。

## 应用能源终端搜索

- **Decision**: 19.2.18 的 `RepoSearch.getPredicates` 对普通词构造 `NameSearchPredicate`，匹配的是 `AEKey.getDisplayName()` 的当前字符串。物品键改为：先保留这个原条件，再接受 `ItemSearchIndex` 里该物品描述键的任一词包含该查询（已是小写）。`@`、`#`、`$`、`*` 条件不替换。非物品键只走原条件。
- **Rationale**: 合成终端和物品终端共用这套仓库搜索。词表在语言重载时已经包含中文、英文、全拼和首字母，切换主语言不会重建。原条件留下，是为了不丢掉终端现有的当前名称匹配。
- **Alternatives considered**: 改物品清单搜索并指望终端跟着变。终端不读那份清单。把 PolyglotTooltip 的 `Ae2SearchPredicate` 拷进来。那是别人的实现，也把流体显示名算进搜索，超出本规格。

## 终端提示上的第二行

- **Decision**: 19.2.18 的格子提示对物品调用带物品堆的 `renderTooltip`。那条路径进入现有的提示收集事件，但不经过会插入第二行的物品提示事件。在提示收集里，若第二行还不存在就补上；已经存在则只保持原来的归位。
- **Rationale**: 背包提示已经先插入再归位。终端格子只有归位，所以第二行会丢。补在收集事件里，主类和终端类都不互相引用。
- **Alternatives considered**: 再 Mixin 终端屏幕。事件已经拿到物品堆，多一个 Mixin 只是重复。

## 殖民地物品和工作台

- **Decision**: 不编译、不 Mixin 殖民地。物品和方块名称走现有提示与注视。BlockUI 窗口按当前语言取句子；快捷键换语言后，会重绘的文字应在下一帧更新，记住旧句子的窗口按契约关闭再打开。quickstart 若发现关闭再打开仍是旧语言，再补客户端刷新，范围只限那个失效的文字。
- **Rationale**: 中文一旦进入语言表，描述键和现有第二行规则就覆盖殖民地物品。工作台不是物品清单，也不需要整页双语。
- **Alternatives considered**: 为每个工作台窗口做专用 Mixin。在会话证明有缺口之前，那是额外界面范围。

## 许可证

- **Decision**: 不把本模组改成 AGPL。不复制 I18nUpdateMod3 的源码，不把它放进编译或发布类路径。当前这条 NeoForge 也加载不了它的 3.7.0 合一 jar，所以开发会话改用各模组自带语言文件和单独的资源包。
- **Rationale**: AGPL 的衍生和再发布义务发生在分发其代码时。独立模组一起运行、由玩家自行安装，不是把该代码放进本模组的发布物。用户接受「若打进发布包就改为 AGPL」，当前不打进。
- **Alternatives considered**: 现在就把许可证改成 AGPL。没有分发 AGPL 代码，改许可证不能解决兼容问题，还会把全部自有代码变成 AGPL。

## 版本线

- **Decision**: 开发依赖选择 Minecraft 1.21.1、NeoForge 的 1.21.1 构建，并且其元数据接受 NeoForge 21.1.251。本模组的 `neo_version` 保持 21.1.251。应用能源 19.2.18 的范围是 NeoForge `[21.1.169,)`。殖民地 1.1.1405 的范围是 NeoForge `[21.1.0,)`。
- **Rationale**: 21.1.251 落在这两段范围里。配套若要求比 21.1.251 更新的 NeoForge，就不能拿来做这次开发依赖。
- **Alternatives considered**: 把本模组的 NeoForge 升到更新的构建。宪法把工具链定在 21.1.251。

## 冲突时停用

- **Decision**: 模组标识 `polyglottooltip` 或 `languagereload` 出现在加载列表时，三份 Mixin 配置全部不应用，构造时不注册语言表和快捷键，提示、Jade 和按键处理直接返回。启动时 `warn` 一条中英说明。玩家进入世界时，聊天再写同一条，每次运行一次。`i18nupdatemod` 不在名单里。
- **Rationale**: 聊天只有进入世界后才存在，所以日志和聊天分成两处。停用后不写语言选项，也不改世界。Mixin 不应用，才能避免和对方改同一处字节码。
- **Alternatives considered**: 直接让加载失败并弹出错误屏。那样进不了世界，聊天要求也无法满足。停用对方模组。破坏最小的是停用自己。

## 可选加载

- **Decision**: 应用能源 Mixin 放在独立配置 `mcbilingual-ae2.mixins.json`，插件只在模组列表里有 `ae2` 时应用。`mods.toml` 把 `ae2` 标成客户端可选。主类不引用 `appeng`。殖民地不写入 `mods.toml`。
- **Rationale**: 与 JEI 的可选 Mixin 相同。缺模组时类不会被转换，客户端仍能进世界。
- **Alternatives considered**: 把应用能源 Mixin 放进现有 JEI 配置。JEI 不在时会连终端搜索一起跳过。
