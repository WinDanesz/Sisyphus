package com.windanesz.sisyphus.integration.crafttweaker;

import com.windanesz.sisyphus.recipe.SmithingRecipes;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.sisyphus.SmithingTable")
public final class SmithingTable {

    private SmithingTable() {
    }

    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient base, IIngredient addition) {
        if (Scripts.present("SmithingTable.addRecipe", output, base, addition)) {
            Scripts.apply("Adding a smithing table recipe for " + output.toCommandString() + " from " + Scripts.name(base) + " and "
                            + Scripts.name(addition),
                    () -> SmithingRecipes.addRecipe(Scripts.ingredient(base), Scripts.ingredient(addition), CraftTweakerMC.getItemStack(output)));
        }
    }

    /**
     * The template is used up.
     */
    @ZenMethod
    public static void addRecipeWithTemplate(IItemStack output, IIngredient template, IIngredient base, IIngredient addition) {
        if (Scripts.present("SmithingTable.addRecipeWithTemplate", output, template, base, addition)) {
            Scripts.apply("Adding a smithing table recipe for " + output.toCommandString() + " from " + Scripts.name(template) + ", "
                            + Scripts.name(base) + " and " + Scripts.name(addition),
                    () -> SmithingRecipes.addRecipe(Scripts.ingredient(template), Scripts.ingredient(base), Scripts.ingredient(addition),
                            CraftTweakerMC.getItemStack(output)));
        }
    }

    @ZenMethod
    public static void remove(IIngredient output) {
        if (Scripts.present("SmithingTable.remove", output)) {
            Scripts.remove("Removing the smithing table recipes for " + Scripts.name(output), output, SmithingRecipes::removeOutputs);
        }
    }

    /**
     * Netherite upgrades included.
     */
    @ZenMethod
    public static void removeAll() {
        Scripts.apply("Removing every smithing table recipe", SmithingRecipes::removeAll);
    }
}
