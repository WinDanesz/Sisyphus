package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockBamboo;
import com.windanesz.sisyphus.block.BlockSweetBerryBush;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class WorldGenWildPlants {

    private WorldGenWildPlants() {
    }

    @SubscribeEvent
    public static void onDecorate(DecorateBiomeEvent.Post event) {
        World world = event.getWorld();
        if (!Settings.allowsDimension(Settings.worldGeneration.wildPlants.dimensions, world)) {
            return;
        }
        Random rand = event.getRand();
        BlockPos chunk = event.getChunkPos().getBlock(0, 0, 0);
        Biome biome = world.getBiome(chunk.add(16, 0, 16));
        Settings.WildPlants wild = Settings.worldGeneration.wildPlants;
        if (Settings.plants.enableBamboo && wild.enableBamboo) {
            decorateBamboo(world, rand, chunk, biome);
        }
        if (Settings.plants.enableSweetBerries && wild.enableSweetBerries) {
            decorateSweetBerries(world, rand, chunk, biome);
        }
        if (Settings.plants.enableFlowers && wild.enableFlowers) {
            decorateFlowers(world, rand, chunk, biome);
        }
    }

    private static void decorateBamboo(World world, Random rand, BlockPos chunk, Biome biome) {
        if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE) || rand.nextInt(Settings.worldGeneration.wildPlants.bambooRarity) != 0) {
            return;
        }
        generateThicket(world, rand, world.getHeight(chunk.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8)), 6 + rand.nextInt(10), 5);
    }

    public static int generateThicket(World world, Random rand, BlockPos origin, int stalks, int radius) {
        Block bamboo = ModBlocks.get("bamboo");
        int placed = 0;
        for (int i = 0; i < stalks; i++) {
            BlockPos base = world.getHeight(origin.add(rand.nextInt(radius + 2) - rand.nextInt(radius + 2), 0, rand.nextInt(radius + 2) - rand.nextInt(radius + 2)));
            if (!bamboo.canPlaceBlockAt(world, base)) {
                continue;
            }
            int height = 5 + rand.nextInt(10);
            for (int y = 0; y < height && world.isAirBlock(base.up(y)); y++) {
                BlockBamboo.Leaves leaves = y == height - 1 ? BlockBamboo.Leaves.LARGE : (y == height - 2 ? BlockBamboo.Leaves.SMALL : BlockBamboo.Leaves.NONE);
                IBlockState state = bamboo.getDefaultState().withProperty(BlockBamboo.AGE, y > 1 ? 1 : 0)
                        .withProperty(BlockBamboo.LEAVES, leaves).withProperty(BlockBamboo.STAGE, y == height - 1 ? 1 : 0);
                world.setBlockState(base.up(y), state, 2);
            }
            placed++;
        }
        return placed;
    }

    private static void decorateSweetBerries(World world, Random rand, BlockPos chunk, Biome biome) {
        if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)) {
            return;
        }
        boolean snowy = BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY);
        Settings.WildPlants wild = Settings.worldGeneration.wildPlants;
        if (rand.nextInt(snowy ? wild.snowySweetBerryRarity : wild.sweetBerryRarity) != 0) {
            return;
        }
        Block bush = ModBlocks.get("sweet_berry_bush");
        BlockPos origin = world.getHeight(chunk.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
        for (int i = 0; i < 12; i++) {
            BlockPos pos = origin.add(rand.nextInt(6) - rand.nextInt(6), rand.nextInt(3) - rand.nextInt(3), rand.nextInt(6) - rand.nextInt(6));
            if (world.isAirBlock(pos) && pos.getY() > 0 && bush.canPlaceBlockAt(world, pos)) {
                IBlockState state = bush.getDefaultState().withProperty(BlockSweetBerryBush.AGE, 2 + rand.nextInt(2));
                world.setBlockState(pos, state, 2);
            }
        }
    }

    private static void decorateFlowers(World world, Random rand, BlockPos chunk, Biome biome) {
        Settings.WildPlants wild = Settings.worldGeneration.wildPlants;
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS) && rand.nextInt(wild.cornflowerRarity) == 0) {
            scatter(world, rand, chunk, ModBlocks.get("cornflower").getDefaultState());
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST) && rand.nextInt(wild.lilyOfTheValleyRarity) == 0) {
            scatter(world, rand, chunk, ModBlocks.get("lily_of_the_valley").getDefaultState());
        }
    }

    private static void scatter(World world, Random rand, BlockPos chunk, IBlockState flower) {
        BlockPos origin = world.getHeight(chunk.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
        for (int i = 0; i < 24; i++) {
            BlockPos pos = origin.add(rand.nextInt(7) - rand.nextInt(7), rand.nextInt(3) - rand.nextInt(3), rand.nextInt(7) - rand.nextInt(7));
            if (world.isAirBlock(pos) && pos.getY() > 0 && flower.getBlock().canPlaceBlockAt(world, pos)) {
                world.setBlockState(pos, flower, 2);
            }
        }
    }
}
