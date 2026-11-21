package com.windanesz.sisyphus.command;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class CommandRegistry {

    private CommandRegistry() {
    }

    public static void register(FMLServerStartingEvent event) {
        if (!Settings.extras.enableCommands) {
            return;
        }
        event.registerServerCommand(new CommandTeamMsg());
        event.registerServerCommand(new CommandSchedule());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Settings.extras.enableCommands) {
            return;
        }
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.getWorld(0) == null) {
            return;
        }
        ScheduledFunctions data = ScheduledFunctions.get(server);
        if (data != null) {
            data.runDue(server, server.getWorld(0).getTotalWorldTime());
        }
    }
}
