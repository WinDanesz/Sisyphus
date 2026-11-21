package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.client.particle.ParticleSmallFlame;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.tileentity.TileEntityCandle;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockCandle extends Block {

    public static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue",
            "brown", "green", "red", "black"};

    public static final String[] DYES = {"dyeWhite", "dyeOrange", "dyeMagenta", "dyeLightBlue", "dyeYellow", "dyeLime", "dyePink", "dyeGray", "dyeLightGray",
            "dyeCyan", "dyePurple", "dyeBlue", "dyeBrown", "dyeGreen", "dyeRed", "dyeBlack"};

    public static final PropertyInteger CANDLES = PropertyInteger.create("candles", 1, 4);
    public static final PropertyBool LIT = PropertyBool.create("lit");

    /**
     * The candle block names, joined with commas, or empty when all of them are the color of the block.
     */
    public static final IUnlistedProperty<String> CANDLE_COLORS = new IUnlistedProperty<String>() {
        @Override
        public String getName() {
            return "colors";
        }

        @Override
        public boolean isValid(String value) {
            return value != null;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public String valueToString(String value) {
            return value;
        }
    };

    private static final AxisAlignedBB[] SHAPES = {
            new AxisAlignedBB(7 / 16.0D, 0.0D, 7 / 16.0D, 9 / 16.0D, 6 / 16.0D, 9 / 16.0D),
            new AxisAlignedBB(5 / 16.0D, 0.0D, 6 / 16.0D, 11 / 16.0D, 6 / 16.0D, 9 / 16.0D),
            new AxisAlignedBB(5 / 16.0D, 0.0D, 6 / 16.0D, 10 / 16.0D, 6 / 16.0D, 11 / 16.0D),
            new AxisAlignedBB(5 / 16.0D, 0.0D, 5 / 16.0D, 11 / 16.0D, 6 / 16.0D, 11 / 16.0D)};

    private static final double[][][] FLAMES = {
            {{0.5D, 0.5D, 0.5D}},
            {{0.375D, 0.44D, 0.5D}, {0.625D, 0.5D, 0.44D}},
            {{0.5D, 0.313D, 0.625D}, {0.375D, 0.44D, 0.5D}, {0.56D, 0.5D, 0.44D}},
            {{0.44D, 0.313D, 0.56D}, {0.625D, 0.44D, 0.56D}, {0.375D, 0.44D, 0.375D}, {0.56D, 0.5D, 0.375D}}};

    public BlockCandle() {
        super(Material.CIRCUITS, MapColor.SAND);
        this.setHardness(0.1F);
        this.setSoundType(ModSounds.type("candle"));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CANDLES, 1).withProperty(LIT, false));
    }

    public static List<String> colors(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityCandle ? ((TileEntityCandle) tile).getCandles() : Collections.emptyList();
    }

    public static boolean canStandOn(IBlockAccess world, BlockPos pos) {
        BlockPos below = pos.down();
        return world.getBlockState(below).getBlockFaceShape(world, below, EnumFacing.UP) == BlockFaceShape.SOLID;
    }

    public static boolean lights(ItemStack stack) {
        return stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE;
    }

    private static void light(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack held) {
        if (world.isRemote) {
            return;
        }
        world.playSound(null, pos, held.getItem() == Items.FLINT_AND_STEEL ? SoundEvents.ITEM_FLINTANDSTEEL_USE : SoundEvents.ITEM_FIRECHARGE_USE,
                SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
        world.setBlockState(pos, state.withProperty(LIT, true), 11);
        if (held.getItem() == Items.FLINT_AND_STEEL) {
            held.damageItem(1, player);
        } else if (!player.capabilities.isCreativeMode) {
            held.shrink(1);
        }
    }

    public static void extinguish(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state.withProperty(LIT, false), 11);
        world.playSound(null, pos, ModSounds.event("block.candle.extinguish"), SoundCategory.BLOCKS, 1.0F, 1.0F);
        smoke(world, pos, FLAMES[Math.max(0, state.getValue(CANDLES) - 1)]);
    }

    private static void smoke(World world, BlockPos pos, double[][] flames) {
        for (double[] flame : flames) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + flame[0], pos.getY() + flame[1], pos.getZ() + flame[2], 0.0D, 0.1D, 0.0D);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{CANDLES, LIT}, new IUnlistedProperty[]{CANDLE_COLORS});
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityCandle();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState)) {
            return state;
        }
        List<String> colors = colors(world, pos);
        return ((IExtendedBlockState) state).withProperty(CANDLE_COLORS, colors.size() == state.getValue(CANDLES) ? String.join(",", colors) : "");
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(CANDLES, (meta & 3) + 1).withProperty(LIT, (meta & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CANDLES) - 1 | (state.getValue(LIT) ? 4 : 0);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SHAPES[state.getValue(CANDLES) - 1];
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SHAPES[state.getValue(CANDLES) - 1];
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
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(LIT) ? 3 * state.getValue(CANDLES) : 0;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return state.getValue(CANDLES);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<String> colors = colors(world, pos);
        if (colors.isEmpty()) {
            super.getDrops(drops, world, pos, state, fortune);
            return;
        }
        for (String name : colors.subList(0, Math.min(colors.size(), state.getValue(CANDLES)))) {
            Block candle = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
            drops.add(new ItemStack(candle instanceof BlockCandle ? candle : this));
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity tile, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, tile, stack);
        world.setBlockToAir(pos);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && canStandOn(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!canStandOn(world, pos)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
                                    float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (!state.getValue(LIT) && lights(held)) {
            light(world, pos, state, player, hand, held);
            return true;
        }
        if (state.getValue(LIT) && held.isEmpty()) {
            if (!world.isRemote) {
                extinguish(world, pos, state);
            }
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (!state.getValue(LIT)) {
            return;
        }
        if (rand.nextFloat() < 0.17F) {
            world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, ModSounds.event("block.candle.ambient"), SoundCategory.BLOCKS,
                    1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
        }
        for (double[] flame : FLAMES[state.getValue(CANDLES) - 1]) {
            double x = pos.getX() + flame[0];
            double y = pos.getY() + flame[1];
            double z = pos.getZ() + flame[2];
            Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmallFlame(world, x, y, z));
            if (rand.nextFloat() < 0.3F) {
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
