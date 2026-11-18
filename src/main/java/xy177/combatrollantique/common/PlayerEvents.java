package xy177.combatrollantique.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.combatrollantique.network.RollNetwork;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerEvents {
    private static final Map<UUID, Integer> invulnerable = new HashMap<>();
    public static void invulnerable(EntityPlayerMP player, int ticks) { if (ticks > 0) invulnerable.put(player.getUniqueID(), ticks); }
    @SubscribeEvent public void construct(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof EntityPlayer) RollAttributes.register((EntityPlayer)event.getEntity());
    }
    @SubscribeEvent public void join(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player=(EntityPlayer) event.getEntity();
            RollAttributes.register(player);
            if (!player.world.isRemote) RollAttributes.equipmentChanged(player);
        }
    }
    @SubscribeEvent public void equipment(LivingEquipmentChangeEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().world.isRemote) RollAttributes.equipmentChanged((EntityPlayer)event.getEntityLiving());
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) { sync(event.player); }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent event) { sync(event.player); }
    private void sync(EntityPlayer player) {
        RollAttributes.equipmentChanged(player);
        if (player instanceof EntityPlayerMP) RollNetwork.sync((EntityPlayerMP)player);
    }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event) { invulnerable.remove(event.player.getUniqueID()); }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) return;
        UUID id=event.player.getUniqueID();
        Integer remaining=invulnerable.get(id);
        if (remaining != null) { if (remaining <= 1) invulnerable.remove(id); else invulnerable.put(id, remaining - 1); }
    }
    @SubscribeEvent public void attack(LivingAttackEvent event) {
        if (!event.getEntityLiving().world.isRemote && invulnerable.containsKey(event.getEntityLiving().getUniqueID())) event.setCanceled(true);
    }
}
