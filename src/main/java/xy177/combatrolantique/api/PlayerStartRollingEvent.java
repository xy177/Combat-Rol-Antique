package xy177.combatrolantique.api;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Fired on the server after the player's roll is published. */
public final class PlayerStartRollingEvent extends Event {
    public final EntityPlayerMP player;
    public final Vec3d velocity;
    public PlayerStartRollingEvent(EntityPlayerMP player, Vec3d velocity) { this.player = player; this.velocity = velocity; }
}
