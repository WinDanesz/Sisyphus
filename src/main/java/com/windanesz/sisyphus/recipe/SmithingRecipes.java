package com.windanesz.sisyphus.recipe;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.item.ArmorTrims;
import com.windanesz.sisyphus.item.NetheriteItems;
import com.windanesz.sisyphus.registry.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Armor trims are not recipes of their own; they follow from the template, the armor and the material.
 */
public final class SmithingRecipes {

    private static final List<Recipe> RECIPES = new ArrayList<>();
    private static boolean built = false;

    private SmithingRecipes() {
    }

    private static void build() {
        if (built) {
            return;
        }
        built = true;
        Item ingot = ModItems.get("netherite_ingot");
        if (ingot == null) {
            return;
        }
        Item template = ArmorTrims.netheriteTemplate();
        Ingredient addition = Ingredient.fromItem(ingot);
        for (Map.Entry<Item, Item> entry : NetheriteItems.getUpgrades().entrySet()) {
            RECIPES.add(new Recipe(template == null ? null : Ingredient.fromItem(template), Ingredient.fromItem(entry.getKey()), addition,
                    new ItemStack(entry.getValue()), true));
        }
    }

    /**
     * Armor trims are not included.
     */
    public static List<Recipe> getAll() {
        build();
        return new ArrayList<>(RECIPES);
    }

    public static ItemStack result(ItemStack template, ItemStack base, ItemStack addition) {
        return match(template, base, addition).output;
    }

    /**
     * Keeps the base's tag with the output's on top, and the base's wear when both can wear out.
     */
    public static Result match(ItemStack template, ItemStack base, ItemStack addition) {
        build();
        if (base.isEmpty() || addition.isEmpty()) {
            return Result.NONE;
        }
        for (Recipe recipe : RECIPES) {
            if (recipe.matches(template, base, addition)) {
                // a netherite template that is not needed is not used up either
                return new Result(make(recipe.output, base), !template.isEmpty() && recipe.needsTemplate());
            }
        }
        String pattern = ArmorTrims.patternOf(template);
        ArmorTrims.Material material = ArmorTrims.materialOf(addition);
        if (pattern != null && material != null && ArmorTrims.isTrimmable(base)) {
            ArmorTrims.Trim trim = new ArmorTrims.Trim(pattern, material);
            if (trim.equals(ArmorTrims.get(base))) {
                return Result.NONE;
            }
            ItemStack out = base.copy();
            out.setCount(1);
            ArmorTrims.set(out, trim);
            return new Result(out, true);
        }
        return Result.NONE;
    }

    private static ItemStack make(ItemStack output, ItemStack base) {
        ItemStack out = output.copy();
        if (out.isItemStackDamageable() && base.isItemStackDamageable()) {
            out.setItemDamage(Math.min(base.getItemDamage(), out.getMaxDamage() - 1));
        }
        if (base.hasTagCompound()) {
            NBTTagCompound tag = base.getTagCompound().copy();
            if (output.hasTagCompound()) {
                tag.merge(output.getTagCompound());
            }
            out.setTagCompound(tag);
        }
        return out;
    }

    public static void addRecipe(Ingredient base, Ingredient addition, ItemStack output) {
        addRecipe(null, base, addition, output);
    }

    public static void addRecipe(@Nullable Ingredient template, Ingredient base, Ingredient addition, ItemStack output) {
        build();
        RECIPES.add(new Recipe(template, base, addition, output.copy(), false));
    }

    public static int removeOutputs(Predicate<ItemStack> output) {
        build();
        int removed = 0;
        for (Iterator<Recipe> recipes = RECIPES.iterator(); recipes.hasNext(); ) {
            if (output.test(recipes.next().output)) {
                recipes.remove();
                removed++;
            }
        }
        return removed;
    }

    static void restoreDefaults() {
        RECIPES.clear();
        built = false;
    }

    /**
     * Armor trims stay, as they are not recipes.
     */
    public static void removeAll() {
        build();
        RECIPES.clear();
    }

    public static final class Recipe {
        /**
         * Null if the template slot stays empty.
         */
        @Nullable
        public final Ingredient template;
        public final Ingredient base;
        public final Ingredient addition;
        public final ItemStack output;
        /**
         * Its template is only needed when the config says so.
         */
        final boolean netheriteUpgrade;

        Recipe(@Nullable Ingredient template, Ingredient base, Ingredient addition, ItemStack output, boolean netheriteUpgrade) {
            this.template = template;
            this.base = base;
            this.addition = addition;
            this.output = output;
            this.netheriteUpgrade = netheriteUpgrade;
        }

        public boolean needsTemplate() {
            return this.template != null && (!this.netheriteUpgrade || Settings.items.netheriteNeedsTemplate);
        }

        boolean matches(ItemStack template, ItemStack base, ItemStack addition) {
            if (!this.base.apply(base) || !this.addition.apply(addition)) {
                return false;
            }
            if (template.isEmpty()) {
                return !needsTemplate();
            }
            return this.template != null && this.template.apply(template);
        }
    }

    public static final class Result {
        public static final Result NONE = new Result(ItemStack.EMPTY, false);

        public final ItemStack output;
        public final boolean usesTemplate;

        Result(ItemStack output, boolean usesTemplate) {
            this.output = output;
            this.usesTemplate = usesTemplate;
        }
    }
}
