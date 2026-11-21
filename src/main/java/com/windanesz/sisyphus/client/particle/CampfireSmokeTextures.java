package com.windanesz.sisyphus.client.particle;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SisyphusBackport.MODID)
public final class CampfireSmokeTextures {

    public static final int COUNT = 12;

    private CampfireSmokeTextures() {
    }

    private static ResourceLocation location(int index) {
        return new ResourceLocation(SisyphusBackport.MODID, "particle/big_smoke_" + index);
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!Settings.functionalBlocks.enableCampfire) {
            return;
        }
        for (int i = 0; i < COUNT; i++) {
            event.getMap().registerSprite(location(i));
        }
    }

    public static TextureAtlasSprite get(int index) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location(index).toString());
    }
}
