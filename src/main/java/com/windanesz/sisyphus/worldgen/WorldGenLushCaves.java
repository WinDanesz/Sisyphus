package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.block.*;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.*;

/**
 * Lush caves are zones here rather than a biome: areas roughly a hundred blocks across, covering about one part in fourteen of the world.
 */
public class WorldGenLushCaves implements IWorldGenerator {

    private static final double FIRST_SCALE = 192.0D;
    private static final double SECOND_SCALE = 64.0D;

    private static Quantiles zoneQuantiles;
    private static Block deepslate;
    private static Block rootedDirt;

    private static Settings.LushCaves settings() {
        return Settings.worldGeneration.lushCaves;
    }

    private static boolean chance(Random random, double percent) {
        return random.nextDouble() * 100.0D < percent;
    }

    private static double cell(long seed, int cx, int cz) {
        Random random = new Random(seed ^ cx * 341873128712L ^ cz * 132897987541L);
        random.nextDouble();
        return random.nextDouble();
    }

    private static double smooth(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    private static double noise(long seed, int x, int z, double scale) {
        double gx = x / scale;
        double gz = z / scale;
        int x0 = (int) Math.floor(gx);
        int z0 = (int) Math.floor(gz);
        double tx = smooth(gx - x0);
        double tz = smooth(gz - z0);
        double top = cell(seed, x0, z0) * (1 - tx) + cell(seed, x0 + 1, z0) * tx;
        double bottom = cell(seed, x0, z0 + 1) * (1 - tx) + cell(seed, x0 + 1, z0 + 1) * tx;
        return top * (1 - tz) + bottom * tz;
    }

    /**
     * Between 0 and 1.
     */
    public static double strength(long seed, int x, int z) {
        return strength(seed, x, z, settings().zoneSize);
    }

    private static double strength(long seed, int x, int z, double size) {
        return 0.75D * noise(seed, x, z, FIRST_SCALE * size) + 0.25D * noise(seed ^ 0x5DEECE66DL, x, z, SECOND_SCALE * size);
    }

    /**
     * Sampled once, with its own seed. About 0.75 for the default coverage of 7.5%.
     */
    public static double threshold() {
        if (zoneQuantiles == null) {
            double[] samples = new double[400 * 400];
            int i = 0;
            for (int a = 0; a < 400; a++) {
                for (int b = 0; b < 400; b++) {
                    samples[i++] = strength(1L, a * 53 - 10000, b * 47 - 10000, 1.0D);
                }
            }
            zoneQuantiles = new Quantiles(samples);
        }
        return zoneQuantiles.top(settings().zoneCoverage / 100.0D);
    }

    public static boolean isLush(long seed, int x, int z) {
        return strength(seed, x, z) >= threshold();
    }

    static Block ground() {
        if (Settings.buildingBlocks.enableMoss && ModBlocks.moss_block != null) {
            return ModBlocks.moss_block;
        }
        return block("rooted_dirt");
    }

    private static Block block(String name) {
        Block block = ModBlocks.get(name);
        return block == Blocks.AIR ? null : block;
    }

    private static boolean isGround(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.GRAVEL || block == Blocks.SAND
                || block == Blocks.CLAY) {
            return true;
        }
        return block != Blocks.AIR && (block == deepslate || block == rootedDirt || block == ModBlocks.moss_block);
    }

    private static boolean isWater(IBlockState state) {
        return state.getMaterial() == Material.WATER;
    }

    private static int top(World world, int x, int z) {
        return Math.min(settings().maxHeight, world.getHeight(x, z) - 5);
    }

    private static int bottom() {
        return Math.max(1, settings().minHeight);
    }

    private static void poolsInColumn(World world, Random random, int x, int z) {
        double pools = settings().clayPools;
        for (int y = top(world, x, z); y >= bottom(); y--) {
            BlockPos floor = new BlockPos(x, y, z);
            if (chance(random, pools) && isGround(world.getBlockState(floor)) && world.isAirBlock(floor.up())
                    && world.isAirBlock(floor.up(2))) {
                clayPool(world, random, floor);
            }
        }
    }

