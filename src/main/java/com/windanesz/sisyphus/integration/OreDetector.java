package com.windanesz.sisyphus.integration;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class OreDetector {

    private static final List<String> NOT_STONE = Arrays.asList("nether", "netherrack", "basalt", "blackstone", "soul", "magma", "end", "endstone", "deepslate",
            "sand", "gravel", "dirt", "clay", "obsidian", "bedrock", "ice", "snow", "moon", "mars", "asteroid");

    private OreDetector() {
    }

    /**
     * Matches 'copper_ore', 'ore_copper', 'ores' and 'copperore'.
     */
    public static boolean isOreName(String path) {
        String name = path.toLowerCase(Locale.ROOT);
        for (String token : name.split("[_/.]")) {
            if (token.equals("ore") || token.equals("ores")) {
                return true;
            }
        }
        // Joined words, as in 'copperore', but not 'core', 'more' or 'restore'
        return name.endsWith("ore") && name.length() >= 6 && !name.endsWith("restore") && !name.endsWith("explore") && !name.endsWith("ignore")
                && !name.endsWith("before") && !name.endsWith("adore") && !name.endsWith("shore") && !name.endsWith("score");
    }

    /**
     * Such as 'nether_gold_ore' or 'end_ore'.
     */
    public static boolean isNotStoneName(String path) {
        for (String token : path.toLowerCase(Locale.ROOT).split("[_/.]")) {
            if (NOT_STONE.contains(token) || token.startsWith("nether")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOreDictName(String name) {
        return name.length() > 3 && name.startsWith("ore") && Character.isUpperCase(name.charAt(3));
    }

    /**
     * 'modid:name', 'modid:*' or 'modid'.
     */
    public static boolean matches(String pattern, ResourceLocation id) {
        String p = pattern.trim();
        if (p.isEmpty()) {
            return false;
        }
        if (p.endsWith(":*")) {
            return id.getNamespace().equals(p.substring(0, p.length() - 2));
        }
        if (!p.contains(":")) {
            return id.getNamespace().equals(p);
        }
        return id.toString().equals(p);
    }

    public static boolean matchesAny(String[] patterns, ResourceLocation id) {
        for (String pattern : patterns) {
            if (matches(pattern, id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Only such a block can be copied as a deepslate variant.
     */
    public static boolean isSimpleRock(Block block) {
        IBlockState state = block.getDefaultState();
        return block.getBlockState().getValidStates().size() == 1 && state.getMaterial() == Material.ROCK && !block.hasTileEntity(state) && state.isFullCube();
    }

    private static boolean inOreDictionary(Block block) {
        ItemStack stack = new ItemStack(block);
        if (stack.isEmpty()) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if (isOreDictName(name) && !isNotStoneName(camelToSnake(name.substring(3)))) {
                return true;
            }
        }
        return false;
    }

    private static String camelToSnake(String name) {
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }

    public static boolean isCandidate(Block block, String[] whitelist, String[] blacklist) {
        ResourceLocation id = block.getRegistryName();
        if (id == null || id.getNamespace().equals("minecraft") || id.getNamespace().equals(SisyphusBackport.MODID) || matchesAny(blacklist, id)) {
            return false;
        }
        if (!isSimpleRock(block)) {
            return false;
        }
        if (matchesAny(whitelist, id)) {
            return true;
        }
        if (isNotStoneName(id.getPath())) {
            return false;
        }
        return isOreName(id.getPath()) || inOreDictionary(block);
    }
}
