# [TRFP] TaCZ Remake For Paper — 迁移规范

## Why
原版 `TACZ (Timeless & Classics Guns: Zero)` 是基于 Forge 1.20.1 的 Minecraft 枪械模组(4200+ 文件、86MB 资源、54 把枪、101 个配件),运行在客户端 + 服务端。本次需要将其改造为 Paper 1.21.1 插件形态,保留所有玩法/枪械/资源,去除 Forge 客户端依赖,改为以 Paper 插件 + 资源包的方式分发,最终输出单一 `jar` 文件。完成迁移后,玩家可凭资源包继续使用所有原版贴图、音效、动画 JSON,Lua 状态机逻辑由插件服务端重写,客户端通过资源包 + Bukkit 事件完成体验闭环。

## What Changes
- 全新项目骨架:Gradle + Paper API 1.21.1,产物 `TRFP-TaCZ-Remake-For-Paper-1.0.0.jar`
- 替换全部原版"可辨认文字":`plugin.yml` 名称/作者/描述、`mods.toml` 风格字段被移除(改用 `paper-plugin.yml`)、资源包 `pack.mcmeta` description、各语言文件 `lang/*.json` 中显式署名等
- 完整搬运 `src/main/resources/assets/tacz/**`(贴图/音效/动画/模型/lang/custom-gunpack),`/assets/tacz/custom/tacz_default_gun/**` 资源包目录保持 1:1 命名,客户端自动加载
- 保留全部 54 把枪械数据 + 101 个配件数据(`*_data.json`、`index/*.json`)+ 23 个 Lua 状态机脚本,作为"枪械包"内容
- 服务端核心系统(等同 Forge 1.20.1 实现)重写到 Bukkit/Paper 1.21.1:
  - 枪械物品注册、枪包加载器、JSON 数据索引、配件属性合并
  - 服务器端射击/换弹/拉栓/瞄准/切枪/换弹取消/换弹取消检视等事件
  - 伤害事件 (`EntityDamageByEntityEvent`) 接入后坐力、爆头、护甲穿透
  - 弹道实体 (`Bullet` 自定义实体)、Target 假人靶、Workbench 方块/Menu
  - 资源包自动注入(写入 `server-resource-packs` 或首次进服提示下载)
  - 命令 `/trfp` (原 `/tacz` 的迁移版)、Tab 补全
- 网络层:本阶段不引入 ProtocolLib,改用 Bukkit `Player` API + PacketEvents(可选软依赖),不重写客户端渲染/动画(由资源包 + 原版矿工客户端理解)
- 不做:KubeJS 集成、Cloth Config 客户端界面、JEI 集成、Oculus/Optifine 兼容、Controllable/ShoulderSurfing 等第三方联动(KubeJS 兼容作为后续阶段)
- 移除 `libs/`、`META-INF/accesstransformer.cfg`、`META-INF/mods.toml`、`tacz.mixins.json`(Paper 1.21.1 禁止 Forge Mixin),替换为 `paper-plugin.yml`

## Impact
- 受影响规范:无既有 spec,本次为全新 spec
- 受影响代码:
  - 新增 `build.gradle`、`settings.gradle`、`gradle.properties`、`paper-plugin.yml`、`gradle/wrapper/**`
  - 源码包名由 `com.tacz.guns.*` 迁移为 `studio.lrxmc.trfp.*`(全新包名,避免与原版共存时类冲突)
  - 资源 `src/main/resources/assets/tacz/**` 全部保留(只改 `pack.mcmeta` 与 lang 描述)
  - 删除 `src/main/java/com/tacz/**`(Java 重写)
  - 删除 `libs/**`、`META-INF/accesstransformer.cfg`、`META-INF/mods.toml`
- 兼容性边界:仅在 Paper 1.21.1 服务端 + 原版 Minecraft 1.21.1 客户端(携带本插件资源包)上工作;**不兼容** Forge 版 TACZ 同服(包名已重命名,物品 ID 重新注册,数据格式沿用 1.20.1 即可,但内部 ID 体系需重新建立)

