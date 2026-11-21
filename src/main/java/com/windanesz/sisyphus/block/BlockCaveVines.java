package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockCaveVines extends Block implements IGrowable {

    public static final PropertyBool BERRIES = PropertyBool.create("berries");
    public static final int MAX_LENGTH = 25;

    private static final AxisAlignedBB TIP = new AxisAlignedBB(4 / 16.0D, 9 / 16.0D, 4 / 16.0D, 12 / 16.0D, 1.0D, 12 / 16.0D);
    private static final AxisAlignedBB BODY = new AxisAlignedBB(1 / 16.0D, 0.0D, 1 / 16.0D, 15 / 16.0D, 1.0D, 15 / 16.0D);

    private final boolean head;

    public BlockCaveVines(boolean head) {
        super(Material.PLANTS);
        this.head = head;
        this.setHardness(0.0F);
        this.setSoundType(ModSounds.type("cave_vines"));
        this.setTickRandomly(head);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BERRIES, false));
    }

    private static boolean isVine(Block block) {
        return block instanceof BlockCaveVines;
    }

    public static boolean canStay(IBlockAccess world, BlockPos pos) {
        BlockPos above = pos.up();
        IBlockState state = world.getBlockState(above);
        return isVine(state.getBlock()) || state.getBlockFaceShape(world, above, EnumFacing.DOWN) == BlockFaceShape.SOLID;
    }

    private static int length(World world, BlockPos pos) {
        int length = 1;
        BlockPos at = pos.up();
        while (length < MAX_LENGTH && isVine(world.getBlockState(at).getBlock())) {
            length++;
            at = at.up();
        }
        return length;
    }

    private static Item berries() {
        return Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, "glow_berries"));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BERRIES);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BERRIES, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BERRIES) ? 1 : 0;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return this.head ? TIP : BODY;
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
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(BERRIES) ? 14 : 0;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && canStay(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!canStay(world, pos)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote || !this.head || rand.nextInt(10) != 0) {
            return;
        }
        BlockPos below = pos.down();
        Block plant = ModBlocks.get("cave_vines_plant");
        if (plant == Blocks.AIR || !world.isAirBlock(below) || length(world, pos) >= MAX_LENGTH) {
            return;
        }
        world.setBlockState(pos, plant.getDefaultState().withProperty(BERRIES, state.getValue(BERRIES)), 3);
        world.setBlockState(below, this.getDefaultState().withProperty(BERRIES, rand.nextFloat() < 0.11F), 3);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
                                    float hitZ) {
        if (!state.getValue(BERRIES)) {
            return false;
        }
        if (!world.isRemote) {
            spawnAsEntity(world, pos, new ItemStack(berries()));
            world.playSound(null, pos, ModSounds.event("block.cave_vines.pick_berries"), SoundCategory.BLOCKS, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);
            world.setBlockState(pos, state.withProperty(BERRIES, false), 3);
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (state.getValue(BERRIES)) {
            drops.add(new ItemStack(berries()));
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(berries());
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return !state.getValue(BERRIES);
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state.withProperty(BERRIES, true), 3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
