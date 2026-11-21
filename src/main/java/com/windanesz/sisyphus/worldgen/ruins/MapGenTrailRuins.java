package com.windanesz.sisyphus.worldgen.ruins;

import com.windanesz.sisyphus.Settings;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MapGenTrailRuins extends MapGenStructure {

    public static final String NAME = "SBTrailRuins";

    private static final int SALT = 83469867;

    private static final Set<Biome> BIOMES = new HashSet<>(Arrays.asList(Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.MUTATED_TAIGA, Biomes.COLD_TAIGA,
            Biomes.COLD_TAIGA_HILLS, Biomes.MUTATED_TAIGA_COLD, Biomes.REDWOOD_TAIGA, Biomes.REDWOOD_TAIGA_HILLS, Biomes.MUTATED_REDWOOD_TAIGA,
            Biomes.MUTATED_REDWOOD_TAIGA_HILLS, Biomes.MUTATED_BIRCH_FOREST, Biomes.MUTATED_BIRCH_FOREST_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS));

    public static boolean isRuinBiome(Biome biome) {
        if (BIOMES.contains(biome)) {
            return true;
        }
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE);
    }

    private static int spacing() {
        return Settings.worldGeneration.trailRuins.spacing;
    }

    private static int separation() {
        return Math.min(Settings.worldGeneration.trailRuins.separation, spacing() - 1);
    }

    static int[] cell(World world, int chunkX, int chunkZ, int spacing, int separation, int salt) {
        int x = Math.floorDiv(chunkX, spacing);
        int z = Math.floorDiv(chunkZ, spacing);
        Random random = world.setRandomSeed(x, z, salt);
        return new int[]{x * spacing + random.nextInt(spacing - separation), z * spacing + random.nextInt(spacing - separation)};
    }

    @Override
    public String getStructureName() {
        return NAME;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int[] cell = cell(this.world, chunkX, chunkZ, spacing(), separation(), SALT);
        if (chunkX != cell[0] || chunkZ != cell[1]) {
            return false;
        }
        Biome biome = this.world.getBiomeProvider().getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
        return isRuinBiome(biome);
    }

    @Override
    public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored) {
        this.world = world;
        return findNearestStructurePosBySpacing(world, this, pos, spacing(), separation(), SALT, false, 100, findUnexplored);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new Start(this.rand, chunkX, chunkZ);
    }

    public static class Start extends StructureStart {

        private boolean levelled;

        public Start() {
        }

        public Start(Random random, int chunkX, int chunkZ) {
            super(chunkX, chunkZ);
            this.components.addAll(TrailRuinsAssembler.assemble(random, chunkX * 16 + 8, chunkZ * 16 + 8));
            this.updateBoundingBox();
        }

        private static boolean intersectsColumns(StructureBoundingBox a, StructureBoundingBox b) {
            return a.maxX >= b.minX && a.minX <= b.maxX && a.maxZ >= b.minZ && a.minZ <= b.maxZ;
        }

        @Override
        public void generateStructure(World world, Random random, StructureBoundingBox box) {
            if (!this.levelled && !this.components.isEmpty()) {
                this.level(world, box);
            }
            super.generateStructure(world, random, box);
        }

        private void level(World world, StructureBoundingBox box) {
            StructureBoundingBox start = this.components.get(0).getBoundingBox();
            StructureBoundingBox area = intersectsColumns(start, box) ? start : this.boundingBox;
            int x = MathHelper.clamp((start.minX + start.maxX) / 2, Math.max(area.minX, box.minX), Math.min(area.maxX, box.maxX));
            int z = MathHelper.clamp((start.minZ + start.maxZ) / 2, Math.max(area.minZ, box.minZ), Math.min(area.maxZ, box.maxZ));
            int surface = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
            // The bottom of the tower is 16 below the first free block above the ground, as in the game
            int dy = Math.max(2, surface - 16) - start.minY;
            for (StructureComponent component : this.components) {
                component.offset(0, dy, 0);
            }
            this.updateBoundingBox();
            this.levelled = true;
        }

        @Override
        public void writeToNBT(NBTTagCompound tag) {
            super.writeToNBT(tag);
            tag.setBoolean("Levelled", this.levelled);
        }

        @Override
        public void readFromNBT(NBTTagCompound tag) {
            super.readFromNBT(tag);
            this.levelled = tag.getBoolean("Levelled");
        }
    }
}
