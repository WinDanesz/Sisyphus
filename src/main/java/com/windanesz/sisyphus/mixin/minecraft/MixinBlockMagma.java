package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.block.BlockBubbleColumn;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMagma;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * A magma block makes a bubble column going down, instead of drying the water up.
 */
@Mixin(BlockMagma.class)
public abstract class MixinBlockMagma extends Block {

    private MixinBlockMagma(Material material, MapColor color) {
        super(material, color);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (BlockBubbleColumn.isEnabled()) {
            world.scheduleUpdate(pos, this, BlockBubbleColumn.SOURCE_DELAY);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        if (fromPos.equals(pos.up()) && BlockBubbleColumn.isEnabled()) {
            world.scheduleUpdate(pos, this, BlockBubbleColumn.SOURCE_DELAY);
        }
    }

    @Inject(method = "updateTick", at = @At("HEAD"), cancellable = true)
    private void sisyphus$makeBubbleColumn(World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (BlockBubbleColumn.isEnabled()) {
            BlockBubbleColumn.refresh(world, pos.up());
            ci.cancel();
        }
    }
}
