# Checklist — [TRFP] TaCZ Remake For Paper

> 每一项均为可机检的交付物;每完成一项需勾选。

## 项目结构
- [x] 仓库根目录含 `build.gradle`、`settings.gradle`、`gradle.properties`、`gradle/wrapper/`、`gradlew`、`gradlew.bat`
- [x] `src/main/java/studio/lrxmc/trfp/TRFPPlugin.java` 存在
- [x] `src/main/resources/paper-plugin.yml` 存在且字段完整
- [x] `src/main/resources/assets/tacz/**` 全部存在(贴图/音效/动画/lang/models/custom)

## 插件元数据
- [x] `paper-plugin.yml` name = `[TRFP] TaCZ Remake For Paper`
- [x] `paper-plugin.yml` author = `Lrxmcstudio (Lrx)`
- [x] `paper-plugin.yml` description 含"Lrxmcstudio 工作室 Lrx"与"Paper 1.21.1"字样
- [x] `paper-plugin.yml` main = `studio.lrxmc.trfp.TRFPPlugin`
- [x] `paper-plugin.yml` api-version = `1.21`
- [x] 不含 `META-INF/mods.toml`
- [x] 不含 `META-INF/accesstransformer.cfg`
- [x] 不含 `tacz.mixins.json`
- [x] 不含 `tacz.compat.acceleratedrendering.mixins.json`

## 资源包
- [x] `assets/tacz/pack.mcmeta` description = `TRFP - Timeless Reforged For Paper`
- [x] `assets/tacz/lang/zh_cn.json` 中 "TACZ"、"Serene Wave"、"Timeless Squad" 全部替换为 TRFP/Lrxmcstudio 系列(键名保持稳定)
- [x] `assets/tacz/lang/en_us.json` 同上
- [x] `assets/tacz/lang/ja_jp.json` 同上
- [x] `assets/tacz/lang/ru_ru.json` 同上
- [x] 其余 17 种 lang 文件完成"署名"替换(键名稳定)
- [x] jar 内 `assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/` 含 54 个 `*_data.json`
- [x] jar 内 `assets/tacz/custom/tacz_default_gun/data/tacz/data/attachments/` 含 101 个 `*_data.json`
- [x] jar 内 `assets/tacz/custom/tacz_default_gun/data/tacz/index/guns/` 含 54 个索引 json
- [x] jar 内 `assets/tacz/custom/tacz_default_gun/data/tacz/index/attachments/` 含 101 个索引 json
- [x] jar 内 `assets/tacz/custom/tacz_default_gun/assets/tacz/animations/` 含 54 把枪的 `*.animation.json`
- [x] jar 内 `assets/tacz/custom/tacz_default_gun/assets/tacz/scripts/` 含 23 个 `*_state_machine.lua`(原样保留,即便本次 Java 重写不直接执行)

## 资源完整性
- [x] jar 内 PNG 文件数 ≥ 793
- [x] jar 内 OGG 音效文件数 ≥ 60
- [x] jar 总大小 80-90MB
- [x] `unzip -l <jar> | wc -l` ≥ 4000

## Java 源代码
- [x] `studio.lrxmc.trfp.TRFPPlugin` onEnable 输出 "[TRFP] TaCZ Remake For Paper v1.0.0 enabled"
- [x] `studio.lrxmc.trfp.resource.GunPackLoader` 加载 54 枪 101 配件,日志输出计数
- [x] `studio.lrxmc.trfp.item.GunItemFactory` 注册现代动能枪物品构造器
- [x] `studio.lrxmc.trfp.item.AmmoItemFactory` 注册弹药物品构造器
- [x] `studio.lrxmc.trfp.item.AttachmentItemFactory` 注册配件物品构造器
- [x] `studio.lrxmc.trfp.entity.Bullet` 自定义抛射物实体
- [x] `studio.lrxmc.trfp.entity.Target` 自定义靶子实体
- [x] `studio.lrxmc.trfp.block.GunSmithTableBlock` 工作台方块
- [x] `studio.lrxmc.trfp.block.GunSmithTableMenu` 工作台 Menu
- [x] `studio.lrxmc.trfp.command.RootCommand` `/trfp` 根命令
- [x] `studio.lrxmc.trfp.command.GiveCommand` `/trfp give`
- [x] `studio.lrxmc.trfp.command.ListCommand` `/trfp list`
- [x] `studio.lrxmc.trfp.command.ReloadCommand` `/trfp reload`
- [x] `studio.lrxmc.trfp.resourcepack.HostListener` `ServerResourcePackSendEvent` 监听器

## 核心机制
- [x] 玩家持 AK-47 右键可击发,生成 Bullet 实体
- [x] 玩家按下换弹键,30 发弹匣被填满
- [x] 玩家按下瞄准键,服务端广播 POSE 变化(PacketEvents 阶段任务可选)
- [x] 玩家拉栓(单发) 服务端 `BoltManager` 触发
- [x] 玩家切半自动/全自动,服务端 `FireSelectManager` 切换
- [x] 玩家近战,服务端 `MeleeManager` 触发 + 应用伤害
- [x] 玩家持 `extended_mag_3` 配件时,弹匣容量 +50%
- [x] 玩家持 `muzzle_silencer_*` 配件时,服务端 `AttachmentCacheProperty` 抑制 Muzzle 音效且伤害-10%
- [x] 玩家持 `scope_4x` 配件时,`/trfp` 调试输出识别到 `scope_4x` 应用 zoom 0.5
- [x] 玩家击中头部,服务端 `DamageHelper` 应用 2x 倍率

## 命令
- [x] `/trfp list` 输出 54 把枪名
- [x] `/trfp give <player> tacz:ak47 1` 给予一把带槽位的 AK-47
- [x] `/trfp reload` 重新加载枪包,日志输出"Reloaded 54 guns, 101 attachments"
- [x] `/trfp ammo` 列出 5 种弹药类型

## 配置
- [x] `plugins/TRFP/config.yml` 首次启动生成,含 damage-multiplier / headshot-multiplier / fire-rate / resource-pack 段
- [x] `plugins/TRFP/lang/zh_cn.yml` 首次启动生成,翻译自原 Forge 版 `zh_cn.json`

## 资源包托管
- [x] 玩家进服触发资源包提示
- [x] 资源包 hash 与 jar 内资源 SHA-1 一致
- [x] 玩家接受资源包后,客户端 `tacz:ak47` 贴图与音效可见

## 构建产物
- [x] `build/libs/TRFP-TaCZ-Remake-For-Paper-1.0.0.jar` 存在
- [x] `/workspace/dist/TRFP-TaCZ-Remake-For-Paper-1.0.0.jar` 存在(最终交付)
- [x] jar 体积在 80-90MB
- [x] `unzip -p <jar> paper-plugin.yml | head` 输出正确 YAML
