# Quickstart: 011 JourneyMap 路径点拼音

## 有 JourneyMap

1. Dev：`localRuntime` 已拉 JM（`1.21.1-6.0.9+neoforge`）后 `./gradlew runClient`。  
2. 打开管理路径点（默认 **N**）。  
3. 新建路径点，名称设为「制图塔」（或任一中文名）；可先放进自定义分组。  
4. 搜索框输入首字母（如 `ztt`）或全拼片段 → 应列出该点。  
5. 输入汉字「制图」→ 仍应列出。  
6. 左侧切换分组后保持搜索词 → 列表仍应按拼音/原文过滤（分组 + 搜索同时可用）。  
7. 列表上显示名仍为中文原文；存档/路径点 JSON 无新增拼音字段。

## 无 JourneyMap

1. 从 `build.gradle` 去掉 `localRuntime` JM，或使用不含 JM 的实例。  
2. 启动进世界 → 无崩溃；日志无 `WaypointManagerQueryService` 相关错误。

## 单测

```bash
./gradlew test --tests pnnh.mcbilingual.search.WaypointNameMatchTest
```
