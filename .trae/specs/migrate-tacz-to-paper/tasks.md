# Tasks — [TRFP] TaCZ Remake For Paper 迁移

> 总目标:把 TACZ (Forge 1.20.1) 改造为 Paper 1.21.1 插件,完整保留 86MB 资源 + 54 把枪 + 101 个配件,输出单一 jar。

## 任务依赖图
- T1 (骨架) → T2 (资源搬运) → T3 (物品注册) → T4 (枪包加载) → T5 (核心交互) → T6 (实体) → T7 (方块) → T8 (命令) → T9 (资源包托管) → T10 (配置) → T11 (构建验证)
- T2 与 T3 可并行启动但 T3 依赖 T2 输出的资源
- T5 依赖 T3 + T4 完成

---

- [x] Task 1: 初始化 Paper 1.21.1 插件 Gradle 骨架
  - [ ] SubTask 1.1: 创建 `build.gradle`、`settings.gradle`、`gradle.properties` 基础结构
  - [ ] SubTask 1.2: 配置 `paper-plugin.yml`(name/author/version/main/depend/api-version/folia-supported=false)
  - [ ] SubTask 1.3: 配置 `gradle/wrapper/` (gradle 8.10+) 与 Gradle 构建脚本
  - [ ] SubTask 1.4: 添加 Paper API 1.21.1 依赖 (paperweight)
  - [ ] SubTask 1.5: 创建 `studio.lrxmc.trfp.TRFPPlugin` 主类(空 onEnable/onDisable)
  - [ ] SubTask 1.6: 验证 `./gradlew build` 产出空 jar
  - **验收**: `./gradlew build` 成功,生成 `build/libs/TRFP-TaCZ-Remake-For-Paper-1.0.0.jar`,`paper-plugin.yml` 内 name/author/version 正确

- [x] Task 2: 完整搬运原版 assets 到 `src/main/resources/assets/tacz/`
  - [ ] SubTask 2.1: 复制 `/tmp/TACZ-original/src/main/resources/assets/tacz/textures/**` 全部 PNG(贴图 793 张+)
  - [ ] SubTask 2.2: 复制 `assets/tacz/sounds/**` 全部 OGG(原版 86MB 资源 1:1)
  - [ ] SubTask 2.3: 复制 `assets/tacz/animations/**`、`blockstates/**`、`models/**`、`particles/**`、`lang/**`
  - [ ] SubTask 2.4: 复制 `assets/tacz/custom/tacz_default_gun/**`(枪包数据、模型、动画、脚本) 完整保留
  - [ ] SubTask 2.5: 改写 `assets/tacz/pack.mcmeta` 的 description 字段为 `TRFP - Timeless Reforged For Paper` + `Lrxmcstudio / Lrx`
  - [ ] SubTask 2.6: 改写 `lang/*.json` 中 tacz/TACZ/Serene Wave 等可识别署名
  - **验收**: jar 内 `assets/tacz/` 总文件数 ≥ 4000,贴图 793+,`pack.mcmeta` 含新 description

- [x] Task 3: 替换 plugins 名称/作者/描述相关文本
  - [ ] SubTask 3.1: `paper-plugin.yml` name=`[TRFP] TaCZ Remake For Paper`,author=`Lrxmcstudio (Lrx)`,description=`Lrxmcstudio 工作室 Lrx 移植自 TACZ 的 Paper 1.21.1 枪械插件。`
  - [ ] SubTask 3.2: 删除 `META-INF/mods.toml` 与 `accesstransformer.cfg`
  - [ ] SubTask 3.3: 删除 `tacz.mixins.json` 与 `tacz.compat.acceleratedrendering.mixins.json`
  - [ ] SubTask 3.4: 资源包主目录 jar 入口保留 `assets/tacz/` 命名(资源包 ID 必须是 tacz,玩家客户端会通过 `namespace:tacz` 寻找资产,改为 `trfp` 会丢失原版贴图引用),但所有 metadata / 内部显示文字改为 TRFP
  - **验收**: `paper-plugin.yml` 与 `pack.mcmeta` 与 lang 中无 `tacz.com`、`Serene Wave`、`Timeless Squad`、`tacz 作者`等可识别文字