    private static void decorateColumn(World world, Random random, int x, int z) {
        for (int y = top(world, x, z); y >= bottom(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(pos);
            if (!isGround(state)) {
                continue;
            }
            if (world.isAirBlock(pos.down())) {
                decorateCeiling(world, random, pos, state);
            }
            if (world.isAirBlock(pos.up()) && state.getBlock() != Blocks.CLAY) {
                decorateFloor(world, random, pos);
            }
        }
    }

    private static void decorateFloor(World world, Random random, BlockPos floor) {
        if (chance(random, settings().floorMoss)) {
            world.setBlockState(floor, ground().getDefaultState(), 2);
            plant(world, random, floor);
        }
        if (chance(random, settings().glowLichen)) {
            // glow lichen on the wall of the cave
            EnumFacing side = EnumFacing.byHorizontalIndex(random.nextInt(4));
            BlockPos air = floor.up(1 + random.nextInt(3));
            if (world.isAirBlock(air) && isGround(world.getBlockState(air.offset(side)))) {
                BlockGlowLichen.addFace(world, air, side);
            }
        }
    }

    private static void plant(World world, Random random, BlockPos floor) {
        BlockPos top = floor.up();
        if (!world.isAirBlock(top)) {
            return;
        }
        if (!chance(random, settings().floorPlants)) {
            return;
        }
        // out of 80: 21 moss carpet, 42 grass, 8 tall grass, 6 azaleas and 3 flowering azaleas
        double roll = random.nextDouble() * 80.0D;
        if (roll < 21.0D) {
            if (Settings.buildingBlocks.enableMoss && ModBlocks.moss_carpet != null) {
                world.setBlockState(top, ModBlocks.moss_carpet.getDefaultState(), 2);
            }
        } else if (roll < 63.0D) {
            world.setBlockState(top, Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS), 2);
        } else if (roll < 71.0D) {
            if (world.isAirBlock(top.up())) {
                Blocks.DOUBLE_PLANT.placeAt(world, top, BlockDoublePlant.EnumPlantType.GRASS, 2);
            }
        } else {
            Block azalea = block(roll < 77.0D ? "azalea" : "flowering_azalea");
            if (azalea != null) {
                world.setBlockState(top, azalea.getDefaultState(), 2);
            }
        }
    }

