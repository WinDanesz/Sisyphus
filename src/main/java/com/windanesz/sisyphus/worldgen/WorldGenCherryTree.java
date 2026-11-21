package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.IPlantable;

import java.util.*;

public class WorldGenCherryTree extends WorldGenAbstractTree {

    private static final int BASE_HEIGHT = 7;
    private static final int FOLIAGE_HEIGHT = 5;
    private static final int FOLIAGE_RADIUS = 4;
    private static final float WIDE_BOTTOM_LAYER_HOLE_CHANCE = 0.25F;
    private static final float CORNER_HOLE_CHANCE = 0.5F;
    private static final float HANGING_LEAVES_CHANCE = 1 / 6.0F;
    private static final float HANGING_LEAVES_EXTENSION_CHANCE = 1 / 3.0F;

    /**
     * Observers are not notified either, as Forge's fixVanillaCascading does for vanilla trees: the crown can reach the edge of the
     * decorated area, and the neighbour check would generate the next chunk.
     */
    private final int flags;

    public WorldGenCherryTree(boolean notify) {
        super(notify);
        this.flags = notify ? 3 : 2 | 16;
    }

    private static boolean isFree(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return validTreePos(world, pos, state) || state.getBlock().isWood(world, pos);
    }

    private static boolean validTreePos(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        return block.isAir(state, world, pos) || block.isLeaves(state, world, pos) || state.getMaterial() == Material.WATER
                || block.isReplaceable(world, pos);
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        Block log = ModBlocks.get("cherry_log");
        Block leaves = ModBlocks.get("cherry_leaves");
        if (log == Blocks.AIR || leaves == Blocks.AIR) {
            return false;
        }
        int height = BASE_HEIGHT + rand.nextInt(2);
        if (pos.getY() < 1 || pos.getY() + height + 1 >= world.getHeight() || !this.hasRoom(world, pos, height)) {
            return false;
        }
        IBlockState ground = world.getBlockState(pos.down());
        if (!ground.getBlock().canSustainPlant(ground, world, pos.down(), EnumFacing.UP, (IPlantable) Blocks.SAPLING)) {
            return false;
        }
        ground.getBlock().onPlantGrow(ground, world, pos.down(), pos);

        Tree tree = new Tree(world, log.getDefaultState(), leaves.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false));
        for (BlockPos attachment : this.placeTrunk(tree, rand, height, pos)) {
            this.placeFoliage(tree, rand, attachment);
        }
        return true;
    }

    private boolean hasRoom(World world, BlockPos pos, int height) {
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int y = 0; y <= height + 1; y++) {
            int radius = y < 1 ? 0 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    at.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (!at.equals(pos) && !isFree(world, at)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private List<BlockPos> placeTrunk(Tree tree, Random rand, int height, BlockPos pos) {
        int firstStart = Math.max(0, height - 1 - 4 + rand.nextInt(2));
        int secondStart = Math.max(0, height - 1 - 4);
        if (secondStart >= firstStart) {
            secondStart++;
        }
        int branches = 1 + rand.nextInt(3);
        boolean three = branches == 3;
        boolean two = branches >= 2;
        int trunk = three ? height : two ? Math.max(firstStart, secondStart) + 1 : firstStart + 1;
        for (int y = 0; y < trunk; y++) {
            tree.log(pos.up(y), BlockLog.EnumAxis.Y);
        }
        List<BlockPos> attachments = new ArrayList<>();
        if (three) {
            attachments.add(pos.up(trunk));
        }
        EnumFacing direction = EnumFacing.Plane.HORIZONTAL.random(rand);
        attachments.add(this.placeBranch(tree, rand, height, pos, direction, firstStart, firstStart < trunk - 1));
        if (two) {
            attachments.add(this.placeBranch(tree, rand, height, pos, direction.getOpposite(), secondStart, secondStart < trunk - 1));
        }
        return attachments;
    }

    private BlockPos placeBranch(Tree tree, Random rand, int height, BlockPos pos, EnumFacing direction, int start, boolean fromTrunk) {
        BlockLog.EnumAxis axis = BlockLog.EnumAxis.fromFacingAxis(direction.getAxis());
        BlockPos at = pos.up(start);
        int endY = height - 1 - rand.nextInt(2);
        boolean longer = fromTrunk || endY < start;
        int length = 2 + rand.nextInt(3) + (longer ? 1 : 0);
        BlockPos end = pos.offset(direction, length).up(endY);
        for (int i = 0; i < (longer ? 2 : 1); i++) {
            at = at.offset(direction);
            tree.log(at, axis);
        }
        EnumFacing vertical = end.getY() > at.getY() ? EnumFacing.UP : EnumFacing.DOWN;
        while (true) {
            int distance = Math.abs(end.getX() - at.getX()) + Math.abs(end.getY() - at.getY()) + Math.abs(end.getZ() - at.getZ());
            if (distance == 0) {
                return end.up();
            }
            boolean up = rand.nextFloat() < (float) Math.abs(end.getY() - at.getY()) / distance;
            at = at.offset(up ? vertical : direction);
            tree.log(at, up ? BlockLog.EnumAxis.Y : axis);
        }
    }

    private void placeFoliage(Tree tree, Random rand, BlockPos center) {
        int range = FOLIAGE_RADIUS - 1;
        this.placeRow(tree, rand, center, range - 2, FOLIAGE_HEIGHT - 3);
        this.placeRow(tree, rand, center, range - 1, FOLIAGE_HEIGHT - 4);
        for (int y = FOLIAGE_HEIGHT - 5; y >= 0; y--) {
            this.placeRow(tree, rand, center, range, y);
        }
        this.placeRowWithHangingLeaves(tree, rand, center, range, -1);
        this.placeRowWithHangingLeaves(tree, rand, center, range - 1, -2);
    }

    private void placeRow(Tree tree, Random rand, BlockPos center, int range, int y) {
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                if (!this.skip(rand, Math.abs(x), y, Math.abs(z), range)) {
                    tree.leaf(center.add(x, y, z));
                }
            }
        }
    }

    private boolean skip(Random rand, int x, int y, int z, int range) {
        if (y == -1 && (x == range || z == range) && rand.nextFloat() < WIDE_BOTTOM_LAYER_HOLE_CHANCE) {
            return true;
        }
        boolean corner = x == range && z == range;
        if (range > 2) {
            return corner || x + z > range * 2 - 2 && rand.nextFloat() < CORNER_HOLE_CHANCE;
        }
        return corner && rand.nextFloat() < CORNER_HOLE_CHANCE;
    }

    private void placeRowWithHangingLeaves(Tree tree, Random rand, BlockPos center, int range, int y) {
        this.placeRow(tree, rand, center, range, y);
        BlockPos log = center.down();
        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            EnumFacing clockwise = side.rotateY();
            BlockPos at = center.add(0, y - 1, 0).offset(clockwise, range).offset(side, -range);
            for (int i = -range; i < range; i++) {
                if (tree.hasLeaf(at.up()) && this.hang(tree, rand, HANGING_LEAVES_CHANCE, log, at)) {
                    this.hang(tree, rand, HANGING_LEAVES_EXTENSION_CHANCE, log, at.down());
                }
                at = at.offset(side);
            }
        }
    }

    private boolean hang(Tree tree, Random rand, float chance, BlockPos log, BlockPos at) {
        int distance = Math.abs(at.getX() - log.getX()) + Math.abs(at.getY() - log.getY()) + Math.abs(at.getZ() - log.getZ());
        return distance < 7 && rand.nextFloat() <= chance && tree.leaf(at);
    }

    private final class Tree {

        private final World world;
        private final IBlockState log;
        private final IBlockState leaves;
        private final Set<BlockPos> placedLeaves = new HashSet<>();

        Tree(World world, IBlockState log, IBlockState leaves) {
            this.world = world;
            this.log = log;
            this.leaves = leaves;
        }

        void log(BlockPos pos, BlockLog.EnumAxis axis) {
            if (validTreePos(this.world, pos, this.world.getBlockState(pos))) {
                this.world.setBlockState(pos, this.log.withProperty(BlockLog.LOG_AXIS, axis), WorldGenCherryTree.this.flags);
            }
        }

        boolean leaf(BlockPos pos) {
            IBlockState state = this.world.getBlockState(pos);
            if (!validTreePos(this.world, pos, state) || state.getBlock().isWood(this.world, pos)) {
                return false;
            }
            this.world.setBlockState(pos, this.leaves, WorldGenCherryTree.this.flags);
            this.placedLeaves.add(pos.toImmutable());
            return true;
        }

        boolean hasLeaf(BlockPos pos) {
            return this.placedLeaves.contains(pos);
        }
    }
}
