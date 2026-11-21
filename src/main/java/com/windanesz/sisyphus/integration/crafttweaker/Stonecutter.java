package com.windanesz.sisyphus.integration.crafttweaker;

import com.windanesz.sisyphus.recipe.StonecutterRecipes;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.sisyphus.Stonecutter")
public final class Stonecutter {

    private Stonecutter() {
    }

    /**
     * One block gives the whole output stack.
     */
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input) {
        if (Scripts.present("Stonecutter.addRecipe", output, input)) {
            Scripts.apply("Adding a stonecutter recipe for " + output.toCommandString() + " from " + Scripts.name(input),
                    () -> StonecutterRecipes.addRecipe(Scripts.ingredient(input), CraftTweakerMC.getItemStack(output)));
        }
    }

    @ZenMethod
    public static void remove(IIngredient output) {
        if (Scripts.present("Stonecutter.remove", output)) {
            Scripts.remove("Removing the stonecutter recipes for " + Scripts.name(output), output, StonecutterRecipes::removeOutputs);
        }
    }

    @ZenMethod
    public static void removeByInput(IIngredient input) {
        if (Scripts.present("Stonecutter.removeByInput", input)) {
            Scripts.remove("Removing the stonecutter recipes from " + Scripts.name(input), input, StonecutterRecipes::removeInputs);
        }
    }

    /**
     * The recipes of this mod are removed too.
     */
    @ZenMethod
    public static void removeAll() {
        Scripts.apply("Removing every stonecutter recipe", StonecutterRecipes::removeAll);
    }
}
