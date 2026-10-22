# Quickstart: JEI 中英与拼音搜索

依赖 `001-bilingual-tooltips` 的语言表已经在客户端加载。

## 准备

- `./gradlew test` 覆盖拼音，不启动游戏。
- `./gradlew runClient` 会通过 `localRuntime` 加载 JEI `19.64.0.455`。中文语言包仍放在 `run/mods`，原版物品自带 `zh_cn`。
- 验证环境不要安装 Just Enough Characters。

## 自动检查

`PinyinTokensTest` 至少包含：

- `苹果` → full `pingguo`，initials `pg`
- 混合大小写输入由搜索侧折叠，词本身是小写
- `TNT` 或中英混排里的拉丁字符不产生假拼音
- 空字符串和纯空白没有词
- 多音字只产出一个读音

## 客户端检查

1. 游戏语言为简体中文。JEI 输入一个原版物品的英文名，该物品出现。
2. 切到英文主语言。JEI 输入同一物品的中文名，该物品出现。
3. 输入 `pingguo` 和 `pg`，苹果出现。再输入 `PINGGUO`，结果相同。
4. 打开该物品的提示框和 Jade。看得见的名称里没有 `pingguo` 或 `pg`。
5. 连续打字时结果跟着变，不出现资源重载画面。
6. 用 003 的快捷键切换主语言后，不重启，重复步骤 1 到 3。
7. 卸下 JEI 启动，客户端能进入世界。

抽查规格里的 10 个物品可以在同一次会话完成。未跑的步骤不要记成通过。
