package studio.lrxmc.trfp;

import org.bukkit.plugin.java.JavaPlugin;
import studio.lrxmc.trfp.command.RootCommand;
import studio.lrxmc.trfp.config.ConfigManager;
import studio.lrxmc.trfp.entity.EntityRegistry;
import studio.lrxmc.trfp.item.ItemRegistry;
import studio.lrxmc.trfp.listener.PlayerEventListener;
import studio.lrxmc.trfp.listener.ResourcePackListener;
import studio.lrxmc.trfp.resource.GunPackLoader;

/**
 * [TRFP] TaCZ-Remake-For-Paper 插件主类
 * Lrxmcstudio 工作室 Lrx 移植自 TACZ (Forge 1.20.1) → Paper 1.21.1
 */
public final class TRFPPlugin extends JavaPlugin {

    private static TRFPPlugin instance;
    private GunPackLoader gunPackLoader;
    private ItemRegistry itemRegistry;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        long startMs = System.currentTimeMillis();

        getLogger().info("================================================");
        getLogger().info("[TRFP] TaCZ-Remake-For-Paper v" + getDescription().getVersion());
        getLogger().info("Author: " + getDescription().getAuthors());
        getLogger().info("Loading gunpack data, please wait...");
        getLogger().info("================================================");

        // 1. 加载配置
        this.configManager = new ConfigManager(this);
        configManager.load();

        // 2. 加载枪包数据
        this.gunPackLoader = new GunPackLoader(this);
        gunPackLoader.load();

        // 3. 注册物品
        this.itemRegistry = new ItemRegistry(this, gunPackLoader);
        itemRegistry.register();

        // 4. 注册实体
        EntityRegistry.register(this);

        // 5. 注册命令
        RootCommand rootCommand = new RootCommand(this, itemRegistry, gunPackLoader);
        getCommand("trfp").setExecutor(rootCommand);
        getCommand("trfp").setTabCompleter(rootCommand);

        // 6. 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this, itemRegistry, gunPackLoader), this);
        getServer().getPluginManager().registerEvents(new ResourcePackListener(this, configManager), this);

        long elapsed = System.currentTimeMillis() - startMs;
        getLogger().info("================================================");
        getLogger().info("[TRFP] Loaded " + gunPackLoader.getGunCount() + " guns, " + gunPackLoader.getAttachmentCount() + " attachments in " + elapsed + "ms");
        getLogger().info("[TRFP] TaCZ-Remake-For-Paper v" + getDescription().getVersion() + " enabled");
        getLogger().info("================================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("[TRFP] TaCZ-Remake-For-Paper v" + getDescription().getVersion() + " disabled");
        if (gunPackLoader != null) {
            gunPackLoader.unload();
        }
        instance = null;
    }

    public static TRFPPlugin getInstance() {
        return instance;
    }

    public GunPackLoader getGunPackLoader() {
        return gunPackLoader;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
