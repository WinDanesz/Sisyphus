package com.windanesz.sisyphus.worldgen.ruins;

import com.windanesz.sisyphus.Settings;
import net.minecraft.init.Biomes;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.BiomeDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGenOceanRuins extends MapGenStructure {

    public static final String NAME = "SBOceanRuins";

    private static final int SALT = 14357621;

    private static final String[] WARM = names("warm_", 1, 2, 3, 4, 5, 6, 7, 8);
    private static final String[] BIG_WARM = names("big_warm_", 4, 5, 6, 7);
    private static final String[] BRICK = names("brick_", 1, 2, 3, 4, 5, 6, 7, 8);
    private static final String[] CRACKED = names("cracked_", 1, 2, 3, 4, 5, 6, 7, 8);
    private static final String[] MOSSY = names("mossy_", 1, 2, 3, 4, 5, 6, 7, 8);
    private static final String[] BIG_BRICK = names("big_brick_", 1, 2, 3, 8);
    private static final String[] BIG_CRACKED = names("big_cracked_", 1, 2, 3, 8);
    private static final String[] BIG_MOSSY = names("big_mossy_", 1, 2, 3, 8);

    private static String[] names(String prefix, int... numbers) {
        String[] names = new String[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            names[i] = "underwater_ruin/" + prefix + numbers[i];
        }
        return names;
    }

    public static boolean isOcean(Biome biome) {
        return biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN || biome == Biomes.FROZEN_OCEAN || BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN);
    }

    private static int spacing() {
        return Settings.worldGeneration.oceanRuins.spacing;
    }

    private static int separation() {
        return Math.min(Settings.worldGeneration.oceanRuins.separation, spacing() - 1);
    }

    static boolean isWarm(World world, Biome biome, int chunkX, int chunkZ) {
        if (biome == Biomes.FROZEN_OCEAN || BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)) {
            return false;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT)) {
            return true;
        }
        Random region = world.setRandomSeed(Math.floorDiv(chunkX, 32), Math.floorDiv(chunkZ, 32), SALT + 1);
        return region.nextBoolean();
    }

    @Override
    public String getStructureName() {
        return NAME;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int[] cell = MapGenTrailRuins.cell(this.world, chunkX, chunkZ, spacing(), separation(), SALT);
        if (chunkX != cell[0] || chunkZ != cell[1]) {
            return false;
        }
        return isOcean(this.world.getBiomeProvider().getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)));
    }

    @Override
    public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored) {
        this.world = world;
        return findNearestStructurePosBySpacing(world, this, pos, spacing(), separation(), SALT, false, 100, findUnexplored);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        Biome biome = this.world.getBiomeProvider().getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
        return new Start(this.rand, chunkX, chunkZ, isWarm(this.world, biome, chunkX, chunkZ));
    }

    public static class Start extends StructureStart {

        public Start() {
        }

        public Start(Random random, int chunkX, int chunkZ, boolean warm) {
            super(chunkX, chunkZ);
            BlockPos pos = new BlockPos(chunkX * 16, 90, chunkZ * 16);
            Rotation rotation = Rotation.values()[random.nextInt(4)];
            boolean large = random.nextFloat() * 100.0F <= Settings.worldGeneration.oceanRuins.largeRuins;
            addRuin(random, pos, rotation, warm, large, large ? 0.9F : 0.8F);
            if (large && random.nextFloat() * 100.0F <= Settings.worldGeneration.oceanRuins.clusters) {
                this.addCluster(random, rotation, pos, warm);
            }
            this.updateBoundingBox();
        }

        private static List<BlockPos> clusterPositions(Random random, BlockPos pos) {
            List<BlockPos> list = new ArrayList<>();
            list.add(pos.add(-16 + MathHelper.getInt(random, 1, 8), 0, 16 + MathHelper.getInt(random, 1, 7)));
            list.add(pos.add(-16 + MathHelper.getInt(random, 1, 8), 0, MathHelper.getInt(random, 1, 7)));
            list.add(pos.add(-16 + MathHelper.getInt(random, 1, 8), 0, -16 + MathHelper.getInt(random, 4, 8)));
            list.add(pos.add(MathHelper.getInt(random, 1, 7), 0, 16 + MathHelper.getInt(random, 1, 7)));
            list.add(pos.add(MathHelper.getInt(random, 1, 7), 0, -16 + MathHelper.getInt(random, 4, 6)));
            list.add(pos.add(16 + MathHelper.getInt(random, 1, 7), 0, 16 + MathHelper.getInt(random, 3, 8)));
            list.add(pos.add(16 + MathHelper.getInt(random, 1, 7), 0, MathHelper.getInt(random, 1, 7)));
            list.add(pos.add(16 + MathHelper.getInt(random, 1, 7), 0, -16 + MathHelper.getInt(random, 4, 8)));
            return list;
        }

        private void addCluster(Random random, Rotation rotation, BlockPos pos, boolean warm) {
            BlockPos corner = pos.add(RuinTemplate.transform(new BlockPos(15, 0, 15), rotation));
            StructureBoundingBox big = new StructureBoundingBox(Math.min(pos.getX(), corner.getX()), pos.getY(), Math.min(pos.getZ(), corner.getZ()),
                    Math.max(pos.getX(), corner.getX()), pos.getY(), Math.max(pos.getZ(), corner.getZ()));
            BlockPos min = new BlockPos(Math.min(pos.getX(), corner.getX()), pos.getY(), Math.min(pos.getZ(), corner.getZ()));
            List<BlockPos> spots = clusterPositions(random, min);
            int count = MathHelper.getInt(random, 4, 8);
            for (int i = 0; i < count && !spots.isEmpty(); i++) {
                BlockPos spot = spots.remove(random.nextInt(spots.size()));
                Rotation turn = Rotation.values()[random.nextInt(4)];
                BlockPos far = spot.add(RuinTemplate.transform(new BlockPos(5, 0, 6), turn));
                StructureBoundingBox small = new StructureBoundingBox(Math.min(spot.getX(), far.getX()), spot.getY(), Math.min(spot.getZ(), far.getZ()),
                        Math.max(spot.getX(), far.getX()), spot.getY(), Math.max(spot.getZ(), far.getZ()));
                if (!small.intersectsWith(big)) {
                    this.addRuin(random, spot, turn, warm, false, 0.8F);
                }
            }
        }

        @Override
        public void generateStructure(World world, Random random, StructureBoundingBox box) {
            // Every piece finds the floor before any is built, so that none of them stands on another, and is then built whole
            List<RuinPiece> settled = new ArrayList<>();
            for (StructureComponent component : this.components) {
                if (component instanceof RuinPiece && ((RuinPiece) component).settleIfNeeded(world, box)) {
                    settled.add((RuinPiece) component);
                }
            }
            List<BlockPos> placed = new ArrayList<>();
            for (RuinPiece piece : settled) {
                piece.buildWhole(world, random, placed);
            }
            if (!placed.isEmpty()) {
                RuinPiece.removeFloatingBlocks(world, placed);
            }
            this.updateBoundingBox();
            super.generateStructure(world, random, box);
        }

        private void addRuin(Random random, BlockPos pos, Rotation rotation, boolean warm, boolean large, float integrity) {
            if (warm) {
                String[] names = large ? BIG_WARM : WARM;
                this.components.add(new RuinPiece(names[random.nextInt(names.length)], pos, rotation, RuinPiece.OCEAN_WARM, integrity, large));
            } else {
                String[] bricks = large ? BIG_BRICK : BRICK;
                int i = random.nextInt(bricks.length);
                this.components.add(new RuinPiece(bricks[i], pos, rotation, RuinPiece.OCEAN_COLD, integrity, large));
                this.components.add(new RuinPiece((large ? BIG_CRACKED : CRACKED)[i], pos, rotation, RuinPiece.OCEAN_COLD, 0.7F, large));
                this.components.add(new RuinPiece((large ? BIG_MOSSY : MOSSY)[i], pos, rotation, RuinPiece.OCEAN_COLD, 0.5F, large));
            }
        }
    }
}
