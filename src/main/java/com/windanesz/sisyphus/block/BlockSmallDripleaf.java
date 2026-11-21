package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockSmallDripleaf extends Block implements IGrowable, IShearable {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF = PropertyEnum.create("half", BlockDoublePlant.EnumBlockHalf.class);

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(2 / 16.0D, 0.0D, 2 / 16.0D, 14 / 16.0D, 13 / 16.0D, 14 / 16.0D);

    public BlockSmallDripleaf() {
        super(Material.PLANTS);
        this.setHardness(0.0F);
        this.setSoundType(ModSounds.type("small_dripleaf"));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER));
    }

    public static boolean canGrowOn(IBlockState below) {
        return below.getBlock() == Blocks.CLAY || below.getBlock() instanceof BlockMoss;
    }

    private static boolean valid(IBlockAccess world, BlockPos pos, IBlockState state) {
        IBlockState below = world.getBlockState(pos.down());
        if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
            return below.getBlock() instanceof BlockSmallDripleaf && below.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER;
        }
        return canGrowOn(below);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HALF);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(HALF, (meta & 4) != 0 ? BlockDoublePlant.EnumBlockHalf.UPPER : BlockDoublePlant.EnumBlockHalf.LOWER);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER ? 4 : 0);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SHAPE;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
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
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        boolean lower = state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER;
        BlockPos other = lower ? pos.up() : pos.down();
        IBlockState otherState = world.getBlockState(other);
        if (!valid(world, pos, state) || !(otherState.getBlock() instanceof BlockSmallDripleaf)) {
            world.setBlockToAir(pos);
            if (otherState.getBlock() instanceof BlockSmallDripleaf) {
                world.setBlockToAir(other);
            }
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        BlockPos lower = state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER ? pos : pos.down();
        IBlockState lowerState = world.getBlockState(lower);
        if (lowerState.getBlock() == this) {
            world.setBlockToAir(lower.up());
            BlockBigDripleaf.place(world, lower, lowerState.getValue(FACING));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
