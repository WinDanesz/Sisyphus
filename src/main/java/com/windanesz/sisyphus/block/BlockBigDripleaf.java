package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockBigDripleaf extends Block implements IGrowable {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<Tilt> TILT = PropertyEnum.create("tilt", Tilt.class);
    private static final int MAX_HEIGHT = 5;
    private static final AxisAlignedBB FLAT = new AxisAlignedBB(0.0D, 11 / 16.0D, 0.0D, 1.0D, 15 / 16.0D, 1.0D);
    private static final AxisAlignedBB LEANING = new AxisAlignedBB(0.0D, 11 / 16.0D, 0.0D, 1.0D, 13 / 16.0D, 1.0D);
    public BlockBigDripleaf() {
        super(Material.PLANTS);
        this.setHardness(0.1F);
        this.setSoundType(ModSounds.type("big_dripleaf"));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TILT, Tilt.NONE));
    }

    public static boolean isSoil(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.CLAY || block == Blocks.MYCELIUM || block == Blocks.FARMLAND
                || block instanceof BlockMoss || block == ModBlocks.get("rooted_dirt");
    }

    public static boolean canStayOn(IBlockState below) {
        return below.getBlock() instanceof BlockBigDripleafStem || below.getBlock() instanceof BlockBigDripleaf || isSoil(below);
    }

    private static int height(World world, BlockPos pos) {
        int height = 1;
        BlockPos at = pos.down();
        while (world.getBlockState(at).getBlock() instanceof BlockBigDripleafStem) {
            height++;
            at = at.down();
        }
        return height;
    }

    public static void place(World world, BlockPos pos, EnumFacing facing) {
        Block stem = ModBlocks.get("big_dripleaf_stem");
        Block leaf = ModBlocks.get("big_dripleaf");
        if (stem == Blocks.AIR || leaf == Blocks.AIR) {
            return;
        }
        world.setBlockState(pos, stem.getDefaultState().withProperty(BlockBigDripleafStem.FACING, facing), 2);
        world.setBlockState(pos.up(), leaf.getDefaultState().withProperty(FACING, facing), 3);
    }

    public static Item item() {
        return Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, "big_dripleaf"));
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        RayTraceResult hit = event.getRayTraceResult();
        World world = event.getEntity().world;
        if (!Settings.plants.enableDripleaf || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || world.isRemote) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockBigDripleaf) {
            ((BlockBigDripleaf) state.getBlock()).onProjectileHit(world, pos, state);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, TILT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3)).withProperty(TILT, Tilt.values()[(meta >> 2) & 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | state.getValue(TILT).ordinal() << 2;
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
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer,
                                            EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(TILT) == Tilt.NONE || state.getValue(TILT) == Tilt.UNSTABLE ? FLAT : LEANING;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch (state.getValue(TILT)) {
            case FULL:
                return NULL_AABB;
            case PARTIAL:
                return LEANING;
            default:
                return FLAT;
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
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && canStayOn(world.getBlockState(pos.down()));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!canStayOn(world.getBlockState(pos.down()))) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            return;
        }
        Block above = world.getBlockState(pos.up()).getBlock();
        Block stem = ModBlocks.get("big_dripleaf_stem");
        if (stem != Blocks.AIR && (above == this || above == stem)) {
            world.setBlockState(pos, stem.getDefaultState().withProperty(BlockBigDripleafStem.FACING, state.getValue(FACING)), 3);
            return;
        }
        if (state.getValue(TILT) != Tilt.NONE && world.isBlockPowered(pos)) {
            this.setTilt(world, pos, state, Tilt.NONE, false);
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(this));
    }

    private void setTilt(World world, BlockPos pos, IBlockState state, Tilt tilt, boolean sound) {
        world.setBlockState(pos, state.withProperty(TILT, tilt), 3);
        if (sound) {
            world.playSound(null, pos, ModSounds.event(tilt == Tilt.NONE ? "block.big_dripleaf.tilt_up" : "block.big_dripleaf.tilt_down"), SoundCategory.BLOCKS,
                    1.0F, 1.0F);
        }
        if (tilt.getDelay() > 0) {
            world.scheduleUpdate(pos, this, tilt.getDelay());
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!world.isRemote && state.getValue(TILT) == Tilt.NONE && entity.onGround && entity.posY > pos.getY() + 11 / 16.0D - 1.0E-5D && !world.isBlockPowered(pos)) {
            this.setTilt(world, pos, state, Tilt.UNSTABLE, false);
        }
    }

    public void onProjectileHit(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote && state.getValue(TILT) != Tilt.FULL) {
            this.setTilt(world, pos, state, Tilt.FULL, true);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        switch (state.getValue(TILT)) {
            case UNSTABLE:
                this.setTilt(world, pos, state, Tilt.PARTIAL, true);
                break;
            case PARTIAL:
                this.setTilt(world, pos, state, Tilt.FULL, true);
                break;
            case FULL:
                this.setTilt(world, pos, state, Tilt.NONE, true);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return world.isAirBlock(pos.up()) && pos.getY() < 250 && height(world, pos) < MAX_HEIGHT;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        if (canGrow(world, pos, state, false)) {
            place(world, pos, state.getValue(FACING));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public enum Tilt implements IStringSerializable {
        NONE("none", -1), UNSTABLE("unstable", 10), PARTIAL("partial", 10), FULL("full", 100);

        private final String name;
        private final int delay;

        Tilt(String name, int delay) {
            this.name = name;
            this.delay = delay;
        }

        @Override
        public String getName() {
            return this.name;
        }

        public int getDelay() {
            return this.delay;
        }
    }
}
