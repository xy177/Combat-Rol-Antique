package xy177.combatrolantique.client.gui;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import xy177.combatrolantique.CombatRolAntique;
import xy177.combatrolantique.client.ClientProxy;
import xy177.combatrolantique.client.RollManager;
import xy177.combatrolantique.config.RollConfig;

public final class RollHud extends Gui {
    private static final ResourceLocation ARROW=texture("arrow"),BACKGROUND=texture("arrow_background"),WIDGETS=texture("widgets");
    private static ResourceLocation texture(String name) { return new ResourceLocation(CombatRolAntique.MOD_ID,"textures/hud/"+name+".png"); }
    public static void render(float partial,boolean preview) {
        Minecraft mc=Minecraft.getMinecraft(); RollManager manager=ClientProxy.manager;
        RollConfig.Client config=RollConfig.client;
        if (!preview && (mc.player==null || mc.player.isSpectator() || mc.player.isCreative()&&!config.showHUDInCreative
            || !config.showWhenFull&&manager.available()==manager.maximum())) return;
        int maximum=preview?3:manager.maximum(), available=preview?1:manager.available();
        ScaledResolution screen=new ScaledResolution(mc);
        RollConfig.Hud position=RollConfig.hud;
        int origin=position.origin.ordinal();
        int x=(origin%3==0?screen.getScaledWidth()/2:origin%3==1?0:screen.getScaledWidth())+position.x;
        int y=(origin<3?0:screen.getScaledHeight())+position.y-7;
        GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
        for (int i=0;i<maximum;i++) {
            float full=i<available?1:0;int color=config.hudArrowColor;
            if (i==available) {
                full=preview?0.5F:manager.total()==0?1:Math.min(manager.elapsed()/(float)manager.total(),1);
                if (!preview && config.playCooldownFlash && manager.total()-manager.elapsed()<=2) {
                    float sparkle=1-(manager.total()-manager.elapsed()-1+(1-partial))/2;
                    int red=(int)((((color>>16)&255)/255F*(1-sparkle)+sparkle)*255);
                    int green=(int)((((color>>8)&255)/255F*(1-sparkle)+sparkle)*255);
                    int blue=(int)(((color&255)/255F*(1-sparkle)+sparkle)*255);
                    color=(red<<16)+(green<<8)+blue;
                }
            }
            mc.getTextureManager().bindTexture(BACKGROUND);GlStateManager.color(1,1,1,config.hudBackgroundOpacity/100F);
            drawModalRectWithCustomSizedTexture(x+i*8,y,0,0,15,15,15,15);
            int height=Math.round(full*13);
            mc.getTextureManager().bindTexture(ARROW);
            GlStateManager.color(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F,full);
            if (height>0) drawModalRectWithCustomSizedTexture(x+i*8+1,y+14-height,0,13-height,13,height,13,13);
        }
        GlStateManager.color(1,1,1,1);GlStateManager.disableBlend();
        if (config.showKeybinding) drawKey(x,y,maximum,config.keybindingLabelPosition==RollConfig.LabelPosition.LEFT);
        GlStateManager.color(1,1,1,1);
    }
    private static void drawKey(int x,int y,int count,boolean left) {
        Minecraft mc=Minecraft.getMinecraft();int code=ClientProxy.ROLL_KEY.getKeyCode();
        int keyX=left?x:x+count*4,keyY=left?y+7:y+1;
        mc.getTextureManager().bindTexture(WIDGETS);GlStateManager.enableBlend();
        if (code>=-100 && code<=-96) {
            int button=code+100,u=button<3?button*16:(button-3)*16,v=button<3?0:16;
            drawModalRectWithCustomSizedTexture(left?keyX-10:keyX-5,left?keyY-6:keyY-12,u,v,10,12,256,256);
        } else {
            String phrase=GameSettings.getKeyDisplayString(code).toUpperCase(Locale.US);
            StringBuilder label=new StringBuilder();for (String token:phrase.split("\\s+")) if (!token.isEmpty()) label.append(token.charAt(0));
            int width=mc.fontRenderer.getStringWidth(label.toString()),center=left?keyX-(width+6)/2:keyX;
            int top=left?keyY-6:keyY-12;
            drawModalRectWithCustomSizedTexture(center-width/2-3,top,0,32,3,12,256,256);
            drawScaledCustomSizeModalRect(center-width/2,top,16,32,16,12,width,12,256,256);
            drawModalRectWithCustomSizedTexture(center+width/2,top,13,32,3,12,256,256);
            int textY=left?keyY-4:keyY-mc.fontRenderer.FONT_HEIGHT-1;
            mc.fontRenderer.drawStringWithShadow(label.toString(),center-width/2F,textY,0xFFFFFF);
        }
        GlStateManager.disableBlend();
    }
}
