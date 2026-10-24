# Quickstart: 007-freeze-extended-translate

## 单测（不启游戏）

```text
./gradlew test --tests pnnh.mcbilingual.dictionary.*
./gradlew test --tests pnnh.mcbilingual.translate.*
```

覆盖：词典命中/未命中、句末断句、模式优先级纯函数（若抽出）。

## 客户端：词典取词

1. `./gradlew runClient`，主语言英文。
2. 打开含英文 UI 的界面，F3+K 定格。
3. 保持默认取词模式，悬停常见词 → 应立即出现中文释义。
4. 若该处另有整段汉化，确认**不会**自动弹出整段。

## 客户端：对照段

1. 定格后按模式键或点浮窗切到对照段。
2. 悬停有中英语言包的多句任务/按钮文案 → 整段对照。
3. 悬停玩家聊天一句（扩展翻译开且 Ollama 已 `pull qwen2.5:3b`）→ 单句机翻或加载后结果。

## Ollama 有 / 无

1. **有**: 安装 Ollama，`ollama pull qwen2.5:1.5b` 与 `qwen2.5:3b`，扩展翻译保持默认开；词典未收录词应能机翻。
2. **无**: 停掉 Ollama，扩展翻译保持开；需机翻的悬停**每次**看到错误；进入世界仍成功。
3. 在客户端配置关闭扩展翻译后，错误提示停止；词典与语言包对照仍可用。

## 无法翻译

悬停无字符串可采区域（若可构造）→ 「无法翻译」，无 OCR。

## 完成标准

编译通过不算完成。上述取词、对照段、配置开关、Ollama 有/无至少各走通一次。