## 关键改动
- 插件标识: name = `[TRFP] TaCZ Remake For Paper`, version = `1.0.0`, author = `Lrxmcstudio (Lrx)`, description = `Lrxmcstudio 工作室 Lrx 移植自 TACZ 的 Paper 1.21.1 枪械插件。`
- 资源包 `pack.mcmeta` description 改为 `TRFP - Timeless & Classics Guns For Paper`,作者写 `Lrxmcstudio / Lrx`
- 所有 `lang/*.json` 中以 "tacz"、"TACZ"、"Timeless & Classics"、"Serene Wave" 形式署名的键值,改为 `trfp`、`TRFP`、`Timeless Reforged`、`Lrxmcstudio`,保持 JSON 键名稳定以免破坏引用

## ADDED Requirements

### Requirement: TRFP 插件骨架
The system SHALL provide a buildable Paper 1.21.1 plugin with Gradle, output a single jar, loadable by Paper servers with API version `1.21`.

#### Scenario: Build success
- **WHEN** developer runs `./gradlew build`
- **THEN** produces `build/libs/TRFP-TaCZ-Remake-For-Paper-1.0.0.jar` containing all classes and resources

#### Scenario: Plugin load
- **WHEN** the jar is placed in `plugins/` and Paper 1.21.1 starts
- **THEN** plugin is enabled, registers commands/items/listeners, sends host resource pack to players on join

### Requirement: 资产完整性保留
The system SHALL include all 86MB of original TACZ assets (textures, sounds, animations, models, lang, custom gunpack data) inside the jar under `assets/tacz/**`, with `assets/tacz/custom/tacz_default_gun/` folder preserved as-is for content reloading.

#### Scenario: Resource pack delivery
- **WHEN** a player joins the server
- **THEN** the embedded resource pack is offered; on accept, the client sees all original textures/sounds/animations for items named `tacz:*`

#### Scenario: Asset zero loss
- **WHEN** comparing `plugins/.../assets/tacz/` against `src/main/resources/assets/tacz/` of original repo
- **THEN** file count and bytes are equal (only `pack.mcmeta` description and `lang/*.json` branding text may differ)

### Requirement: 枪械物品注册
The system SHALL register all 54 guns and 101 attachments as Bukkit custom items (via PersistentDataContainer / PDC-backed items) so players can obtain them via `/trfp give <id>` and via Workbench crafting.

#### Scenario: Give command
- **WHEN** admin runs `/trfp give <player> tacz:ak47`
- **THEN** the player receives a stack of the AK-47 with correct model data, ammo type, attachments slots, and gun data

### Requirement: 枪包数据加载
The system SHALL load `assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/*.json` and `attachments/*.json` and `index/*.json` at server start, building an in-memory index of all gun/attachment definitions, identical to original Forge loader semantics.

#### Scenario: Cold start
- **WHEN** plugin enables with the embedded gunpack
- **THEN** log shows "Loaded 54 guns, 101 attachments" and `/trfp list` lists all of them

### Requirement: 核心枪械交互(服务端)
The system SHALL implement the following server-authoritative mechanics matching the original Forge mod behavior:
- Shoot (左键,服务端验证弹药 + 后坐力 + 散布 + 弹道生成)
- Reload (R 键,服务端换弹逻辑 + 进度同步)
- Bolt (拉栓,手动枪专属)
- Aim / Zoom (右键,服务端下发 POSE/视场)
- Fire Select (切半/全自动)
- Melee (近战,枪托攻击)
- Inspect (检视)
- Magazine attachment state (扩/减弹匣)
- Ammo consumption + per-ammo damage modifiers

#### Scenario: Shooting
- **WHEN** player right-clicks (shoot) with a loaded AK-47 in main hand
- **THEN** server consumes 1 ammo, spawns a `Bullet` entity, plays sound, applies recoil to player rotation, applies damage to hit target

