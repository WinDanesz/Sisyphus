package com.windanesz.sisyphus.mixin.minecraft;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Draws a flower pot up to the top of its block, where a plant of another mod reaches.
 */
@Mixin(TileEntityFlowerPot.class)
public abstract class MixinTileEntityFlowerPot extends TileEntity {

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos);
    }
}
