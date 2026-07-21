package studio.lrxmc.trfp.resource;

/**
 * 配件数据 POJO — 对应原版 attachment_data.json
 */
public class AttachmentData {
    public String id;
    public Type type;
    public double zoomMultiplier;
    public double extraDamage;
    public double extraHeadshot;
    public double extraArmorIgnore;
    public double extraInaccuracy;
    public int extraMagazineSize;
    public double silencedDamageMultiplier;
    public boolean silencer;

    public enum Type {
        SCOPE,           // 瞄具
        SIGHT,           // 准星
        MUZZLE,          // 枪口
        GRIP,            // 握把
        STOCK,           // 枪托
        EXTENDED_MAG,    // 扩弹匣
        LIGHT_EXTENDED_MAG,
        SHOTGUN_EXTENDED_MAG,
        SNIPER_EXTENDED_MAG,
        AMMO_MOD,        // 弹药改造
        BAYONET          // 刺刀
    }
}
