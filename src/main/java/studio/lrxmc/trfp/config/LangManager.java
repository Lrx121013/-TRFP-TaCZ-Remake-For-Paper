package studio.lrxmc.trfp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * 服务器端 lang 文件生成器 - 独立于资源包
 */
public class LangManager {
    private final JavaPlugin plugin;
    private File langDir;

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();
        // 默认生成 zh_cn.yml 与 en_us.yml
        saveDefault("zh_cn.yml", getDefaultZhCn());
        saveDefault("en_us.yml", getDefaultEnUs());
    }

    private void saveDefault(String name, String content) {
        File f = new File(langDir, name);
        if (f.exists()) return;
        try (java.io.FileWriter w = new java.io.FileWriter(f)) {
            w.write(content);
        } catch (IOException e) {
            plugin.getLogger().warning("[TRFP] Failed to save lang " + name + ": " + e.getMessage());
        }
    }

    private String getDefaultZhCn() {
        return "# [TRFP] 服务器端语言文件 - zh_cn\n" +
                "prefix: \"§a§l[TRFP]§r \"\n" +
                "no-per Theorie: \"§c没有找到对应的 ID。\"\n" +
                "no-permission: \"§c你没有权限执行该命令。\"\n" +
                "give-success: \"§a成功给予 §e%player% §a一把 §e%gun% §a。\"\n" +
                "reload-success: \"§a所有枪包已重新加载,耗时 §e%time% ms§a。\"\n" +
                "gun-list-header: \"§a=== [TRFP] 枪械列表 (共 %count% 把) ===\"\n" +
                "attachment-list-header: \"§a=== [TRFP] 配件列表 (共 %count% 个) ===\"\n" +
                "ammo-list-header: \"§a=== [TRFP] 弹药列表 (共 %count% 种) ===\"\n" +
                "shoot-no-ammo: \"§c弹药已耗尽,需要换弹!\"\n" +
                "reload-start: \"§a开始换弹...\"\n" +
                "reload-done: \"§a换弹完成。\"\n" +
                "recoil-warning: \"§e你的后坐力过大,准星上抬了。\"\n";
    }

    private String getDefaultEnUs() {
        return "# [TRFP] Server-side language file - en_us\n" +
                "prefix: \"§a§l[TRFP]§r \"\n" +
                "no-id: \"§cNo matching ID found.\"\n" +
                "no-permission: \"§cYou don't have permission to do this.\"\n" +
                "give-success: \"§aGave §e%gun% §ato §e%player%§a.\"\n" +
                "reload-success: \"§aAll gunpacks reloaded in §e%time% ms§a.\"\n" +
                "gun-list-header: \"§a=== [TRFP] Gun List (%count% guns) ===\"\n" +
                "attachment-list-header: \"§a=== [TRFP] Attachment List (%count% items) ===\"\n" +
                "ammo-list-header: \"§a=== [TRFP] Ammo List (%count% types) ===\"\n" +
                "shoot-no-ammo: \"§cOut of ammo! Reload needed!\"\n" +
                "reload-start: \"§aReloading...\"\n" +
                "reload-done: \"§aReload complete.\"\n" +
                "recoil-warning: \"§eRecoil kicked in, your crosshair went up.\"\n";
    }
}
