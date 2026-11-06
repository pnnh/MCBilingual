# Research: 三方模组兼容模式

## 配置怎么装下一张默认为空的键值表

**Decision**: 使用 NeoForge 客户端配置里的一个允许为空的字符串列表。每一项写成 `modid=Full`、`modid=Generic` 或 `modid=Off`。`ModConfigSpec.Builder.defineListAllowEmpty` 接受任意字符串，校验放在本模组的解析器里。配置界面用 `translation` 指向 `zh_cn.json` 和 `en_us.json` 里的说明。加载和重载监听 `ModConfigEvent.Loading` 与 `ModConfigEvent.Reloading`，换成一张不可变快照。

**Rationale**: NeoForge 21.1.251 的配置规格没有地图类型。`defineList` 在空列表上会拒绝默认值，所以用 `defineListAllowEmpty`。把非法取值挡在配置规格里会让界面无法保存，规格却要求非法取值记日志并按 Full 运行。快照在重载事件里替换，下一次绘制读到新值，满足“保存后不用重启”。

**Alternatives considered**: 为四个已知模组各做一个枚举项。列表就不再是玩家指定的任意 mod id。单独写一个配置屏幕。宪法要求可调行为用 NeoForge 客户端配置，模组列表里的配置按钮已经能编辑字符串列表。

## 门禁为什么不能只放在 Mixin 插件里

**Decision**: 第三方 Mixin 仍在对应模组存在且未被同类语言模组停用时应用。Full、Generic、Off 在入口方法里读取当前快照。Jade 组件也照常注册，第二行在 `JadeSecondaryNames.add` 里判断。

**Rationale**: Mixin 插件的 `onLoad` 早于配置文件加载。只在插件里决定应用与否，会把模式冻成启动时的值，改配置必须重启。规格要求回到游戏后生效。

**Alternatives considered**: 启动时读 toml 文件而不等 NeoForge 配置事件。那样会和配置界面的保存时机分叉，也绕过 NeoForge 的重载。

## Off 怎样只盖住该模组自己的界面

**Decision**: 两层归属。

1. 当前 `Screen` 的类属于哪个 mod id。`RecipesGui`、应用能源的 `appeng.client.gui` 屏幕、Integrated Terminals 的 `ContainerScreenTerminal*` 因此整页算那个模组。原版 `InventoryScreen` 不算 JEI。
2. 绘制栈。JEI 的 `IngredientListOverlay.drawScreen`、`drawForeground`、`drawTooltips` 入栈 `jei`。Jade 的 `OverlayRenderer.renderOverlay` 入栈 `jade`。栈在每帧开始清空，避免某次绘制抛错后把后面的界面粘住。

类到 mod id 的查找先走四个已知包名，再用 `ModList.get().getModFiles()` 里 `IModFile.findResource` 对类文件做一次查找，结果按类缓存。找不到就当作原版。

**Rationale**: JEI 物品列表画在原版背包上，只看当前 Screen 会把原版格子也关掉，或者完全管不到列表。Jade 浮层不是 Screen。整页终端本身就是该模组的 Screen，不需要再包一层绘制。

**Alternatives considered**: 只看 Screen 类。JEI 覆盖层和 Jade 浮层对不上规格。扫描调用栈判断调用者。每行文字都走栈，且混淆后不稳定。

## 三种模式分别挡住什么

**Decision**:

- 专用兼容只在模式为 Full、该 mod id 没有初始化失败、且同类语言模组未停用时执行。Generic 和 Off 都跳过。入口是 JEI 搜索词追加、AE2 `RepoSearch` 谓词、终端物品和流体的默认名称谓词、Jade 第二行。
- 双语提示和冻结查看在“当前 Screen 或绘制栈里有任一 Off 的 mod id”时跳过。Generic 不挡这两项。
- 语言切换不读这份列表。

**Rationale**: 规格把 Generic 定义成“不执行专为该模组写的代码”，把 Off 定义成“不在它自己的界面和覆盖层上加载本模组的任何功能”。提示和冻结查看是共用功能，只有 Off 才按界面关掉。

**Alternatives considered**: Generic 也关掉该界面上的提示。那会和“接近原版的文本仍可能显示双语”冲突。Off 只关专用搜索。那又达不到“这一页上没有双语提示或冻结查看”。

## 初始化失败怎样停在一个模组上

**Decision**: 每个专用入口捕获 `Exception` 和 `LinkageError`，由 `IntegrationGuard` 对每个 mod id 记一条日志，然后这次和之后的专用调用都跳过。插件 `onLoad` 同样捕获，失败时不应用 Mixin。`mcbilingual.mixins.json`、`mcbilingual-ae2.mixins.json`、`mcbilingual-integratedterminals.mixins.json` 的 `required` 改为 `false`。语言切换和冻结查看的 Mixin 配置保持必需。失败不写回配置。

**Rationale**: 注入点找不到是 Mixin 应用期的错误，方法内部的 try/catch 接不住。JEI 已有类文件核对。AE2 和终端加上同样的“目标方法还在才应用”，核对失败就记日志并跳过。`required: false` 是未知签名变化时的后盾，避免一条可选 Mixin 中止客户端。原版 Mixin 仍是本模组自己的功能，失败应保持显式。

**Alternatives considered**: 所有 Mixin 配置都改成非必需。语言切换或冻结查看悄悄失效时，玩家看不到本模组自身坏了。把失败模组写成 Off。规格要求不改玩家保存的值。
