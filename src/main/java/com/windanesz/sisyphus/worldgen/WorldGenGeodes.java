package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.block.BlockAmethystCluster;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenGeodes implements IWorldGenerator {

    private static final int RADIUS = 7;

    public static boolean generate(World world, Random random, BlockPos center) {
        Block amethyst = ModBlocks.get("amethyst_block");
        Block budding = ModBlocks.get("budding_amethyst");
        Block calcite = ModBlocks.get("calcite");
        Block basalt = ModBlocks.get("smooth_basalt");
        if (amethyst == Blocks.AIR || budding == Blocks.AIR || calcite == Blocks.AIR || basalt == Blocks.AIR) {
            return false;
        }
        float scale = 0.9F + random.nextFloat() * 0.25F;
        // The outside has to be rock: with more than a few open places around it, it would be a hole in a cave
        int open = 0;
        for (int i = 0; i < 48; i++) {
            double x = random.nextGaussian();
            double y = random.nextGaussian();
            double z = random.nextGaussian();
            double length = Math.sqrt(x * x + y * y + z * z);
            if (length == 0.0D) {
                continue;
            }
            double reach = 6.2D * scale / length;
            BlockPos at = center.add(Math.round(x * reach), Math.round(y * reach), Math.round(z * reach));
            if (!world.getBlockState(at).getMaterial().isSolid()) {
                open++;
            }
        }
        if (open > 4) {
            return false;
        }
        List<BlockPos> lining = new ArrayList<>();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz) + random.nextDouble() * 0.5D - 0.25D;
                    BlockPos at = center.add(dx, dy, dz);
                    if (distance < 2.9D * scale) {
                        world.setBlockState(at, Blocks.AIR.getDefaultState(), 2);
                    } else if (distance < 4.0D * scale) {
                        lining.add(at);
                        world.setBlockState(at, (random.nextFloat() < 0.083F ? budding : amethyst).getDefaultState(), 2);
                    } else if (distance < 5.0D * scale) {
                        world.setBlockState(at, calcite.getDefaultState(), 2);
                    } else if (distance < 6.0D * scale) {
                        world.setBlockState(at, basalt.getDefaultState(), 2);
                    }
                }
            }
        }
        String[] growth = {"small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud", "amethyst_cluster"};
        for (BlockPos wall : lining) {
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos at = wall.offset(side);
                if (world.isAirBlock(at) && random.nextFloat() < 0.35F) {
                    Block bud = ModBlocks.get(growth[random.nextInt(growth.length)]);
                    IBlockState state = bud.getDefaultState().withProperty(BlockAmethystCluster.FACING, side);
                    world.setBlockState(at, state, 2);
                }
            }
        }
        return true;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator generator, IChunkProvider provider) {
        if (!Settings.allowsDimension(Settings.worldGeneration.geodes.dimensions, world) || !Settings.buildingBlocks.enableAmethyst || !Settings.worldGeneration.geodes.enabled
                || random.nextInt(Settings.worldGeneration.geodes.rarity) != 0) {
            return;
        }
        // The heights that the center of a geode is at, both included
        int minY = Math.min(Settings.worldGeneration.geodes.minHeight, Settings.worldGeneration.geodes.maxHeight);
        int maxY = Math.max(Settings.worldGeneration.geodes.minHeight, Settings.worldGeneration.geodes.maxHeight);
        BlockPos center = new BlockPos(chunkX * 16 + 8 + random.nextInt(16), minY + random.nextInt(maxY - minY + 1), chunkZ * 16 + 8 + random.nextInt(16));
        generate(world, random, center);
    }
}
