package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
final class StriderTextures {

    private static final ResourceLocation SADDLE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/strider/strider_saddle.png");
    private static final Map<Boolean, ResourceLocation> SADDLED = new HashMap<>();

    private StriderTextures() {
    }

    public static ResourceLocation get(boolean cold, boolean saddled) {
        ResourceLocation base = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/strider/" + (cold ? "strider_cold" : "strider") + ".png");
        if (!saddled) {
            return base;
        }
        ResourceLocation cached = SADDLED.get(cold);
        if (cached != null) {
            return cached;
        }
        try {
            BufferedImage image = read(base);
            BufferedImage saddle = read(SADDLE);
            Graphics2D graphics = image.createGraphics();
            graphics.drawImage(saddle, 0, 0, null);
            graphics.dispose();
            ResourceLocation location = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation(SisyphusBackport.MODID + "_strider_saddled_" + (cold ? "cold" : "warm"), new DynamicTexture(image));
            SADDLED.put(cold, location);
            return location;
        } catch (IOException e) {
            SisyphusBackport.logger.warn("Could not draw the saddle on the strider", e);
            SADDLED.put(cold, base);
            return base;
        }
    }

    private static BufferedImage read(ResourceLocation location) throws IOException {
        try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()) {
            return TextureUtil.readBufferedImage(stream);
        }
    }
}
