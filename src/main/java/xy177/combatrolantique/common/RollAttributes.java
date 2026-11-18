package xy177.combatrolantique.common;

import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import java.util.UUID;

public final class RollAttributes {
    public static final IAttribute DISTANCE = attribute("distance", 3, 1, 24);
    public static final IAttribute RECHARGE = attribute("recharge", 20, 0.1, 200);
    public static final IAttribute COUNT = attribute("count", 1, 0, 20);
    private static final UUID DISTANCE_ID = UUID.fromString("b4050559-353d-4c15-a520-1f1a8f4ec7b1");
    private static final UUID RECHARGE_ID = UUID.fromString("0e55b4f8-9cce-4a95-99ca-34ca07498146");
    private static final UUID COUNT_ID = UUID.fromString("ccac9074-305c-46c7-a8c0-5ac123ee4ad7");

    private static IAttribute attribute(String name, double value, double min, double max) {
        return new RangedAttribute(null, "combatrolantique." + name, value, min, max).setShouldWatch(true);
    }
    public static void register(EntityPlayer player) {
        for (IAttribute attr : new IAttribute[]{DISTANCE, RECHARGE, COUNT}) {
            if (player.getAttributeMap().getAttributeInstance(attr) == null) player.getAttributeMap().registerAttribute(attr);
        }
    }
    public static double value(EntityPlayer player, IAttribute attribute) {
        register(player);
        return player.getEntityAttribute(attribute).getAttributeValue();
    }
    public static void equipmentChanged(EntityPlayer player) {
        register(player);
        modifier(player, DISTANCE, DISTANCE_ID, EnchantmentHelper.getEnchantmentLevel(RollContent.LONGFOOTED, player.getItemStackFromSlot(EntityEquipmentSlot.FEET)), 0);
        modifier(player, RECHARGE, RECHARGE_ID, 0.2 * EnchantmentHelper.getEnchantmentLevel(RollContent.ACROBAT, player.getItemStackFromSlot(EntityEquipmentSlot.CHEST)), 1);
        modifier(player, COUNT, COUNT_ID, EnchantmentHelper.getEnchantmentLevel(RollContent.MULTI_ROLL, player.getItemStackFromSlot(EntityEquipmentSlot.HEAD)), 0);
    }
    private static void modifier(EntityPlayer player, IAttribute attr, UUID id, double amount, int operation) {
        IAttributeInstance instance = player.getEntityAttribute(attr);
        AttributeModifier previous = instance.getModifier(id);
        if (previous != null && previous.getAmount() == amount) return;
        if (previous != null) instance.removeModifier(previous);
        if (amount != 0) instance.applyModifier(new AttributeModifier(id, "Combat roll enchantment", amount, operation).setSaved(false));
    }
}
