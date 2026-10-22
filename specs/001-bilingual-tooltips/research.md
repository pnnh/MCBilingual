# Research: 物品与 Jade 双语名称

## 语言表如何加载

- **Decision**: 在客户端资源重载时，用当前 `ResourceManager` 分别构建 `zh_cn` 和 `en_us` 两份 `ClientLanguage`，保存在 `LanguageTexts`。与原版语言加载走同一条读取路径。
- **Rationale**: 译文已经在资源包和其他模组的语言文件里。单独再解析一份不会改掉游戏当前语言，也能在第一次打开提示框前准备好第二语言。
- **Alternatives considered**: 每次渲染时临时切换语言再读词，会污染当前语言并增加卡顿。把 I18nUpdateMod 打进编译依赖，违反宪法。

## 何时算「没有译文」

- **Decision**: 查询结果为空、只有空白，或等于翻译键时，视为缺失。与当前标题忽略大小写和首尾空白后相同，也视为不需要额外行。
- **Rationale**: 原版缺词会把键本身交回来。规格禁止把键显示成第二行。
- **Alternatives considered**: 缺词时回退到英文。那会在中文主语言下把英文又显示一遍，或在英文主语言下造出假的中文行。

## 标题与第二行的来源

- **Decision**: 标题永远用游戏已经生成的悬停名。第二行用物品描述翻译键在另一种语言里的文本。铁砧名只留在标题上，第二行仍查原物品名。
- **Rationale**: 自定义名没有可靠译文。规格要求保留自定义名，并允许语言包原名作为第二行。
- **Alternatives considered**: 不给有自定义名的物品加第二行。玩家会失去原名对照，规格允许显示语言包原名。

## 插入位置

- **Decision**: 在物品提示收集事件里把第二行插到索引 1，或紧接在标题及其附着行之后。再用提示组件收集事件，按我们标记过的文本把该行归位到标题下。同一轮收集只保留一行。
- **Rationale**: 有的模组会在名称收集之后重排组件。PolyglotTooltip 在 NeoForge 1.21.1 上同时处理这两个时机，本项目按这个行为重写，不复制其源文件。
- **Alternatives considered**: 只改一个事件。另一个事件重排时第二行会掉到说明中间，通不过 FR-006。

## 视觉

- **Decision**: 第二行使用灰色。本阶段不提供配置。
- **Rationale**: 规格只要求弱于标题。灰色是原版提示框里次要文字的常见外观。
- **Alternatives considered**: 复制 PolyglotTooltip 的 `&7` 配置项。本阶段规格没有配置需求。

## Jade

- **Decision**: `compileOnly` Jade 1.21.1 NeoForge 构建。插件类使用 Jade 的插件注解，只注册方块（含流体方块）和实体的名称组件。主模组类不引用这些类。`neoforge.mods.toml` 把 Jade 标成客户端可选依赖，且在 Jade 存在时排在它之后。开发运行用 `localRuntime`，不发布该依赖。
- **Rationale**: PolyglotTooltip `main` 已用 Jade `15.10.5+neoforge` 验证过这条插件路径。精确构件版本在实现时对照 Modrinth 或 Maven 上的 1.21.1 NeoForge 构建再定，避免写死一个未在本仓库解析过的版本。
- **Alternatives considered**: 用 mixin 改 Jade。插件 API 足够插入名称，而且未安装时不会加载。

## 游戏语言在语言对之外

- **Decision**: 标题保持游戏当前名称。中英两份里与标题不同的文本各加一行，顺序固定为中文然后英文，并去掉与标题重复的行。
- **Rationale**: 对应规格边界。003 在第一次按键前也要保持这个行为。
- **Alternatives considered**: 强行把主语言改成中文。那是 003 的按键行为，不是查看提示框的行为。

## 与后续功能的接缝

- **Decision**: `LanguageTexts` 提供按语言代码读取译文、读取当前游戏语言、列出需要附加的语言。003 切换主语言时只改变游戏当前语言并刷新界面，不改这个查询规则。002 从这里读取中英物品名。
- **Rationale**: 三份规格共享同一对语言，不应各读各的。
- **Alternatives considered**: 每个功能自己加载语言文件。会重复解析，也可能在切换后不一致。
