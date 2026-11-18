package xy177.combatrollantique.client;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;
import xy177.combatrollantique.CombatRollAntique;
import xy177.combatrollantique.client.animation.RollAnimation;
import xy177.combatrollantique.client.gui.RollHud;
import xy177.combatrollantique.common.*;
import xy177.combatrollantique.config.RollConfig;
import xy177.combatrollantique.network.RollNetwork;

public final class ClientProxy extends CommonProxy {
    public static final KeyBinding ROLL_KEY=new KeyBinding("keybinds.combatrollantique.roll",Keyboard.KEY_R,"combatrollantique.mod_name");
    public static RollManager manager=new RollManager();
    private static final Map<AbstractClientPlayer,RollAnimation> ANIMATIONS=new WeakHashMap<>();
    private static final Field ITEM_USE_COOLDOWN=ReflectionHelper.findField(Minecraft.class,"rightClickDelayTimer","field_71467_ac");
    private EntityPlayerSP lastPlayer;
    @Override public void init() {
        ClientRegistry.registerKeyBinding(ROLL_KEY);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RenderHooks());
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(RollAnimation::reload);
    }
    public static RollAnimation animation(AbstractClientPlayer player) { return ANIMATIONS.get(player); }
    @Override public void receiveAnimation(int id,double x,double y,double z) {
        Minecraft mc=Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (mc.world == null) return;
            Entity entity=mc.world.getEntityByID(id);
            if (entity instanceof AbstractClientPlayer && entity != mc.player) play((AbstractClientPlayer)entity,new Vec3d(x,y,z));
        });
    }
    @Override public void receiveConfig(RollConfig.Server config) { Minecraft.getMinecraft().addScheduledTask(() -> RollConfig.setRemote(config)); }
    @SubscribeEvent public void configChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!CombatRollAntique.MOD_ID.equals(event.getModID())) return;
        ConfigManager.sync(CombatRollAntique.MOD_ID,Config.Type.INSTANCE);
        Minecraft mc=Minecraft.getMinecraft();
        if (mc.isSingleplayer() && mc.getIntegratedServer()!=null) {
            RollConfig.setRemote(null);
            mc.getIntegratedServer().addScheduledTask(() -> mc.getIntegratedServer().getPlayerList().getPlayers().forEach(RollNetwork::sync));
        }
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END) return;
        if (lastPlayer != mc.player) {
            manager=new RollManager(); ANIMATIONS.clear(); lastPlayer=mc.player;
            if (mc.player==null) RollConfig.setRemote(null);
        }
        if (mc.player==null || mc.isGamePaused()) return;
        ANIMATIONS.values().forEach(RollAnimation::tick);
        manager.tick(mc.player);
    }
    @SubscribeEvent public void overlay(RenderGameOverlayEvent.Post event) {
        if (event.getType()==RenderGameOverlayEvent.ElementType.ALL && !Minecraft.getMinecraft().gameSettings.hideGUI) RollHud.render(event.getPartialTicks(),false);
    }
    /** Called at the end of Minecraft's input processing, before movement updates. */
    public static void tryRolling() {
        Minecraft mc=Minecraft.getMinecraft(); EntityPlayerSP player=mc.player;
        if (player==null || mc.isGamePaused() || mc.currentScreen!=null || !ROLL_KEY.isKeyDown() || !manager.canRoll(player)) return;
        RollConfig.Server config=RollConfig.server();
        if ((!config.allow_rolling_while_airborn && !player.onGround) || player.getFoodStats().getFoodLevel()<=config.food_level_required
            || player.isRiding() || player.isHandActive() || player.isActiveItemStackBlocking()
            || (!config.allow_rolling_while_weapon_cooldown && player.getCooledAttackStrength(0)<0.95F) || mc.gameSettings.keyBindAttack.isKeyDown()) return;
        try { if (ITEM_USE_COOLDOWN.getInt(mc)>0) return; } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
        float forward=player.movementInput.moveForward, sideways=player.movementInput.moveStrafe;
        Vec3d direction=forward==0 && sideways==0 ? new Vec3d(0,0,1) : new Vec3d(sideways,0,forward).normalize();
        direction=direction.rotateYaw((float)Math.toRadians(-player.rotationYaw));
        direction=direction.scale(0.475*(RollAttributes.value(player,RollAttributes.DISTANCE)+config.additional_roll_distance));
        if (player.isInWater()) direction=direction.scale(Math.max(1-Math.min(waterHeight(player),1)*3,0.3));
        if (player.isInLava()) direction=direction.scale(0.3);
        BlockPos floor=new BlockPos(player).down();
        float slipperiness=player.world.getBlockState(floor).getBlock().slipperiness;
        float grass=Blocks.GRASS.slipperiness;
        if (slipperiness>grass) direction=direction.scale((grass/slipperiness)*(grass/slipperiness));
        player.addVelocity(direction.x,direction.y,direction.z);
        manager.onRoll();
        RollNetwork.CHANNEL.sendToServer(new RollNetwork.Publish(direction));
        play(player,direction);
    }
    private static double waterHeight(EntityPlayerSP player) {
        AxisAlignedBB box=player.getEntityBoundingBox().grow(-0.001);
        double height=0;
        for (BlockPos pos:BlockPos.getAllInBox(new BlockPos(box.minX,box.minY,box.minZ),new BlockPos(box.maxX,box.maxY,box.maxZ))) {
            IBlockState state=player.world.getBlockState(pos);
            if (state.getMaterial()==Material.WATER) {
                double surface=pos.getY()+1-BlockLiquid.getLiquidHeightPercent(state.getValue(BlockLiquid.LEVEL));
                if (surface>=box.minY) height=Math.max(height,surface-box.minY);
            }
        }
        return height;
    }
    private static void play(AbstractClientPlayer player,Vec3d direction) {
        ANIMATIONS.computeIfAbsent(player,key -> new RollAnimation()).start(direction);
        if (RollConfig.client.playRollSound) player.world.playSound(player.posX,player.posY,player.posZ,RollContent.ROLL,SoundCategory.PLAYERS,1,1,true);
        for (int i=0;i<15;i++) player.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
            player.posX+(player.getRNG().nextDouble()*2-1)*player.width*1.5,
            player.posY+player.height*player.getRNG().nextGaussian()*0.3,
            player.posZ+(player.getRNG().nextDouble()*2-1)*player.width*1.5,
            player.getRNG().nextGaussian()*0.02,player.getRNG().nextGaussian()*0.02,player.getRNG().nextGaussian()*0.02);
    }
    public static boolean blockActions() { return Minecraft.getMinecraft().player!=null && manager.isRolling(); }
    public static void adjustInput(EntityPlayerSP player) {
        if (blockActions() && !RollConfig.server().allow_jump_while_rolling) player.movementInput.jump=false;
    }
    public static boolean blockAutoJump() { return blockActions() && !RollConfig.server().allow_auto_jump_while_rolling; }
}
