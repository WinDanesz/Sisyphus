package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.integration.ModdedWoodSigns;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SideOnly(Side.CLIENT)
final class GeneratedSignResources {

    private static final ResourceLocation OAK_PLANKS = new ResourceLocation("minecraft", "textures/blocks/planks_oak.png");
    private static final ResourceLocation OAK_SIGN = new ResourceLocation("minecraft", "textures/entity/sign.png");
    private static final ResourceLocation OAK_SIGN_ITEM = new ResourceLocation("minecraft", "textures/items/sign.png");

    private static final String[][] FILES = {
            {"blockstates/", "_standing_sign.json"},
            {"blockstates/", "_wall_sign.json"},
            {"models/block/", "_sign_particle.json"},
            {"models/item/", "_sign.json"},
            {"textures/items/generated/", "_sign.png"},
            {"textures/entity/signs/", ".png"},
    };

    private static final StateMapperBase VARIANT_NAMES = new StateMapperBase() {
        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return new ModelResourceLocation(state.getBlock().getRegistryName(), this.getPropertyString(state.getProperties()));
        }
    };

    private GeneratedSignResources() {
    }

    @Nullable
    private static String woodOf(ResourceLocation location, String[] file) {
        String path = location.getPath();
        if (!location.getNamespace().equals(SisyphusBackport.MODID) || !path.startsWith(file[0]) || !path.endsWith(file[1])
                || path.length() <= file[0].length() + file[1].length()) {
            return null;
        }
        String wood = path.substring(file[0].length(), path.length() - file[1].length());
        return ModdedWoodSigns.all().containsKey(wood) ? wood : null;
    }

    static boolean exists(ResourceLocation location) {
        if (ModdedWoodSigns.all().isEmpty()) {
            return false;
        }
        for (String[] file : FILES) {
            if (woodOf(location, file) != null) {
                return true;
            }
        }
        return false;
    }

    private static byte[] bytes(String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }

    private static String variants(String property, String[] values, String model) {
        StringBuilder json = new StringBuilder("{\"variants\":{");
        for (int i = 0; i < values.length; i++) {
            json.append(i == 0 ? "" : ",").append('"').append(property).append('=').append(values[i]).append("\":{\"model\":\"").append(model).append("\"}");
        }
        return json.append("}}").toString();
    }

    static byte[] generate(ResourceLocation location) throws IOException {
        for (int kind = 0; kind < FILES.length; kind++) {
            String wood = woodOf(location, FILES[kind]);
            if (wood == null) {
                continue;
            }
            String particle = SisyphusBackport.MODID + ":" + wood + "_sign_particle";
            switch (kind) {
                case 0:
                    String[] rotations = new String[16];
                    for (int i = 0; i < 16; i++) {
                        rotations[i] = String.valueOf(i);
                    }
                    return bytes(variants("rotation", rotations, particle));
                case 1:
                    return bytes(variants("facing", new String[]{"north", "south", "east", "west"}, particle));
                case 2:
                    ResourceLocation planks = planksTexture(ModdedWoodSigns.all().get(wood));
                    return bytes("{\"textures\":{\"particle\":\"" + (planks == null ? "minecraft:blocks/planks_oak" : planks.toString()) + "\"}}");
                case 3:
                    return bytes("{\"parent\":\"item/generated\",\"textures\":{\"layer0\":\"" + SisyphusBackport.MODID + ":items/generated/" + wood + "_sign\"}}");
                case 4:
                    return picture(OAK_SIGN_ITEM, ModdedWoodSigns.all().get(wood));
                default:
                    return picture(OAK_SIGN, ModdedWoodSigns.all().get(wood));
            }
        }
        throw new FileNotFoundException(location.toString());
    }

    @Nullable
    private static ResourceLocation planksTexture(ModdedWoodSigns.Wood wood) {
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        String variant = wood.meta < 0 ? null : VARIANT_NAMES.getPropertyString(wood.state().getProperties());
        return OreTextures.findTexture(location -> GeneratedOrePack.text(manager, location), wood.planks.getRegistryName(), variant);
    }

    private static byte[] picture(ResourceLocation oak, ModdedWoodSigns.Wood planks) throws IOException {
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        BufferedImage picture = GeneratedOrePack.read(manager, oak);
        ResourceLocation found = planksTexture(planks);
        if (found != null) {
            try {
                BufferedImage wood = GeneratedOrePack.read(manager, new ResourceLocation(found.getNamespace(), "textures/" + found.getPath() + ".png"));
                picture = WoodTextures.recolor(picture, GeneratedOrePack.read(manager, OAK_PLANKS), wood);
            } catch (IOException e) {
                SisyphusBackport.logger.warn("Could not read the picture of {}", planks.planks.getRegistryName(), e);
            }
        } else {
            SisyphusBackport.logger.warn("Could not find the picture of {}, its sign looks like the oak sign", planks.planks.getRegistryName());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(picture, "png", out);
        return out.toByteArray();
    }
}
