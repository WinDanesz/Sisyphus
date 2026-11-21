package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.integration.jei.SBJEIIntegration;
import com.windanesz.sisyphus.packet.ServerSettings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID, value = Side.CLIENT)
public final class ClientServerSettings {

    private ClientServerSettings() {
    }

    public static void apply() {
        SBJEIIntegration.updateBrushingTab();
    }

    @SubscribeEvent
    public static void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ServerSettings.setFromServer(null, null);
            apply();
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(SisyphusBackport.MODID)) {
            apply();
        }
    }
}
