package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.block.BlockNetherVines;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGenNetherFungi implements IWorldGenerator {

    private static BlockPos nyliumIn(World world, BlockPos column, Block nylium) {
        for (int dy = 3; dy >= -3; dy--) {
            BlockPos at = column.up(dy);
            if (world.getBlockState(at).getBlock() == nylium && world.isAirBlock(at.up())) {
                return at;
            }
        }
        return null;
    }

    private static void growVine(World world, Random random, BlockPos start, Block vines, boolean up) {
        int length = 1 + random.nextInt(5);
        BlockPos at = start;
        for (int i = 0; i < length && vines.canPlaceBlockAt(world, at); i++) {
            world.setBlockState(at, vines.getDefaultState().withProperty(BlockNetherVines.AGE, i), 2);
            at = up ? at.up() : at.down();
        }
    }

    private static BlockPos findFloor(World world, Random random, int x, int z) {
        int start = 40 + random.nextInt(60);
        for (int y = start; y > 33; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() != Blocks.NETHERRACK || !world.isAirBlock(pos.up())) {
                continue;
            }
            boolean room = true;
            for (int up = 1; up <= 16 && room; up++) {
                room = world.isAirBlock(pos.up(up));
            }
            if (room) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator generator, IChunkProvider provider) {
        if (!Settings.allowsDimension(Settings.worldGeneration.nether.dimensions, world) || !Settings.plants.enableNetherFungi || !Settings.worldGeneration.nether.enableFungiPatches
                || random.nextInt(Settings.worldGeneration.nether.fungiPatchRarity) != 0) {
            return;
        }
        boolean warped = random.nextBoolean();
        // Inside the 16 by 16 blocks that are known to be there, which start 8 blocks into the chunk
        int x = chunkX * 16 + 8 + 4 + random.nextInt(8);
        int z = chunkZ * 16 + 8 + 4 + random.nextInt(8);
        BlockPos floor = findFloor(world, random, x, z);
        if (floor == null) {
            return;
        }
        Block nylium = ModBlocks.getOrNull(warped ? "warped_nylium" : "crimson_nylium");
        if (nylium == null) {
            return;
        }
        String[] plants = warped ? new String[]{"warped_roots", "warped_roots", "nether_sprouts", "nether_sprouts", "warped_fungus"}
                : new String[]{"crimson_roots", "crimson_roots", "crimson_roots", "crimson_fungus"};

        // Nylium over the netherrack around the middle, with plants on it
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (dx * dx + dz * dz > 14 + random.nextInt(5)) {
                    continue;
                }
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos at = floor.add(dx, dy, dz);
                    if (world.getBlockState(at).getBlock() != Blocks.NETHERRACK || !world.isAirBlock(at.up())) {
                        continue;
                    }
                    world.setBlockState(at, nylium.getDefaultState(), 2);
                    if (random.nextInt(3) == 0) {
                        Block plant = ModBlocks.getOrNull(plants[random.nextInt(plants.length)]);
                        if (plant != null) {
                            world.setBlockState(at.up(), plant.getDefaultState(), 2);
                        }
                    }
                    break;
                }
            }
        }

        // Vines hang from the ceiling above, twisting vines grow up from the warped nylium
        Block vines = ModBlocks.getOrNull(warped ? "twisting_vines" : "weeping_vines");
        if (vines != null) {
            for (int i = 0; i < 10; i++) {
                BlockPos at = floor.add(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
                if (warped) {
                    // The nylium of a column is not always at the height of the middle, so it is looked for
                    BlockPos ground = nyliumIn(world, at, nylium);
                    if (ground != null) {
                        growVine(world, random, ground.up(), vines, true);
                    }
                } else {
                    BlockPos ceiling = at.up(4);
                    for (int up = 0; up < 12 && world.isAirBlock(ceiling); up++) {
                        ceiling = ceiling.up();
                    }
                    if (!world.isAirBlock(ceiling)) {
                        growVine(world, random, ceiling.down(), vines, false);
                    }
                }
            }
        }

        // The huge fungi
        int fungi = 1 + (random.nextInt(3) == 0 ? 1 : 0);
        for (int i = 0; i < fungi; i++) {
            BlockPos base = floor.add(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos at = base.up(dy);
                if (world.getBlockState(at).getBlock() == nylium && WorldGenHugeFungus.generate(world, random, at.up(), warped)) {
                    break;
                }
            }
        }
    }
}
