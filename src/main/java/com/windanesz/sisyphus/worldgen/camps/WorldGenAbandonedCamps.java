package com.windanesz.sisyphus.worldgen.camps;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.worldgen.SiteProgress;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

import javax.annotation.Nullable;
import java.util.*;

/**
 * From Wilderlands.
 */
public class WorldGenAbandonedCamps implements IWorldGenerator {

    public static final String LOCATE = "AbandonedCamp";
    public static final ResourceLocation LOOT = new ResourceLocation(SisyphusBackport.MODID, "chests/abandoned_camp");
    private static final String PROGRESS = "sisyphus_camps";
    private static final int REACH = AbandonedCamp.RADIUS + 5;
    private static final int MAX_TRUNKS = 6;
    private static final int LOCATE_RANGE = 16;
    private static final int TRIES = 24;

    private static final Map<World, PlanCache> PLANS = new WeakHashMap<>();

    private static boolean activeIn(World world) {
        Settings.AbandonedCamps settings = Settings.worldGeneration.abandonedCamps;
        return settings.enabled && Settings.allowsDimension(settings.dimensions, world) && world.getWorldInfo().isMapFeaturesEnabled()
                && world.getWorldType() != WorldType.FLAT;
    }

    @Nullable
    private static Camp plan(World world, int squareX, int squareZ) {
        Settings.AbandonedCamps settings = Settings.worldGeneration.abandonedCamps;
        BlockPos spawn = world.getSpawnPoint();
        int signature = Arrays.hashCode(new int[]{settings.spacing, settings.chance, settings.minSpawnDistance, spawn.getX(), spawn.getZ()});
        long key = (long) squareX << 32 | squareZ & 0xFFFFFFFFL;
        PlanCache cache;
        synchronized (PLANS) {
            cache = PLANS.computeIfAbsent(world, w -> new PlanCache());
        }
        if (cache.signature != signature) {
            cache.clear();
            cache.signature = signature;
        }
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Camp camp = planSquare(world, squareX, squareZ, settings);
        if (camp != null) {
            double dx = camp.x - spawn.getX();
            double dz = camp.z - spawn.getZ();
            if (dx * dx + dz * dz < (double) settings.minSpawnDistance * settings.minSpawnDistance) {
                camp = null;
            }
        }
        cache.put(key, camp);
        return camp;
    }

    // Planning

    @Nullable
    private static Camp planSquare(World world, int squareX, int squareZ, Settings.AbandonedCamps settings) {
        Random random = new Random(world.getSeed() + squareX * 341873128712L + squareZ * 132897987541L + 0x63616d70L);
        if (random.nextInt(100) >= settings.chance) {
            return null;
        }
        int size = settings.spacing * 16;
        // Away from the edges of the square, so that the camps of two squares do not meet
        int inset = REACH + 2;
        int cellX = (squareX * size + inset) >> 2;
        int cellZ = (squareZ * size + inset) >> 2;
        int cells = (size - 2 * inset) >> 2;
        Biome[] grid = world.getBiomeProvider().getBiomesForGeneration(null, cellX, cellZ, cells, cells);
        List<Integer> spots = new ArrayList<>();
        for (int cx = 3; cx < cells - 3; cx += 2) {
            for (int cz = 3; cz < cells - 3; cz += 2) {
                spots.add(cz * cells + cx);
            }
        }
        Collections.shuffle(spots, random);
        // Open plains and savannas first, whose ground is flattest and has the fewest trees, then forests and taigas
        for (boolean open : new boolean[]{true, false}) {
            int tried = 0;
            for (int spot : spots) {
                int cx = spot % cells;
                int cz = spot / cells;
                if (!fits(grid[spot], open)) {
                    continue;
                }
                if (tried++ >= TRIES) {
                    break;
                }
                boolean all = true;
                for (int dx = -3; dx <= 3 && all; dx++) {
                    for (int dz = -3; dz <= 3 && all; dz++) {
                        all = fits(grid[(cz + dz) * cells + cx + dx], open);
                    }
                }
                if (all) {
                    return new Camp((cellX + cx) * 4 + 2, (cellZ + cz) * 4 + 2, random.nextInt(4), random.nextLong());
                }
            }
        }
        return null;
    }

