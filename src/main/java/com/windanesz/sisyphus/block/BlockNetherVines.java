package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockNetherVines extends Block implements IGrowable {

    public static final int MAX_AGE = 15;
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, MAX_AGE);
    public static final PropertyBool TIP = PropertyBool.create("tip");

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);

    private final EnumFacing growth;

    public BlockNetherVines(boolean twisting) {
        super(Material.PLANTS);
        this.growth = twisting ? EnumFacing.UP : EnumFacing.DOWN;
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0).withProperty(TIP, true));
        this.setSoundType(ModSounds.type("weeping_vines"));
        this.setHardness(0.0F);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, TIP);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(TIP, world.getBlockState(pos.offset(this.growth)).getBlock() != this);
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
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }

    private boolean canStay(IBlockAccess world, BlockPos pos) {
        BlockPos support = pos.offset(this.growth.getOpposite());
        IBlockState state = world.getBlockState(support);
        return state.getBlock() == this || state.getBlockFaceShape(world, support, this.growth) == BlockFaceShape.SOLID;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && this.canStay(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!this.canStay(world, pos)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        // A vine that has nothing to hold it, as some were made before they were checked in the generation, is let go
        if (!this.canStay(world, pos)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            return;
        }
        int age = state.getValue(AGE);
        BlockPos next = pos.offset(this.growth);
        if (age < MAX_AGE && rand.nextInt(10) == 0 && this.canGrowInto(world, next)) {
            world.setBlockState(next, this.getDefaultState().withProperty(AGE, age + 1), 2);
        }
    }

    private boolean canGrowInto(World world, BlockPos pos) {
        return pos.getY() > 0 && pos.getY() < world.getHeight() - 1 && world.isAirBlock(pos);
    }

    private BlockPos tipOf(World world, BlockPos pos) {
        BlockPos tip = pos;
        while (world.getBlockState(tip.offset(this.growth)).getBlock() == this) {
            tip = tip.offset(this.growth);
        }
        return tip;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        BlockPos tip = this.tipOf(world, pos);
        return world.getBlockState(tip).getValue(AGE) < MAX_AGE && this.canGrowInto(world, tip.offset(this.growth));
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        BlockPos tip = this.tipOf(world, pos);
        int age = world.getBlockState(tip).getValue(AGE);
        for (int i = 1 + rand.nextInt(3); i > 0 && age < MAX_AGE; i--) {
            BlockPos next = tip.offset(this.growth);
            if (!this.canGrowInto(world, next)) {
                break;
            }
            age++;
            world.setBlockState(next, this.getDefaultState().withProperty(AGE, age), 2);
            tip = next;
        }
    }
}
