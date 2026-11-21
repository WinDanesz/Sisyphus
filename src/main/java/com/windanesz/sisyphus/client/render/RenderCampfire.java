package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.block.BlockCampfire;
import com.windanesz.sisyphus.tileentity.TileEntityCampfire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class RenderCampfire extends TileEntitySpecialRenderer<TileEntityCampfire> {

    @Override
    public void render(TileEntityCampfire tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!tile.hasWorld() || !(tile.getWorld().getBlockState(tile.getPos()).getBlock() instanceof BlockCampfire)) {
            return;
        }
        EnumFacing facing = tile.getWorld().getBlockState(tile.getPos()).getValue(BlockCampfire.FACING);
        float angle = -facing.getHorizontalAngle();

        for (int i = 0; i < tile.getItems().size(); i++) {
            ItemStack stack = tile.getItems().get(i);
            if (stack.isEmpty()) {
                continue;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + 0.44921875D, z + 0.5D);
            GlStateManager.rotate(angle + (i * 90.0F), 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.3125D, 0.0D, -0.3125D);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.375F, 0.375F, 0.375F);
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }
}
