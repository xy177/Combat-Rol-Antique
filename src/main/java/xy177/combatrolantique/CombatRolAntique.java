/* Copyright (c) 2026 xy177. Licensed under GNU GPL version 3. See LICENSE. */
package xy177.combatrolantique;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import xy177.combatrolantique.common.CommonProxy;
import xy177.combatrolantique.common.PlayerEvents;
import xy177.combatrolantique.common.RollContent;
import xy177.combatrolantique.network.RollNetwork;

@Mod(modid = CombatRolAntique.MOD_ID, name = CombatRolAntique.NAME, useMetadata = true,
    acceptedMinecraftVersions = "[1.12.2]", guiFactory = "xy177.combatrolantique.client.gui.RollGuiFactory")
public final class CombatRolAntique {
    public static final String MOD_ID = "combatrolantique";
    public static final String NAME = "Combat Rol Antique";
    @SidedProxy(clientSide = "xy177.combatrolantique.client.ClientProxy", serverSide = "xy177.combatrolantique.common.CommonProxy")
    public static CommonProxy proxy;
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new RollContent());
        MinecraftForge.EVENT_BUS.register(new PlayerEvents());
        RollNetwork.init();
        proxy.init();
    }
}
