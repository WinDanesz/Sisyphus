package com.windanesz.sisyphus.integration.crafttweaker;

import com.windanesz.sisyphus.recipe.CookingRecipes;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;

final class Cooking {

    private Cooking() {
    }

    static void addRecipe(String block, CookingRecipes recipes, IItemStack output, IIngredient input) {
        if (Scripts.present(block + ".addRecipe", output, input)) {
            Scripts.apply("Adding a " + block + " recipe for " + output.toCommandString() + " from " + Scripts.name(input),
                    () -> recipes.addRecipe(Scripts.ingredient(input), CraftTweakerMC.getItemStack(output)));
        }
    }

    static void remove(String block, CookingRecipes recipes, IIngredient output) {
        if (Scripts.present(block + ".remove", output)) {
            Scripts.remove("Removing the " + block + " recipes for " + Scripts.name(output), output, recipes::removeOutput);
        }
    }

    static void removeByInput(String block, CookingRecipes recipes, IIngredient input) {
        if (Scripts.present(block + ".removeByInput", input)) {
            Scripts.remove("Removing the " + block + " recipes from " + Scripts.name(input), input, recipes::removeInput);
        }
    }

    static void removeAll(String block, CookingRecipes recipes) {
        Scripts.apply("Removing every " + block + " recipe", recipes::removeAll);
    }
}
