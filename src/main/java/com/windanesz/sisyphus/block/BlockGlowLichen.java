package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.tileentity.TileEntityGlowLichen;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
import java.util.Random;

public class BlockGlowLichen extends Block implements IGrowable, IShearable {

    private static final PropertyBool[] SIDES = new PropertyBool[6];

    static {
        for (EnumFacing side : EnumFacing.values()) {
            SIDES[side.getIndex()] = PropertyBool.create(side.getName());
        }
    }

    public BlockGlowLichen() {
        super(Material.VINE);
        this.setHardness(0.2F);
        this.setSoundType(SoundType.PLANT);
        this.setLightLevel(7.0F / 15.0F);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    private static AxisAlignedBB sideBox(EnumFacing side) {
        double t = 1 / 16.0D;
        switch (side) {
            case DOWN:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, t, 1.0D);
            case UP:
                return new AxisAlignedBB(0.0D, 1.0D - t, 0.0D, 1.0D, 1.0D, 1.0D);
            case NORTH:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, t);
            case SOUTH:
                return new AxisAlignedBB(0.0D, 0.0D, 1.0D - t, 1.0D, 1.0D, 1.0D);
            case WEST:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, t, 1.0D, 1.0D);
            default:
                return new AxisAlignedBB(1.0D - t, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        }
    }

    public static boolean supported(IBlockAccess world, BlockPos pos, EnumFacing side) {
        BlockPos support = pos.offset(side);
        return world.getBlockState(support).getBlockFaceShape(world, support, side.getOpposite()) == BlockFaceShape.SOLID;
    }

    /**
     * False if the face is already there or has nothing to attach to.
     */
    public static boolean addFace(World world, BlockPos pos, EnumFacing side) {
        Block lichen = ModBlocks.get("glow_lichen");
        if (lichen == Blocks.AIR || !supported(world, pos, side)) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == lichen) {
            TileEntity tile = world.getTileEntity(pos);
            if (!(tile instanceof TileEntityGlowLichen) || ((TileEntityGlowLichen) tile).has(side)) {
                return false;
            }
            ((TileEntityGlowLichen) tile).setFaces(((TileEntityGlowLichen) tile).getFaces() | 1 << side.getIndex());
            return true;
        }
        if (!world.isAirBlock(pos) && !state.getBlock().isReplaceable(world, pos)) {
            return false;
        }
        world.setBlockState(pos, lichen.getDefaultState(), 3);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityGlowLichen) {
            ((TileEntityGlowLichen) tile).setFaces(1 << side.getIndex());
        }
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SIDES);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityGlowLichen();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityGlowLichen) {
            for (EnumFacing side : EnumFacing.values()) {
                state = state.withProperty(SIDES[side.getIndex()], ((TileEntityGlowLichen) tile).has(side));
            }
        }
        return state;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        AxisAlignedBB box = null;
        if (tile instanceof TileEntityGlowLichen) {
            for (EnumFacing side : EnumFacing.values()) {
                if (((TileEntityGlowLichen) tile).has(side)) {
                    box = box == null ? sideBox(side) : box.union(sideBox(side));
                }
            }
        }
        return box == null ? sideBox(EnumFacing.DOWN) : box;
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
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityGlowLichen)) {
            return;
        }
        TileEntityGlowLichen lichen = (TileEntityGlowLichen) tile;
        int faces = lichen.getFaces();
        for (EnumFacing side : EnumFacing.values()) {
            if (lichen.has(side) && !supported(world, pos, side)) {
                faces &= ~(1 << side.getIndex());
            }
        }
        if (faces == 0) {
            world.setBlockToAir(pos);
        } else if (faces != lichen.getFaces()) {
            lichen.setFaces(faces);
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
        TileEntity tile = world.getTileEntity(pos);
        int count = tile instanceof TileEntityGlowLichen ? Math.max(1, ((TileEntityGlowLichen) tile).count()) : 1;
        return Collections.singletonList(new ItemStack(this, count));
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
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityGlowLichen)) {
            return;
        }
        TileEntityGlowLichen lichen = (TileEntityGlowLichen) tile;
        for (int attempt = 0; attempt < 12; attempt++) {
            EnumFacing mine = EnumFacing.byIndex(rand.nextInt(6));
            if (!lichen.has(mine)) {
                continue;
            }
            EnumFacing toward = EnumFacing.byIndex(rand.nextInt(6));
            if (toward == mine || toward == mine.getOpposite()) {
                // into the corner of the same block, or the other side of it
                if (addFace(world, pos, rand.nextBoolean() ? toward : EnumFacing.byIndex(rand.nextInt(6)))) {
                    return;
                }
                continue;
            }
            if (addFace(world, pos.offset(toward), mine)) {
                return;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
