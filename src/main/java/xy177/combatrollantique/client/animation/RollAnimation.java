/* Adapted from minecraftPlayerAnimator, Copyright (c) 2022 KosmX.
 * MIT License; see META-INF/licenses/player-animation-MIT.txt. */
package xy177.combatrollantique.client.animation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import xy177.combatrollantique.CombatRollAntique;
import xy177.combatrollantique.config.RollConfig;

public final class RollAnimation {
    private static KeyframeClip clip;
    private Frame current,previous;
    private Vec3d direction=Vec3d.ZERO;
    private float speed=1.2F,delta,shiftedDelta;
    private int fadeTime;
    public static void reload(IResourceManager resources) {
        try (IResource resource=resources.getResource(new ResourceLocation(CombatRollAntique.MOD_ID,"player_animations/roll.json"));
             Reader reader=new InputStreamReader(resource.getInputStream(),StandardCharsets.UTF_8)) {
            clip=new KeyframeClip(reader);
        } catch (IOException exception) { throw new IllegalStateException("Unable to load the roll animation",exception); }
    }
    public void start(Vec3d direction) {
        previous=current!=null && current.active()?current:null;
        current=new Frame(); fadeTime=0; this.direction=direction;
        speed=clip.endTick/(float)RollConfig.server().roll_duration;
    }
    public void tick() { float remaining=1-delta; delta=0; step(remaining); }
    public void setup(float partial) { float difference=partial-delta; delta=partial; step(difference); }
    private void step(float difference) {
        float amount=difference*speed+shiftedDelta;
        while (amount>1) {
            amount-=1;
            if (current!=null && current.active()) current.tick++;
            if (previous!=null && previous.active()) previous.tick++;
            fadeTime++;
            if (fadeTime>=clip.beginTick) previous=null;
        }
        shiftedDelta=amount;
    }
    public boolean isActive() { return current!=null && current.active(); }
    public float sample(String name,int axis,float vanilla) {
        if (!isActive()) return vanilla;
        float result=clip.sample(name,axis,current.tick,shiftedDelta,vanilla);
        if (previous!=null && fadeTime+shiftedDelta<=clip.beginTick) {
            float alpha=(float)(-(Math.cos(Math.PI*(fadeTime+shiftedDelta)/clip.beginTick)-1)/2);
            float from=previous.active()?clip.sample(name,axis,previous.tick,shiftedDelta,vanilla):vanilla;
            result=result*alpha+from*(1-alpha);
        }
        return result;
    }
    public float directionYaw(float bodyYaw) {
        Vec3d forward=new Vec3d(0,0,1).rotateYaw((float)Math.toRadians(-bodyYaw));
        double cosine=forward.dotProduct(direction)/(forward.lengthVector()*direction.lengthVector());
        double angle=Math.acos(cosine)*Math.signum(forward.crossProduct(direction).y);
        if (Double.isNaN(angle)) return 0;
        float fade=previous!=null?1:Math.min((current.tick+shiftedDelta)/clip.beginTick,1)*Math.min((clip.stopTick-current.tick-shiftedDelta)/(clip.stopTick-clip.endTick),1);
        return (float)angle*fade;
    }
    private static final class Frame {
        int tick;
        boolean active() { return tick<clip.stopTick; }
    }
}
