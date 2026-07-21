package studio.lrxmc.trfp.resource;

import org.bukkit.plugin.java.JavaPlugin;
import studio.lrxmc.trfp.util.MiniJson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 枪包加载器 - 从 jar 内 assets/tacz/custom/tacz_default_gun/ 读取所有 JSON 数据
 * 使用内部 MiniJson 解析器,不依赖 gson
 */
public class GunPackLoader {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final Map<String, GunData> gunIndex = new LinkedHashMap<>();
    private final Map<String, AttachmentData> attachmentIndex = new LinkedHashMap<>();
    private final Map<String, AmmoData> ammoIndex = new LinkedHashMap<>();

    public GunPackLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void load() {
        long start = System.currentTimeMillis();
        logger.info("[TRFP] Loading gunpack from embedded resources...");

        loadGunsFromJar();
        loadAttachmentsFromJar();
        loadAmmoFromJar();

        long elapsed = System.currentTimeMillis() - start;
        logger.info("[TRFP] Gunpack loaded in " + elapsed + "ms");
    }

    private void loadGunsFromJar() {
        try {
            java.io.File jarFile = new java.io.File(plugin.getClass().getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (!jarFile.isFile()) {
                loadFromFilesystem();
                return;
            }
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith("assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/")
                            && name.endsWith("_data.json")) {
                        String id = name.substring(name.lastIndexOf('/') + 1, name.length() - "_data.json".length());
                        try (InputStream is = jar.getInputStream(entry)) {
                            String text = readAll(is);
                            Map<String, Object> json = MiniJson.parseObject(text);
                            GunData data = parseGun(id, json);
                            if (data != null) {
                                gunIndex.put(id, data);
                            }
                        } catch (Exception e) {
                            logger.warning("[TRFP] Failed to load gun: " + name + " - " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("[TRFP] Failed to read from jar: " + e.getMessage());
            loadFromFilesystem();
        }
    }

    private void loadAttachmentsFromJar() {
        try {
            java.io.File jarFile = new java.io.File(plugin.getClass().getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (!jarFile.isFile()) return;
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith("assets/tacz/custom/tacz_default_gun/data/tacz/data/attachments/")
                            && name.endsWith("_data.json")) {
                        String id = name.substring(name.lastIndexOf('/') + 1, name.length() - "_data.json".length());
                        try (InputStream is = jar.getInputStream(entry)) {
                            String text = readAll(is);
                            Map<String, Object> json = MiniJson.parseObject(text);
                            AttachmentData data = parseAttachment(id, json);
                            if (data != null) {
                                attachmentIndex.put(id, data);
                            }
                        } catch (Exception e) {
                            logger.warning("[TRFP] Failed to load attachment: " + name + " - " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("[TRFP] Failed to load attachments: " + e.getMessage());
        }
    }

    private String readAll(InputStream is) throws java.io.IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    private void loadAmmoFromJar() {
        AmmoData[] defaults = new AmmoData[] {
            ammo("7.62x39", "7.62x39mm", 64, 1.0, 2.0, 0.0),
            ammo("5.56x45", "5.56x45mm", 64, 1.0, 1.8, 0.0),
            ammo("9x19", "9x19mm Parabellum", 64, 0.6, 1.5, 0.0),
            ammo("12gauge", "12 Gauge", 32, 0.8, 1.6, 0.0),
            ammo("50bmg", ".50 BMG", 32, 2.0, 3.0, 0.3)
        };
        for (AmmoData a : defaults) ammoIndex.put(a.id, a);
    }

    private AmmoData ammo(String id, String name, int stack, double dmgMul, double headMul, double armor) {
        AmmoData a = new AmmoData();
        a.id = id; a.displayName = name; a.stackSize = stack;
        a.damageMultiplier = dmgMul; a.headshotMultiplier = headMul; a.armorIgnore = armor;
        return a;
    }

    private void loadFromFilesystem() {
        java.io.File base = new java.io.File("src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns");
        if (!base.exists()) return;
        java.io.File[] files = base.listFiles((d, n) -> n.endsWith("_data.json"));
        if (files == null) return;
        for (java.io.File f : files) {
            try (java.io.FileReader r = new java.io.FileReader(f)) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[4096];
                int n;
                while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
                String id = f.getName().replace("_data.json", "");
                Map<String, Object> json = MiniJson.parseObject(sb.toString());
                GunData data = parseGun(id, json);
                if (data != null) gunIndex.put(id, data);
            } catch (Exception e) {
                logger.warning("[TRFP] Failed to load: " + f.getName() + " - " + e.getMessage());
            }
        }
    }

    private GunData parseGun(String id, Map<String, Object> json) {
        try {
            GunData g = new GunData();
            g.id = id;
            Map<String, Object> ammo = MiniJson.getMap(json, "ammo");
            g.ammoType = ammo == null ? "7.62x39" : MiniJson.getString(ammo, "type", "7.62x39");
            g.magazineSize = ammo == null ? 30 : MiniJson.getInt(ammo, "magazine_size", 30);
            g.reloadTime = ammo == null ? 40 : MiniJson.getInt(ammo, "reload_time", 40);
            Map<String, Object> dmg = MiniJson.getMap(json, "damage");
            g.damage = dmg == null ? 8.0 : MiniJson.getDouble(dmg, "base", 8.0);
            g.headshotMultiplier = dmg == null ? 2.0 : MiniJson.getDouble(dmg, "head_shot", 2.0);
            g.armorIgnore = dmg == null ? 0.0 : MiniJson.getDouble(dmg, "armor_ignore", 0.0);
            Map<String, Object> shoot = MiniJson.getMap(json, "shoot");
            g.fireRate = shoot == null ? 600 : MiniJson.getInt(shoot, "rpm", 600);
            g.inaccuracy = shoot == null ? 1.0 : MiniJson.getDouble(shoot, "inaccuracy", 1.0);
            g.recoil = shoot == null ? 1.0 : MiniJson.getDouble(shoot, "recoil", 1.0);
            g.fireMode = id.contains("auto") || id.contains("ak") || id.contains("m4") || id.contains("hk") || id.contains("aug") || id.contains("g36") || id.contains("fn") || id.contains("mp5") || id.contains("ump") || id.contains("p90") || id.contains("vector") || id.contains("minigun") || id.contains("rpk") || id.contains("m249") || id.contains("scar") || id.contains("type_81") || id.contains("qbz") || id.contains("uzi") || id.contains("lonetrail")
                    ? GunData.FireMode.AUTO : GunData.FireMode.SEMI;
            g.boltType = id.contains("kar") || id.contains("m700") || id.contains("m95") || id.contains("m107") || id.contains("ai_awp") || id.contains("springfield") || id.contains("timeless50") || id.contains("db_") || id.contains("taurus") || id.contains("rhino") || id.contains("mk14")
                ? GunData.BoltType.MANUAL : GunData.BoltType.SEMI_AUTO;
            g.maxAmmoLimit = g.magazineSize;
            g.attachments = new String[]{"scope", "sight", "muzzle", "grip", "stock", "extended_mag", "light_extended_mag", "ammo_mod"};
            return g;
        } catch (Exception e) {
            return null;
        }
    }

    private AttachmentData parseAttachment(String id, Map<String, Object> json) {
        try {
            AttachmentData a = new AttachmentData();
            a.id = id;
            if (id.startsWith("scope_")) a.type = AttachmentData.Type.SCOPE;
            else if (id.startsWith("sight_")) a.type = AttachmentData.Type.SIGHT;
            else if (id.startsWith("muzzle_")) a.type = AttachmentData.Type.MUZZLE;
            else if (id.startsWith("grip_")) a.type = AttachmentData.Type.GRIP;
            else if (id.startsWith("stock_")) a.type = AttachmentData.Type.STOCK;
            else if (id.startsWith("shotgun_extended_mag")) a.type = AttachmentData.Type.SHOTGUN_EXTENDED_MAG;
            else if (id.startsWith("sniper_extended_mag")) a.type = AttachmentData.Type.SNIPER_EXTENDED_MAG;
            else if (id.startsWith("light_extended_mag")) a.type = AttachmentData.Type.LIGHT_EXTENDED_MAG;
            else if (id.startsWith("extended_mag")) a.type = AttachmentData.Type.EXTENDED_MAG;
            else if (id.startsWith("ammo_mod_")) a.type = AttachmentData.Type.AMMO_MOD;
            else if (id.startsWith("bayonet_")) a.type = AttachmentData.Type.BAYONET;
            else a.type = AttachmentData.Type.GRIP;
            a.zoomMultiplier = 0.5;
            a.extraMagazineSize = 0;
            a.silencer = id.contains("silencer");
            a.silencedDamageMultiplier = a.silencer ? 0.9 : 1.0;
            return a;
        } catch (Exception e) {
            return null;
        }
    }

    public void unload() {
        gunIndex.clear();
        attachmentIndex.clear();
        ammoIndex.clear();
    }

    public int getGunCount() { return gunIndex.size(); }
    public int getAttachmentCount() { return attachmentIndex.size(); }
    public int getAmmoCount() { return ammoIndex.size(); }

    public Map<String, GunData> getAllGuns() { return Collections.unmodifiableMap(gunIndex); }
    public Map<String, AttachmentData> getAllAttachments() { return Collections.unmodifiableMap(attachmentIndex); }
    public Map<String, AmmoData> getAllAmmo() { return Collections.unmodifiableMap(ammoIndex); }

    public GunData getGun(String id) { return gunIndex.get(id); }
    public AttachmentData getAttachment(String id) { return attachmentIndex.get(id); }
    public AmmoData getAmmo(String id) { return ammoIndex.get(id); }
}
