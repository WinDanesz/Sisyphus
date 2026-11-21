package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.tileentity.TileEntityDecoratedPot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockDecoratedPot extends BlockContainer {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(1.0D / 16.0D, 0.0D, 1.0D / 16.0D, 15.0D / 16.0D, 1.0D, 15.0D / 16.0D);

    public BlockDecoratedPot() {
        super(Material.CLAY, MapColor.RED_STAINED_HARDENED_CLAY);
        this.setHardness(0.0F);
        this.setSoundType(ModSounds.type("decorated_pot"));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    public static boolean breaksPots(ItemStack stack) {
        Item item = stack.getItem();
        return (item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemHoe)
                && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0;
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!Settings.archaeology.enableArchaeology) {
            return;
        }
        RayTraceResult hit = event.getRayTraceResult();
        Entity projectile = event.getEntity();
        World world = projectile.world;
        if (world.isRemote || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || !world.getGameRules().getBoolean("mobGriefing")) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockDecoratedPot)) {
            return;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDecoratedPot) {
            ((TileEntityDecoratedPot) tile).setCracked(true);
        }
        world.destroyBlock(pos, true);
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
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        // The front faces the one who places it
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityDecoratedPot();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
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

    // Drops: the tile entity has to be there when they are worked out, so the block is removed after the harvest, as the flower pot does

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityDecoratedPot)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        TileEntityDecoratedPot pot = (TileEntityDecoratedPot) tile;
        ItemStack held = player.getHeldItem(hand);
        ItemStack inside = pot.getTheItem();
        if (!held.isEmpty() && (inside.isEmpty() || (TileEntityDecoratedPot.canStack(inside, held) && inside.getCount() < inside.getMaxStackSize()))) {
            pot.wobble(TileEntityDecoratedPot.WobbleStyle.POSITIVE);
            StatBase used = StatList.getObjectUseStats(held.getItem());
            if (used != null) {
                player.addStat(used);
            }
            ItemStack one;
            if (player.capabilities.isCreativeMode) {
                one = held.copy();
                one.setCount(1);
            } else {
                one = held.splitStack(1);
            }
            float fullness;
            if (inside.isEmpty()) {
                pot.setTheItem(one);
                fullness = (float) one.getCount() / one.getMaxStackSize();
            } else {
                inside.grow(1);
                pot.setTheItem(inside);
                fullness = (float) inside.getCount() / inside.getMaxStackSize();
            }
            world.playSound(null, pos, ModSounds.event("block.decorated_pot.insert"), SoundCategory.BLOCKS, 1.0F, 0.7F + 0.5F * fullness);
            if (world instanceof WorldServer) {
                // A puff of dust from the neck of the pot, as the dust plume of the game
                ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 7, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        } else {
            world.playSound(null, pos, ModSounds.event("block.decorated_pot.insert_fail"), SoundCategory.BLOCKS, 1.0F, 1.0F);
            pot.wobble(TileEntityDecoratedPot.WobbleStyle.NEGATIVE);
        }
        return true;
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        TileEntity tile = world.getTileEntity(pos);
        return tile != null && tile.receiveClientEvent(id, param);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity tile, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, tile, tool);
        world.setBlockToAir(pos);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityDecoratedPot)) {
            drops.add(new ItemStack(this));
            return;
        }
        TileEntityDecoratedPot pot = (TileEntityDecoratedPot) tile;
        EntityPlayer harvester = harvesters.get();
        boolean shatters = pot.isCracked() || (harvester != null && breaksPots(harvester.getHeldItemMainhand()));
        if (shatters) {
            for (Item item : pot.getDecorations()) {
                drops.add(new ItemStack(item));
            }
            if (world instanceof World) {
                ((World) world).playSound(null, pos, ModSounds.event("block.decorated_pot.shatter"), SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            drops.add(TileEntityDecoratedPot.createPotItem(Item.getItemFromBlock(this), pot.getDecorations()));
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDecoratedPot) {
            InventoryHelper.dropInventoryItems(world, pos, (TileEntityDecoratedPot) tile);
            world.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityDecoratedPot)) {
            return new ItemStack(this);
        }
        TileEntityDecoratedPot pot = (TileEntityDecoratedPot) tile;
        return TileEntityDecoratedPot.createPotItem(Item.getItemFromBlock(this), pot.getDecorations());
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return Container.calcRedstone(world.getTileEntity(pos));
    }
}
