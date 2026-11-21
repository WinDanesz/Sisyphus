package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
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

public class BlockScaffolding extends Block {

    public static final PropertyInteger DISTANCE = PropertyInteger.create("distance", 0, 7);
    public static final PropertyBool BOTTOM = PropertyBool.create("bottom");

    public static final SoundType SCAFFOLDING = new SoundType(1.0F, 1.0F, ModSounds.BLOCK_SCAFFOLDING_BREAK, ModSounds.BLOCK_SCAFFOLDING_STEP,
            ModSounds.BLOCK_SCAFFOLDING_PLACE, ModSounds.BLOCK_SCAFFOLDING_HIT, ModSounds.BLOCK_SCAFFOLDING_FALL);

    private static final AxisAlignedBB FULL = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB TOP_PLATE = new AxisAlignedBB(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB BOTTOM_PLATE = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
    private static final AxisAlignedBB[] LEGS = {
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 0.125D), new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D),
            new AxisAlignedBB(0.0D, 0.0D, 0.875D, 0.125D, 1.0D, 1.0D), new AxisAlignedBB(0.875D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D)};

    public BlockScaffolding() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(DISTANCE, 7).withProperty(BOTTOM, false));
        this.setHardness(0.0F);
        this.setSoundType(SCAFFOLDING);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    /**
     * 0 on solid ground, one more than the lowest neighbour otherwise, 7 for no support.
     */
    public static int getDistance(IBlockAccess world, BlockPos pos) {
        BlockPos below = pos.down();
        IBlockState belowState = world.getBlockState(below);
        int distance = 7;
        if (belowState.getBlock() instanceof BlockScaffolding) {
            distance = belowState.getValue(DISTANCE);
        } else if (belowState.isSideSolid(world, below, EnumFacing.UP)) {
            return 0;
        }

        for (EnumFacing side : EnumFacing.HORIZONTALS) {
            IBlockState neighbour = world.getBlockState(pos.offset(side));
            if (neighbour.getBlock() instanceof BlockScaffolding) {
                distance = Math.min(distance, neighbour.getValue(DISTANCE) + 1);
                if (distance == 1) {
                    break;
                }
            }
        }
        return distance;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DISTANCE, BOTTOM);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DISTANCE, meta & 7).withProperty(BOTTOM, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DISTANCE) | (state.getValue(BOTTOM) ? 8 : 0);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, TOP_PLATE);
        for (AxisAlignedBB leg : LEGS) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, leg);
        }
        if (state.getValue(BOTTOM)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, BOTTOM_PLATE);
        }
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
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return false;
    }

    private boolean isBottom(IBlockAccess world, BlockPos pos, int distance) {
        return distance > 0 && !(world.getBlockState(pos.down()).getBlock() instanceof BlockScaffolding);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        int distance = getDistance(world, pos);
        return this.getDefaultState().withProperty(DISTANCE, distance).withProperty(BOTTOM, this.isBottom(world, pos, distance));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && getDistance(world, pos) < 7;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            world.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!world.isRemote) {
            world.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        int distance = getDistance(world, pos);
        IBlockState updated = state.withProperty(DISTANCE, distance).withProperty(BOTTOM, this.isBottom(world, pos, distance));

        if (distance == 7) {
            if (state.getValue(DISTANCE) == 7) {
                // Nothing holds it up any more: it falls
                EntityFallingBlock falling = new EntityFallingBlock(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, state);
                world.spawnEntity(falling);
            } else {
                world.destroyBlock(pos, true);
            }
        } else if (updated != state) {
            world.setBlockState(pos, updated, 3);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
