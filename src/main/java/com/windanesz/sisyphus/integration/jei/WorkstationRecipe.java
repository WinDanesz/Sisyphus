package com.windanesz.sisyphus.integration.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class WorkstationRecipe implements IRecipeWrapper {

    private final List<List<ItemStack>> inputs;
    private final List<ItemStack> outputs;
    @Nullable
    private final String note;
    private final Object[] noteArguments;

    WorkstationRecipe(List<List<ItemStack>> inputs, ItemStack output, @Nullable String noteKey, Object... noteArguments) {
        this(inputs, Collections.singletonList(output), noteKey, noteArguments);
    }

    /**
     * Lists of the same length are cycled together, so the output follows the inputs it is made from.
     */
    WorkstationRecipe(List<List<ItemStack>> inputs, List<ItemStack> outputs, @Nullable String noteKey, Object... noteArguments) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.note = noteKey == null ? null : "jei.sisyphus." + noteKey;
        this.noteArguments = noteArguments;
    }

    static WorkstationRecipe of(ItemStack output, @Nullable String noteKey, ItemStack... inputs) {
        List<List<ItemStack>> lists = new ArrayList<>();
        for (ItemStack input : inputs) {
            lists.add(Collections.singletonList(input));
        }
        return new WorkstationRecipe(lists, output, noteKey);
    }

    int inputCount() {
        return this.inputs.size();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, this.inputs);
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(this.outputs));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (this.note == null) {
            return;
        }
        String text = I18n.format(this.note, this.noteArguments);
        minecraft.fontRenderer.drawString(text, (recipeWidth - minecraft.fontRenderer.getStringWidth(text)) / 2, recipeHeight - 10, 0x808080);
    }
}
