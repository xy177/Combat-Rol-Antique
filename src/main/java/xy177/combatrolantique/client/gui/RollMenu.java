package xy177.combatrolantique.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import xy177.combatrolantique.CombatRolAntique;

public final class RollMenu extends GuiScreen {
    private final GuiScreen parent;
    public RollMenu(GuiScreen parent) { this.parent=parent; }
    @Override public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0,width/2-60,height/2-40,120,20,text("close")));
        buttonList.add(new GuiButton(1,width/2-60,height/2-10,120,20,text("settings")));
        buttonList.add(new GuiButton(2,width/2-60,height/2+20,120,20,text("hud")));
    }
    static String text(String key) { return I18n.format("gui.combatrolantique."+key); }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id==0) mc.displayGuiScreen(parent);
        if (button.id==1) mc.displayGuiScreen(new GuiConfig(this,CombatRolAntique.MOD_ID,text("config_menu")));
        if (button.id==2) mc.displayGuiScreen(new RollHudScreen(this));
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partial) {
        drawDefaultBackground();drawCenteredString(fontRenderer,text("config_menu"),width/2,height/2-70,0xFFFFFF);
        super.drawScreen(mouseX,mouseY,partial);
    }
    @Override protected void keyTyped(char typed,int key) throws IOException {
        if (key==1) mc.displayGuiScreen(parent);else super.keyTyped(typed,key);
    }
}
