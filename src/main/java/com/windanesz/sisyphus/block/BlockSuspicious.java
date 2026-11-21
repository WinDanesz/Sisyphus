package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.tileentity.TileEntityBrushable;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockSuspicious extends BlockFalling implements ITileEntityProvider {

    public static final PropertyInteger DUSTED = PropertyInteger.create("dusted", 0, 3);

    private final IBlockState turnsInto;
    private final SoundEvent brushSound;
    private final SoundEvent brushCompletedSound;
    private final int dustColor;

    public BlockSuspicious(IBlockState turnsInto, int dustColor, SoundType sound, SoundEvent brushSound, SoundEvent brushCompletedSound) {
        super(turnsInto.getMaterial());
        this.turnsInto = turnsInto;
        this.brushSound = brushSound;
        this.brushCompletedSound = brushCompletedSound;
        this.dustColor = dustColor;
        this.setHardness(0.25F);
        this.setSoundType(sound);
        this.setHarvestLevel("shovel", 0);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(DUSTED, 0));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getDustColor(IBlockState state) {
        return this.dustColor;
    }

    public IBlockState getTurnsInto() {
        return this.turnsInto;
    }

    public SoundEvent getBrushSound() {
        return this.brushSound;
    }

    public SoundEvent getBrushCompletedSound() {
        return this.brushCompletedSound;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DUSTED);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DUSTED, Math.min(meta, 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DUSTED);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBrushable();
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBrushable) {
            ((TileEntityBrushable) tile).checkReset();
        }
        // Falls when there is nothing below it
        super.updateTick(world, pos, state, rand);
    }

    @Override
    protected void onStartFalling(EntityFallingBlock entity) {
        entity.shouldDropItem = false;
    }

    @Override
    public void onEndFalling(World world, BlockPos pos, IBlockState fallingState, IBlockState hitState) {
        // It breaks where it lands, as the item in it is lost. The block that was placed there has no item, so nothing is lost twice
        world.destroyBlock(pos, false);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        super.eventReceived(state, world, pos, id, param);
        TileEntity tile = world.getTileEntity(pos);
        return tile != null && tile.receiveClientEvent(id, param);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return this.turnsInto.getMapColor(world, pos);
    }
}
