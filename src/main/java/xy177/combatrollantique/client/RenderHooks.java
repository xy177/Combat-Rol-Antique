package xy177.combatrollantique.client;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.combatrollantique.client.animation.RollAnimation;

public final class RenderHooks {
    private static AbstractClientPlayer rendering;
    private static float partial;
    private static final Map<ModelBiped,float[][]> ORIGINALS=new WeakHashMap<>();
    private static final Set<ModelBiped> ACTIVE=Collections.newSetFromMap(new WeakHashMap<ModelBiped,Boolean>());
    @SubscribeEvent(priority=EventPriority.LOWEST) public void before(RenderPlayerEvent.Pre event) {
        rendering=(AbstractClientPlayer)event.getEntityPlayer(); partial=event.getPartialRenderTick();
        RollAnimation animation=ClientProxy.animation(rendering);
        if (animation!=null) animation.setup(partial);
    }
    @SubscribeEvent public void after(RenderPlayerEvent.Post event) { rendering=null; }
    private static ModelRenderer[] parts(ModelBiped model) {
        return new ModelRenderer[]{model.bipedHead,model.bipedBody,model.bipedRightArm,model.bipedLeftArm,model.bipedRightLeg,model.bipedLeftLeg};
    }
    public static void beforeAngles(ModelBiped model,Entity entity) {
        if (rendering!=null && entity==rendering) ACTIVE.add(model);else ACTIVE.remove(model);
        float[][] original=ORIGINALS.remove(model);
        if (original==null) return;
        ModelRenderer[] parts=parts(model);
        for (int i=0;i<parts.length;i++) {
            parts[i].rotationPointX=original[i][0];parts[i].rotationPointY=original[i][1];parts[i].rotationPointZ=original[i][2];
            parts[i].rotateAngleX=original[i][3];parts[i].rotateAngleY=original[i][4];parts[i].rotateAngleZ=original[i][5];
        }
    }
    public static void afterAngles(ModelBiped model) {
        if (!ACTIVE.remove(model) || rendering==null) return;
        RollAnimation animation=ClientProxy.animation(rendering);
        if (animation==null || !animation.isActive()) return;
        ModelRenderer[] parts=parts(model);
        String[] names={"head","torso","rightArm","leftArm","rightLeg","leftLeg"};
        float[][] snapshot=new float[parts.length][];
        for (int i=0;i<parts.length;i++) {
            ModelRenderer p=parts[i];
            snapshot[i]=new float[]{p.rotationPointX,p.rotationPointY,p.rotationPointZ,p.rotateAngleX,p.rotateAngleY,p.rotateAngleZ};
            p.rotationPointX=animation.sample(names[i],0,p.rotationPointX);p.rotationPointY=animation.sample(names[i],1,p.rotationPointY);p.rotationPointZ=animation.sample(names[i],2,p.rotationPointZ);
            p.rotateAngleX=animation.sample(names[i],3,p.rotateAngleX);p.rotateAngleY=animation.sample(names[i],4,p.rotateAngleY);p.rotateAngleZ=animation.sample(names[i],5,p.rotateAngleZ);
        }
        ORIGINALS.put(model,snapshot);
        net.minecraft.client.model.ModelBase.copyModelAngles(model.bipedHead,model.bipedHeadwear);
    }
    public static void body() {
        if (rendering==null) return;
        RollAnimation animation=ClientProxy.animation(rendering);
        if (animation==null || !animation.isActive()) return;
        animation.setup(partial);
        GlStateManager.translate(animation.sample("body",0,0),animation.sample("body",1,0)+0.7,animation.sample("body",2,0));
        float bodyYaw=rendering.renderYawOffset;
        GlStateManager.rotate((float)Math.toDegrees(animation.sample("body",5,0)),0,0,1);
        GlStateManager.rotate((float)Math.toDegrees(animation.sample("body",4,0)+animation.directionYaw(bodyYaw)),0,1,0);
        GlStateManager.rotate((float)Math.toDegrees(animation.sample("body",3,0)),1,0,0);
        GlStateManager.translate(0,-0.7,0);
    }
}
