package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public final class WorldGenAzaleaTree {

    private WorldGenAzaleaTree() {
    }

    private static boolean replaceable(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return world.isAirBlock(pos) || state.getBlock() instanceof BlockLeaves || state.getBlock().isReplaceable(world, pos)
                || state.getBlock() == ModBlocks.get("azalea") || state.getBlock() == ModBlocks.get("flowering_azalea");
    }

    /**
     * False, leaving the azalea, if there is no room.
     */
    public static boolean generate(World world, Random rand, BlockPos pos) {
        return generate(world, rand, pos, 6);
    }

    /**
     * The height limit makes a tree fit in a cave.
     */
    public static boolean generate(World world, Random rand, BlockPos pos, int maxHeight) {
        Block leaves = ModBlocks.get("azalea_leaves");
        Block flowering = ModBlocks.get("flowering_azalea_leaves");
        if (leaves == Blocks.AIR || flowering == Blocks.AIR) {
            return false;
        }
        int height = Math.min(4 + rand.nextInt(3), maxHeight);
        if (height < 3) {
            return false;
        }
        for (int y = 0; y <= height + 1; y++) {
            if (!replaceable(world, pos.up(y))) {
                return false;
            }
        }
        Block roots = ModBlocks.get("rooted_dirt");
        if (roots != Blocks.AIR && world.getBlockState(pos.down()).getBlock() != Blocks.AIR) {
            world.setBlockState(pos.down(), roots.getDefaultState(), 2);
        }
        BlockPos top = pos.up(height);
        for (int attempt = 0; attempt < 50; attempt++) {
            BlockPos at = top.add(rand.nextInt(7) - 3, rand.nextInt(4) - 2, rand.nextInt(7) - 3);
            if (at.distanceSq(top.getX(), at.getY(), top.getZ()) <= 9.0D && replaceable(world, at)) {
                world.setBlockState(at, (rand.nextInt(4) == 0 ? flowering : leaves).getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false), 2);
            }
        }
        for (int y = 0; y < height; y++) {
            world.setBlockState(pos.up(y), Blocks.LOG.getDefaultState(), 2);
        }
        world.setBlockState(top, (rand.nextInt(4) == 0 ? flowering : leaves).getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false), 2);
        return true;
    }
}
