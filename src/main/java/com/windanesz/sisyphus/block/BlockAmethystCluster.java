package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockAmethystCluster extends Block {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    private final int height;
    private final int inset;

    public BlockAmethystCluster(String sound, int height, int inset, int light) {
        super(Material.ROCK, MapColor.PURPLE);
        this.height = height;
        this.inset = inset;
        this.setHardness(1.5F);
        this.setResistance(1.5F);
        this.setSoundType(ModSounds.type(sound));
        this.setLightLevel(light / 15.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
    }

    private static boolean supported(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos support = pos.offset(facing.getOpposite());
        return world.getBlockState(support).getBlockFaceShape(world, support, facing) == BlockFaceShape.SOLID;
    }

    private static boolean isPickaxe(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem().getToolClasses(stack).contains("pickaxe");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(Math.min(meta, 5)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
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
        double h = this.height / 16.0D;
        double a = this.inset / 16.0D;
        double b = 1.0D - a;
        switch (state.getValue(FACING)) {
            case DOWN:
                return new AxisAlignedBB(a, 1.0D - h, a, b, 1.0D, b);
            case NORTH:
                return new AxisAlignedBB(a, a, 1.0D - h, b, b, 1.0D);
            case SOUTH:
                return new AxisAlignedBB(a, a, 0.0D, b, b, h);
            case EAST:
                return new AxisAlignedBB(0.0D, a, a, h, b, b);
            case WEST:
                return new AxisAlignedBB(1.0D - h, a, a, 1.0D, b, b);
            default:
                return new AxisAlignedBB(a, 0.0D, a, b, h, b);
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return this.getBoundingBox(state, world, pos);
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
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return super.canPlaceBlockAt(world, pos) && supported(world, pos, side);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer,
                                            EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!supported(world, pos, state.getValue(FACING))) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (this != ModBlocks.get("amethyst_cluster")) {
            return;
        }
        EntityPlayer harvester = harvesters.get();
        int count = 2;
        if (harvester != null && isPickaxe(harvester.getHeldItemMainhand())) {
            count = 4;
            if (fortune > 0) {
                Random rand = world instanceof World ? ((World) world).rand : new Random();
                count *= Math.max(1, rand.nextInt(fortune + 2));
            }
        }
        drops.add(new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("sisyphus", "amethyst_shard")), count));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