    private static boolean fits(Biome biome, boolean open) {
        if (biome == null) {
            return false;
        }
        for (BiomeDictionary.Type type : new BiomeDictionary.Type[]{BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER, BiomeDictionary.Type.BEACH,
                BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WATER, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.SNOWY,
                BiomeDictionary.Type.SANDY, BiomeDictionary.Type.MESA, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.SPOOKY, BiomeDictionary.Type.DENSE,
                BiomeDictionary.Type.MUSHROOM, BiomeDictionary.Type.WASTELAND, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.END}) {
            if (BiomeDictionary.hasType(biome, type)) {
                return false;
            }
        }
        boolean grassy = BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SAVANNA)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS);
        return grassy && biome.getBaseHeight() <= 0.3F && biome.getHeightVariation() <= (open ? 0.1F : 0.25F);
    }

    private static boolean build(World world, Camp camp) {
        for (int x = camp.chunks[0]; x <= camp.chunks[0] + camp.chunks[2]; x++) {
            for (int z = camp.chunks[1]; z <= camp.chunks[1] + camp.chunks[3]; z++) {
                if (!world.isChunkGeneratedAt(x, z)) {
                    return false;
                }
            }
        }
        int radius = AbandonedCamp.RADIUS;
        int[][] heights = new int[radius * 2 + 1][radius * 2 + 1];
        int low = Integer.MAX_VALUE;
        int high = Integer.MIN_VALUE;
        int trunks = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int height = groundHeight(world, camp.x + dx, camp.z + dz);
                if (height < 0) {
                    return false;
                }
                heights[dx + radius][dz + radius] = height;
                // The slope is measured under the clearing, which is round, not out in the corners
                if (Math.hypot(dx, dz) <= radius - 1) {
                    low = Math.min(low, height);
                    high = Math.max(high, height);
                }
                if (world.getBlockState(new BlockPos(camp.x + dx, height + 1, camp.z + dz)).getMaterial() == Material.WOOD) {
                    trunks++;
                }
            }
        }
        if (low < world.getSeaLevel() - 1 || high - low > Settings.worldGeneration.abandonedCamps.maxRelief || trunks > MAX_TRUNKS) {
            return false;
        }
        clearTrees(world, camp, low, high);
        int base = heights[radius][radius];
        Random random = new Random(camp.seed);
        AbandonedCamp.Plan plan = AbandonedCamp.build(camp.x, camp.z, base, camp.rotation,
                (x, z) -> heights[Math.max(0, Math.min(2 * radius, x - camp.x + radius))][Math.max(0, Math.min(2 * radius, z - camp.z + radius))], LOOT, random);
        for (Map.Entry<BlockPos, IBlockState> entry : plan.blocks.entrySet()) {
            world.setBlockState(entry.getKey(), entry.getValue(), 2);
        }
        for (Map.Entry<BlockPos, ResourceLocation> chest : plan.chests.entrySet()) {
            TileEntity tile = world.getTileEntity(chest.getKey());
            if (tile instanceof TileEntityChest) {
                ((TileEntityChest) tile).setLootTable(chest.getValue(), random.nextLong());
            }
        }
        return true;
    }

    private static int groundHeight(World world, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = world.getChunk(pos).getTopFilledSegment() + 15; y > 0; y--) {
            pos.setY(y);
            Material material = world.getBlockState(pos).getMaterial();
            if (material.isLiquid() || material == Material.ICE || material == Material.PACKED_ICE) {
                return -1;
            }
            if (material == Material.AIR || material == Material.LEAVES || material == Material.WOOD || material == Material.PLANTS || material == Material.VINE
                    || material == Material.SNOW || material == Material.WEB || material == Material.GOURD || material == Material.CACTUS || material.isReplaceable()) {
                continue;
            }
            if (material.blocksMovement()) {
                return y;
            }
        }
        return -1;
    }

    // Building

    private static void clearTrees(World world, Camp camp, int low, int high) {
        int radius = AbandonedCamp.RADIUS;
        Set<BlockPos> logs = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int y = low; y <= high + 2; y++) {
                    BlockPos pos = new BlockPos(camp.x + dx, y, camp.z + dz);
                    if (world.getBlockState(pos).getMaterial() == Material.WOOD && logs.add(pos)) {
                        queue.add(pos);
                    }
                }
            }
        }
        while (!queue.isEmpty() && logs.size() < 2000) {
            BlockPos log = queue.poll();
            for (BlockPos next : BlockPos.getAllInBox(log.add(-1, -1, -1), log.add(1, 1, 1))) {
                if (Math.abs(next.getX() - camp.x) <= REACH && Math.abs(next.getZ() - camp.z) <= REACH && !logs.contains(next)
                        && world.getBlockState(next).getMaterial() == Material.WOOD) {
                    BlockPos found = next.toImmutable();
                    logs.add(found);
                    queue.add(found);
                }
            }
        }
        IBlockState air = Blocks.AIR.getDefaultState();
        Set<BlockPos> cleared = new HashSet<>();
        for (BlockPos log : logs) {
            for (BlockPos near : BlockPos.getAllInBox(log.add(-4, -4, -4), log.add(4, 4, 4))) {
                if (Math.abs(near.getX() - camp.x) > REACH || Math.abs(near.getZ() - camp.z) > REACH || cleared.contains(near)) {
                    continue;
                }
                Material material = world.getBlockState(near).getMaterial();
                if (material == Material.LEAVES || material == Material.VINE) {
                    BlockPos found = near.toImmutable();
                    cleared.add(found);
                    world.setBlockState(found, air, 2);
                }
            }
        }
        for (BlockPos log : logs) {
            world.setBlockState(log, air, 2);
        }
    }

    @Nullable
    public static BlockPos locate(World world, String name, BlockPos pos) {
        if (!LOCATE.equalsIgnoreCase(name) || !activeIn(world)) {
            return null;
        }
        int size = Settings.worldGeneration.abandonedCamps.spacing * 16;
        int centerX = Math.floorDiv(pos.getX(), size);
        int centerZ = Math.floorDiv(pos.getZ(), size);
        for (int ring = 0; ring <= LOCATE_RANGE; ring++) {
            BlockPos nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (int squareX = centerX - ring; squareX <= centerX + ring; squareX++) {
                for (int squareZ = centerZ - ring; squareZ <= centerZ + ring; squareZ++) {
                    if (Math.max(Math.abs(squareX - centerX), Math.abs(squareZ - centerZ)) != ring) {
                        continue;
                    }
                    Camp camp = plan(world, squareX, squareZ);
                    if (camp != null && isBuilt(world, camp)) {
                        BlockPos center = new BlockPos(camp.x, world.getSeaLevel(), camp.z);
                        double distance = center.distanceSq(pos);
                        if (distance < nearestDistance) {
                            nearest = center;
                            nearestDistance = distance;
                        }
                    }
                }
            }
            if (nearest != null) {
                return nearest;
            }
        }
        return null;
    }

    /**
     * A plan is made from the biomes alone and the ground can still turn it down, so missing chunks are generated here.
     */
    private static boolean isBuilt(World world, Camp camp) {
        SiteProgress progress = SiteProgress.get(world, PROGRESS);
        Boolean outcome = progress.outcome(camp.key());
        if (outcome != null) {
            return outcome;
        }
        boolean generated = true;
        for (int x = camp.chunks[0]; x < camp.chunks[0] + camp.chunks[2] && generated; x++) {
            for (int z = camp.chunks[1]; z < camp.chunks[1] + camp.chunks[3] && generated; z++) {
                generated = world.isChunkGeneratedAt(x, z);
            }
        }
        if (generated && !progress.isPending(camp.key())) {
            // Decorated before outcomes were kept: as /locate always answered
            return true;
        }
        // A chunk is decorated once the chunks on its positive sides are there too
        for (int x = camp.chunks[0]; x <= camp.chunks[0] + camp.chunks[2]; x++) {
            for (int z = camp.chunks[1]; z <= camp.chunks[1] + camp.chunks[3]; z++) {
                world.getChunk(x, z);
            }
        }
        return Boolean.TRUE.equals(progress.outcome(camp.key()));
    }

    // Locating

    public static boolean locatable() {
        return Settings.worldGeneration.abandonedCamps.enabled;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!activeIn(world)) {
            return;
        }
        int size = Settings.worldGeneration.abandonedCamps.spacing * 16;
        int margin = REACH + 24;
        for (int squareX = Math.floorDiv(chunkX * 16 - margin, size); squareX <= Math.floorDiv(chunkX * 16 + 15 + margin, size); squareX++) {
            for (int squareZ = Math.floorDiv(chunkZ * 16 - margin, size); squareZ <= Math.floorDiv(chunkZ * 16 + 15 + margin, size); squareZ++) {
                Camp camp = plan(world, squareX, squareZ);
                if (camp == null) {
                    continue;
                }
                int bit = camp.chunkBit(chunkX, chunkZ);
                SiteProgress progress = SiteProgress.get(world, PROGRESS);
                if (bit >= 0 && progress.decorate(camp.key(), bit, camp.fullMask())) {
                    progress.record(camp.key(), build(world, camp));
                }
            }
        }
    }

    static final class Camp {
        final int x;
        final int z;
        final int rotation;
        final long seed;
        final int[] chunks;

        Camp(int x, int z, int rotation, long seed) {
            this.x = x;
            this.z = z;
            this.rotation = rotation;
            this.seed = seed;
            this.chunks = SiteProgress.chunksUnder(x - REACH, z - REACH, x + REACH, z + REACH);
        }

        String key() {
            return x + "," + z;
        }

        int chunkBit(int chunkX, int chunkZ) {
            int dx = chunkX - chunks[0];
            int dz = chunkZ - chunks[1];
            return dx < 0 || dz < 0 || dx >= chunks[2] || dz >= chunks[3] ? -1 : dz * chunks[2] + dx;
        }

        long fullMask() {
            return (1L << chunks[2] * chunks[3]) - 1L;
        }
    }

    /**
     * Null for a square without a camp.
     */
    private static final class PlanCache extends LinkedHashMap<Long, Camp> {
        private static final long serialVersionUID = 1L;
        int signature;

        PlanCache() {
            super(256, 0.75F, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Camp> eldest) {
            return size() > 4096;
        }
    }
}
