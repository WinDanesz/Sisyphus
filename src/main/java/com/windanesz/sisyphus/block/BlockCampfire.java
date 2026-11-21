package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.tileentity.TileEntityCampfire;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockCampfire extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool LIT = PropertyBool.create("lit");

    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.4375D, 1.0D);

    private final boolean soul;

    public BlockCampfire() {
        this(false);
    }

    public BlockCampfire(boolean soul) {
        super(Material.WOOD);
        this.soul = soul;
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(LIT, true));
        this.setHardness(2.0F);
        this.setSoundType(SoundType.WOOD);
        this.setLightLevel(1.0F);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public static void douse(World world, BlockPos pos, IBlockState state) {
        if (!state.getValue(LIT)) {
            return;
        }
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.setBlockState(pos, state.withProperty(LIT, false), 3);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCampfire) {
            ((TileEntityCampfire) tile).dropItems();
        }
    }

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent.Arrow event) {
        Entity arrow = event.getEntity();
        RayTraceResult hit = event.getRayTraceResult();
        World world = arrow.world;
        if (world.isRemote || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || !arrow.isBurning() || !Settings.gameplay.enableCampfireArrows) {
            return;
        }
        IBlockState state = world.getBlockState(hit.getBlockPos());
        if (state.getBlock() instanceof BlockCampfire && !state.getValue(BlockCampfire.LIT)) {
            world.setBlockState(hit.getBlockPos(), state.withProperty(BlockCampfire.LIT, true), 11);
            world.playSound(null, hit.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LIT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3)).withProperty(LIT, (meta & 4) == 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(LIT) ? 0 : 4);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(LIT) ? (this.soul ? 10 : 15) : 0;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing()).withProperty(LIT, true);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOX;
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCampfire();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        Item item = stack.getItem();
        boolean lit = state.getValue(LIT);

        // Relight with flint and steel or a fire charge
        if (!lit && (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE)) {
            if (!world.isRemote) {
                world.playSound(null, pos, item == Items.FLINT_AND_STEEL ? SoundEvents.ITEM_FLINTANDSTEEL_USE : SoundEvents.ITEM_FIRECHARGE_USE,
                        SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
                world.setBlockState(pos, state.withProperty(LIT, true), 11);
                if (item == Items.FLINT_AND_STEEL) {
                    stack.damageItem(1, player);
                } else if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
            return true;
        }

        // Put out with a water bucket
        if (lit && item == Items.WATER_BUCKET) {
            if (!world.isRemote) {
                douse(world, pos, state);
                if (!player.capabilities.isCreativeMode) {
                    player.setHeldItem(hand, new ItemStack(Items.BUCKET));
                }
            }
            return true;
        }

        // Put food on it
        if (!stack.isEmpty() && !TileEntityCampfire.getResult(stack).isEmpty()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityCampfire) {
                if (!world.isRemote && ((TileEntityCampfire) tile).addItem(stack) && !player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        IBlockState state = world.getBlockState(pos);
        if (state.getValue(LIT) && entity instanceof EntityLivingBase && !entity.isSneaking() && !entity.isImmuneToFire()) {
            entity.attackEntityFrom(DamageSource.IN_FIRE, this.soul ? 2.0F : 1.0F);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCampfire) {
            ((TileEntityCampfire) tile).dropItems();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (this.soul) {
            Block soil = ModBlocks.get("soul_soil");
            drops.add(soil == Blocks.AIR ? new ItemStack(Blocks.SOUL_SAND) : new ItemStack(soil));
        } else {
            drops.add(new ItemStack(Items.COAL, 2, 1));
        }
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (!state.getValue(LIT)) {
            return;
        }
        if (rand.nextInt(10) == 0) {
            world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, ModSounds.BLOCK_CAMPFIRE_CRACKLE,
                    SoundCategory.BLOCKS, 0.5F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.6F, false);
        }
        if (rand.nextInt(5) == 0) {
            // Sparks. The smoke comes from the tile entity
            for (int i = 0; i < rand.nextInt(1) + 1; i++) {
                world.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        rand.nextFloat() / 2.0F, 5.0E-5D, rand.nextFloat() / 2.0F);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
