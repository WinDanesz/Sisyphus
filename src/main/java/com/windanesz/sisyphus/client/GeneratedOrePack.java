package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.ArmorTrimTextures;
import com.windanesz.sisyphus.integration.ModdedDeepslateOres;
import com.windanesz.sisyphus.integration.ModdedWoodSigns;
import com.windanesz.sisyphus.item.ArmorTrims;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A resource pack built at runtime, with the assets of the deepslate ores and sign woods of other mods and of the colored armor trims.
 */
@SideOnly(Side.CLIENT)
public final class GeneratedOrePack implements IResourcePack {

    private static final ResourceLocation STONE = new ResourceLocation("minecraft", "textures/blocks/stone.png");
    private static final ResourceLocation DEEPSLATE = new ResourceLocation(SisyphusBackport.MODID, "textures/blocks/deepslate.png");
    private static boolean installed = false;
    private final Map<String, Block> byName = new HashMap<>();

    private GeneratedOrePack() {
    }

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        if (!ModdedDeepslateOres.isEnabled() && !ModdedWoodSigns.isEnabled() && !ArmorTrims.isEnabled()) {
            return;
        }
        List<IResourcePack> packs = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
        packs.add(new GeneratedOrePack());
    }

    private static byte[] bytes(String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }

    static BufferedImage read(IResourceManager manager, ResourceLocation location) throws IOException {
        try (IResource resource = manager.getResource(location)) {
            return ImageIO.read(resource.getInputStream());
        }
    }

    @Nullable
    static String text(IResourceManager manager, ResourceLocation location) {
        try (IResource resource = manager.getResource(location); InputStream input = resource.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (int n; (n = input.read(buffer)) > 0; ) {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static byte[] texture(Block original) throws IOException {
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        BufferedImage deepslate = read(manager, DEEPSLATE);
        BufferedImage picture = deepslate;
        ResourceLocation found = OreTextures.findTexture(location -> text(manager, location), original.getRegistryName());
        if (found != null) {
            try {
                BufferedImage ore = read(manager, new ResourceLocation(found.getNamespace(), "textures/" + found.getPath() + ".png"));
                picture = OreTextures.composite(ore, read(manager, STONE), deepslate).image;
            } catch (IOException e) {
                SisyphusBackport.logger.warn("Could not read the picture of {}", original.getRegistryName(), e);
            }
        } else {
            SisyphusBackport.logger.warn("Could not find the picture of {}, its deepslate version looks like plain deepslate", original.getRegistryName());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(picture, "png", out);
        return out.toByteArray();
    }

    /**
     * Blocks are registered after the pack is installed, so the names are collected lazily.
     */
    private synchronized Map<String, Block> names() {
        if (byName.size() != ModdedDeepslateOres.all().size()) {
            byName.clear();
            for (Map.Entry<Block, Block> entry : ModdedDeepslateOres.all().entrySet()) {
                byName.put(entry.getValue().getRegistryName().getPath(), entry.getKey());
            }
        }
        return byName;
    }

    @Nullable
    private String nameOf(ResourceLocation location, String folder, String extension) {
        if (!location.getNamespace().equals(SisyphusBackport.MODID) || !location.getPath().startsWith(folder) || !location.getPath().endsWith(extension)) {
            return null;
        }
        String name = location.getPath().substring(folder.length(), location.getPath().length() - extension.length());
        return names().containsKey(name) ? name : null;
    }

    private byte[] generate(ResourceLocation location) throws IOException {
        String name = nameOf(location, "blockstates/", ".json");
        if (name != null) {
            return bytes("{\"variants\":{\"normal\":{\"model\":\"" + SisyphusBackport.MODID + ":" + name + "\"}}}");
        }
        name = nameOf(location, "models/block/", ".json");
        if (name != null) {
            return bytes("{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"" + SisyphusBackport.MODID + ":blocks/generated/" + name + "\"}}");
        }
        name = nameOf(location, "models/item/", ".json");
        if (name != null) {
            return bytes("{\"parent\":\"" + SisyphusBackport.MODID + ":block/" + name + "\"}");
        }
        name = nameOf(location, "textures/blocks/generated/", ".png");
        if (name != null) {
            return texture(names().get(name));
        }
        byte[] trim = ArmorTrimTextures.generate(location);
        return trim != null ? trim : GeneratedSignResources.generate(location);
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        return new ByteArrayInputStream(generate(location));
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return nameOf(location, "blockstates/", ".json") != null || nameOf(location, "models/block/", ".json") != null
                || nameOf(location, "models/item/", ".json") != null || nameOf(location, "textures/blocks/generated/", ".png") != null
                || GeneratedSignResources.exists(location) || ArmorTrimTextures.exists(location);
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton(SisyphusBackport.MODID);
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer serializer, String section) {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        throw new FileNotFoundException("no icon");
    }

    @Override
    public String getPackName() {
        return "Sisyphus Backport generated ores, signs and armor trims";
    }
}
