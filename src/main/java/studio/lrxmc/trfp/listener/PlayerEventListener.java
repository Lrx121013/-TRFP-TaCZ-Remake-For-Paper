package studio.lrxmc.trfp.listener;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import studio.lrxmc.trfp.TRFPPlugin;
import studio.lrxmc.trfp.config.ConfigManager;
import studio.lrxmc.trfp.entity.BulletHelper;
import studio.lrxmc.trfp.item.ItemRegistry;
import studio.lrxmc.trfp.resource.*;

import java.util.*;

/**
 * 玩家事件监听器 - 服务端核心交互
 * 右键 = 射击 (左键也支持)
 * 潜行 + 右键 = 换弹
 * 切换武器 = 切枪音效
 */
public class PlayerEventListener implements Listener {

    private final JavaPlugin plugin;
    private final ItemRegistry itemRegistry;
    private final GunPackLoader gunPackLoader;
    private final ConfigManager config;
    private final Map<UUID, BukkitTask> reloadTasks = new HashMap<>();
    private final Map<UUID, Long> lastShootTime = new HashMap<>();
    private final Map<UUID, Long> lastMeleeTime = new HashMap<>();
    private final Map<UUID, Boolean> aimingState = new HashMap<>();

    public PlayerEventListener(JavaPlugin plugin, ItemRegistry itemRegistry, GunPackLoader gunPackLoader) {
        this.plugin = plugin;
        this.itemRegistry = itemRegistry;
        this.gunPackLoader = gunPackLoader;
        this.config = ((TRFPPlugin) plugin).getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (itemRegistry == null || !itemRegistry.isGun(hand)) return;

        Action act = event.getAction();
        String gunId = itemRegistry.getGunId(hand);
        GunData data = gunPackLoader.getGun(gunId);
        if (data == null) return;

        // 潜行 + 右键 = 换弹
        if (act == Action.RIGHT_CLICK_AIR || act == Action.RIGHT_CLICK_BLOCK) {
            if (p.isSneaking()) {
                event.setCancelled(true);
                startReload(p, hand, data);
                return;
            }
        }

        // 鼠标 = 射击
        if (act == Action.LEFT_CLICK_AIR || act == Action.LEFT_CLICK_BLOCK ||
            act == Action.RIGHT_CLICK_AIR || act == Action.RIGHT_CLICK_BLOCK) {
            if (!p.isSneaking()) {
                event.setCancelled(true);
                handleShoot(p, hand, data);
            }
        }
    }

    private void handleShoot(Player p, ItemStack hand, GunData data) {
        long now = System.currentTimeMillis();
        long minDelay = 60_000L / Math.min(data.fireRate, config.fireRateCap);
        if (now - lastShootTime.getOrDefault(p.getUniqueId(), 0L) < minDelay) return;

        int mag = itemRegistry.getMagazineCount(hand);
        if (mag <= 0) {
            p.playSound(p.getLocation(), "tacz:fire.empty", SoundCategory.PLAYERS, 1.0f, 1.0f);
            p.sendMessage("§c[TRFP] §f弹药已耗尽,需要换弹!");
            return;
        }

        mag -= 1;
        itemRegistry.setMagazineCount(hand, mag);
        lastShootTime.put(p.getUniqueId(), now);

        // 后坐力
        double recoil = data.recoil * config.defaultRecoil;
        Vector kick = new Vector(
                (Math.random() - 0.5) * 0.01 * recoil,
                recoil * 0.05,
                0
        );
        p.setVelocity(p.getVelocity().add(kick));

        p.playSound(p.getLocation(), silencer(data) ? "tacz:guns.fire.silenced" : "tacz:guns.fire",
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        BulletHelper.fireBullet(p, data.damage * config.damageMultiplier, data.headshotMultiplier, data.armorIgnore);

        if (config.logShoot) {
            plugin.getLogger().info("[TRFP] " + p.getName() + " fired " + data.id + " mag=" + mag);
        }
    }

    private boolean silencer(GunData data) {
        return data.attachments != null && Arrays.asList(data.attachments).contains("muzzle_silencer");
    }

    private void startReload(Player p, ItemStack hand, GunData data) {
        if (reloadTasks.containsKey(p.getUniqueId())) return;
        int cur = itemRegistry.getMagazineCount(hand);
        if (cur >= data.magazineSize) {
            p.sendMessage("§e[TRFP] §f弹匣已满,无需换弹。");
            return;
        }
        p.sendMessage("§a[TRFP] §f开始换弹...");
        if (config.logReload) {
            plugin.getLogger().info("[TRFP] " + p.getName() + " reloading " + data.id);
        }
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int target = data.magazineSize;
            int cap = Math.min(target, config.magazineSizeCap);
            itemRegistry.setMagazineCount(hand, cap);
            p.sendMessage("§a[TRFP] §f换弹完成 (" + cap + "/" + cap + ")");
            p.playSound(p.getLocation(), "tacz:guns.reload", SoundCategory.PLAYERS, 1.0f, 1.0f);
            reloadTasks.remove(p.getUniqueId());
        }, Math.max(20L, data.reloadTime));
        reloadTasks.put(p.getUniqueId(), task);
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!itemRegistry.isGun(hand)) return;
        boolean aiming = event.isSneaking();
        aimingState.put(p.getUniqueId(), aiming);
        p.sendMessage(aiming ? "§a[TRFP] §f进入瞄准" : "§7[TRFP] §f退出瞄准");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!BulletHelper.isTrfpBullet(arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        event.setCancelled(true);

        double damage = event.getDamage();
        boolean headshot = arrow.getLocation().getY() > target.getEyeLocation().getY() - 0.4;
        if (headshot) {
            damage *= 2.0 * config.headshotMultiplier;
        }
        if (target instanceof Player tp) {
            org.bukkit.attribute.AttributeInstance armorAttr = tp.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR);
            if (armorAttr != null) {
                double reduction = 0.04 * Math.min(20, armorAttr.getValue());
                damage = damage * (1.0 - Math.max(0, reduction - 0.05));
            }
        }
        target.damage(damage, shooter);
        arrow.remove();
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        ItemStack newItem = p.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = p.getInventory().getItem(event.getPreviousSlot());
        if (oldItem != null && itemRegistry.isGun(oldItem)) {
            p.playSound(p.getLocation(), "tacz:guns.draw", SoundCategory.PLAYERS, 0.6f, 1.0f);
        }
        if (newItem != null && itemRegistry.isGun(newItem)) {
            p.playSound(p.getLocation(), "tacz:guns.put_away", SoundCategory.PLAYERS, 0.6f, 1.0f);
        }
    }

    @EventHandler
    public void onEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player p)) return;
        long now = System.currentTimeMillis();
        if (now - lastMeleeTime.getOrDefault(p.getUniqueId(), 0L) < 1000) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!itemRegistry.isGun(hand)) return;
        lastMeleeTime.put(p.getUniqueId(), now);
        event.setDamage(4.0);
        p.playSound(p.getLocation(), "tacz:guns.melee", SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        reloadTasks.remove(id);
        lastShootTime.remove(id);
        lastMeleeTime.remove(id);
        aimingState.remove(id);
    }
}
