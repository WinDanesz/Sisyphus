package com.windanesz.sisyphus.integration.crafttweaker;

import com.windanesz.sisyphus.recipe.CookingRecipes;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.sisyphus.Campfire")
public final class Campfire {

    private static final String NAME = "campfire";

    private Campfire() {
    }

    /**
     * Comes before the furnace recipe for the same input.
     */
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input) {
        Cooking.addRecipe(NAME, CookingRecipes.CAMPFIRE, output, input);
    }

    @ZenMethod
    public static void remove(IIngredient output) {
        Cooking.remove(NAME, CookingRecipes.CAMPFIRE, output);
    }

    @ZenMethod
    public static void removeByInput(IIngredient input) {
        Cooking.removeByInput(NAME, CookingRecipes.CAMPFIRE, input);
    }

    /**
     * Also drops the furnace recipes it would otherwise take.
     */
    @ZenMethod
    public static void removeAll() {
        Cooking.removeAll(NAME, CookingRecipes.CAMPFIRE);
    }
}
