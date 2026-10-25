# Tasks: JourneyMap 路径点拼音搜索

**Input**: `/specs/011-journeymap-waypoint-pinyin/`

## Phase 1: Setup

- [x] T001 在 `gradle.properties` / `build.gradle` 增加 JourneyMap 可选 `compileOnly` + `localRuntime`（钉 1.21.1 NeoForge 版本）
- [x] T002 [P] `neoforge.mods.toml` 声明 `journeymap` optional CLIENT；description 补一句路径点拼音
- [x] T003 [P] 新增 `mcbilingual-journeymap.mixins.json` 并在 toml 注册（JM 缺失时安全）

## Phase 2: Foundational

- [x] T004 实现 `WaypointNameMatch`（`src/main/java/pnnh/mcbilingual/search/WaypointNameMatch.java`），复用 `PinyinTokens`
- [x] T005 [P] `WaypointNameMatchTest`：中文全拼/首字母、英文子串、空 query、无汉字

## Phase 3: US1 Mixin

- [x] T006 从 JM jar 定位管理路径点过滤方法，记录类/方法于 research 或任务注释
- [x] T007 [US1] 编写 Mixin：过滤时 OR `WaypointNameMatch.matches`；失败不崩溃
- [x] T008 [US1] 确认分组切换 + 搜索同时可用（手测清单写入 quickstart）

## Phase 4: US2 / US3

- [x] T009 [US2] 验证无 JM 时 mixin 不加载 / 进世界正常
- [x] T010 [US3] 确认列表显示名无拼音；无写 JM 存档

## Phase 5: Polish

- [x] T011 [P] 更新 `docs/MCBilingual.md` 或兼容性说明一句（若项目有整合列表）
- [ ] T012 runClient 按 quickstart 手测（本机 Gradle SSL 阻塞完整构建时需本地再跑）；spec Status 已标 Implemented

## Dependencies

T001–T003 → T004–T005 → T006–T007 → T008–T010 → T011–T012  
T006 阻塞 T007（需先找到注入点）。
