package xy177.combatrolantique.common;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.combatrolantique.CombatRolAntique;

public final class RollContent {
    // The source weight of Longfooted is 3, which is absent from vanilla's rarity enum.
    private static final Enchantment.Rarity WEIGHT_THREE = EnumHelper.addEnum(Enchantment.Rarity.class, "COMBAT_ROL_ANTIQUE_WEIGHT_THREE", new Class<?>[]{int.class}, 3);
    public static final Enchantment LONGFOOTED = new RollEnchantment("longfooted", WEIGHT_THREE, EnumEnchantmentType.ARMOR_FEET, EntityEquipmentSlot.FEET, 4, 12, 8, 20, 10);
    public static final Enchantment ACROBAT = new RollEnchantment("acrobat", Enchantment.Rarity.UNCOMMON, EnumEnchantmentType.ARMOR_CHEST, EntityEquipmentSlot.CHEST, 5, 10, 5, 20, 7);
    public static final Enchantment MULTI_ROLL = new RollEnchantment("multi_roll", Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.ARMOR_HEAD, EntityEquipmentSlot.HEAD, 3, 30, 15, 40, 20);
    public static final SoundEvent ROLL = sound("roll");
    public static final SoundEvent READY = sound("roll_cooldown_ready");
    private static SoundEvent sound(String name) {
        ResourceLocation id = new ResourceLocation(CombatRolAntique.MOD_ID, name);
        return new SoundEvent(id).setRegistryName(id);
    }
    @SubscribeEvent public void enchantments(RegistryEvent.Register<Enchantment> event) { event.getRegistry().registerAll(LONGFOOTED, ACROBAT, MULTI_ROLL); }
    @SubscribeEvent public void sounds(RegistryEvent.Register<SoundEvent> event) { event.getRegistry().registerAll(ROLL, READY); }

    public static int anvilCost(Enchantment enchantment,int vanilla) {
        if (enchantment==MULTI_ROLL) return 10;
        if (enchantment==LONGFOOTED || enchantment==ACROBAT) return 5;
        return vanilla;
    }
    private static final class RollEnchantment extends Enchantment {
        private final int levels, minBase, minStep, maxBase, maxStep;
        RollEnchantment(String name, Rarity rarity, EnumEnchantmentType type, EntityEquipmentSlot slot, int levels, int minBase, int minStep, int maxBase, int maxStep) {
            super(rarity, type, new EntityEquipmentSlot[]{slot});
            setRegistryName(CombatRolAntique.MOD_ID, name);
            setName(CombatRolAntique.MOD_ID + "." + name);
            this.levels=levels; this.minBase=minBase; this.minStep=minStep; this.maxBase=maxBase; this.maxStep=maxStep;
        }
        @Override public int getMaxLevel() { return levels; }
        @Override public int getMinEnchantability(int level) { return minBase + (level - 1) * minStep; }
        @Override public int getMaxEnchantability(int level) { return maxBase + (level - 1) * maxStep; }
    }
}
