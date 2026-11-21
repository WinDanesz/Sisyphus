package com.windanesz.sisyphus.worldgen.camps;

import com.windanesz.sisyphus.block.BlockCampfire;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * From Wilderlands.
 */
final class AbandonedCamp {

    static final int RADIUS = 9;
    private static final int HALF = 4;

    private AbandonedCamp() {
    }

    static Plan build(int cx, int cz, int base, int rotation, Ground ground, ResourceLocation loot, Random random) {
        Builder b = new Builder(cx, cz, base, rotation, ground, random);
        b.clearing();
        b.tent();
        b.props(loot);
        b.webs();
        b.flora();
        b.settle();
        return b.plan;
    }

    private static IBlockState air() {
        return Blocks.AIR.getDefaultState();
    }

    private static IBlockState wool() {
        return Blocks.WOOL.getDefaultState();
    }

    private static IBlockState coarse() {
        return Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    }

    private static IBlockState doublePlant(BlockDoublePlant.EnumPlantType type, BlockDoublePlant.EnumBlockHalf half) {
        return Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, type).withProperty(BlockDoublePlant.HALF, half);
    }

    private static IBlockState flower(Random random) {
        BlockFlower.EnumFlowerType[] reds = {BlockFlower.EnumFlowerType.OXEYE_DAISY, BlockFlower.EnumFlowerType.OXEYE_DAISY, BlockFlower.EnumFlowerType.HOUSTONIA,
                BlockFlower.EnumFlowerType.HOUSTONIA, BlockFlower.EnumFlowerType.BLUE_ORCHID, BlockFlower.EnumFlowerType.POPPY, BlockFlower.EnumFlowerType.ALLIUM};
        if (random.nextInt(3) == 0) {
            return Blocks.YELLOW_FLOWER.getDefaultState();
        }
        return Blocks.RED_FLOWER.getDefaultState().withProperty(Blocks.RED_FLOWER.getTypeProperty(), reds[random.nextInt(reds.length)]);
    }

    /**
     * Under any plants, trees and snow.
     */
    interface Ground {
        int height(int x, int z);
    }

    static final class Plan {
        final Map<BlockPos, IBlockState> blocks = new LinkedHashMap<>();
        final Map<BlockPos, ResourceLocation> chests = new LinkedHashMap<>();
    }

    private static final class Builder {
        final int cx, cz, base, rotation;
        final Ground ground;
        final Random random;
        final Rotation turn;
        final Plan plan = new Plan();
        final double phaseA, phaseB;
        final int fallen;

        Builder(int cx, int cz, int base, int rotation, Ground ground, Random random) {
            this.cx = cx;
            this.cz = cz;
            this.base = base;
            this.rotation = rotation;
            this.ground = ground;
            this.random = random;
            this.turn = Rotation.values()[rotation];
            phaseA = random.nextDouble() * 2 * Math.PI;
            phaseB = random.nextDouble() * 2 * Math.PI;
            fallen = random.nextBoolean() ? -1 : 1;
        }

        BlockPos pos(int x, int y, int z) {
            for (int i = 0; i < rotation; i++) {
                int old = x;
                x = -z;
                z = old;
            }
            return new BlockPos(cx + x, base + y, cz + z);
        }

        void put(int x, int y, int z, IBlockState state) {
            plan.blocks.put(pos(x, y, z), state.withRotation(turn));
        }

        boolean free(int x, int y, int z) {
            IBlockState state = plan.blocks.get(pos(x, y, z));
            return state == null || state.getBlock() == Blocks.AIR;
        }

        boolean isWool(int x, int y, int z) {
            IBlockState state = plan.blocks.get(pos(x, y, z));
            return state != null && state.getBlock() == Blocks.WOOL;
        }

        int roof(int x) {
            return HALF + 1 - Math.abs(x);
        }

        void paved(int x, int z, IBlockState surface, int headroom) {
            BlockPos p = pos(x, 0, z);
            int terrain = ground.height(p.getX(), p.getZ());
            for (int y = terrain + 1; y < p.getY(); y++) {
                plan.blocks.put(new BlockPos(p.getX(), y, p.getZ()), Blocks.DIRT.getDefaultState());
            }
            put(x, 0, z, surface);
            for (int y = p.getY() + 1; y <= Math.max(terrain, p.getY() + headroom); y++) {
                plan.blocks.put(new BlockPos(p.getX(), y, p.getZ()), air());
            }
        }

        double reach(double angle) {
            return 6.2 + 0.8 * Math.sin(2 * angle + phaseA) + 0.6 * Math.sin(3 * angle + phaseB);
        }

        void clearing() {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    double r = Math.hypot(x, z);
                    double reach = reach(Math.atan2(z, x));
                    boolean tent = Math.abs(x) <= HALF + 1 && Math.abs(z) <= HALF + 1;
                    boolean apron = Math.abs(x) <= 3 && z > HALF + 1 && z <= 7;
                    if (!tent && !apron && r > reach + 1) {
                        continue;
                    }
                    double wear = tent || apron ? .85 : Math.max(0, Math.min(.85, (reach - r) / 3));
                    double roll = random.nextDouble();
                    IBlockState surface = roll < wear * .75 ? Blocks.GRASS_PATH.getDefaultState() : roll < wear ? coarse() : Blocks.GRASS.getDefaultState();
                    paved(x, z, surface, tent ? HALF + 3 : 4);
                }
            }
        }

        /**
         * A stepped white wool tent along the z axis, half collapsed.
         */
        void tent() {
            int window = -2 + random.nextInt(4);
            for (int z = -HALF; z <= HALF; z++) {
                boolean end = Math.abs(z) >= HALF - 1;
                for (int x = -HALF; x <= HALF; x++) {
                    int y = roof(x);
                    if (x * fallen > 0) {
                        if (random.nextInt(100) < (end ? 72 : 10)) {
                            put(x, y, z, wool());
                        }
                    } else if (x != 0 && z >= window && z <= window + 1 && y >= 2 && y <= HALF) {
                        if (random.nextBoolean()) {
                            put(x, y, z, Blocks.WEB.getDefaultState());
                        }
                    } else if (random.nextInt(100) >= (x == 0 ? 15 : 4)) {
                        put(x, y, z, wool());
                    }
                }
            }
            // Cloth from the fallen slope, folded over the ground and what is left of its edge
            for (int z = -HALF; z <= HALF; z++) {
                for (int x = 2; x <= HALF + 1; x++) {
                    int fx = x * fallen;
                    if (isWool(fx, 1, z) || random.nextInt(100) >= 38) {
                        continue;
                    }
                    put(fx, 0, z, coarse());
                    put(fx, 1, z, wool());
                    if (random.nextInt(4) == 0) {
                        put(fx, 2, z, wool());
                    }
                }
            }
            // The back gable, with the holes of whatever has rotted through it
            for (int x = -HALF + 1; x <= HALF - 1; x++) {
                for (int y = 1; y < roof(x); y++) {
                    if (random.nextInt(100) < (x * fallen > 0 ? 45 : 80)) {
                        put(x, y, -HALF, wool());
                    }
                }
            }
            // Poles under the standing slope, one of them snapped, and a stub on the fallen side
            for (int z : new int[]{-3, 0, 3}) {
                if (random.nextInt(100) < 18) {
                    continue;
                }
                pole(-fallen, z, roof(1) - 1 - (random.nextInt(4) == 0 ? 1 : 0));
            }
            if (random.nextBoolean()) {
                pole(fallen, 3 * random.nextInt(3) - 3, 1 + random.nextInt(2));
            }
        }

        void pole(int x, int z, int height) {
            put(x, 0, z, coarse());
            for (int y = 1; y <= height; y++) {
                put(x, y, z, Blocks.BIRCH_FENCE.getDefaultState());
            }
        }

        void props(ResourceLocation loot) {
            put(-1, 1, 6, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH));
            plan.chests.put(pos(-1, 1, 6), loot);
            for (int x = 1; x <= 2; x++) {
                put(x, 1, 6, Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
            }
            put(0, 1, 5, Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.RED));
            // The fire they sat round, long gone out, where the campfire is turned on
            Block campfire = ModBlocks.get("campfire");
            if (campfire instanceof BlockCampfire) {
                put(0, 1, 7, campfire.getDefaultState().withProperty(BlockCampfire.LIT, false)
                        .withProperty(BlockCampfire.FACING, EnumFacing.byHorizontalIndex(random.nextInt(4))));
            }
        }

        void settle() {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    IBlockState floor = plan.blocks.get(pos(x, 0, z));
                    IBlockState above = plan.blocks.get(pos(x, 1, z));
                    if (floor != null && floor.getBlock() == Blocks.GRASS_PATH && above != null && above.getBlock() != Blocks.WEB && above.getMaterial().isSolid()) {
                        put(x, 0, z, coarse());
                    }
                }
            }
        }

        boolean anchored(int x, int y, int z) {
            return isWool(x, y + 1, z) || isWool(x + 1, y, z) || isWool(x - 1, y, z) || isWool(x, y, z + 1) || isWool(x, y, z - 1);
        }

        void webs() {
            for (int z = -HALF; z <= HALF; z++) {
                for (int x = -HALF + 1; x <= HALF - 1; x++) {
                    for (int y = 1; y < roof(x); y++) {
                        if (free(x, y, z) && anchored(x, y, z) && random.nextInt(100) < 9) {
                            put(x, y, z, Blocks.WEB.getDefaultState());
                        }
                    }
                }
            }
        }

        void flora() {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    IBlockState floor = plan.blocks.get(pos(x, 0, z));
                    if (floor == null || floor.getBlock() != Blocks.GRASS || !free(x, 1, z) || !free(x, 2, z)) {
                        continue;
                    }
                    int roll = random.nextInt(100);
                    if (roll < 7 && Math.hypot(x, z) > 4.5) {
                        put(x, 1, z, doublePlant(BlockDoublePlant.EnumPlantType.ROSE, BlockDoublePlant.EnumBlockHalf.LOWER));
                        put(x, 2, z, doublePlant(BlockDoublePlant.EnumPlantType.ROSE, BlockDoublePlant.EnumBlockHalf.UPPER));
                    } else if (roll < 12) {
                        put(x, 1, z, flower(random));
                    } else if (roll < 30) {
                        put(x, 1, z, Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS));
                    } else if (roll < 35) {
                        put(x, 1, z, doublePlant(BlockDoublePlant.EnumPlantType.GRASS, BlockDoublePlant.EnumBlockHalf.LOWER));
                        put(x, 2, z, doublePlant(BlockDoublePlant.EnumPlantType.GRASS, BlockDoublePlant.EnumBlockHalf.UPPER));
                    }
                }
            }
        }
    }
}
