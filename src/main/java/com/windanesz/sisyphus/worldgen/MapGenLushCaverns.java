package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MapGenLushCaverns extends MapGenBase {

    private static final double EDGE = 0.05D;
    private static final double EDGE_RISE = 0.9D;

    private final MapGenBase inner;
    private CavernShape shape;
    private long shapeSeed;

    public MapGenLushCaverns(MapGenBase inner) {
        this.inner = inner;
    }

    @SubscribeEvent
    public static void onInitMapGen(InitMapGenEvent event) {
        if (event.getType() == InitMapGenEvent.EventType.CAVE && !(event.getNewGen() instanceof MapGenLushCaverns)) {
            event.setNewGen(new MapGenLushCaverns(event.getNewGen()));
        }
    }

    private static int groundTop(ChunkPrimer primer, int lx, int lz) {
        for (int y = 255; y > 0; y--) {
            IBlockState state = primer.getBlockState(lx, y, lz);
            if (state.getMaterial() != Material.AIR && !state.getMaterial().isLiquid()) {
                return y;
            }
        }
        return 0;
    }

    private static boolean nextToLiquid(ChunkPrimer primer, int lx, int y, int lz) {
        return isLiquid(primer, lx, y + 1, lz) || isLiquid(primer, lx, y - 1, lz) || isLiquid(primer, lx - 1, y, lz)
                || isLiquid(primer, lx + 1, y, lz) || isLiquid(primer, lx, y, lz - 1) || isLiquid(primer, lx, y, lz + 1);
    }

    private static boolean isLiquid(ChunkPrimer primer, int lx, int y, int lz) {
        return lx >= 0 && lx < 16 && lz >= 0 && lz < 16 && y >= 0 && y < 256 && primer.getBlockState(lx, y, lz).getMaterial().isLiquid();
    }

    @Override
    public void generate(World world, int chunkX, int chunkZ, ChunkPrimer primer) {
        this.inner.generate(world, chunkX, chunkZ, primer);
        if (Settings.allowsDimension(Settings.worldGeneration.lushCaves.dimensions, world) && Settings.worldGeneration.lushCaves.enabled && Settings.worldGeneration.lushCaves.enableBigCaves
                && WorldGenLushCaves.ground() != null) {
            this.carve(world.getSeed(), chunkX, chunkZ, primer);
        }
    }

    private void carve(long seed, int chunkX, int chunkZ, ChunkPrimer primer) {
        int x0 = chunkX * 16;
        int z0 = chunkZ * 16;
        // The zones are big, so if none of the corners, the middles of the sides and the middle is in one, the chunk is not
        boolean inZone = false;
        for (int i = 0; i < 9 && !inZone; i++) {
            inZone = WorldGenLushCaves.isLush(seed, x0 + Math.min(15, (i % 3) * 8), z0 + Math.min(15, (i / 3) * 8));
        }
        if (!inZone) {
            return;
        }
        if (this.shape == null || this.shapeSeed != seed) {
            this.shape = new CavernShape(seed);
            this.shapeSeed = seed;
        }
        Settings.LushCaves settings = Settings.worldGeneration.lushCaves;
        double base = CavernShape.baseBar(settings.bigCaveOpenness / 100.0D);
        if (Double.isInfinite(base) && base > 0) {
            return;
        }
        double threshold = WorldGenLushCaves.threshold();
        int bottom = Math.max(1, settings.bigCaveMinHeight);
        IBlockState air = Blocks.AIR.getDefaultState();
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = x0 + lx;
                int z = z0 + lz;
                double strength = WorldGenLushCaves.strength(seed, x, z);
                if (strength < threshold) {
                    continue;
                }
                double rise = (1.0D - Math.min(1.0D, (strength - threshold) / EDGE)) * EDGE_RISE;
                int top = Math.min(Math.min(254, settings.bigCaveMaxHeight), groundTop(primer, lx, lz) - settings.bigCaveRoof);
                for (int y = bottom; y <= top; y++) {
                    if (primer.getBlockState(lx, y, lz).getBlock() == Blocks.STONE
                            && this.shape.density(x, y, z, settings.bigCaveSize) > CavernShape.bar(base, y, bottom, top) + rise
                            && !nextToLiquid(primer, lx, y, lz)) {
                        primer.setBlockState(lx, y, lz, air);
                    }
                }
            }
        }
    }
}
