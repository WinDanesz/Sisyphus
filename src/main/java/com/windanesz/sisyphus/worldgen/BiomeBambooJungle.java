package com.windanesz.sisyphus.worldgen;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeJungle;

import java.util.Random;

public class BiomeBambooJungle extends BiomeJungle {

    private static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);

    public BiomeBambooJungle() {
        super(false, new BiomeProperties("Bamboo Jungle").setBaseHeight(0.1F).setHeightVariation(0.2F).setTemperature(0.95F).setRainfall(0.9F));
        // Far fewer trees than a jungle
        this.decorator.treesPerChunk = 6;
        this.decorator.grassPerChunk = 25;
        this.decorator.flowersPerChunk = 4;
    }

    @Override
    public void decorate(World world, Random rand, BlockPos pos) {
        // Podzol first, so that the bamboo can stand on it
        for (int i = 0; i < 3; i++) {
            BlockPos center = world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8)).down();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx * dx + dz * dz > 10 + rand.nextInt(3)) {
                        continue;
                    }
                    BlockPos target = world.getHeight(center.add(dx, 0, dz)).down();
                    if (world.getBlockState(target).getBlock() == Blocks.GRASS || world.getBlockState(target).getBlock() == Blocks.DIRT) {
                        world.setBlockState(target, PODZOL, 2);
                    }
                }
            }
        }

        super.decorate(world, rand, pos);

        // A lot of bamboo
        int thickets = 6 + rand.nextInt(6);
        for (int i = 0; i < thickets; i++) {
            WorldGenWildPlants.generateThicket(world, rand, world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8)), 8 + rand.nextInt(10), 4);
        }
    }
}
