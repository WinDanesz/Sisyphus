package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.worldgen.WorldGenAzaleaTree;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockAzalea extends Block implements IGrowable {

    private static final AxisAlignedBB LEAVES = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB STEM = new AxisAlignedBB(6 / 16.0D, 0.0D, 6 / 16.0D, 10 / 16.0D, 0.5D, 10 / 16.0D);

    public BlockAzalea(String sound) {
        super(Material.PLANTS);
        this.setHardness(0.0F);
        this.setSoundType(ModSounds.type(sound));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public static boolean canStandOn(IBlockState below) {
        Block block = below.getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.MYCELIUM || block == Blocks.CLAY || block == Blocks.FARMLAND
                || block == ModBlocks.get("rooted_dirt") || block instanceof BlockMoss;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && canStandOn(world.getBlockState(pos.down()));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!canStandOn(world.getBlockState(pos.down()))) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> boxes, @Nullable Entity entity,
                                      boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, boxes, LEAVES);
        addCollisionBoxToList(pos, entityBox, boxes, STEM);
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
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return rand.nextFloat() < 0.45F;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        WorldGenAzaleaTree.generate(world, rand, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