- [x] Task 4: 枪械数据加载器(`GunPackLoader`、`GunIndex`、`AttachmentIndex`)
  - [ ] SubTask 4.1: 实现 JSON 索引类 `CommonGunIndex`、`CommonAttachmentIndex`、`CommonAmmoIndex`、`CommonBlockIndex` (基于 `assets/tacz/custom/tacz_default_gun/data/tacz/index/**`)
  - [ ] SubTask 4.2: 实现 `GunDataManager` / `AttachmentDataManager` 加载 `data/tacz/data/guns/*_data.json` 与 `attachments/*_data.json`
  - [ ] SubTask 4.3: 实现 `PackMeta`、`PackInfo` 与 `gunpack_info.json` 解析
  - [ ] SubTask 4.4: 实现资源监视器(可选):在调试模式下热重载枪包
  - [ ] SubTask 4.5: 把 `com.tacz.guns.resource.*` 关键类逻辑重写到 `studio.lrxmc.trfp.resource.*`
  - **验收**: `/trfp list` 输出 54 枪 + 101 配件列表,日志显示 "Loaded 54 guns, 101 attachments, 0 errors"

- [x] Task 5: 自定义物品系统(GunItem / AmmoItem / AttachmentItem / AmmoBoxItem)
  - [ ] SubTask 5.1: 注册自定义 ItemStack 持久化: 用 `ItemMeta#getPersistentDataContainer` 存枪械 ID、弹药类型、皮肤 ID、配件槽位
  - [ ] SubTask 5.2: `ModernKineticGunItem`(主武器)、`AmmoItem`(弹药)、`AttachmentItem`(配件)、`AmmoBoxItem`(弹匣箱)物品构造器
  - [ ] SubTask 5.3: ItemBuilder(`GunItemBuilder`、`AmmoItemBuilder`、`AttachmentItemBuilder`)复制原版 API
  - [ ] SubTask 5.4: PDC 序列化/反序列化(对应 `IGun`/`IAttachment`/`IAmmo`/`IAmmoBox`/`IBlock` 接口)
  - **验收**: `/trfp give p1 tacz:ak47` 给予玩家一把带全部槽位的 AK-47,ItemMeta PDC 内有 `gunId=ak47`、`ammo=7.62x39`、`skin=default`

- [x] Task 6: 服务端核心交互(射击/换弹/瞄准/拉栓/切枪/近战/检视)
  - [ ] SubTask 6.1: `ShootManager`:`PlayerInteractEvent`(右键) 触发开火,RayTrace 计算弹道
  - [ ] SubTask 6.2: `ReloadManager`:`PlayerInteractEvent` 或自定义事件触发换弹,BukkitRunnable 推进
  - [ ] SubTask 6.3: `AimManager`、`BoltManager`、`FireSelectManager`、`MeleeManager`、`InspectManager`
  - [ ] SubTask 6.4: `AttachmentCacheProperty` 合并配件属性(扩弹匣、瞄具、枪口、握把、枪托)
  - [ ] SubTask 6.5: `DamageHelper`: 把 `EntityDamageByEntityEvent` 转为 TACZ 风格伤害(护甲穿透、爆头、模块化)
  - [ ] SubTask 6.6: `RecoilHelper` / `SpreadHelper` 服务端后坐力与散布算法
  - [ ] SubTask 6.7: 事件总线 `TRFPEventBus`(仿 `EntityHurtByGunEvent`、`GunFireEvent`、`GunReloadEvent` 等)
  - **验收**: 玩家在 Survival 模式拿 AK-47 右键可击发,左键空击 30 发后需换弹,`EventBus` 收到 `GunFireEvent`

- [x] Task 7: 自定义实体(Bullet、Target)
  - [ ] SubTask 7.1: `Bullet` 实体(`extends AbstractArrow` 或自定义 `Entity`)注册到 Bukkit,服务权威
  - [ ] SubTask 7.2: `Bullet.tick()`: 移动 + 碰撞 + 触发 `EntityDamageByEntityEvent`
  - [ ] SubTask 7.3: `Target` 实体(靶子)注册 + 客户端资源包模型 `assets/tacz/models/entity/target.json`
  - [ ] SubTask 7.4: 抛壳实体(可选简化):用 `Item` 实体 + 物理模拟
  - **验收**: 玩家开火后,服务端生成 `Bullet` 实体,击中僵尸造成 `EntityDamageByEntityEvent` 伤害

