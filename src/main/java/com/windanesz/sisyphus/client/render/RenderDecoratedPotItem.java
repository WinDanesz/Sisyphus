package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.tileentity.TileEntityDecoratedPot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDecoratedPotItem extends TileEntityItemStackRenderer {

    private final DecoratedPotModel model = new DecoratedPotModel();

    @Override
    public void renderByItem(ItemStack stack) {
        GlStateManager.pushMatrix();
        this.model.render(TileEntityDecoratedPot.decorationsOf(stack));
        GlStateManager.popMatrix();
    }
}
