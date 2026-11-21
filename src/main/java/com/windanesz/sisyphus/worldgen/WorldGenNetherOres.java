package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGenNetherOres implements IWorldGenerator {

    private static void vein(World world, Random random, int chunkX, int chunkZ, WorldGenMinable generator, int minY, int range) {
        BlockPos pos = new BlockPos(chunkX * 16 + random.nextInt(16), minY + random.nextInt(range), chunkZ * 16 + random.nextInt(16));
        generator.generate(world, random, pos);
    }

    private static void hiddenVein(World world, Random random, int chunkX, int chunkZ, IBlockState state, int size, int y) {
        BlockPos pos = new BlockPos(chunkX * 16 + 8 + random.nextInt(16), y, chunkZ * 16 + 8 + random.nextInt(16));
        for (int i = 0; i < size; i++) {
            if (world.getBlockState(pos).getBlock() == Blocks.NETHERRACK && !touchesAir(world, pos)) {
                world.setBlockState(pos, state, 2);
            }
            pos = pos.offset(EnumFacing.values()[random.nextInt(6)]);
        }
    }

    private static boolean touchesAir(World world, BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            if (world.isAirBlock(pos.offset(side))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator generator, IChunkProvider provider) {
        if (!Settings.allowsDimension(Settings.worldGeneration.nether.dimensions, world)) {
            return;
        }
        Settings.NetherGeneration nether = Settings.worldGeneration.nether;
        if (Settings.buildingBlocks.enableNetherGold && nether.enableNetherGold) {
            WorldGenMinable ore = new WorldGenMinable(ModBlocks.get("nether_gold_ore").getDefaultState(), nether.netherGoldVeinSize, BlockMatcher.forBlock(Blocks.NETHERRACK));
            for (int i = 0; i < nether.netherGoldVeins; i++) {
                vein(world, random, chunkX, chunkZ, ore, 10, 107);
            }
        }
        if (Settings.buildingBlocks.enableBlackstone && nether.enableBlackstone) {
            WorldGenMinable blackstone = new WorldGenMinable(ModBlocks.get("blackstone").getDefaultState(), nether.blackstonePatchSize, BlockMatcher.forBlock(Blocks.NETHERRACK));
            for (int i = 0; i < nether.blackstonePatches; i++) {
                vein(world, random, chunkX, chunkZ, blackstone, 5, 115);
            }
        }
        if (Settings.buildingBlocks.enableBasalt && nether.enableBasalt) {
            WorldGenMinable basalt = new WorldGenMinable(ModBlocks.get("basalt").getDefaultState(), nether.basaltPatchSize, BlockMatcher.forBlock(Blocks.NETHERRACK));
            for (int i = 0; i < nether.basaltPatches; i++) {
                vein(world, random, chunkX, chunkZ, basalt, 5, 115);
            }
        }
        if (Settings.items.enableNetherite && nether.enableAncientDebris) {
            IBlockState debris = ModBlocks.get("ancient_debris").getDefaultState();
            // A larger one low down, in the 8 to 24 band, and a smaller one anywhere from 8 up. Neither is ever exposed to air.
            for (int i = 0; i < nether.ancientDebrisTries; i++) {
                hiddenVein(world, random, chunkX, chunkZ, debris, 3, 8 + random.nextInt(9) + random.nextInt(9));
                hiddenVein(world, random, chunkX, chunkZ, debris, 2, 8 + random.nextInt(112));
            }
        }
    }
}
