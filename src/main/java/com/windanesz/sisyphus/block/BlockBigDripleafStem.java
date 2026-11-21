package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBigDripleafStem extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockBigDripleafStem() {
        super(Material.PLANTS);
        this.setHardness(0.1F);
        this.setSoundType(ModSounds.type("big_dripleaf"));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
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
        switch (state.getValue(FACING)) {
            case SOUTH:
                return new AxisAlignedBB(5 / 16.0D, 0.0D, 1 / 16.0D, 11 / 16.0D, 1.0D, 7 / 16.0D);
            case WEST:
                return new AxisAlignedBB(9 / 16.0D, 0.0D, 5 / 16.0D, 15 / 16.0D, 1.0D, 11 / 16.0D);
            case EAST:
                return new AxisAlignedBB(1 / 16.0D, 0.0D, 5 / 16.0D, 7 / 16.0D, 1.0D, 11 / 16.0D);
            default:
                return new AxisAlignedBB(5 / 16.0D, 0.0D, 9 / 16.0D, 11 / 16.0D, 1.0D, 15 / 16.0D);
        }
    }

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
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && BlockBigDripleaf.canStayOn(world.getBlockState(pos.down()));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!BlockBigDripleaf.canStayOn(world.getBlockState(pos.down()))) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            return;
        }
        Block above = world.getBlockState(pos.up()).getBlock();
        Block leaf = ModBlocks.get("big_dripleaf");
        if (leaf != Blocks.AIR && above != this && above != leaf) {
            world.setBlockState(pos, leaf.getDefaultState().withProperty(BlockBigDripleaf.FACING, state.getValue(FACING)), 3);
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(BlockBigDripleaf.item()));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(BlockBigDripleaf.item());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
