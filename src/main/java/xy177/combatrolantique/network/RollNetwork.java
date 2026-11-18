package xy177.combatrolantique.network;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.Side;
import xy177.combatrolantique.CombatRolAntique;
import xy177.combatrolantique.api.PlayerStartRollingEvent;
import xy177.combatrolantique.common.PlayerEvents;
import xy177.combatrolantique.config.RollConfig;

public final class RollNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(CombatRolAntique.MOD_ID);
    private static final Gson GSON = new Gson();
    public static void init() {
        CHANNEL.registerMessage(PublishHandler.class, Publish.class, 0, Side.SERVER);
        CHANNEL.registerMessage(AnimationHandler.class, Animation.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(ConfigHandler.class, ServerConfig.class, 2, Side.CLIENT);
    }
    public static void sync(EntityPlayerMP player) { CHANNEL.sendTo(new ServerConfig(RollConfig.server), player); }
    public static class Publish implements IMessage {
        double x, y, z;
        public Publish() { }
        public Publish(Vec3d direction) { x=direction.x; y=direction.y; z=direction.z; }
        @Override public void toBytes(ByteBuf buf) { buf.writeDouble(x).writeDouble(y).writeDouble(z); }
        @Override public void fromBytes(ByteBuf buf) { x=buf.readDouble(); y=buf.readDouble(); z=buf.readDouble(); }
    }
    public static final class PublishHandler implements IMessageHandler<Publish,IMessage> {
        @Override public IMessage onMessage(Publish message, MessageContext ctx) {
            EntityPlayerMP player=ctx.getServerHandler().player;
            if (!Double.isFinite(message.x) || !Double.isFinite(message.y) || !Double.isFinite(message.z)) return null;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.isDead) return;
                CHANNEL.sendToAllTracking(new Animation(player.getEntityId(), new Vec3d(message.x,message.y,message.z)), player);
                PlayerEvents.invulnerable(player, RollConfig.server().invulnerable_ticks_upon_roll);
                player.addExhaustion(RollConfig.server().exhaust_on_roll);
                MinecraftForge.EVENT_BUS.post(new PlayerStartRollingEvent(player, new Vec3d(message.x,message.y,message.z)));
            });
            return null;
        }
    }
    public static final class Animation extends Publish {
        int id;
        public Animation() { }
        Animation(int id, Vec3d direction) { super(direction); this.id=id; }
        @Override public void toBytes(ByteBuf buf) { buf.writeInt(id); super.toBytes(buf); }
        @Override public void fromBytes(ByteBuf buf) { id=buf.readInt(); super.fromBytes(buf); }
    }
    public static final class AnimationHandler implements IMessageHandler<Animation,IMessage> {
        @Override public IMessage onMessage(Animation message, MessageContext ctx) {
            CombatRolAntique.proxy.receiveAnimation(message.id,message.x,message.y,message.z);
            return null;
        }
    }
    public static final class ServerConfig implements IMessage {
        RollConfig.Server config;
        public ServerConfig() { }
        ServerConfig(RollConfig.Server config) { this.config=config; }
        @Override public void toBytes(ByteBuf buf) { ByteBufUtils.writeUTF8String(buf,GSON.toJson(config)); }
        @Override public void fromBytes(ByteBuf buf) { config=GSON.fromJson(ByteBufUtils.readUTF8String(buf),RollConfig.Server.class); }
    }
    public static final class ConfigHandler implements IMessageHandler<ServerConfig,IMessage> {
        @Override public IMessage onMessage(ServerConfig message, MessageContext ctx) {
            CombatRolAntique.proxy.receiveConfig(message.config);
            return null;
        }
    }
}
