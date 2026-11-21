package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockBamboo extends Block implements IGrowable {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 1);
    public static final PropertyEnum<Leaves> LEAVES = PropertyEnum.create("leaves", Leaves.class);
    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 1);
    public static final SoundType BAMBOO = new SoundType(1.0F, 1.0F, ModSounds.BLOCK_BAMBOO_BREAK, ModSounds.BLOCK_BAMBOO_STEP,
            ModSounds.BLOCK_BAMBOO_PLACE, ModSounds.BLOCK_BAMBOO_HIT, ModSounds.BLOCK_BAMBOO_FALL);
    private static final int MAX_HEIGHT = 16;
    private static final AxisAlignedBB THIN = new AxisAlignedBB(0.40625D, 0.0D, 0.40625D, 0.59375D, 1.0D, 0.59375D);
    private static final AxisAlignedBB THICK = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 1.0D, 0.6875D);
    public BlockBamboo() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0).withProperty(LEAVES, Leaves.NONE).withProperty(STAGE, 0));
        this.setTickRandomly(true);
        this.setHardness(1.0F);
        this.setResistance(1.0F);
        this.setSoundType(BAMBOO);
    }

    public static boolean canPlantOn(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.SAND || block == Blocks.GRAVEL || block == Blocks.MYCELIUM;
    }

    public static boolean canStandOn(IBlockAccess world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        return below.getBlock() instanceof BlockBamboo || canPlantOn(below);
    }

    private static int heightBelow(IBlockAccess world, BlockPos pos) {
        int count = 0;
        while (count < MAX_HEIGHT && world.getBlockState(pos.down(count + 1)).getBlock() instanceof BlockBamboo) {
            count++;
        }
        return count;
    }

    private static int heightAbove(IBlockAccess world, BlockPos pos) {
        int count = 0;
        while (count < MAX_HEIGHT && world.getBlockState(pos.up(count + 1)).getBlock() instanceof BlockBamboo) {
            count++;
        }
        return count;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, LEAVES, STAGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, meta & 1).withProperty(STAGE, (meta >> 1) & 1)
                .withProperty(LEAVES, Leaves.values()[Math.min((meta >> 2) & 3, 2)]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE) | (state.getValue(STAGE) << 1) | (state.getValue(LEAVES).ordinal() << 2);
    }

    @Override
    public Block.EnumOffsetType getOffsetType() {
        return Block.EnumOffsetType.XZ;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        Vec3d offset = state.getOffset(source, pos);
        return (state.getValue(AGE) == 0 ? THIN : THICK).offset(offset.x, offset.y, offset.z);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return this.getBoundingBox(state, source, pos);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.get("bamboo");
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(ModItems.get("bamboo"));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && canStandOn(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!canStandOn(world, pos)) {
            world.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!canStandOn(world, pos)) {
            world.destroyBlock(pos, true);
            return;
        }
        // Only the top of the stalk grows
        if (state.getValue(STAGE) == 0 && rand.nextInt(3) == 0 && world.isAirBlock(pos.up()) && world.getLightFromNeighbors(pos.up()) >= 9) {
            int height = heightBelow(world, pos) + 1;
            if (height < MAX_HEIGHT) {
                growBamboo(state, world, pos, rand, height);
            }
        }
    }

    private void growBamboo(IBlockState state, World world, BlockPos pos, Random rand, int height) {
        IBlockState below = world.getBlockState(pos.down());
        BlockPos below2Pos = pos.down(2);
        IBlockState below2 = world.getBlockState(below2Pos);
        Leaves leaves = Leaves.NONE;

        if (height >= 1) {
            if (below.getBlock() == this && below.getValue(LEAVES) != Leaves.NONE) {
                leaves = Leaves.LARGE;
                if (below2.getBlock() == this) {
                    world.setBlockState(pos.down(), below.withProperty(LEAVES, Leaves.SMALL), 3);
                    world.setBlockState(below2Pos, below2.withProperty(LEAVES, Leaves.NONE), 3);
                }
            } else {
                leaves = Leaves.SMALL;
            }
        }

        int age = state.getValue(AGE) != 1 && below2.getBlock() != this ? 0 : 1;
        int stage = (height >= 11 && rand.nextFloat() < 0.25F) || height == 15 ? 1 : 0;
        world.setBlockState(pos.up(), this.getDefaultState().withProperty(AGE, age).withProperty(LEAVES, leaves).withProperty(STAGE, stage), 3);
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        int above = heightAbove(world, pos);
        int below = heightBelow(world, pos);
        BlockPos top = pos.up(above);
        return above + below + 1 < MAX_HEIGHT && world.getBlockState(top).getValue(STAGE) != 1 && world.isAirBlock(top.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        int above = heightAbove(world, pos);
        int below = heightBelow(world, pos);
        int height = above + below + 1;
        int steps = 1 + rand.nextInt(2);

        for (int i = 0; i < steps; i++) {
            BlockPos top = pos.up(above);
            IBlockState topState = world.getBlockState(top);
            if (height >= MAX_HEIGHT || topState.getValue(STAGE) == 1 || !world.isAirBlock(top.up())) {
                return;
            }
            growBamboo(topState, world, top, rand, height);
            above++;
            height++;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public enum Leaves implements IStringSerializable {
        NONE, SMALL, LARGE;

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }
    }
}
