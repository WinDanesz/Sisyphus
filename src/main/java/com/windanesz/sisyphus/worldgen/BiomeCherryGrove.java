package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.block.BlockPinkPetals;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BiomeCherryGrove extends Biome {

    private static final int GRASS_COLOR = 0xB6DB61;
    private static final int SKY_COLOR = 0x7BA4FF;
    private static final int WATER_COLOR = 0x5DB7EF;

    private static final int PETAL_TRIES = 96;
    private static final int PETAL_SPREAD = 6;

    public BiomeCherryGrove() {
        super(new BiomeProperties("Cherry Grove").setBaseHeight(0.7F).setHeightVariation(0.4F).setTemperature(0.5F).setRainfall(0.8F)
                .setWaterColor(WATER_COLOR));
        this.decorator.treesPerChunk = 10;
        this.decorator.extraTreeChance = 0.1F;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 10;
        this.spawnableCreatureList.clear();
        this.spawnableCreatureList.add(new SpawnListEntry(EntityPig.class, 5, 1, 2));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityRabbit.class, 10, 2, 6));
        this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 10, 2, 4));
    }

    @Override
    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return new WorldGenCherryTree(false);
    }

    @Override
    public void addDefaultFlowers() {
        // The petals are its flowers, put down in decorate
    }

    @Override
    public void decorate(World world, Random rand, BlockPos pos) {
        ChunkPos chunk = new ChunkPos(pos);
        if (TerrainGen.decorate(world, rand, chunk, DecorateBiomeEvent.Decorate.EventType.GRASS)) {
            DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
            // Two patches of tall grass, at the top of the ground and not at a random height as in the plains, which mostly misses it
            for (int i = 0; i < 2; i++) {
                DOUBLE_PLANT_GENERATOR.generate(world, rand, world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8)));
            }
        }
        if (TerrainGen.decorate(world, rand, chunk, DecorateBiomeEvent.Decorate.EventType.FLOWERS)) {
            this.decoratePetals(world, rand, pos);
        }
        super.decorate(world, rand, pos);
    }

    private void decoratePetals(World world, Random rand, BlockPos pos) {
        Block petals = ModBlocks.get("pink_petals");
        if (!(petals instanceof BlockPinkPetals)) {
            return;
        }
        double noise = GRASS_COLOR_NOISE.getValue((pos.getX() + 8) / 200.0D, (pos.getZ() + 8) / 200.0D);
        int patches = noise < -0.8D ? 5 : 10;
        for (int i = 0; i < patches; i++) {
            BlockPos center = world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
            for (int j = 0; j < PETAL_TRIES; j++) {
                BlockPos at = center.add(rand.nextInt(PETAL_SPREAD + 1) - rand.nextInt(PETAL_SPREAD + 1), rand.nextInt(3) - rand.nextInt(3),
                        rand.nextInt(PETAL_SPREAD + 1) - rand.nextInt(PETAL_SPREAD + 1));
                if (world.isAirBlock(at) && petals.canPlaceBlockAt(world, at)) {
                    IBlockState state = petals.getDefaultState().withProperty(BlockPinkPetals.FACING, EnumFacing.byHorizontalIndex(rand.nextInt(4)))
                            .withProperty(BlockPinkPetals.AMOUNT, 1 + rand.nextInt(BlockPinkPetals.MAX));
                    world.setBlockState(at, state, 2 | 16);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos) {
        return this.getModdedBiomeGrassColor(GRASS_COLOR);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos) {
        return this.getModdedBiomeFoliageColor(GRASS_COLOR);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSkyColorByTemp(float temperature) {
        return SKY_COLOR;
    }
}
