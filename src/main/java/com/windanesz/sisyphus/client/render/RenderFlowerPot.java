package com.windanesz.sisyphus.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFlowerPot extends TileEntitySpecialRenderer<TileEntityFlowerPot> {

    @Override
    public void render(TileEntityFlowerPot tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile.getBlockType() == Blocks.FLOWER_POT) {
            RenderPottedPlant.render(tile, x, y, z);
        }
    }
}
