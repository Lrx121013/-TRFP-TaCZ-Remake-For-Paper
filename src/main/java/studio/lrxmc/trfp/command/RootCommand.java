package studio.lrxmc.trfp.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.lrxmc.trfp.TRFPPlugin;
import studio.lrxmc.trfp.item.ItemRegistry;
import studio.lrxmc.trfp.resource.AmmoData;
import studio.lrxmc.trfp.resource.AttachmentData;
import studio.lrxmc.trfp.resource.GunData;
import studio.lrxmc.trfp.resource.GunPackLoader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * /trfp 主命令 - 取代原版 /tacz
 */
public class RootCommand implements CommandExecutor, TabCompleter {

    private final TRFPPlugin plugin;
    private final ItemRegistry itemRegistry;
    private final GunPackLoader gunPackLoader;

    public RootCommand(TRFPPlugin plugin, ItemRegistry itemRegistry, GunPackLoader gunPackLoader) {
        this.plugin = plugin;
        this.itemRegistry = itemRegistry;
        this.gunPackLoader = gunPackLoader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give":    return cmdGive(sender, args);
            case "list":    return cmdList(sender, args);
            case "reload":  return cmdReload(sender);
            case "ammo":    return cmdAmmo(sender);
            case "attachment": return cmdAttachment(sender, args);
            case "debug":   return cmdDebug(sender, args);
            case "info":    return cmdInfo(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean cmdGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("trfp.give") && !sender.hasPermission("trfp.admin")) {
            sender.sendMessage("§c[TRFP] 你没有权限使用此命令。");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§c用法: /trfp give <player> <gun_id|ammo_id|attachment_id> [amount]");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§c[TRFP] 找不到玩家: " + args[1]);
            return true;
        }
        String id = args[2];
        int amount = args.length >= 4 ? Math.max(1, parseInt(args[3])) : 1;

        ItemStack item = null;
        if (gunPackLoader.getGun(id) != null) {
            item = itemRegistry.createGun(id);
        } else if (gunPackLoader.getAttachment(id) != null) {
            item = itemRegistry.createAttachment(id);
            item.setAmount(amount);
        } else if (gunPackLoader.getAmmo(id) != null) {
            item = itemRegistry.createAmmo(id, amount);
        } else {
            sender.sendMessage("§c[TRFP] 找不到物品: " + id);
            return true;
        }
        if (item == null) return true;

        HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            target.getWorld().dropItemNaturally(target.getLocation(), item);
        }
        sender.sendMessage("§a[TRFP] 成功给予 §e" + target.getName() + " §a" + amount + " 个 §e" + id + "§a。");
        target.sendMessage("§a[TRFP] 你获得了 §e" + amount + " §a个 §e" + id + "§a。");
        return true;
    }

    private boolean cmdList(CommandSender sender, String[] args) {
        sender.sendMessage("§a§l[TRFP] §f枪械列表 (共 " + gunPackLoader.getGunCount() + " 把):");
        List<String> guns = new ArrayList<>(gunPackLoader.getAllGuns().keySet());
        Collections.sort(guns);
        for (int i = 0; i < guns.size(); i += 7) {
            StringBuilder sb = new StringBuilder("§7");
            for (int j = i; j < Math.min(i + 7, guns.size()); j++) {
                sb.append("§f").append(guns.get(j)).append("§7, ");
            }
            sender.sendMessage(sb.toString());
        }
        return true;
    }

    private boolean cmdReload(CommandSender sender) {
        if (!sender.hasPermission("trfp.reload") && !sender.hasPermission("trfp.admin")) {
            sender.sendMessage("§c[TRFP] 你没有权限。");
            return true;
        }
        long t = System.currentTimeMillis();
        gunPackLoader.unload();
        gunPackLoader.load();
        plugin.getConfigManager().reload();
        sender.sendMessage("§a[TRFP] 重新加载完成 (耗时 " + (System.currentTimeMillis() - t) + " ms, " +
                gunPackLoader.getGunCount() + " 枪, " + gunPackLoader.getAttachmentCount() + " 配件)");
        return true;
    }

    private boolean cmdAmmo(CommandSender sender) {
        sender.sendMessage("§a§l[TRFP] §f弹药类型 (共 " + gunPackLoader.getAmmoCount() + " 种):");
        for (AmmoData a : gunPackLoader.getAllAmmo().values()) {
            sender.sendMessage("§7- §f" + a.id + " §7(" + a.displayName + ") x" + a.damageMultiplier + " dmg x" + a.headshotMultiplier + " hs");
        }
        return true;
    }

    private boolean cmdAttachment(CommandSender sender, String[] args) {
        sender.sendMessage("§a§l[TRFP] §f配件列表 (共 " + gunPackLoader.getAttachmentCount() + " 个):");
        List<String> atts = new ArrayList<>(gunPackLoader.getAllAttachments().keySet());
        Collections.sort(atts);
        for (int i = 0; i < atts.size(); i += 7) {
            StringBuilder sb = new StringBuilder("§7");
            for (int j = i; j < Math.min(i + 7, atts.size()); j++) {
                sb.append("§f").append(atts.get(j)).append("§7, ");
            }
            sender.sendMessage(sb.toString());
        }
        return true;
    }

    private boolean cmdDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("trfp.admin")) {
            sender.sendMessage("§c[TRFP] 你没有权限。");
            return true;
        }
        if (args.length >= 2) {
            String op = args[1].toLowerCase();
            switch (op) {
                case "on": case "enable":
                    plugin.getConfig().set("debug.enabled", true);
                    sender.sendMessage("§a[TRFP] 调试已启用");
                    return true;
                case "off": case "disable":
                    plugin.getConfig().set("debug.enabled", false);
                    sender.sendMessage("§a[TRFP] 调试已关闭");
                    return true;
            }
        }
        sender.sendMessage("§7[TRFP] 调试: " + (plugin.getConfigManager().debug ? "§aON" : "§cOFF"));
        return true;
    }

    private boolean cmdInfo(CommandSender sender) {
        sender.sendMessage("§a§l[TRFP] §fTaCZ-Remake-For-Paper v" + plugin.getDescription().getVersion());
        sender.sendMessage("§7  Author: §fLrxmcstudio (Lrx)");
        sender.sendMessage("§7  Guns: §f" + gunPackLoader.getGunCount());
        sender.sendMessage("§7  Attachments: §f" + gunPackLoader.getAttachmentCount());
        sender.sendMessage("§7  Ammo Types: §f" + gunPackLoader.getAmmoCount());
        sender.sendMessage("§7  API: §fPaper 1.21.1");
        sender.sendMessage("§7  Migrated from: §fTACZ Forge 1.20.1 (MCModderAnchor)");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§a§l========== [TRFP] TaCZ-Remake-For-Paper ==========");
        sender.sendMessage("§7  /trfp info §f- 插件信息");
        sender.sendMessage("§7  /trfp list §f- 列出所有枪械");
        sender.sendMessage("§7  /trfp ammo §f- 列出所有弹药");
        sender.sendMessage("§7  /trfp attachment §f- 列出所有配件");
        sender.sendMessage("§7  /trfp give <player> <id> [amount] §f- 给予物品");
        sender.sendMessage("§7  /trfp reload §f- 重新加载配置与枪包");
        sender.sendMessage("§7  /trfp debug [on|off] §f- 调试开关");
        sender.sendMessage("§7  Author: §fLrxmcstudio (Lrx)");
        sender.sendMessage("§a§l================================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("info", "list", "ammo", "attachment", "give", "reload", "debug"), args[0]);
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give": {
                    List<String> names = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
                    return filter(names, args[1]);
                }
                case "debug": return filter(Arrays.asList("on", "off"), args[1]);
            }
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            List<String> ids = new ArrayList<>();
            ids.addAll(gunPackLoader.getAllGuns().keySet());
            ids.addAll(gunPackLoader.getAllAttachments().keySet());
            ids.addAll(gunPackLoader.getAllAmmo().keySet());
            return filter(ids, args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> src, String prefix) {
        String l = prefix.toLowerCase();
        return src.stream().filter(s -> s.toLowerCase().startsWith(l)).collect(Collectors.toList());
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 1; }
    }
}
