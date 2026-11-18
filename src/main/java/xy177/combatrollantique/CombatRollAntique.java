/* Copyright (c) 2026 xy177. Licensed under GNU GPL version 3. See LICENSE. */
package xy177.combatrollantique;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import xy177.combatrollantique.common.CommonProxy;
import xy177.combatrollantique.common.PlayerEvents;
import xy177.combatrollantique.common.RollContent;
import xy177.combatrollantique.network.RollNetwork;

@Mod(modid = CombatRollAntique.MOD_ID, name = CombatRollAntique.NAME, useMetadata = true,
    acceptedMinecraftVersions = "[1.12.2]", guiFactory = "xy177.combatrollantique.client.gui.RollGuiFactory")
public final class CombatRollAntique {
    public static final String MOD_ID = "combatrollantique";
    public static final String NAME = "Combat Roll Antique";
    @SidedProxy(clientSide = "xy177.combatrollantique.client.ClientProxy", serverSide = "xy177.combatrollantique.common.CommonProxy")
    public static CommonProxy proxy;
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new RollContent());
        MinecraftForge.EVENT_BUS.register(new PlayerEvents());
        RollNetwork.init();
        proxy.init();
    }
}
