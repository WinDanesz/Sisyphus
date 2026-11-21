package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.ai.EntityAIHideFromBell;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.tileentity.TileEntityBell;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockBell extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum<Attachment> ATTACHMENT = PropertyEnum.create("attachment", Attachment.class);
    public BlockBell() {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ATTACHMENT, Attachment.FLOOR));
        this.setHardness(5.0F);
        this.setSoundType(SoundType.ANVIL);
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    private static boolean sturdy(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return world.getBlockState(pos).isSideSolid(world, pos, face);
    }

    @SubscribeEvent
    public static void onJoinWorld(EntityJoinWorldEvent event) {
        if (Settings.functionalBlocks.enableBell && !event.getWorld().isRemote && event.getEntity() instanceof EntityVillager) {
            EntityAIHideFromBell.addTo((EntityVillager) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        RayTraceResult hit = event.getRayTraceResult();
        Entity projectile = event.getEntity();
        if (!Settings.functionalBlocks.enableBell || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || projectile.world.isRemote) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        IBlockState state = projectile.world.getBlockState(pos);
        if (state.getBlock() instanceof BlockBell) {
            ((BlockBell) state.getBlock()).ring(projectile.world, pos, hit.sideHit.getAxis().isHorizontal() ? hit.sideHit : state.getValue(BlockBell.FACING));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ATTACHMENT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3)).withProperty(ATTACHMENT, Attachment.values()[(meta >> 2) & 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(ATTACHMENT).ordinal() << 2);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBell();
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        boolean alongX = state.getValue(FACING).getAxis() == EnumFacing.Axis.X;
        switch (state.getValue(ATTACHMENT)) {
            case CEILING:
                return new AxisAlignedBB(0.25D, 0.25D, 0.25D, 0.75D, 1.0D, 0.75D);
            case DOUBLE_WALL:
                return alongX ? new AxisAlignedBB(0.0D, 0.25D, 0.25D, 1.0D, 1.0D, 0.75D) : new AxisAlignedBB(0.25D, 0.25D, 0.0D, 0.75D, 1.0D, 1.0D);
            case SINGLE_WALL:
                switch (state.getValue(FACING)) {
                    case NORTH:
                        return new AxisAlignedBB(0.25D, 0.25D, 0.0D, 0.75D, 1.0D, 0.6875D);
                    case SOUTH:
                        return new AxisAlignedBB(0.25D, 0.25D, 0.3125D, 0.75D, 1.0D, 1.0D);
                    case EAST:
                        return new AxisAlignedBB(0.3125D, 0.25D, 0.25D, 1.0D, 1.0D, 0.75D);
                    default:
                        return new AxisAlignedBB(0.0D, 0.25D, 0.25D, 0.6875D, 1.0D, 0.75D);
                }
            default:
                // The frame of a floor bell runs across the way it faces
                return alongX ? new AxisAlignedBB(0.25D, 0.0D, 0.0D, 0.75D, 0.9375D, 1.0D) : new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 0.9375D, 0.75D);
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            return this.getDefaultState().withProperty(ATTACHMENT, facing == EnumFacing.DOWN ? Attachment.CEILING : Attachment.FLOOR)
                    .withProperty(FACING, placer.getHorizontalFacing());
        }
        // On a wall: between two walls when there is one on each side along the axis that was clicked
        boolean both = sturdy(world, pos.offset(facing), facing.getOpposite()) && sturdy(world, pos.offset(facing.getOpposite()), facing);
        return this.getDefaultState().withProperty(FACING, facing.getOpposite())
                .withProperty(ATTACHMENT, both ? Attachment.DOUBLE_WALL : Attachment.SINGLE_WALL);
    }

    private boolean isSupported(IBlockAccess world, BlockPos pos, IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        switch (state.getValue(ATTACHMENT)) {
            case CEILING:
                return sturdy(world, pos.up(), EnumFacing.DOWN);
            case FLOOR:
                return sturdy(world, pos.down(), EnumFacing.UP);
            case SINGLE_WALL:
                return sturdy(world, pos.offset(facing), facing.getOpposite());
            default:
                return sturdy(world, pos.offset(facing), facing.getOpposite()) || sturdy(world, pos.offset(facing.getOpposite()), facing);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!this.isSupported(world, pos, state)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            return;
        }
        // A bell rings when it gets power
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBell && !world.isRemote) {
            TileEntityBell bell = (TileEntityBell) tile;
            boolean powered = world.isBlockPowered(pos);
            if (powered && !bell.wasPowered()) {
                this.ring(world, pos, state.getValue(FACING));
            }
            bell.setWasPowered(powered);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            this.ring(world, pos, facing.getAxis() == EnumFacing.Axis.Y ? state.getValue(FACING) : facing);
        }
        return true;
    }

    public void ring(World world, BlockPos pos, EnumFacing direction) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBell && !world.isRemote) {
            ((TileEntityBell) tile).onHit(direction);
            world.playSound(null, pos, ModSounds.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2.0F, 1.0F);
            if (Settings.functionalBlocks.bellSendsVillagersIndoors) {
                EntityAIHideFromBell.alarm(world, pos);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public enum Attachment implements IStringSerializable {
        FLOOR, CEILING, SINGLE_WALL, DOUBLE_WALL;

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }
    }
}
