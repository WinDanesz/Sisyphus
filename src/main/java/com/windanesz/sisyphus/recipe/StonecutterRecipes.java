package com.windanesz.sisyphus.recipe;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public final class StonecutterRecipes {

    private static final List<Recipe> RECIPES = new ArrayList<>();
    private static boolean built = false;

    private StonecutterRecipes() {
    }

    private static Block ours(String name) {
        return Block.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name));
    }

    private static ItemStack mine(String name, int count) {
        Block block = ours(name);
        return block == Blocks.AIR ? ItemStack.EMPTY : new ItemStack(block, count);
    }

    private static ItemStack of(Block block, int meta, int count) {
        return new ItemStack(block, count, meta);
    }

    private static void add(ItemStack input, ItemStack... outputs) {
        if (input.isEmpty()) {
            return;
        }
        Recipe recipe = new Recipe(Ingredient.fromStacks(input));
        for (ItemStack output : outputs) {
            if (!output.isEmpty()) {
                recipe.outputs.add(output);
            }
        }
        if (!recipe.outputs.isEmpty()) {
            RECIPES.add(recipe);
        }
    }

    private static void build() {
        if (built) {
            return;
        }
        built = true;

        // Stone and its relatives
        add(of(Blocks.STONE, 0, 1), mine("stone_slab", 2), mine("stone_stairs", 1), of(Blocks.STONEBRICK, 0, 1),
                of(Blocks.STONE_SLAB, 5, 2), of(Blocks.STONE_BRICK_STAIRS, 0, 1), mine("stone_brick_wall", 1), of(Blocks.STONEBRICK, 3, 1));
        add(of(Blocks.COBBLESTONE, 0, 1), of(Blocks.STONE_SLAB, 3, 2), of(Blocks.STONE_STAIRS, 0, 1), of(Blocks.COBBLESTONE_WALL, 0, 1));
        add(of(Blocks.STONEBRICK, 0, 1), of(Blocks.STONE_SLAB, 5, 2), of(Blocks.STONE_BRICK_STAIRS, 0, 1), mine("stone_brick_wall", 1), of(Blocks.STONEBRICK, 3, 1));
        add(of(Blocks.STONEBRICK, 1, 1), mine("mossy_stone_brick_slab", 2), mine("mossy_stone_brick_stairs", 1), mine("mossy_stone_brick_wall", 1));
        add(of(Blocks.MOSSY_COBBLESTONE, 0, 1), mine("mossy_cobblestone_slab", 2), mine("mossy_cobblestone_stairs", 1), of(Blocks.COBBLESTONE_WALL, 1, 1));
        add(mine("smooth_stone", 1), of(Blocks.STONE_SLAB, 0, 2));

        // Granite, diorite and andesite, and their polished versions (meta 1 to 6 of the stone block)
        String[] names = {"granite", "polished_granite", "diorite", "polished_diorite", "andesite", "polished_andesite"};
        int[] metas = {1, 2, 3, 4, 5, 6};
        for (int i = 0; i < names.length; i += 2) {
            String rough = names[i];
            String polished = names[i + 1];
            add(of(Blocks.STONE, metas[i], 1), mine(rough + "_slab", 2), mine(rough + "_stairs", 1), mine(rough + "_wall", 1),
                    of(Blocks.STONE, metas[i + 1], 1), mine(polished + "_slab", 2), mine(polished + "_stairs", 1));
            add(of(Blocks.STONE, metas[i + 1], 1), mine(polished + "_slab", 2), mine(polished + "_stairs", 1));
        }

        // Sandstone and red sandstone
        add(of(Blocks.SANDSTONE, 0, 1), of(Blocks.STONE_SLAB, 1, 2), of(Blocks.SANDSTONE_STAIRS, 0, 1), mine("sandstone_wall", 1),
                of(Blocks.SANDSTONE, 1, 1), of(Blocks.SANDSTONE, 2, 1));
        add(mine("smooth_sandstone", 1), mine("smooth_sandstone_slab", 2), mine("smooth_sandstone_stairs", 1));
        add(of(Blocks.RED_SANDSTONE, 0, 1), of(Blocks.STONE_SLAB2, 0, 2), of(Blocks.RED_SANDSTONE_STAIRS, 0, 1), mine("red_sandstone_wall", 1),
                of(Blocks.RED_SANDSTONE, 1, 1), of(Blocks.RED_SANDSTONE, 2, 1));
        add(mine("smooth_red_sandstone", 1), mine("smooth_red_sandstone_slab", 2), mine("smooth_red_sandstone_stairs", 1));

        // Quartz
        add(of(Blocks.QUARTZ_BLOCK, 0, 1), of(Blocks.STONE_SLAB, 7, 2), of(Blocks.QUARTZ_STAIRS, 0, 1), of(Blocks.QUARTZ_BLOCK, 1, 1), of(Blocks.QUARTZ_BLOCK, 2, 1), mine("quartz_bricks", 1));
        add(mine("smooth_quartz", 1), mine("smooth_quartz_slab", 2), mine("smooth_quartz_stairs", 1));

        // Bricks
        add(of(Blocks.BRICK_BLOCK, 0, 1), of(Blocks.STONE_SLAB, 4, 2), of(Blocks.BRICK_STAIRS, 0, 1), mine("brick_wall", 1));
        add(of(Blocks.NETHER_BRICK, 0, 1), of(Blocks.STONE_SLAB, 6, 2), of(Blocks.NETHER_BRICK_STAIRS, 0, 1), mine("nether_brick_wall", 1), mine("chiseled_nether_bricks", 1));
        add(of(Blocks.RED_NETHER_BRICK, 0, 1), mine("red_nether_brick_slab", 2), mine("red_nether_brick_stairs", 1), mine("red_nether_brick_wall", 1));

        // The end
        add(of(Blocks.END_STONE, 0, 1), of(Blocks.END_BRICKS, 0, 1), mine("end_stone_brick_slab", 2), mine("end_stone_brick_stairs", 1), mine("end_stone_brick_wall", 1));
        add(of(Blocks.END_BRICKS, 0, 1), mine("end_stone_brick_slab", 2), mine("end_stone_brick_stairs", 1), mine("end_stone_brick_wall", 1));
        add(of(Blocks.PURPUR_BLOCK, 0, 1), of(Blocks.PURPUR_SLAB, 0, 2), of(Blocks.PURPUR_STAIRS, 0, 1), of(Blocks.PURPUR_PILLAR, 0, 1));

        // Blackstone and basalt
        add(mine("blackstone", 1), mine("blackstone_slab", 2), mine("blackstone_stairs", 1), mine("blackstone_wall", 1), mine("polished_blackstone", 1),
                mine("polished_blackstone_slab", 2), mine("polished_blackstone_stairs", 1), mine("polished_blackstone_wall", 1), mine("chiseled_polished_blackstone", 1),
                mine("polished_blackstone_bricks", 1), mine("polished_blackstone_brick_slab", 2), mine("polished_blackstone_brick_stairs", 1),
                mine("polished_blackstone_brick_wall", 1));
        add(mine("polished_blackstone", 1), mine("polished_blackstone_slab", 2), mine("polished_blackstone_stairs", 1), mine("polished_blackstone_wall", 1),
                mine("chiseled_polished_blackstone", 1), mine("polished_blackstone_bricks", 1), mine("polished_blackstone_brick_slab", 2),
                mine("polished_blackstone_brick_stairs", 1), mine("polished_blackstone_brick_wall", 1));
        add(mine("polished_blackstone_bricks", 1), mine("polished_blackstone_brick_slab", 2), mine("polished_blackstone_brick_stairs", 1),
                mine("polished_blackstone_brick_wall", 1));
        add(mine("basalt", 1), mine("polished_basalt", 1));

        // Mud bricks
        add(mine("mud_bricks", 1), mine("mud_brick_slab", 2), mine("mud_brick_stairs", 1), mine("mud_brick_wall", 1));

        // Prismarine
        add(of(Blocks.PRISMARINE, 0, 1), mine("prismarine_slab", 2), mine("prismarine_stairs", 1), mine("prismarine_wall", 1), of(Blocks.PRISMARINE, 1, 1));
        add(of(Blocks.PRISMARINE, 1, 1), mine("prismarine_brick_slab", 2), mine("prismarine_brick_stairs", 1));
        add(of(Blocks.PRISMARINE, 2, 1), mine("dark_prismarine_slab", 2), mine("dark_prismarine_stairs", 1));
    }

    public static List<Cut> getAll() {
        build();
        List<Cut> cuts = new ArrayList<>();
        for (Recipe recipe : RECIPES) {
            for (ItemStack output : recipe.outputs) {
                cuts.add(new Cut(recipe.input, output.copy()));
            }
        }
        return cuts;
    }

    /**
     * Outputs of several recipes for the same block are merged in the order they were added.
     */
    public static List<ItemStack> getResults(ItemStack input) {
        build();
        if (input.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemStack> results = null;
        for (Recipe recipe : RECIPES) {
            if (recipe.input.apply(input)) {
                if (results == null) {
                    results = new ArrayList<>();
                }
                results.addAll(recipe.outputs);
            }
        }
        return results == null ? Collections.<ItemStack>emptyList() : results;
    }

    public static void addRecipe(Ingredient input, ItemStack output) {
        build();
        Recipe recipe = new Recipe(input);
        recipe.outputs.add(output.copy());
        RECIPES.add(recipe);
    }

    public static int removeOutputs(Predicate<ItemStack> output) {
        build();
        int removed = 0;
        for (Iterator<Recipe> recipes = RECIPES.iterator(); recipes.hasNext(); ) {
            Recipe recipe = recipes.next();
            for (Iterator<ItemStack> outputs = recipe.outputs.iterator(); outputs.hasNext(); ) {
                if (output.test(outputs.next())) {
                    outputs.remove();
                    removed++;
                }
            }
            if (recipe.outputs.isEmpty()) {
                recipes.remove();
            }
        }
        return removed;
    }

    public static int removeInputs(Predicate<ItemStack> input) {
        build();
        int removed = 0;
        for (Iterator<Recipe> recipes = RECIPES.iterator(); recipes.hasNext(); ) {
            Recipe recipe = recipes.next();
            for (ItemStack stack : recipe.input.getMatchingStacks()) {
                if (input.test(stack)) {
                    removed += recipe.outputs.size();
                    recipes.remove();
                    break;
                }
            }
        }
        return removed;
    }

    static void restoreDefaults() {
        RECIPES.clear();
        built = false;
    }

    public static void removeAll() {
        build();
        RECIPES.clear();
    }

    private static final class Recipe {
        final Ingredient input;
        final List<ItemStack> outputs = new ArrayList<>();

        Recipe(Ingredient input) {
            this.input = input;
        }
    }

    public static final class Cut {
        public final Ingredient input;
        public final ItemStack output;

        Cut(Ingredient input, ItemStack output) {
            this.input = input;
            this.output = output;
        }
    }
}
