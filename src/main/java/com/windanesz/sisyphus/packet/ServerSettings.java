package com.windanesz.sisyphus.packet;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class ServerSettings {

    @Nullable
    private static Boolean serverShowsBrushingInJei;

    @Nullable
    private static Integer serverDiscWindUp;

    private ServerSettings() {
    }

    public static void setFromServer(@Nullable Boolean showBrushingInJei, @Nullable Integer discWindUp) {
        serverShowsBrushingInJei = showBrushingInJei;
        serverDiscWindUp = discWindUp;
    }

    private static PacketServerSettings.Message current() {
        return new PacketServerSettings.Message(Settings.compatibility.showBrushingInJei, Settings.items.throwingDiscs.windUp);
    }

    public static boolean showBrushingInJei() {
        Boolean server = serverShowsBrushingInJei;
        return server != null ? server : Settings.compatibility.showBrushingInJei;
    }

    /**
     * Both sides must agree, or the client would throw a disc that the server is still drawing back.
     */
    public static int discWindUp() {
        Integer server = serverDiscWindUp;
        return server != null ? server : Settings.items.throwingDiscs.windUp;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ModPacketHandler.net.sendTo(current(), (EntityPlayerMP) event.player);
        }
    }

    /**
     * Runs after the config sync, which is at normal priority.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!event.getModID().equals(SisyphusBackport.MODID)) {
            return;
        }
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            server.addScheduledTask(() -> ModPacketHandler.net.sendToAll(current()));
        }
    }
}