    /**
     * Water only goes where it has rock or clay on every side, so none of it runs off.
     */
    private static void clayPool(World world, Random random, BlockPos center) {
        // at most seven blocks, so that the pool and the blocks next to it are in the chunks that are there
        int largest = Math.max(2, Math.min(7, settings().largestClayPool));
        int radius = Math.max(2, largest - 2) + random.nextInt(largest - Math.max(2, largest - 2) + 1);
        List<BlockPos> floors = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // an uneven edge
                if (dx * dx + dz * dz > radius * radius - random.nextInt(radius + 1)) {
                    continue;
                }
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos at = center.add(dx, dy, dz);
                    if (isGround(world.getBlockState(at)) && world.isAirBlock(at.up())) {
                        floors.add(at);
                        break;
                    }
                }
            }
        }
        Set<BlockPos> pool = new HashSet<>(floors);
        for (BlockPos floor : floors) {
            world.setBlockState(floor, Blocks.CLAY.getDefaultState(), 2);
            if (isGround(world.getBlockState(floor.down()))) {
                world.setBlockState(floor.down(), Blocks.CLAY.getDefaultState(), 2);
            }
        }
        // The water: a cell of the pool that is closed in on every side by rock or by the pool, and has ground under it. Where a
        // cell is not, it stays clay, which closes in the cells next to it.
        List<BlockPos> water = new ArrayList<>();
        for (BlockPos floor : floors) {
            if (holdsWater(world, pool, floor)) {
                water.add(floor);
            }
        }
        for (BlockPos at : water) {
            world.setBlockState(at, Blocks.WATER.getDefaultState(), 2);
        }
        for (BlockPos floor : floors) {
            boolean wet = isWater(world.getBlockState(floor));
            BlockPos soil = wet ? floor.down() : floor;
            double roll = random.nextDouble();
            if (roll < (wet ? 0.14D : 0.16D)) {
                placeSmallDripleaf(world, random, soil);
            } else if (roll < (wet ? 0.21D : 0.22D)) {
                placeBigDripleaf(world, random, soil);
            }
        }
    }

    private static boolean holdsWater(World world, Set<BlockPos> pool, BlockPos at) {
        if (!world.getBlockState(at.down()).getMaterial().isSolid() || !world.isAirBlock(at.up())) {
            return false;
        }
        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            BlockPos next = at.offset(side);
            if (!pool.contains(next) && !world.getBlockState(next).isFullCube()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRoom(World world, BlockPos pos) {
        return world.isAirBlock(pos) || isWater(world.getBlockState(pos));
    }

    private static void placeSmallDripleaf(World world, Random random, BlockPos floor) {
        Block small = block("small_dripleaf");
        BlockPos top = floor.up();
        if (small == null || !BlockSmallDripleaf.canGrowOn(world.getBlockState(floor)) || !isRoom(world, top) || !world.isAirBlock(top.up())) {
            return;
        }
        IBlockState state = small.getDefaultState().withProperty(BlockSmallDripleaf.FACING, EnumFacing.byHorizontalIndex(random.nextInt(4)));
        world.setBlockState(top, state.withProperty(BlockSmallDripleaf.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 2);
        world.setBlockState(top.up(), state.withProperty(BlockSmallDripleaf.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 2);
    }

    private static void placeBigDripleaf(World world, Random random, BlockPos floor) {
        Block stem = block("big_dripleaf_stem");
        Block leaf = block("big_dripleaf");
        BlockPos top = floor.up();
        if (stem == null || leaf == null || !BlockBigDripleaf.isSoil(world.getBlockState(floor)) || !isRoom(world, top)) {
            return;
        }
        boolean wet = isWater(world.getBlockState(top));
        EnumFacing facing = EnumFacing.byHorizontalIndex(random.nextInt(4));
        // in water, the leaf is above the water
        int height = (wet ? 2 : 1) + random.nextInt(3);
        for (int i = 1; i < height; i++) {
            if (!world.isAirBlock(top.up(i))) {
                height = i;
                break;
            }
        }
        if (wet && height < 2) {
            return;
        }
        for (int i = 0; i < height - 1; i++) {
            world.setBlockState(top.up(i), stem.getDefaultState().withProperty(BlockBigDripleafStem.FACING, facing), 2);
        }
        world.setBlockState(top.up(height - 1), leaf.getDefaultState().withProperty(BlockBigDripleaf.FACING, facing), 2);
    }

    private static void decorateCeiling(World world, Random random, BlockPos ceiling, IBlockState state) {
        Settings.LushCaves settings = settings();
        if (Settings.buildingBlocks.enableMoss && ModBlocks.moss_block != null && state.getBlock() != Blocks.CLAY && chance(random, settings.roofMoss)) {
            world.setBlockState(ceiling, ModBlocks.moss_block.getDefaultState(), 2);
        }
        BlockPos under = ceiling.down();
        // how much air there is under the roof, down to the floor or to the water
        int room = 0;
        while (room < settings.longestCaveVine + 2 && world.isAirBlock(under.down(room))) {
            room++;
        }
        // one of them at most under each block of the roof
        double roll = random.nextDouble() * 100.0D;
        if (roll < settings.caveVines) {
            // the vines stop a block or two above the floor
            hangVine(world, random, under, room - 2);
        } else if (roll < settings.caveVines + settings.sporeBlossoms) {
            Block spore = block("spore_blossom");
            if (spore != null) {
                world.setBlockState(under, spore.getDefaultState(), 2);
            }
        } else if (roll < settings.caveVines + settings.sporeBlossoms + settings.glowLichen) {
            BlockGlowLichen.addFace(world, under, EnumFacing.UP);
        }
    }

    private static void hangVine(World world, Random random, BlockPos start, int maxLength) {
        Block vines = block("cave_vines");
        Block plant = block("cave_vines_plant");
        if (vines == null || plant == null || maxLength < 1) {
            return;
        }
        Settings.LushCaves settings = settings();
        int kind = random.nextInt(15);
        int length = kind < 2 ? 1 + random.nextInt(Math.max(1, settings.longestCaveVine)) : kind < 5 ? 1 + random.nextInt(3) : 1 + random.nextInt(7);
        length = Math.min(length, Math.min(maxLength, settings.longestCaveVine));
        for (int i = 0; i < length; i++) {
            Block block = i == length - 1 ? vines : plant;
            world.setBlockState(start.down(i), block.getDefaultState().withProperty(BlockCaveVines.BERRIES, chance(random, settings.glowBerries)), 2);
        }
    }

    private static void rootedAzaleaTree(World world, Random random, int x, int z) {
        Block rooted = block("rooted_dirt");
        Block hanging = block("hanging_roots");
        if (rooted == null || hanging == null) {
            return;
        }
        int top = Math.min(settings().maxHeight, world.getHeight(x, z) - 6);
        for (int y = top; y > bottom() + 4; y--) {
            BlockPos air = new BlockPos(x, y, z);
            BlockPos ceiling = air.up();
            if (!world.isAirBlock(air) || !isGround(world.getBlockState(ceiling))) {
                continue;
            }
            // the floor under it, in the same cave
            BlockPos floor = null;
            for (int down = 1; down <= 14; down++) {
                BlockPos at = air.down(down);
                if (!world.isAirBlock(at)) {
                    floor = isGround(world.getBlockState(at)) ? at : null;
                    break;
                }
            }
            if (floor == null || air.getY() - floor.getY() < 5) {
                continue;
            }
            if (!WorldGenAzaleaTree.generate(world, random, floor.up(), air.getY() - floor.getY() - 2)) {
                return;
            }
            roots(world, random, ceiling, rooted, hanging);
            return;
        }
    }

    private static void roots(World world, Random random, BlockPos ceiling, Block rooted, Block hanging) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx * dx + dz * dz > 9) {
                    continue;
                }
                if (dx != 0 || dz != 0) {
                    if (random.nextFloat() < 0.45F) {
                        continue;
                    }
                }
                // the column of rooted dirt goes up from the roof, for as long as there is rock for it to take the place of
                for (int up = 0; up < 100; up++) {
                    BlockPos at = ceiling.add(dx, up, dz);
                    if (at.getY() > 250 || !isGround(world.getBlockState(at))) {
                        break;
                    }
                    world.setBlockState(at, rooted.getDefaultState(), 2);
                }
                BlockPos under = ceiling.add(dx, -1, dz);
                if (world.isAirBlock(under) && random.nextFloat() < 0.55F && world.getBlockState(under.up()).getBlock() == rooted) {
                    world.setBlockState(under, hanging.getDefaultState(), 2);
                    if (random.nextBoolean() && world.isAirBlock(under.down())) {
                        world.setBlockState(under.down(), hanging.getDefaultState(), 2);
                    }
                }
            }
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator generator, IChunkProvider provider) {
        if (!Settings.allowsDimension(Settings.worldGeneration.lushCaves.dimensions, world) || !Settings.worldGeneration.lushCaves.enabled || ground() == null) {
            return;
        }
        long seed = world.getSeed();
        int x0 = chunkX * 16 + 8;
        int z0 = chunkZ * 16 + 8;
        // The zones are big, so if none of the corners, the middles of the sides and the middle is in one, the chunk is not
        boolean inZone = false;
        for (int i = 0; i < 9 && !inZone; i++) {
            inZone = isLush(seed, x0 + (i % 3) * 8 - (i % 3 == 2 ? 1 : 0), z0 + (i / 3) * 8 - (i / 3 == 2 ? 1 : 0));
        }
        if (!inZone) {
            return;
        }
        boolean[] lush = new boolean[256];
        for (int i = 0; i < 256; i++) {
            lush[i] = isLush(seed, x0 + (i & 15), z0 + (i >> 4));
        }
        deepslate = block("deepslate");
        rootedDirt = block("rooted_dirt");
        // The pools come first, and the moss does not cover their clay, as in the game
        for (int i = 0; i < 256; i++) {
            if (lush[i]) {
                poolsInColumn(world, random, x0 + (i & 15), z0 + (i >> 4));
            }
        }
        for (int i = 0; i < 256; i++) {
            if (lush[i]) {
                decorateColumn(world, random, x0 + (i & 15), z0 + (i >> 4));
            }
        }
        double trees = settings().rootedAzaleaTrees;
        for (int i = (int) trees + (random.nextDouble() < trees - (int) trees ? 1 : 0); i > 0; i--) {
            int x = x0 + 3 + random.nextInt(10);
            int z = z0 + 3 + random.nextInt(10);
            if (isLush(seed, x, z)) {
                rootedAzaleaTree(world, random, x, z);
            }
        }
    }
}
