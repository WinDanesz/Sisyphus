package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.particle.ParticleSoulFlame;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
public class BlockSoulFire extends Block {

    public BlockSoulFire() {
        super(Material.FIRE);
        this.setHardness(0.0F);
        this.setLightLevel(10.0F / 15.0F);
        this.setSoundType(SoundType.CLOTH);
    }

    public static boolean canBurnOn(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.SOUL_SAND || ModBlocks.isSoulSoil(block);
    }

    @SubscribeEvent
    public static void onLightSoulFire(PlayerInteractEvent.RightClickBlock event) {
        if (!Settings.functionalBlocks.enableSoulFire) {
            return;
        }
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        if ((item != Items.FLINT_AND_STEEL && item != Items.FIRE_CHARGE) || event.getFace() != EnumFacing.UP) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        BlockPos above = pos.up();
        if (!BlockSoulFire.canBurnOn(world.getBlockState(pos)) || !world.isAirBlock(above)) {
            return;
        }
        if (!world.isRemote) {
            world.playSound(null, above, item == Items.FLINT_AND_STEEL ? SoundEvents.ITEM_FLINTANDSTEEL_USE : SoundEvents.ITEM_FIRECHARGE_USE,
                    SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
            world.setBlockState(above, ModBlocks.get("soul_fire").getDefaultState(), 11);
            if (item == Items.FLINT_AND_STEEL) {
                stack.damageItem(1, event.getEntityPlayer());
            } else if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && canBurnOn(world.getBlockState(pos.down()));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!canBurnOn(world.getBlockState(pos.down()))) {
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!entity.isImmuneToFire()) {
            entity.attackEntityFrom(DamageSource.IN_FIRE, 2.0F);
            entity.setFire(8);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
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
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Nullable
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(24) == 0) {
            world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS,
                    1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
        }
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + rand.nextDouble();
            double y = pos.getY() + rand.nextDouble() * 0.5D + 0.5D;
            double z = pos.getZ() + rand.nextDouble();
            Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSoulFlame(world, x, y, z, 0.0D, 0.0D, 0.0D));
        }
    }
}
