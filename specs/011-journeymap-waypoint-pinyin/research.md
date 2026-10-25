# Research: JourneyMap 路径点拼音

## 为何不能只靠官方 API

- **Decision**: UI 搜索用 Mixin；API（`getAllWaypoints` 等）不负责管理界面过滤。
- **Rationale**: [JourneyMap API](https://github.com/TeamJM/journeymap-api) 面向创建/查询路径点与图层，无「注册搜索匹配器」。JM 本体源码不开源（ARR），过滤点需从运行 jar 定位。
- **Alternatives**: 自建路径点列表 UI — 超出范围且体验分裂。

## 拼音

- **Decision**: 复用 `PinyinTokens`；匹配逻辑抽 `WaypointNameMatch`（可单测）。
- **Rationale**: 与 JEI/AE2 语义一致；名称是任意字符串，不需要 LanguageTexts。
- **Alternatives**: 预计算写入 JM custom data — 污染存档且无必要。

## Mixin 稳健性

- **Decision**: 独立 mixin config；JM 缺失时不加载；注入失败用 try/可选 `required=false`，保证不拖垮客户端。
- **Rationale**: FR-003 / FR-006；JM 版本漂移常见。
- **Alternatives**: 反射每次搜索 — 更慢且同样脆。

## 版本钉扎

- **Decision**: `journeymap_version=1.21.1-6.0.9+neoforge`（Modrinth NeoForge 1.21.1 稳定），`compileOnly` + `localRuntime`。
- **Rationale**: 与 Jade/JEI 模式一致。

## 注入点（T006，钉 6.0.9）

- **Class**: `journeymap.client.ui.waypointmanager.WaypointManagerQueryService`
- **Method**: private synthetic `lambda$buildWaypointRows$6(String search, ClientWaypointImpl waypoint)Z`
- **Caller**: `buildWaypointRows` 在 `selection.search()` 上挂的 `Stream.filter`（约 line 145）
- **Native logic**: 空 search → true；否则 `waypoint.getName().toLowerCase(ROOT).contains(search.toLowerCase(ROOT))`
- **Mixin**: `WaypointManagerQueryServiceMixin` `@Inject` RETURN，若原生为 false 再 OR `WaypointNameMatch.matches`；`require = 0` + try/catch，JM 改名时不拖垮客户端。
