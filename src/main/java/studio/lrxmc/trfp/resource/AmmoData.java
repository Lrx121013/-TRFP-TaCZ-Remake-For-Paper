package studio.lrxmc.trfp.resource;

/**
 * 弹药数据 POJO
 */
public class AmmoData {
    public String id;
    public String displayName;
    public int stackSize;
    public double damageMultiplier;
    public double headshotMultiplier;
    public double armorIgnore;
    public int speed;
    public boolean explosive;       // HE 弹
    public boolean incendiary;      // 燃烧
    public boolean slug;            // 独头弹
    public boolean hollowPoint;      // 空尖弹
}
