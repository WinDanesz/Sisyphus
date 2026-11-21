package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.item.ItemHangingSign;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.tileentity.TileEntityHangingSign;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockSign;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public abstract class BlockHangingSign extends BlockSign {

    private static final AxisAlignedBB BOARD_X = box(1, 0, 7, 15, 10, 9);
    private static final AxisAlignedBB BOARD_Z = box(7, 0, 1, 9, 10, 15);

    public final String wood;

    protected BlockHangingSign(String wood) {
        super();
        this.wood = wood;
        this.setHardness(1.0F);
        this.setSoundType(ModSounds.hangingSignType(wood));
        this.disableStats();
    }

    private static AxisAlignedBB box(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AxisAlignedBB(x1 / 16.0D, y1 / 16.0D, z1 / 16.0D, x2 / 16.0D, y2 / 16.0D, z2 / 16.0D);
    }

    public static boolean canHangUnder(IBlockAccess world, BlockPos above) {
        IBlockState state = world.getBlockState(above);
        Block block = state.getBlock();
        if (block instanceof BlockChain) {
            return state.getValue(BlockChain.AXIS) == EnumFacing.Axis.Y;
        }
        BlockFaceShape shape = state.getBlockFaceShape(world, above, EnumFacing.DOWN);
        return shape == BlockFaceShape.SOLID || shape == BlockFaceShape.CENTER || shape == BlockFaceShape.CENTER_BIG
                || shape == BlockFaceShape.CENTER_SMALL;
    }

    public static boolean canHangOnWall(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return canAttachTo(world, pos.offset(facing.rotateY()), facing.rotateYCCW(), facing)
                || canAttachTo(world, pos.offset(facing.rotateYCCW()), facing.rotateY(), facing);
    }

    private static boolean canAttachTo(IBlockAccess world, BlockPos pos, EnumFacing side, EnumFacing facing) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof Wall) {
            return state.getValue(Wall.FACING).getAxis() == facing.getAxis();
        }
        return state.getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID;
    }

    protected abstract boolean canStay(World world, BlockPos pos, IBlockState state);

    protected abstract boolean isChainingSide(IBlockState state, EnumFacing side);

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!this.canStay(world, pos, state)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
        super.neighborChanged(state, world, pos, block, fromPos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX,
                                    float hitY, float hitZ) {
        if (player.getHeldItem(hand).getItem() instanceof ItemHangingSign && this.isChainingSide(state, facing)) {
            return false;
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityHangingSign();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        Item item = ModItems.getOrNull(this.wood + "_hanging_sign");
        return item == null ? Items.AIR : item;
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        Item item = ModItems.getOrNull(this.wood + "_hanging_sign");
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN ? BlockFaceShape.CENTER : BlockFaceShape.UNDEFINED;
    }

    public static class Ceiling extends BlockHangingSign {

        public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);
        private static final AxisAlignedBB TURNED = box(3, 0, 3, 13, 16, 13);

        public Ceiling(String wood) {
            super(wood);
            this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION, 0));
        }

        @Nullable
        public static EnumFacing facingOf(int rotation) {
            return rotation % 4 == 0 ? EnumFacing.byHorizontalIndex(rotation / 4) : null;
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, ROTATION);
        }

        @Override
        public IBlockState getStateFromMeta(int meta) {
            return this.getDefaultState().withProperty(ROTATION, meta & 15);
        }

        @Override
        public int getMetaFromState(IBlockState state) {
            return state.getValue(ROTATION);
        }

        @Override
        public IBlockState withRotation(IBlockState state, Rotation rotation) {
            return state.withProperty(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
        }

        @Override
        public IBlockState withMirror(IBlockState state, Mirror mirror) {
            return state.withProperty(ROTATION, mirror.mirrorRotation(state.getValue(ROTATION), 16));
        }

        @Override
        public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
            switch (state.getValue(ROTATION)) {
                case 0:
                case 8:
                    return BOARD_X;
                case 4:
                case 12:
                    return BOARD_Z;
                default:
                    return TURNED;
            }
        }

        @Override
        protected boolean canStay(World world, BlockPos pos, IBlockState state) {
            return canHangUnder(world, pos.up());
        }

        @Override
        protected boolean isChainingSide(IBlockState state, EnumFacing side) {
            return side == EnumFacing.DOWN;
        }
    }

    public static class Wall extends BlockHangingSign {

        public static final PropertyDirection FACING = BlockHorizontal.FACING;
        private static final AxisAlignedBB PLANK_X = box(0, 14, 6, 16, 16, 10);
        private static final AxisAlignedBB PLANK_Z = box(6, 14, 0, 10, 16, 16);
        private static final AxisAlignedBB SHAPE_X = box(0, 0, 6, 16, 16, 10);
        private static final AxisAlignedBB SHAPE_Z = box(6, 0, 0, 10, 16, 16);

        public Wall(String wood) {
            super(wood);
            this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        }

        private static boolean alongX(IBlockState state) {
            return state.getValue(FACING).getAxis() == EnumFacing.Axis.Z;
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
            return alongX(state) ? SHAPE_X : SHAPE_Z;
        }

        @Nullable
        @Override
        public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
            return alongX(state) ? PLANK_X : PLANK_Z;
        }

        @Override
        public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> boxes,
                                          @Nullable Entity entity, boolean isActualState) {
            addCollisionBoxToList(pos, entityBox, boxes, alongX(state) ? PLANK_X : PLANK_Z);
        }

        @Override
        public boolean isPassable(IBlockAccess world, BlockPos pos) {
            return false;
        }

        @Override
        protected boolean canStay(World world, BlockPos pos, IBlockState state) {
            return canHangOnWall(world, pos, state.getValue(FACING));
        }

        @Override
        protected boolean isChainingSide(IBlockState state, EnumFacing side) {
            return side.getAxis() != state.getValue(FACING).getAxis();
        }
    }
}
