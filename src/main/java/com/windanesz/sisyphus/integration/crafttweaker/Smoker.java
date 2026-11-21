package com.windanesz.sisyphus.integration.crafttweaker;

import com.windanesz.sisyphus.recipe.CookingRecipes;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.sisyphus.Smoker")
public final class Smoker {

    private static final String NAME = "smoker";

    private Smoker() {
    }

    /**
     * Comes before the furnace recipe for the same input.
     */
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input) {
        Cooking.addRecipe(NAME, CookingRecipes.SMOKER, output, input);
    }

    @ZenMethod
    public static void remove(IIngredient output) {
        Cooking.remove(NAME, CookingRecipes.SMOKER, output);
    }

    @ZenMethod
    public static void removeByInput(IIngredient input) {
        Cooking.removeByInput(NAME, CookingRecipes.SMOKER, input);
    }

    /**
     * Also drops the furnace recipes it would otherwise take.
     */
    @ZenMethod
    public static void removeAll() {
        Cooking.removeAll(NAME, CookingRecipes.SMOKER);
    }
}
