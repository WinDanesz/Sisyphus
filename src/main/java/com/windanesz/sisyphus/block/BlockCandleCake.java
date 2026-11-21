package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.particle.ParticleSmallFlame;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockCandleCake extends Block {

    public static final PropertyBool LIT = PropertyBool.create("lit");

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(1 / 16.0D, 0.0D, 1 / 16.0D, 15 / 16.0D, 14 / 16.0D, 15 / 16.0D);
    private static final AxisAlignedBB CAKE = new AxisAlignedBB(1 / 16.0D, 0.0D, 1 / 16.0D, 15 / 16.0D, 8 / 16.0D, 15 / 16.0D);

    private final String candle;

    public BlockCandleCake(String candle) {
        super(Material.CAKE);
        this.candle = candle;
        this.setHardness(0.5F);
        this.setSoundType(SoundType.CLOTH);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LIT, false));
    }

    @Nullable
    public static IBlockState cakeWith(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock) || !(((ItemBlock) stack.getItem()).getBlock() instanceof BlockCandle)) {
            return null;
        }
        String name = ((ItemBlock) stack.getItem()).getBlock().getRegistryName().getPath();
        Block cake = ModBlocks.get(name + "_cake");
        return cake instanceof BlockCandleCake ? cake.getDefaultState() : null;
    }

    @SubscribeEvent
    public static void onUseCandleOnCake(PlayerInteractEvent.RightClickBlock event) {
        if (!Settings.buildingBlocks.enableCandles || event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != Blocks.CAKE || state.getValue(BlockCake.BITES) != 0) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        ItemStack held = event.getItemStack();
        IBlockState cake = cakeWith(held);
        if (cake == null || !player.canPlayerEdit(pos, event.getFace(), held)) {
            return;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, cake, 3);
            world.playSound(null, pos, cake.getBlock().getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LIT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(LIT, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    public Block candleBlock() {
        return ModBlocks.get(this.candle);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SHAPE;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return CAKE;
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
        return state.getValue(LIT) ? 3 : 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this.candleBlock());
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Items.CAKE);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!world.getBlockState(pos.down()).getMaterial().isSolid()) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
                                    float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (!state.getValue(LIT) && BlockCandle.lights(held)) {
            if (!world.isRemote) {
                world.playSound(null, pos, held.getItem() == Items.FLINT_AND_STEEL ? SoundEvents.ITEM_FLINTANDSTEEL_USE : SoundEvents.ITEM_FIRECHARGE_USE,
                        SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
                world.setBlockState(pos, state.withProperty(LIT, true), 11);
                if (held.getItem() == Items.FLINT_AND_STEEL) {
                    held.damageItem(1, player);
                } else if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
            }
            return true;
        }
        if (state.getValue(LIT) && held.isEmpty()) {
            if (!world.isRemote) {
                world.setBlockState(pos, state.withProperty(LIT, false), 11);
                world.playSound(null, pos, ModSounds.event("block.candle.extinguish"), SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }
        return this.eat(world, pos, player);
    }

    private boolean eat(World world, BlockPos pos, EntityPlayer player) {
        if (!player.canEat(false)) {
            return false;
        }
        if (!world.isRemote) {
            player.getFoodStats().addStats(2, 0.1F);
            world.setBlockState(pos, Blocks.CAKE.getDefaultState().withProperty(BlockCake.BITES, 1), 3);
            spawnAsEntity(world, pos, new ItemStack(Item.getItemFromBlock(this.candleBlock())));
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.0D;
        double z = pos.getZ() + 0.5D;
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmallFlame(world, x, y, z));
        if (rand.nextFloat() < 0.3F) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D);
        }
        if (rand.nextFloat() < 0.17F) {
            world.playSound(x, y, z, ModSounds.event("block.candle.ambient"), SoundCategory.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
