package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.block.BlockDecoratedPot;
import com.windanesz.sisyphus.tileentity.TileEntityDecoratedPot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public class RenderDecoratedPot extends TileEntitySpecialRenderer<TileEntityDecoratedPot> {

    private final DecoratedPotModel model = new DecoratedPotModel();

    private static void wobble(TileEntityDecoratedPot tile, float partialTicks) {
        TileEntityDecoratedPot.WobbleStyle style = tile.lastWobbleStyle;
        if (style == null) {
            return;
        }
        float progress = (tile.getWorld().getTotalWorldTime() - tile.wobbleStartedAtTick + partialTicks) / style.duration;
        if (progress < 0.0F || progress > 1.0F) {
            return;
        }
        if (style == TileEntityDecoratedPot.WobbleStyle.POSITIVE) {
            float angle = progress * ((float) Math.PI * 2.0F);
            float tiltX = -1.5F * (MathHelper.cos(angle) + 0.5F) * MathHelper.sin(angle / 2.0F);
            rotateAround(tiltX * 0.015625F, 1.0F, 0.0F, 0.0F);
            rotateAround(MathHelper.sin(angle) * 0.015625F, 0.0F, 0.0F, 1.0F);
        } else {
            float turn = MathHelper.sin(-progress * 3.0F * (float) Math.PI) * 0.125F;
            rotateAround(turn * (1.0F - progress), 0.0F, 1.0F, 0.0F);
        }
    }

    private static void rotateAround(float radians, float x, float y, float z) {
        GlStateManager.translate(0.5F, 0.0F, 0.5F);
        GlStateManager.rotate((float) Math.toDegrees(radians), x, y, z);
        GlStateManager.translate(-0.5F, 0.0F, -0.5F);
    }

    @Override
    public void render(TileEntityDecoratedPot tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        EnumFacing facing = EnumFacing.NORTH;
        if (tile.hasWorld()) {
            IBlockState state = tile.getWorld().getBlockState(tile.getPos());
            if (state.getBlock() instanceof BlockDecoratedPot) {
                facing = state.getValue(BlockDecoratedPot.FACING);
            }
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5D, 0.0D, 0.5D);
        GlStateManager.rotate(180.0F - facing.getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5D, 0.0D, -0.5D);
        if (tile.hasWorld()) {
            wobble(tile, partialTicks);
        }
        this.model.render(tile.getDecorations());
        GlStateManager.popMatrix();
    }
}
