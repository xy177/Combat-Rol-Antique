/* Adapted from minecraftPlayerAnimator, Copyright (c) 2022 KosmX.
 * MIT License; see META-INF/licenses/player-animation-MIT.txt. */
package xy177.combatrolantique.client.animation;

import com.google.gson.*;
import java.io.Reader;
import java.util.*;

/** The non-looping, linear Emotecraft keyframe subset used by the original roll. */
public final class KeyframeClip {
    private static final String[] AXES={"x","y","z","pitch","yaw","roll"};
    private final Map<String,Axis[]> parts=new HashMap<>();
    public final int beginTick,endTick,stopTick;
    private final int returnTick;
    public KeyframeClip(Reader reader) {
        JsonObject root=new JsonParser().parse(reader).getAsJsonObject();
        JsonObject emote=root.getAsJsonObject("emote");
        beginTick=emote.get("beginTick").getAsInt(); endTick=emote.get("endTick").getAsInt(); stopTick=emote.get("stopTick").getAsInt();
        returnTick=emote.has("returnTick")?emote.get("returnTick").getAsInt():0;
        int version=root.has("version")?root.get("version").getAsInt():1;
        boolean degrees=!emote.has("degrees")||emote.get("degrees").getAsBoolean();
        if (emote.has("isLoop") && emote.get("isLoop").getAsBoolean()) throw new IllegalArgumentException("The roll must be non-looping");
        for (JsonElement element:emote.getAsJsonArray("moves")) {
            JsonObject move=element.getAsJsonObject(); int tick=move.get("tick").getAsInt();
            if (move.has("easing") && !"LINEAR".equals(move.get("easing").getAsString())) throw new IllegalArgumentException("Unsupported roll easing");
            int turn=move.has("turn")?move.get("turn").getAsInt():0;
            for (Map.Entry<String,JsonElement> entry:move.entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                String name=entry.getKey(); if (version<3 && "torso".equals(name)) name="body";
                Axis[] axes=parts.computeIfAbsent(name,KeyframeClip::createAxes);
                JsonObject values=entry.getValue().getAsJsonObject();
                for (int i=0;i<AXES.length;i++) if (values.has(AXES[i])) {
                    float value=values.get(AXES[i]).getAsFloat();
                    if (i>=3) value=(degrees?(float)Math.toRadians(value):value)+(float)(turn*Math.PI*2);
                    axes[i].keys.put(tick,value);
                }
            }
        }
    }
    private static Axis[] createAxes(String name) {
        float x=0,y=0,z=0;
        if ("rightArm".equals(name)) { x=-5; y=2; }
        if ("leftArm".equals(name)) { x=5; y=2; }
        if ("rightLeg".equals(name)) { x=-1.9F; y=12; z=0.1F; }
        if ("leftLeg".equals(name)) { x=1.9F; y=12; z=0.1F; }
        return new Axis[]{new Axis(x),new Axis(y),new Axis(z),new Axis(0),new Axis(0),new Axis(0)};
    }
    public float sample(String name,int axis,int tick,float delta,float current) {
        Axis[] axes=parts.get(name);
        if (axes==null || axes[axis].keys.isEmpty()) return current;
        if (axis>=3) current=radians(current);
        Axis state=axes[axis];
        Map.Entry<Integer,Float> before=state.keys.floorEntry(tick),after=state.keys.higherEntry(tick);
        int beforeTick; float beforeValue;
        if (before==null) {
            beforeTick=tick<beginTick?0:tick<endTick?beginTick:endTick;
            beforeValue=tick<beginTick?current:state.defaultValue;
        } else {
            beforeTick=before.getKey(); beforeValue=before.getValue();
            if (tick>=endTick && beforeTick<endTick && before.getKey().equals(state.keys.lastKey())) beforeTick=endTick;
        }
        int afterTick; float afterValue;
        if (after!=null) { afterTick=after.getKey(); afterValue=after.getValue(); }
        else if (tick<endTick) { afterTick=endTick; afterValue=state.keys.lastEntry().getValue(); }
        else { afterTick=stopTick; afterValue=current; }
        if (beforeTick>=afterTick) {
            if (tick<beforeTick) beforeTick-=endTick-returnTick+1;
            else afterTick+=endTick-returnTick+1;
        }
        float value=beforeTick==afterTick?beforeValue:beforeValue+(afterValue-beforeValue)*(tick+delta-beforeTick)/(afterTick-beforeTick);
        return axis>=3?radians(value):value;
    }
    public static float radians(float value) {
        double wrapped=(value+Math.PI)%(Math.PI*2);
        if (wrapped<0) wrapped+=Math.PI*2;
        return (float)(wrapped-Math.PI);
    }
    private static final class Axis {
        final float defaultValue;
        final NavigableMap<Integer,Float> keys=new TreeMap<>();
        Axis(float value) { defaultValue=value; }
    }
}
