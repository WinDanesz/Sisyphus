package com.windanesz.sisyphus.recipe;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class CookingRecipes {

    public static final CookingRecipes SMOKER = new CookingRecipes((input, result) -> result.getItem() instanceof ItemFood);

    public static final CookingRecipes BLAST_FURNACE = new CookingRecipes((input, result) -> {
        for (int id : OreDictionary.getOreIDs(input)) {
            if (OreDictionary.getOreName(id).startsWith("ore")) {
                return true;
            }
        }
        return false;
    });

    public static final CookingRecipes CAMPFIRE = new CookingRecipes((input, result) -> result.getItem() instanceof ItemFood);
    private final BiPredicate<ItemStack, ItemStack> rule;
    private final List<Recipe> added = new ArrayList<>();
    private final List<Predicate<ItemStack>> removedInputs = new ArrayList<>();
    private final List<Predicate<ItemStack>> removedOutputs = new ArrayList<>();
    private boolean furnaceRemoved = false;
    private CookingRecipes(BiPredicate<ItemStack, ItemStack> rule) {
        this.rule = rule;
    }

    /**
     * The furnace keeps most recipes under a wildcard, which the ore dictionary does not match, so metadata 0 is checked instead.
     */
    private static ItemStack concrete(ItemStack stack) {
        return stack.getMetadata() == OreDictionary.WILDCARD_VALUE ? new ItemStack(stack.getItem(), 1, 0) : stack;
    }

    public ItemStack result(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (Recipe recipe : this.added) {
            if (recipe.input.apply(input)) {
                return recipe.output.copy();
            }
        }
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(input);
        return !result.isEmpty() && this.takesFurnaceRecipe(input, result) ? result : ItemStack.EMPTY;
    }

    /**
     * The input may be a wildcard key of the furnace list; it is read as metadata 0.
     */
    public boolean takesFurnaceRecipe(ItemStack input, ItemStack result) {
        input = concrete(input);
        if (this.furnaceRemoved || !this.rule.test(input, result)) {
            return false;
        }
        for (Predicate<ItemStack> removed : this.removedInputs) {
            if (removed.test(input)) {
                return false;
            }
        }
        for (Predicate<ItemStack> removed : this.removedOutputs) {
            if (removed.test(result)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasOwnRecipeFor(ItemStack input) {
        input = concrete(input);
        for (Recipe recipe : this.added) {
            if (recipe.input.apply(input)) {
                return true;
            }
        }
        return false;
    }

    public List<Recipe> getAdded() {
        return new ArrayList<>(this.added);
    }

    public void addRecipe(Ingredient input, ItemStack output) {
        this.added.add(new Recipe(input, output.copy()));
    }

    public int removeOutput(Predicate<ItemStack> output) {
        int removed = this.countFurnace((input, result) -> output.test(result));
        this.removedOutputs.add(output);
        for (Iterator<Recipe> recipes = this.added.iterator(); recipes.hasNext(); ) {
            if (output.test(recipes.next().output)) {
                recipes.remove();
                removed++;
            }
        }
        return removed;
    }

    public int removeInput(Predicate<ItemStack> input) {
        int removed = this.countFurnace((stack, result) -> input.test(stack));
        this.removedInputs.add(input);
        for (Iterator<Recipe> recipes = this.added.iterator(); recipes.hasNext(); ) {
            for (ItemStack stack : recipes.next().input.getMatchingStacks()) {
                if (input.test(stack)) {
                    recipes.remove();
                    removed++;
                    break;
                }
            }
        }
        return removed;
    }

    private int countFurnace(BiPredicate<ItemStack, ItemStack> match) {
        int count = 0;
        for (Map.Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            if (this.takesFurnaceRecipe(entry.getKey(), entry.getValue()) && match.test(concrete(entry.getKey()), entry.getValue())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Recipes added after this stay.
     */
    public void removeAll() {
        this.furnaceRemoved = true;
        this.added.clear();
    }

    void restoreDefaults() {
        this.added.clear();
        this.removedInputs.clear();
        this.removedOutputs.clear();
        this.furnaceRemoved = false;
    }

    public static final class Recipe {
        public final Ingredient input;
        public final ItemStack output;

        Recipe(Ingredient input, ItemStack output) {
            this.input = input;
            this.output = output;
        }
    }
}
