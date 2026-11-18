package xy177.combatrolantique.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.*;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import xy177.combatrolantique.CombatRolAntique;
import xy177.combatrolantique.config.RollConfig;

public final class RollHudScreen extends GuiScreen {
    private final GuiScreen parent;
    private boolean dragging;
    private int lastX,lastY;
    public RollHudScreen(GuiScreen parent) { this.parent=parent; }
    @Override public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0,width/2-60,height/2-40,120,20,RollMenu.text("close")));
        buttonList.add(new GuiButton(1,width/2-60,height/2-10,120,20,RollMenu.text("corner")));
        buttonList.add(new GuiButton(2,width/2-60,height/2+20,120,20,RollMenu.text("reset")));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id==0) mc.displayGuiScreen(parent);
        if (button.id==1) {
            RollConfig.Hud hud=RollConfig.hud;
            hud.origin=RollConfig.Origin.values()[(hud.origin.ordinal()+1)%RollConfig.Origin.values().length];
            int origin=hud.origin.ordinal();hud.x=origin%3==0?0:origin%3==1?12:-12;hud.y=origin<3?12:-12;
        }
        if (button.id==2) RollConfig.hud=new RollConfig.Hud();
    }
    @Override protected void mouseClicked(int x,int y,int button) throws IOException {
        boolean overButton=false;for (GuiButton widget:buttonList) if (widget.mousePressed(mc,x,y)) overButton=true;
        dragging=button==0 && !overButton;lastX=x;lastY=y;super.mouseClicked(x,y,button);
    }
    @Override protected void mouseClickMove(int x,int y,int button,long elapsed) {
        if (dragging && button==0) { RollConfig.hud.x+=x-lastX;RollConfig.hud.y+=y-lastY;lastX=x;lastY=y; }
        super.mouseClickMove(x,y,button,elapsed);
    }
    @Override protected void mouseReleased(int x,int y,int button) { dragging=false;super.mouseReleased(x,y,button); }
    @Override public void onGuiClosed() { ConfigManager.sync(CombatRolAntique.MOD_ID,Config.Type.INSTANCE); }
    @Override protected void keyTyped(char typed,int key) throws IOException {
        if (key==1) mc.displayGuiScreen(parent);else super.keyTyped(typed,key);
    }
    @Override public void drawScreen(int x,int y,float partial) { drawDefaultBackground();super.drawScreen(x,y,partial);RollHud.render(partial,true); }
}
