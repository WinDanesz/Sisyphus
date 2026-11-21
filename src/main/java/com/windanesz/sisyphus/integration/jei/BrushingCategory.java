package com.windanesz.sisyphus.integration.jei;

import com.windanesz.sisyphus.SisyphusBackport;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

final class BrushingCategory implements IRecipeCategory<BrushingRecipe> {

    static final String ID = "brushing";

    private static final ResourceLocation FURNACE = new ResourceLocation("textures/gui/container/furnace.png");
    private static final int TOP = 12;
    private static final int GRID_X = 48;
    private static final int COLUMNS = 6;
    private static final int ROWS = 6;

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    BrushingCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(GRID_X + COLUMNS * 18, TOP + ROWS * 18);
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.createDrawable(FURNACE, 79, 34, 24, 17);
    }

    @Override
    public String getUid() {
        return SisyphusBackport.MODID + ":" + ID;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.sisyphus.category." + ID);
    }

    @Override
    public String getModName() {
        return SisyphusBackport.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        this.arrow.draw(minecraft, 20, TOP + 10);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, BrushingRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(0, true, 0, TOP);
        stacks.set(0, recipe.block());
        stacks.init(1, true, 0, TOP + 20);
        stacks.set(1, recipe.brush());
        List<ItemStack> finds = recipe.finds();
        int shown = Math.min(finds.size(), COLUMNS * ROWS);
        for (int i = 0; i < shown; i++) {
            stacks.init(2 + i, false, GRID_X + (i % COLUMNS) * 18, TOP + (i / COLUMNS) * 18);
            stacks.set(2 + i, finds.get(i));
        }
        for (int i = 0; i < 2 + shown; i++) {
            stacks.setBackground(i, this.slot);
        }
        stacks.addTooltipCallback((index, input, stack, tooltip) -> {
            if (!input && index >= 2) {
                tooltip.add(I18n.format("jei.sisyphus.brushing_chance", recipe.chance(index - 2)));
            }
        });
    }
}
