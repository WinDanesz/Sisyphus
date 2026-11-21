package com.windanesz.sisyphus.client.particle;

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
public final class NetherParticleTextures {

    public static final int SOUL_FRAMES = 11;

    private static final ResourceLocation SOUL_FLAME = new ResourceLocation(SisyphusBackport.MODID, "particle/soul_fire_flame");

    private NetherParticleTextures() {
    }

    private static ResourceLocation soul(int index) {
        return new ResourceLocation(SisyphusBackport.MODID, "particle/soul_" + index);
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(SOUL_FLAME);
        for (int i = 0; i < SOUL_FRAMES; i++) {
            event.getMap().registerSprite(soul(i));
        }
    }

    public static TextureAtlasSprite soulFlame() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(SOUL_FLAME.toString());
    }

    public static TextureAtlasSprite soulSprite(int index) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(soul(index).toString());
    }
}
