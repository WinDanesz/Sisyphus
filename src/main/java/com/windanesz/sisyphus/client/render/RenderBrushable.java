package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.block.BlockSuspicious;
import com.windanesz.sisyphus.tileentity.TileEntityBrushable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class RenderBrushable extends TileEntitySpecialRenderer<TileEntityBrushable> {

    private static float[] translations(EnumFacing side, int dusted) {
        float[] at = {0.5F, 0.0F, 0.5F};
        float out = dusted / 10.0F * 0.75F;
        switch (side) {
            case EAST:
                at[0] = 0.73F + out;
                break;
            case WEST:
                at[0] = 0.25F - out;
                break;
            case UP:
                at[1] = 0.25F + out;
                break;
            case DOWN:
                at[1] = -0.23F - out;
                break;
            case NORTH:
                at[2] = 0.25F - out;
                break;
            case SOUTH:
                at[2] = 0.73F + out;
                break;
        }
        return at;
    }

    @Override
    public void render(TileEntityBrushable tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!tile.hasWorld()) {
            return;
        }
        IBlockState state = tile.getWorld().getBlockState(tile.getPos());
        if (!(state.getBlock() instanceof BlockSuspicious)) {
            return;
        }
        int dusted = state.getValue(BlockSuspicious.DUSTED);
        EnumFacing side = tile.getHitDirection();
        ItemStack stack = tile.getItem();
        if (dusted <= 0 || side == null || stack.isEmpty()) {
            return;
        }
        float[] offset = translations(side, dusted);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.5D, z);
        GlStateManager.translate(offset[0], offset[1], offset[2]);
        GlStateManager.rotate(75.0F, 0.0F, 1.0F, 0.0F);
        boolean eastOrWest = side == EnumFacing.EAST || side == EnumFacing.WEST;
        GlStateManager.rotate((eastOrWest ? 90.0F : 0.0F) + 11.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        int light = tile.getWorld().getCombinedLight(tile.getPos().offset(side), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536.0F);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityBrushable tile) {
        return false;
    }
}
