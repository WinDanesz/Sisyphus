package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
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

public class BlockHangingPlant extends Block implements IShearable {

    private static final AxisAlignedBB ROOTS = new AxisAlignedBB(2 / 16.0D, 10 / 16.0D, 2 / 16.0D, 14 / 16.0D, 1.0D, 14 / 16.0D);
    private static final AxisAlignedBB BLOSSOM = new AxisAlignedBB(2 / 16.0D, 13 / 16.0D, 2 / 16.0D, 14 / 16.0D, 1.0D, 14 / 16.0D);

    private final boolean shearsOnly;
    private final AxisAlignedBB shape;

    public BlockHangingPlant(String sound, boolean shearsOnly) {
        super(shearsOnly ? Material.VINE : Material.PLANTS);
        this.shearsOnly = shearsOnly;
        this.shape = shearsOnly ? ROOTS : BLOSSOM;
        this.setHardness(0.0F);
        this.setSoundType(ModSounds.type(sound));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    private static boolean hangsFromSomething(IBlockAccess world, BlockPos pos) {
        BlockPos above = pos.up();
        return world.getBlockState(above).getBlockFaceShape(world, above, EnumFacing.DOWN) == BlockFaceShape.SOLID;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return this.shape;
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
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && hangsFromSomething(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!hangsFromSomething(world, pos)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (!this.shearsOnly) {
            drops.add(new ItemStack(this));
        }
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return this.shearsOnly;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
