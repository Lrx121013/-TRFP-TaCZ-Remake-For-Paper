package studio.lrxmc.trfp.resource;

/**
 * 枪械数据 POJO — 对应原版 gun_data.json
 */
public class GunData {
    public String id;
    public String ammoType;
    public int magazineSize;
    public int fireRate;          // RPM
    public double damage;         // 基础伤害
    public double headshotMultiplier;
    public double armorIgnore;
    public double inaccuracy;     // 散布
    public double recoil;         // 后坐力
    public int reloadTime;        // ticks
    public FireMode fireMode;
    public int maxAmmoLimit;
    public BoltType boltType;
    public String[] attachments;  // 槽位: scope, muzzle, grip, stock, magazine, extended_mag, light_extended_mag, ammo_mod

    public enum FireMode { SEMI, AUTO, BURST }
    public enum BoltType { MANUAL, SEMI_AUTO, AUTO }
}
