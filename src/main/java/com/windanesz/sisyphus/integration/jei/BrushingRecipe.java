package com.windanesz.sisyphus.integration.jei;

import com.windanesz.sisyphus.worldgen.ArchaeologyLoot;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class BrushingRecipe implements IRecipeWrapper {

    private final String table;
    private final ItemStack block;
    private final ItemStack brush;
    private final List<ItemStack> finds = new ArrayList<>();
    private final List<Integer> weights = new ArrayList<>();
    private int totalWeight;

    BrushingRecipe(String table, ItemStack block, ItemStack brush) {
        this.table = table;
        this.block = block;
        this.brush = brush;
        for (ArchaeologyLoot.Find find : ArchaeologyLoot.finds(table)) {
            Item item = Item.REGISTRY.getObject(find.item);
            if (item == null || item == Items.AIR) {
                continue;
            }
            this.finds.add(new ItemStack(item, 1, find.meta));
            this.weights.add(find.weight);
            this.totalWeight += find.weight;
        }
    }

    ItemStack block() {
        return this.block;
    }

    ItemStack brush() {
        return this.brush;
    }

    List<ItemStack> finds() {
        return this.finds;
    }

    boolean isEmpty() {
        return this.finds.isEmpty();
    }

    String chance(int index) {
        double percent = 100.0 * this.weights.get(index) / this.totalWeight;
        return percent == Math.floor(percent) ? String.valueOf((int) percent) : String.format("%.1f", percent);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(Collections.singletonList(this.block));
        inputs.add(Collections.singletonList(this.brush));
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutputs(VanillaTypes.ITEM, this.finds);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String title = I18n.format("jei.sisyphus.brushing." + this.table);
        minecraft.fontRenderer.drawString(title, 0, 0, 0x404040);
    }
}
