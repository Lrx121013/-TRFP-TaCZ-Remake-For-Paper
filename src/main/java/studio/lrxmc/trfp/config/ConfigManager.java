package studio.lrxmc.trfp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import studio.lrxmc.trfp.TRFPPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * 配置管理器
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private File configFile;
    private YamlConfiguration config;

    // 配置项
    public double damageMultiplier = 1.0;
    public double headshotMultiplier = 2.0;
    public double armorIgnoreBase = 0.0;
    public int fireRateCap = 1200;
    public double defaultInaccuracy = 1.0;
    public double defaultRecoil = 1.0;
    public int magazineSizeCap = 200;

    // 资源包配置
    public boolean resourcePackEnabled = true;
    public boolean resourcePackForce = true;
    public String resourcePackPrompt = "§a[TRFP] §f服务器请求安装资源包以显示所有枪械贴图与音效。\n§7  Author: §fLrxmcstudio (Lrx)";

    // 调试
    public boolean debug = false;
    public boolean logShoot = false;
    public boolean logReload = false;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        damageMultiplier = config.getDouble("combat.damage-multiplier", 1.0);
        headshotMultiplier = config.getDouble("combat.headshot-multiplier", 2.0);
        armorIgnoreBase = config.getDouble("combat.armor-ignore-base", 0.0);
        fireRateCap = config.getInt("combat.fire-rate-cap", 1200);
        defaultInaccuracy = config.getDouble("combat.default-inaccuracy", 1.0);
        defaultRecoil = config.getDouble("combat.default-recoil", 1.0);
        magazineSizeCap = config.getInt("combat.magazine-size-cap", 200);

        resourcePackEnabled = config.getBoolean("resource-pack.enabled", true);
        resourcePackForce = config.getBoolean("resource-pack.force", true);
        resourcePackPrompt = config.getString("resource-pack.prompt-message",
                "§a[TRFP] §f服务器请求安装资源包以显示所有枪械贴图与音效。\n§7  Author: §fLrxmcstudio (Lrx)");

        debug = config.getBoolean("debug.enabled", false);
        logShoot = config.getBoolean("debug.log-shoot", false);
        logReload = config.getBoolean("debug.log-reload", false);

        logger.info("[TRFP] Config loaded: damage=" + damageMultiplier + " headshot=" + headshotMultiplier);
    }

    private void saveDefaultConfig() {
        try (InputStream in = plugin.getResource("config.yml")) {
            if (in != null) {
                java.nio.file.Files.copy(in, configFile.toPath());
                return;
            }
        } catch (IOException ignored) {}

        // 内置默认配置
        configFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration def = new YamlConfiguration();
        def.set("combat.damage-multiplier", 1.0);
        def.set("combat.headshot-multiplier", 2.0);
        def.set("combat.armor-ignore-base", 0.0);
        def.set("combat.fire-rate-cap", 1200);
        def.set("combat.default-inaccuracy", 1.0);
        def.set("combat.default-recoil", 1.0);
        def.set("combat.magazine-size-cap", 200);
        def.set("resource-pack.enabled", true);
        def.set("resource-pack.force", true);
        def.set("resource-pack.prompt-message", "§a[TRFP] §f服务器请求安装资源包以显示所有枪械贴图与音效。\n§7  Author: §fLrxmcstudio (Lrx)");
        def.set("resource-pack.url", "");
        def.set("resource-pack.hash", "");
        def.set("debug.enabled", false);
        def.set("debug.log-shoot", false);
        def.set("debug.log-reload", false);
        try {
            def.save(configFile);
        } catch (IOException e) {
            logger.warning("[TRFP] Failed to save default config: " + e.getMessage());
        }
    }

    public void reload() {
        load();
    }

    public File getConfigFile() {
        return configFile;
    }
}
