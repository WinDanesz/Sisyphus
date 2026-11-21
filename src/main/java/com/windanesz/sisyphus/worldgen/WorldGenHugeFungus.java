package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.block.BlockNetherPlant;
import com.windanesz.sisyphus.block.BlockNetherVines;
import com.windanesz.sisyphus.block.BlockNylium;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public final class WorldGenHugeFungus {

    private WorldGenHugeFungus() {
    }

    /**
     * False, changing nothing, when there is no room.
     */
    public static boolean generate(World world, Random rand, BlockPos pos, boolean warped) {
        Block stem = ModBlocks.getOrNull(warped ? "warped_stem" : "crimson_stem");
        Block wart = warped ? ModBlocks.getOrNull("warped_wart_block") : Blocks.NETHER_WART_BLOCK;
        if (stem == null || wart == null) {
            return false;
        }
        int height = 4 + rand.nextInt(10);
        if (pos.getY() + height + 3 >= world.getHeight()) {
            return false;
        }
        for (int y = 0; y <= height + 1; y++) {
            if (!replaceable(world, pos.up(y))) {
                return false;
            }
        }

        Block shroomlight = ModBlocks.get("shroomlight");
        boolean light = shroomlight != Blocks.AIR;

        for (int y = 0; y < height; y++) {
            world.setBlockState(pos.up(y), stem.getDefaultState(), 2);
        }
        // the cap: two wide layers around the top of the stem, a smaller one above and a small cover
        int[][] layers = {{height - 2, 3}, {height - 1, 3}, {height, 2}, {height + 1, 1}};
        for (int[] layer : layers) {
            int y = layer[0];
            int radius = layer[1];
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int distance = dx * dx + dz * dz;
                    if (distance > radius * radius + 1 || (dx == 0 && dz == 0 && y < height)) {
                        continue;
                    }
                    BlockPos at = pos.add(dx, y, dz);
                    if (!replaceable(world, at)) {
                        continue;
                    }
                    boolean edge = distance >= radius * radius - 1;
                    boolean bottom = y == height - 2;
                    IBlockState state = light && bottom && edge && rand.nextInt(4) == 0 ? shroomlight.getDefaultState() : wart.getDefaultState();
                    world.setBlockState(at, state, 2);
                    if (!warped && bottom && distance >= 5 && rand.nextInt(3) == 0) {
                        vine(world, rand, at.down(), 1 + rand.nextInt(4), false);
                    }
                }
            }
        }
        if (warped) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos at = pos.add(dx, 0, dz);
                    if ((dx != 0 || dz != 0) && dx * dx + dz * dz <= 10 && rand.nextInt(10) == 0
                            && world.getBlockState(at.down()).getBlock() instanceof BlockNylium) {
                        vine(world, rand, at, 1 + rand.nextInt(3), true);
                    }
                }
            }
        }
        return true;
    }

    private static void vine(World world, Random rand, BlockPos start, int length, boolean twisting) {
        Block vines = ModBlocks.getOrNull(twisting ? "twisting_vines" : "weeping_vines");
        if (vines == null) {
            return;
        }
        BlockPos at = start;
        // Each block needs something to be held up by: the cap or the nylium, or the vine before it
        for (int i = 0; i < length && vines.canPlaceBlockAt(world, at); i++) {
            world.setBlockState(at, vines.getDefaultState().withProperty(BlockNetherVines.AGE, i), 2);
            at = twisting ? at.up() : at.down();
        }
    }

    private static boolean replaceable(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return world.isAirBlock(pos) || state.getBlock() instanceof BlockNetherPlant || state.getBlock() instanceof BlockNetherVines
                || state.getBlock().isReplaceable(world, pos);
    }
}