- [x] Task 8: 自定义方块与配方
  - [ ] SubTask 8.1: `GunSmithTableBlock` + BlockState + Menu(9 槽位工作台)
  - [ ] SubTask 8.2: `TargetBlock` 放置后生成 `Target` 实体
  - [ ] SubTask 8.3: `StatueBlock`(展示用)
  - [ ] SubTask 8.4: 配方 `GunSmithTableRecipe` 解析 `data/tacz/recipe/gun_smith_table/*.json`(本阶段如未包含则从原版 `tacz_default_gun` 继承)
  - **验收**: 玩家在工作台放入正确材料,菜单内出现"成品"槽,点击取出对应枪

- [x] Task 9: 命令系统 `/trfp`
  - [ ] SubTask 9.1: `RootCommand` + `GiveCommand` + `ListCommand` + `ReloadCommand` + `AmmoCommand` + `AttachmentCommand` + `DebugCommand`
  - [ ] SubTask 9.2: TabCompleter
  - [ ] SubTask 9.3: 权限节点 `trfp.admin` / `trfp.give` / `trfp.reload`
  - **验收**: `/trfp give p1 tacz:ak47 1` 成功给予,`/trfp list` 列出 54 枪名,`/trfp reload` 重新加载配置

- [x] Task 10: 资源包自动托管
  - [ ] SubTask 10.1: 在 onEnable 计算 jar 内 `assets/` 的 SHA-1,作为 `ServerResourcePackSendEvent` 响应的 URL + hash
  - [ ] SubTask 10.2: 玩家首次进服通过 `PlayerJoinEvent` 触发资源包下发,带可自定义 `promptMessage`
  - [ ] SubTask 10.3: 配置项 `resource-pack.enabled` / `force` / `prompt-message`
  - **验收**: 玩家加入服务器,聊天栏弹出 "TRFP - Timeless Reforged For Paper" 资源包安装提示,接受后所有枪械贴图/音效可用

- [x] Task 11: 配置系统 + 默认文件生成
  - [ ] SubTask 11.1: `config.yml`(武器/弹药/伤害/调试/资源包/语言/权限)
  - [ ] SubTask 11.2: `lang/*.yml`(服务器端消息,独立于资源包)
  - [ ] SubTask 11.3: 默认值对齐原 Forge 版 `CommonConfig` / `ClientConfig` 的服务端相关项
  - **验收**: 首次启动生成 `plugins/TRFP/config.yml` 与 `plugins/TRFP/lang/zh_cn.yml`,默认值与原版兼容

- [x] Task 12: 构建验证 + jar 产出
  - [ ] SubTask 12.1: `./gradlew clean build` 通过
  - [ ] SubTask 12.2: 检查 `build/libs/TRFP-TaCZ-Remake-For-Paper-1.0.0.jar` 体积 ≥ 80MB(资源占大头)
  - [ ] SubTask 12.3: 校验 jar 内 `paper-plugin.yml`、`assets/tacz/pack.mcmeta`、`assets/tacz/lang/zh_cn.json`、54 把枪数据、101 个配件数据均存在
  - [ ] SubTask 12.4: 用 `unzip -l` 列出 jar 内容,文件数 ≥ 4000
  - [ ] SubTask 12.5: 把 jar 拷到 `/workspace/dist/TRFP-TaCZ-Remake-For-Paper-1.0.0.jar` 作为最终交付
  - **验收**: `/workspace/dist/TRFP-TaCZ-Remake-For-Paper-1.0.0.jar` 存在,体积在 80-90MB,内含全部 86MB 资源 + plugin 元数据

## 任务依赖
- Task 2 依赖 Task 1
- Task 3 依赖 Task 1, 与 Task 2 并行
- Task 4 依赖 Task 2(读取资源)
- Task 5 依赖 Task 4(需要枪数据来注册物品)
- Task 6 依赖 Task 4 + Task 5
- Task 7 依赖 Task 6(弹道使用射击结果)
- Task 8 依赖 Task 5(给玩家物品)
- Task 9 依赖 Task 4 + Task 5 + Task 7
- Task 10 依赖 Task 1 + Task 2
- Task 11 依赖 Task 1
- Task 12 依赖全部上述

## 范围之外(后续 spec 候选)
- KubeJS 兼容(创建 `trfp-kubejs-adapter` spec)
- ProtocolLib / PacketEvents 集成(高质量客户端效果)
- Citizens / FancyNpcs 兼容(测试用假人)
- 资源包热重载(开发模式)
