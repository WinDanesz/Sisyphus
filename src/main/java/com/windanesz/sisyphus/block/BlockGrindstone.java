package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.inventory.ModGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGrindstone extends Block {

    public static final PropertyEnum<Face> FACE = PropertyEnum.create("face", Face.class);
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    private static final AxisAlignedBB UPRIGHT = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);
    private static final AxisAlignedBB WALL_NORTH_SOUTH = new AxisAlignedBB(0.125D, 0.125D, 0.0D, 0.875D, 0.875D, 1.0D);
    private static final AxisAlignedBB WALL_EAST_WEST = new AxisAlignedBB(0.0D, 0.125D, 0.125D, 1.0D, 0.875D, 0.875D);
    public BlockGrindstone() {
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACE, Face.FLOOR).withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(2.0F);
        this.setResistance(6.0F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    private static EnumFacing attachedTo(IBlockState state) {
        switch (state.getValue(FACE)) {
            case CEILING:
                return EnumFacing.UP;
            case FLOOR:
                return EnumFacing.DOWN;
            default:
                return state.getValue(FACING).getOpposite();
        }
    }

    private static boolean isSupported(IBlockAccess world, BlockPos pos, IBlockState state) {
        EnumFacing side = attachedTo(state);
        BlockPos support = pos.offset(side);
        return world.getBlockState(support).isSideSolid(world, support, side.getOpposite());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACE, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3)).withProperty(FACE, Face.values()[Math.min((meta >> 2) & 3, 2)]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(FACE).ordinal() << 2);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(FACE) != Face.WALL) {
            return UPRIGHT;
        }
        return state.getValue(FACING).getAxis() == EnumFacing.Axis.Z ? WALL_NORTH_SOUTH : WALL_EAST_WEST;
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
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            return this.getDefaultState().withProperty(FACE, facing == EnumFacing.UP ? Face.FLOOR : Face.CEILING)
                    .withProperty(FACING, placer.getHorizontalFacing());
        }
        return this.getDefaultState().withProperty(FACE, Face.WALL).withProperty(FACING, facing);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos support = pos.offset(side);
            if (world.getBlockState(support).isSideSolid(world, support, side.getOpposite())) {
                return super.canPlaceBlockAt(world, pos);
            }
        }
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!isSupported(world, pos, state)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(SisyphusBackport.instance, ModGuiHandler.GRINDSTONE, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public enum Face implements IStringSerializable {
        FLOOR, WALL, CEILING;

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }
    }
}
