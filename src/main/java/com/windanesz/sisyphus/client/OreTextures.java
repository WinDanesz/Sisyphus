package com.windanesz.sisyphus.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Deepslate textures for the ores of other mods: the stone in their pictures is swapped for deepslate.
 */
public final class OreTextures {

    public static final int TOLERANCE = 30;

    public static final float MIN_STONE = 0.35F;
    private static final String[] PREFERRED = {"all", "side", "north", "particle", "end", "top"};

    private OreTextures() {
    }

    private static int sample(BufferedImage image, int x, int y, int width, int height) {
        return image.getRGB(Math.min(image.getWidth() - 1, x * image.getWidth() / width), Math.min(image.getHeight() - 1, y * image.getHeight() / height));
    }

    private static boolean isStone(int ore, int stone) {
        int dr = ((ore >> 16) & 255) - ((stone >> 16) & 255);
        int dg = ((ore >> 8) & 255) - ((stone >> 8) & 255);
        int db = (ore & 255) - (stone & 255);
        return dr * dr + dg * dg + db * db <= TOLERANCE * TOLERANCE;
    }

    /**
     * Non-square pictures are animated and are left as they are.
     */
    public static Result composite(BufferedImage ore, BufferedImage stone, BufferedImage deepslate) {
        int w = ore.getWidth();
        int h = ore.getHeight();
        if (w != h) {
            return new Result(ore, 0.0F, false);
        }
        int total = 0;
        int rock = 0;
        boolean[] isRock = new boolean[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = ore.getRGB(x, y);
                if ((argb >>> 24) < 255) {
                    continue;
                }
                total++;
                if (isStone(argb, sample(stone, x, y, w, h))) {
                    isRock[x + y * w] = true;
                    rock++;
                }
            }
        }
        float share = total == 0 ? 0.0F : (float) rock / total;
        if (share < MIN_STONE) {
            return new Result(ore, share, false);
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, isRock[x + y * w] ? sample(deepslate, x, y, w, h) | 0xFF000000 : ore.getRGB(x, y));
            }
        }
        return new Result(out, share, true);
    }

    @Nullable
    private static JsonObject object(Function<ResourceLocation, String> read, ResourceLocation location) {
        String text = read.apply(location);
        if (text == null) {
            return null;
        }
        try {
            JsonElement parsed = new JsonParser().parse(text);
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String model(JsonObject state, Map<String, String> textures, @Nullable String variant) {
        JsonObject source = null;
        if (state.has("variants") && state.get("variants").isJsonObject()) {
            JsonObject variants = state.getAsJsonObject("variants");
            JsonElement chosen = variant != null && variants.has(variant) ? variants.get(variant) : variants.has("normal") ? variants.get("normal") : variants.has("") ? variants.get("") : variants.entrySet().isEmpty() ? null : variants.entrySet().iterator().next().getValue();
            if (chosen != null && chosen.isJsonArray()) {
                JsonArray array = chosen.getAsJsonArray();
                chosen = array.size() == 0 ? null : array.get(0);
            }
            if (chosen != null && chosen.isJsonObject()) {
                source = chosen.getAsJsonObject();
            }
        } else if (state.has("defaults") && state.get("defaults").isJsonObject()) {
            source = state.getAsJsonObject("defaults");
        }
        if (source == null) {
            return null;
        }
        if (source.has("textures") && source.get("textures").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : source.getAsJsonObject("textures").entrySet()) {
                textures.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return source.has("model") ? source.get("model").getAsString() : null;
    }

    private static String resolveReference(Map<String, String> textures, String value) {
        for (int i = 0; i < 8 && value != null && value.startsWith("#"); i++) {
            value = textures.get(value.substring(1));
        }
        return value == null || value.startsWith("#") ? null : value;
    }

    /**
     * The reader returns the text of a resource, or null if it is missing.
     */
    @Nullable
    public static ResourceLocation findTexture(Function<ResourceLocation, String> read, ResourceLocation block) {
        return findTexture(read, block, null);
    }

    @Nullable
    public static ResourceLocation findTexture(Function<ResourceLocation, String> read, ResourceLocation block, @Nullable String variant) {
        JsonObject state = object(read, new ResourceLocation(block.getNamespace(), "blockstates/" + block.getPath() + ".json"));
        if (state == null) {
            return null;
        }
        Map<String, String> textures = new HashMap<>();
        String name = model(state, textures, variant);
        if (name == null) {
            return null;
        }
        ResourceLocation current = name.contains(":") ? new ResourceLocation(name) : new ResourceLocation(block.getNamespace(), name);
        String path = current.getPath().startsWith("block/") ? current.getPath() : "block/" + current.getPath();
        ResourceLocation model = new ResourceLocation(current.getNamespace(), "models/" + path + ".json");
        for (int depth = 0; depth < 8 && model != null; depth++) {
            JsonObject json = object(read, model);
            if (json == null) {
                break;
            }
            if (json.has("textures") && json.get("textures").isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("textures").entrySet()) {
                    textures.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
                }
            }
            if (json.has("parent")) {
                ResourceLocation parent = new ResourceLocation(json.get("parent").getAsString());
                model = new ResourceLocation(parent.getNamespace(), "models/" + parent.getPath() + ".json");
            } else {
                model = null;
            }
        }
        String chosen = null;
        for (String key : PREFERRED) {
            chosen = resolveReference(textures, textures.containsKey(key) ? textures.get(key) : null);
            if (chosen != null) {
                break;
            }
        }
        if (chosen == null) {
            for (String value : textures.values()) {
                chosen = resolveReference(textures, value);
                if (chosen != null) {
                    break;
                }
            }
        }
        return chosen == null ? null : new ResourceLocation(chosen);
    }

    public static final class Result {
        public final BufferedImage image;
        public final float stoneShare;
        public final boolean replaced;

        Result(BufferedImage image, float stoneShare, boolean replaced) {
            this.image = image;
            this.stoneShare = stoneShare;
            this.replaced = replaced;
        }
    }
}
