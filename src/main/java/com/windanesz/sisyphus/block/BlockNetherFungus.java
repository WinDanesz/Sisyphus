package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.worldgen.WorldGenHugeFungus;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockNetherFungus extends BlockNetherPlant implements IGrowable {

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.5625D, 0.75D);

    private final boolean warped;

    public BlockNetherFungus(boolean warped) {
        super(SHAPE, false, ModSounds.type("fungus"));
        this.warped = warped;
    }

    private boolean growsOn(World world, BlockPos pos) {
        IBlockState soil = world.getBlockState(pos.down());
        return soil.getBlock() instanceof BlockNylium && ((BlockNylium) soil.getBlock()).isWarped() == this.warped;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return this.growsOn(world, pos);
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return rand.nextFloat() < 0.4F;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        world.setBlockToAir(pos);
        if (!WorldGenHugeFungus.generate(world, rand, pos, this.warped)) {
            world.setBlockState(pos, state, 3);
        }
    }
}
