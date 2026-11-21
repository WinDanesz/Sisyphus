package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.item.ArmorTrims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Each pattern is drawn in the eight grays of {@code trim_palette.png}, which are swapped for the eight colors of the material's palette.
 * Both are in {@code textures/trims/}, so resource packs can change them.
 */
@SideOnly(Side.CLIENT)
public final class ArmorTrimTextures {

    private static final String ROOT = "textures/trims/";
    private static final String GENERATED_ENTITY = ROOT + "generated/entity/";
    private static final String GENERATED_ITEMS = ROOT + "generated/items/";
    private static final String[] LAYERS = {"humanoid", "humanoid_leggings"};
    private static final String[] PIECES = {"helmet", "chestplate", "leggings", "boots"};

    private ArmorTrimTextures() {
    }

    public static List<String> palettes() {
        List<String> palettes = new ArrayList<>();
        for (ArmorTrims.Material material : ArmorTrims.Material.values()) {
            palettes.add(material.name);
            if (material.hasDarker()) {
                palettes.add(material.name + "_darker");
            }
        }
        return palettes;
    }

    public static ResourceLocation entityTexture(String pattern, String palette, boolean legs) {
        return new ResourceLocation(SisyphusBackport.MODID, GENERATED_ENTITY + LAYERS[legs ? 1 : 0] + "/" + pattern + "_" + palette + ".png");
    }

    public static ResourceLocation itemSprite(String piece, String palette) {
        return new ResourceLocation(SisyphusBackport.MODID, "trims/generated/items/" + piece + "_trim_" + palette);
    }

    public static List<ResourceLocation> itemSprites() {
        List<ResourceLocation> sprites = new ArrayList<>();
        for (String piece : PIECES) {
            for (String palette : palettes()) {
                sprites.add(itemSprite(piece, palette));
            }
        }
        return sprites;
    }

    @Nullable
    private static Source sourceOf(ResourceLocation location) {
        if (!ArmorTrims.isEnabled() || !location.getNamespace().equals(SisyphusBackport.MODID) || !location.getPath().endsWith(".png")) {
            return null;
        }
        String path = location.getPath();
        String name;
        String folder;
        if (path.startsWith(GENERATED_ENTITY)) {
            String rest = path.substring(GENERATED_ENTITY.length(), path.length() - 4);
            int slash = rest.indexOf('/');
            if (slash < 0 || !Arrays.asList(LAYERS).contains(rest.substring(0, slash))) {
                return null;
            }
            folder = "entity/" + rest.substring(0, slash) + "/";
            name = rest.substring(slash + 1);
        } else if (path.startsWith(GENERATED_ITEMS)) {
            folder = "items/";
            name = path.substring(GENERATED_ITEMS.length(), path.length() - 4);
        } else {
            return null;
        }
        for (String palette : palettes()) {
            String suffix = "_" + palette;
            if (name.endsWith(suffix)) {
                String base = name.substring(0, name.length() - suffix.length());
                return new Source(new ResourceLocation(SisyphusBackport.MODID, ROOT + folder + base + ".png"), palette);
            }
        }
        return null;
    }

    public static boolean exists(ResourceLocation location) {
        return sourceOf(location) != null;
    }

    @Nullable
    public static byte[] generate(ResourceLocation location) throws IOException {
        Source source = sourceOf(location);
        if (source == null) {
            return null;
        }
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        BufferedImage picture = recolor(read(manager, source.picture), read(manager, palette("trim_palette")), read(manager, palette(source.palette)));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(picture, "png", out);
        return out.toByteArray();
    }

    private static ResourceLocation palette(String name) {
        return new ResourceLocation(SisyphusBackport.MODID, ROOT + "color_palettes/" + name + ".png");
    }

    private static BufferedImage read(IResourceManager manager, ResourceLocation location) throws IOException {
        try (IResource resource = manager.getResource(location)) {
            return ImageIO.read(resource.getInputStream());
        }
    }

    static BufferedImage recolor(BufferedImage picture, BufferedImage key, BufferedImage palette) {
        Map<Integer, Integer> swap = new HashMap<>();
        for (int i = 0; i < key.getWidth() && i < palette.getWidth(); i++) {
            swap.put(key.getRGB(i, 0) & 0xFFFFFF, palette.getRGB(i, 0) & 0xFFFFFF);
        }
        BufferedImage out = new BufferedImage(picture.getWidth(), picture.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < picture.getHeight(); y++) {
            for (int x = 0; x < picture.getWidth(); x++) {
                int argb = picture.getRGB(x, y);
                Integer rgb = swap.get(argb & 0xFFFFFF);
                out.setRGB(x, y, rgb == null || (argb >>> 24) == 0 ? argb : (argb & 0xFF000000) | rgb);
            }
        }
        return out;
    }

    private static final class Source {
        final ResourceLocation picture;
        final String palette;

        Source(ResourceLocation picture, String palette) {
            this.picture = picture;
            this.palette = palette;
        }
    }
}
