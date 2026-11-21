package com.windanesz.sisyphus.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

final class Scripts {

    private Scripts() {
    }

    static Ingredient ingredient(IIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : CraftTweakerMC.getIngredient(ingredient);
    }

    static Predicate<ItemStack> matcher(IIngredient ingredient) {
        return stack -> !stack.isEmpty() && ingredient.matches(CraftTweakerMC.getIItemStack(stack));
    }

    static String name(IIngredient ingredient) {
        return ingredient == null ? "nothing" : ingredient.toCommandString();
    }

    static boolean present(String what, Object... arguments) {
        for (Object argument : arguments) {
            if (argument == null) {
                CraftTweakerAPI.logError(what + ": an item that the recipe needs is null, maybe it is misspelled or its mod is missing");
                return false;
            }
        }
        return true;
    }

    static void apply(String description, Runnable change) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                change.run();
            }

            @Override
            public String describe() {
                return description;
            }
        });
    }

    static void remove(String description, IIngredient output, ToIntFunction<Predicate<ItemStack>> removal) {
        apply(description, () -> {
            if (removal.applyAsInt(matcher(output)) == 0) {
                CraftTweakerAPI.logWarning(description + ": no recipe matched");
            }
        });
    }
}
