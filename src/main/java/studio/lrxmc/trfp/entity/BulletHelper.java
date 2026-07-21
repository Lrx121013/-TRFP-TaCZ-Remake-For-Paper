package studio.lrxmc.trfp.entity;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 子弹辅助 - 用 Arrow 实体替代,带伤害与穿透逻辑
 */
public class BulletHelper {

    private static final Set<UUID> trfpBullets = new HashSet<>();
    private static final double BULLET_SPEED = 60.0;

    /**
     * 发射一颗子弹
     * @param shooter 射手
     * @param damage 基础伤害
     * @param headshotMul 爆头倍率
     * @param armorIgnore 护甲穿透
     */
    public static void fireBullet(Player shooter, double damage, double headshotMul, double armorIgnore) {
        World world = shooter.getWorld();
        Location eye = shooter.getEyeLocation();
        Vector direction = eye.getDirection().normalize().multiply(BULLET_SPEED);
        // 散布 - 简单随机偏移
        direction.add(new Vector(
                (Math.random() - 0.5) * 0.05,
                (Math.random() - 0.5) * 0.05,
                (Math.random() - 0.5) * 0.05
        ));

        Arrow arrow = world.spawnArrow(eye, direction, (float) BULLET_SPEED, 0.0f);
        arrow.setShooter(shooter);
        arrow.setDamage(0); // 禁用原版伤害,我们自己算
        arrow.setSilent(true);
        arrow.setVisualFire(false);
        arrow.setCritical(false);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        trfpBullets.add(arrow.getUniqueId());
        arrow.setCustomName("trfp_bullet");
        arrow.setCustomNameVisible(false);

        // 服务端命中追踪 (因为原版 Arrow 会触发 EntityDamageByEntityEvent)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!arrow.isValid() || arrow.isDead()) {
                    trfpBullets.remove(arrow.getUniqueId());
                    cancel();
                }
                if (arrow.isOnGround() || arrow.getTicksLived() > 100) {
                    arrow.remove();
                    trfpBullets.remove(arrow.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("TRFP-TaCZ-Remake-For-Paper"), 1L, 1L);
    }

    public static boolean isTrfpBullet(Entity entity) {
        return entity instanceof Arrow && trfpBullets.contains(entity.getUniqueId());
    }
}
