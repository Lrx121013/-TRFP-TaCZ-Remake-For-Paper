package studio.lrxmc.trfp.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import studio.lrxmc.trfp.resource.AmmoData;
import studio.lrxmc.trfp.resource.AttachmentData;
import studio.lrxmc.trfp.resource.GunData;
import studio.lrxmc.trfp.resource.GunPackLoader;

import java.util.*;

/**
 * 物品注册器 - 使用 PDC + 自定义模型数据 模拟原版 TACZ 物品
 * 物品命名空间保留为 tacz,这样原版资源包内的所有物品模型/贴图/音效可以直接引用
 */
public class ItemRegistry {

    public static final String NAMESPACE = "tacz";
    private final JavaPlugin plugin;
    private final GunPackLoader gunPackLoader;
    private final NamespacedKey gunIdKey;
    private final NamespacedKey ammoKey;
    private final NamespacedKey magKey;        // 弹匣当前余量
    private final NamespacedKey attachmentKey; // 配件槽位 JSON
    private final NamespacedKey skinKey;
    private final NamespacedKey itemTypeKey;
    private final NamespacedKey fireModeKey;

    // ItemType
    public static final String TYPE_GUN = "gun";
    public static final String TYPE_AMMO = "ammo";
    public static final String TYPE_ATTACHMENT = "attachment";
    public static final String TYPE_AMMO_BOX = "ammo_box";

    public ItemRegistry(JavaPlugin plugin, GunPackLoader gunPackLoader) {
        this.plugin = plugin;
        this.gunPackLoader = gunPackLoader;
        this.gunIdKey = new NamespacedKey(plugin, "gun_id");
        this.ammoKey = new NamespacedKey(plugin, "ammo_type");
        this.magKey = new NamespacedKey(plugin, "magazine_count");
        this.attachmentKey = new NamespacedKey(plugin, "attachments");
        this.skinKey = new NamespacedKey(plugin, "skin_id");
        this.itemTypeKey = new NamespacedKey(plugin, "item_type");
        this.fireModeKey = new NamespacedKey(plugin, "fire_mode");
    }

    public void register() {
        plugin.getLogger().info("[TRFP] ItemRegistry ready (namespace: " + NAMESPACE + ")");
    }

    /**
     * 创建一把枪
     */
    public ItemStack createGun(String gunId) {
        GunData data = gunPackLoader.getGun(gunId);
        if (data == null) {
            plugin.getLogger().warning("[TRFP] Unknown gun: " + gunId);
            return null;
        }
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f" + prettyId(gunId));
        meta.setCustomModelData(gunModelId(gunId));
        meta.setLore(Arrays.asList(
                "§7Type: " + data.ammoType,
                "§7Magazine: " + data.magazineSize,
                "§7Fire Rate: " + data.fireRate + " RPM",
                "§7Damage: " + data.damage,
                "",
                "§8[TRFP] " + prettyId(gunId)
        ));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        pdc.set(itemTypeKey, PersistentDataType.STRING, TYPE_GUN);
        pdc.set(gunIdKey, PersistentDataType.STRING, gunId);
        pdc.set(ammoKey, PersistentDataType.STRING, data.ammoType);
        pdc.set(magKey, PersistentDataType.INTEGER, data.magazineSize);
        pdc.set(skinKey, PersistentDataType.STRING, "default");
        pdc.set(fireModeKey, PersistentDataType.STRING, data.fireMode.name());
        pdc.set(attachmentKey, PersistentDataType.STRING, "[]");
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 创建弹药
     */
    public ItemStack createAmmo(String ammoId, int amount) {
        AmmoData data = gunPackLoader.getAmmo(ammoId);
        if (data == null) data = gunPackLoader.getAmmo("7.62x39");
        ItemStack item = new ItemStack(Material.IRON_NUGGET, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f" + data.displayName);
        meta.setCustomModelData(ammoModelId(ammoId));
        meta.setLore(Arrays.asList(
                "§7Caliber: " + ammoId,
                "§7Damage x" + data.damageMultiplier,
                "§7Headshot x" + data.headshotMultiplier,
                "",
                "§8[TRFP] " + data.displayName
        ));
        item.setItemMeta(meta);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(itemTypeKey, PersistentDataType.STRING, TYPE_AMMO);
        pdc.set(ammoKey, PersistentDataType.STRING, ammoId);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 创建配件
     */
    public ItemStack createAttachment(String attachmentId) {
        AttachmentData data = gunPackLoader.getAttachment(attachmentId);
        ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f" + prettyId(attachmentId));
        meta.setCustomModelData(attachmentModelId(attachmentId));
        meta.setLore(Arrays.asList(
                "§7Type: " + (data != null ? data.type : "ATTACHMENT"),
                "§7ID: " + attachmentId,
                "",
                "§8[TRFP] Attachment"
        ));
        item.setItemMeta(meta);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(itemTypeKey, PersistentDataType.STRING, TYPE_ATTACHMENT);
        pdc.set(gunIdKey, PersistentDataType.STRING, attachmentId);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 创建弹匣箱
     */
    public ItemStack createAmmoBox(String ammoId) {
        ItemStack item = new ItemStack(Material.SHULKER_SHELL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6" + prettyId(ammoId) + " Ammo Box");
        meta.setCustomModelData(ammoBoxModelId(ammoId));
        meta.setLore(Arrays.asList(
                "§7Ammo Box: " + ammoId,
                "§7Capacity: 240",
                "",
                "§8[TRFP] Ammo Box"
        ));
        item.setItemMeta(meta);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(itemTypeKey, PersistentDataType.STRING, TYPE_AMMO_BOX);
        pdc.set(ammoKey, PersistentDataType.STRING, ammoId);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 解析物品类型
     */
    public String getItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(itemTypeKey, PersistentDataType.STRING);
    }

    public String getGunId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(gunIdKey, PersistentDataType.STRING);
    }

    public String getAmmoType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ammoKey, PersistentDataType.STRING);
    }

    public int getMagazineCount(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer v = item.getItemMeta().getPersistentDataContainer().get(magKey, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public void setMagazineCount(ItemStack item, int count) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(magKey, PersistentDataType.INTEGER, count);
        item.setItemMeta(meta);
    }

    public boolean isGun(ItemStack item) {
        return TYPE_GUN.equals(getItemType(item));
    }

    public boolean isAmmo(ItemStack item) {
        return TYPE_AMMO.equals(getItemType(item));
    }

    public boolean isAttachment(ItemStack item) {
        return TYPE_ATTACHMENT.equals(getItemType(item));
    }

    public boolean isAmmoBox(ItemStack item) {
        return TYPE_AMMO_BOX.equals(getItemType(item));
    }

    private String prettyId(String id) {
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private int gunModelId(String id) {
        return Math.abs(id.hashCode() % 100000);
    }

    private int ammoModelId(String id) {
        return Math.abs(id.hashCode() % 100000) + 1000;
    }

    private int attachmentModelId(String id) {
        return Math.abs(id.hashCode() % 100000) + 2000;
    }

    private int ammoBoxModelId(String id) {
        return Math.abs(id.hashCode() % 100000) + 3000;
    }

    public NamespacedKey getGunIdKey() { return gunIdKey; }
    public NamespacedKey getAmmoKey() { return ammoKey; }
    public NamespacedKey getMagKey() { return magKey; }
    public NamespacedKey getAttachmentKey() { return attachmentKey; }
    public NamespacedKey getSkinKey() { return skinKey; }
    public NamespacedKey getItemTypeKey() { return itemTypeKey; }
    public NamespacedKey getFireModeKey() { return fireModeKey; }
}
