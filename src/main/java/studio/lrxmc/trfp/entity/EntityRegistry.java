package studio.lrxmc.trfp.entity;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 实体注册器 - 注册自定义 Bullet 与 Target
 * 注意: Paper 不允许像 Forge 一样完全自定义实体,这里用 Arrow + Minecart 替代
 */
public class EntityRegistry {

    public static void register(JavaPlugin plugin) {
        plugin.getLogger().info("[TRFP] EntityRegistry ready (using vanilla Arrow + Minecart base entities)");
    }
}
