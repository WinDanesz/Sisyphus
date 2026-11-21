package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.block.BlockBubbleColumn;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

/**
 * Soul sand makes a bubble column going up in the water over it.
 */
@Mixin(BlockSoulSand.class)
public abstract class MixinBlockSoulSand extends Block {

    private MixinBlockSoulSand(Material material, MapColor color) {
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

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        if (BlockBubbleColumn.isEnabled()) {
            BlockBubbleColumn.refresh(world, pos.up());
        }
    }
}