#### Scenario: Reload
- **WHEN** player presses reload key with 0/30 ammo
- **THEN** server starts a reload task, locks fire, plays reload sequence, refills magazine

### Requirement: 自定义实体
The system SHALL register custom entities `Bullet` (抛射物) and `Target` (靶子), with vanilla-compatible spawn/death handling and proper collision detection.

#### Scenario: Bullet hit
- **WHEN** a `Bullet` entity collides with a LivingEntity
- **THEN** server applies damage based on ammo type, headshot multiplier, and armor-ignore modifier, then despawns the bullet

### Requirement: 方块与配方
The system SHALL register Workbench (Gun Smith Table) and Target as Bukkit custom blocks with crafting recipes for all 54 guns using the original `gun_smith_table` recipe data.

#### Scenario: Crafting
- **WHEN** player places required materials in the Workbench GUI
- **THEN** server returns the configured gun item

### Requirement: 命令 `/trfp`
The system SHALL provide `/trfp <subcommand>` replacing the original `/tacz`, with subcommands: `give`, `list`, `reload`, `ammo`, `debug`, `attachment`.

#### Scenario: Reload config
- **WHEN** admin runs `/trfp reload`
- **THEN** server reloads gunpack JSON from disk and re-indexes; running players notified

### Requirement: 资源包自动托管
The system SHALL host the embedded resource pack at the SHA-1 URL of the jar, and set it as server-resource-pack on first player join (with prompt message).

#### Scenario: First join
- **WHEN** player joins for the first time
- **THEN** they see a resource pack prompt with "TRFP - Timeless Reforged" and on accept, all assets are applied

### Requirement: 配置系统
The system SHALL provide `plugins/TRFP/config.yml` for server-side config (ammo limits, fire-rate, head-shot multiplier, debug flags), with default values matching original TACZ defaults.

#### Scenario: Config persistence
- **WHEN** admin edits `config.yml` and runs `/trfp reload`
- **THEN** new values take effect without restart

## MODIFIED Requirements
无(本规范为新增)。

## REMOVED Requirements
### Requirement: Forge 客户端集成
**Reason**: Paper 1.21.1 不运行 Forge 客户端;原版 TACZ 客户端组件(Lua 状态机执行、Bedrock 模型渲染、第一人称动画、Cloth Config 屏幕、JEI 集成、Oculus 兼容)无法在 Paper 端移植。
**Migration**: 客户端视觉效果由资源包承担;Lua 状态机由 Java 状态机替代(关键时机点一一对应);JEI 替换为 `/trfp list` 与配方书;Cloth Config 替换为 `config.yml`;Oculus/Optifine 兼容直接移除(无对应 Paper 端概念)。

### Requirement: Mixin 注入
**Reason**: Paper 1.21.1 服务端没有渲染层,所有 Mixin(`tacz.mixins.json`、`tacz.compat.acceleratedrendering.mixins.json`)用于修改客户端渲染管线,在 Paper 端无对应可注入点。
**Migration**: 由 Bukkit 事件 + 资源包 + 自定义物品/方块/实体替代。

### Requirement: KubeJS 集成
**Reason**: 减少本次迁移规模,降低耦合;KubeJS 是 KubeJS 团队为 Forge 设计的,与 Paper 集成需要适配层。
**Migration**: 留待后续阶段(创建新 spec `trfp-kubejs-adapter`)。

## 限制与免责
- 客户端视觉效果(第一人称持枪姿势、换弹动画、Bedrock 骨骼动画)由原版 Minecraft 客户端 + 资源包(geo.json / animation.json)承担;服务端不保证"动画流畅度"等同 Forge 版
- Lua 状态机映射为 Java 状态机的关键时机(预扣扳机/抛壳/拉栓/换弹结束),但"动画时序精度"无法与 Forge 版完全一致
- 不保证与原 Forge 版 TACZ 同服互通(物品 ID 体系独立)
- 不实现 OBR/Optifine/CIT Runes 等纯客户端特性(玩家需自备)
