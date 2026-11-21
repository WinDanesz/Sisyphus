package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.integration.ModdedDeepslateOres;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Function;

public class WorldGenDeepslate implements IWorldGenerator {

    /**
     * 2 sends the change to the players, 16 keeps observers from seeing it. Without 16, each edge block makes the game look into the next
     * chunk, which loads and is populated by this generator in turn, until the stack overflows.
     */
    public static final int SET_FLAGS = 2 | 16;
    private static final int REACH = 24;

    /**
     * {@code maxX} and {@code maxZ} are exclusive. Returns how many blocks changed.
     */
    public static int convert(Grid grid, long seed, int minX, int minZ, int maxX, int maxZ, int yThreshold, int transitionRange,
                              Function<IBlockState, IBlockState> deepslateFor) {
        int changed = 0;
        int top = Math.min(yThreshold, 256);
        Random random = new Random();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = 0; y < top; y++) {
                    float chance = replacementChance(y, yThreshold, transitionRange);
                    if (chance <= 0.0F) {
                        continue;
                    }
                    pos.setPos(x, y, z);
                    IBlockState replacement = deepslateFor.apply(grid.get(pos));
                    if (replacement == null) {
                        continue;
                    }
                    if (chance < 1.0F) {
                        random.setSeed(seed + x * 341873128712L + y * 132897987541L + z * 938193739L);
                        if (random.nextFloat() >= chance) {
                            continue;
                        }
                    }
                    grid.set(new BlockPos(x, y, z), replacement);
                    changed++;
                }
            }
        }
        return changed;
    }

    /**
     * 1 from {@code transitionRange} blocks below the level, 0 from the level up, a smooth rise in between. A range of 0 is a hard edge.
     */
    public static float replacementChance(int y, int yThreshold, int transitionRange) {
        if (transitionRange <= 0) {
            return y < yThreshold ? 1.0F : 0.0F;
        }
        float distance = yThreshold - y;
        if (distance >= transitionRange) {
            return 1.0F;
        }
        if (distance <= 0) {
            return 0.0F;
        }
        float normalized = distance / transitionRange;
        return normalized * normalized * (3.0F - 2.0F * normalized);
    }

    @Nullable
    public static IBlockState deepslateFor(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONE) {
            return state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE ? ModBlocks.deepslate.getDefaultState() : null;
        }
        if (block == Blocks.AIR || !Settings.deepslate.replaceOresWithDeepslate) {
            return null;
        }
        Block ore = deepslateOre(block);
        return ore == null ? null : ore.getDefaultState();
    }

    @Nullable
    private static Block deepslateOre(Block block) {
        if (block == Blocks.COAL_ORE) {
            return ModBlocks.deepslate_coal_ore;
        } else if (block == Blocks.IRON_ORE) {
            return ModBlocks.deepslate_iron_ore;
        } else if (block == Blocks.GOLD_ORE) {
            return ModBlocks.deepslate_gold_ore;
        } else if (block == Blocks.DIAMOND_ORE) {
            return ModBlocks.deepslate_diamond_ore;
        } else if (block == Blocks.EMERALD_ORE) {
            return ModBlocks.deepslate_emerald_ore;
        } else if (block == Blocks.LAPIS_ORE) {
            return ModBlocks.deepslate_lapis_ore;
        } else if (block == Blocks.REDSTONE_ORE) {
            return ModBlocks.deepslate_redstone_ore;
        } else if (block == Blocks.LIT_REDSTONE_ORE) {
            return ModBlocks.lit_deepslate_redstone_ore;
        }
        return ModdedDeepslateOres.variantOf(block);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator generator, IChunkProvider provider) {
        if (!Settings.allowsDimension(Settings.deepslate.deepslateDimensions, world) || !Settings.deepslate.enableDeepslate || !Settings.deepslate.enableDeepslateGeneration) {
            return;
        }
        Grid grid = new Grid() {
            @Override
            public IBlockState get(BlockPos pos) {
                return world.getBlockState(pos);
            }

            @Override
            public void set(BlockPos pos, IBlockState state) {
                // Without waking up the neighbours of each block, or the observers next to it, which would load the chunks around
                world.setBlockState(pos, state, SET_FLAGS);
            }
        };
        int x = chunkX * 16;
        int z = chunkZ * 16;
        convert(grid, world.getSeed(), x, z, x + REACH, z + REACH, Settings.deepslate.deepslateYThreshold, Settings.deepslate.deepslateTransitionRange, WorldGenDeepslate::deepslateFor);
    }

    /**
     * The world, or a stand-in for tests.
     */
    public interface Grid {
        IBlockState get(BlockPos pos);

        void set(BlockPos pos, IBlockState state);
    }
}
