package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockBambooSapling extends Block implements IGrowable {

    public static final SoundType SAPLING = new SoundType(1.0F, 1.0F, ModSounds.BLOCK_BAMBOO_SAPLING_BREAK, ModSounds.BLOCK_BAMBOO_STEP,
            ModSounds.BLOCK_BAMBOO_SAPLING_PLACE, ModSounds.BLOCK_BAMBOO_SAPLING_HIT, ModSounds.BLOCK_BAMBOO_FALL);

    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.5D, 0.75D);

    public BlockBambooSapling() {
        super(Material.PLANTS);
        this.setTickRandomly(true);
        this.setHardness(0.0F);
        this.setSoundType(SAPLING);
    }

    @Override
    public Block.EnumOffsetType getOffsetType() {
        return Block.EnumOffsetType.XZ;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        Vec3d offset = state.getOffset(source, pos);
        return BOX.offset(offset.x, offset.y, offset.z);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
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
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.get("bamboo");
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(ModItems.get("bamboo"));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && BlockBamboo.canPlantOn(world.getBlockState(pos.down()));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!BlockBamboo.canPlantOn(world.getBlockState(pos.down()))) {
            world.destroyBlock(pos, true);
        } else if (world.getBlockState(pos.up()).getBlock() instanceof BlockBamboo) {
            // A stalk grew on top: this is part of it now
            world.setBlockState(pos, ModBlocks.get("bamboo").getDefaultState(), 2);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (rand.nextInt(3) == 0 && world.isAirBlock(pos.up()) && world.getLightFromNeighbors(pos.up()) >= 9) {
            this.grow(world, rand, pos, state);
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return world.isAirBlock(pos.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        world.setBlockState(pos.up(), ModBlocks.get("bamboo").getDefaultState(), 3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
