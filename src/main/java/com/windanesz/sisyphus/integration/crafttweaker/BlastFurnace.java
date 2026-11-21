package com.windanesz.sisyphus.integration.crafttweaker;

import com.windanesz.sisyphus.recipe.CookingRecipes;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.sisyphus.BlastFurnace")
public final class BlastFurnace {

    private static final String NAME = "blast furnace";

    private BlastFurnace() {
    }

    /**
     * Comes before the furnace recipe for the same input.
     */
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input) {
        Cooking.addRecipe(NAME, CookingRecipes.BLAST_FURNACE, output, input);
    }

    @ZenMethod
    public static void remove(IIngredient output) {
        Cooking.remove(NAME, CookingRecipes.BLAST_FURNACE, output);
    }

    @ZenMethod
    public static void removeByInput(IIngredient input) {
        Cooking.removeByInput(NAME, CookingRecipes.BLAST_FURNACE, input);
    }

    /**
     * Also drops the furnace recipes it would otherwise take.
     */
    @ZenMethod
    public static void removeAll() {
        Cooking.removeAll(NAME, CookingRecipes.BLAST_FURNACE);
    }
}
