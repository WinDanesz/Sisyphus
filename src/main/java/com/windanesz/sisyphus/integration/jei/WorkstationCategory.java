package com.windanesz.sisyphus.integration.jei;

import com.windanesz.sisyphus.SisyphusBackport;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

final class WorkstationCategory implements IRecipeCategory<WorkstationRecipe> {

    private static final ResourceLocation FURNACE = new ResourceLocation("textures/gui/container/furnace.png");

    private final String uid;
    private final String titleKey;
    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;
    @Nullable
    private final IDrawableAnimated animatedArrow;
    private final int[][] inputs;
    private final int[] output;
    private final int[] arrowAt;
    private final int[][] pluses;

    WorkstationCategory(IGuiHelper helper, String id, int width, int height, int[][] inputs, int[] output, int[] arrowAt, int[][] pluses, int cookTicks) {
        this.uid = SisyphusBackport.MODID + ":" + id;
        this.titleKey = "jei.sisyphus.category." + id;
        this.background = helper.createBlankDrawable(width, height);
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.createDrawable(FURNACE, 79, 34, 24, 17);
        this.animatedArrow = cookTicks > 0 ? helper.drawableBuilder(FURNACE, 176, 14, 24, 17).buildAnimated(cookTicks, IDrawableAnimated.StartDirection.LEFT, false) : null;
        this.inputs = inputs;
        this.output = output;
        this.arrowAt = arrowAt;
        this.pluses = pluses;
    }

    @Override
    public String getUid() {
        return this.uid;
    }

    @Override
    public String getTitle() {
        return I18n.format(this.titleKey);
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
        this.arrow.draw(minecraft, this.arrowAt[0], this.arrowAt[1]);
        if (this.animatedArrow != null) {
            this.animatedArrow.draw(minecraft, this.arrowAt[0], this.arrowAt[1]);
        }
        for (int[] plus : this.pluses) {
            minecraft.fontRenderer.drawString("+", plus[0], plus[1], 0x808080);
        }
    }

    @Override
    public void setRecipe(IRecipeLayout layout, WorkstationRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        int count = recipe.inputCount();
        for (int i = 0; i < count; i++) {
            stacks.init(i, true, this.inputs[i][0], this.inputs[i][1]);
            stacks.setBackground(i, this.slot);
        }
        stacks.init(count, false, this.output[0], this.output[1]);
        stacks.setBackground(count, this.slot);
        stacks.set(ingredients);
    }
}
