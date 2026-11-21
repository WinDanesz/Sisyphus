package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityAxolotl;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemAxolotlBucket extends ItemBucket {

    private ItemStack emptying = ItemStack.EMPTY;

    public ItemAxolotlBucket() {
        super(Blocks.FLOWING_WATER);
        this.setContainerItem(Items.BUCKET);
    }

    public static void registerDispenserBehavior() {
        Item bucket = Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, "axolotl_bucket"));
        if (!(bucket instanceof ItemAxolotlBucket)) {
            return;
        }
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(bucket, new BehaviorDefaultDispenseItem() {
            private final BehaviorDefaultDispenseItem drop = new BehaviorDefaultDispenseItem();

            @Override
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                BlockPos front = source.getBlockPos().offset(facing);
                if (((ItemAxolotlBucket) stack.getItem()).release(null, source.getWorld(), front, stack)) {
                    return new ItemStack(Items.BUCKET);
                }
                return this.drop.dispense(source, stack);
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        this.emptying = player.getHeldItem(hand);
        try {
            return super.onItemRightClick(world, player, hand);
        } finally {
            this.emptying = ItemStack.EMPTY;
        }
    }

    @Override
    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World world, BlockPos pos) {
        return this.release(player, world, pos, this.emptying);
    }

    /**
     * In the Nether the water boils away, but the axolotl is still released.
     */
    public boolean release(@Nullable EntityPlayer player, World world, BlockPos pos, ItemStack bucket) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        boolean notSolid = !material.isSolid();
        boolean replaceable = state.getBlock().isReplaceable(world, pos);
        if (!world.isAirBlock(pos) && !notSolid && !replaceable) {
            return false;
        }
        if (world.provider.doesWaterVaporize()) {
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                    2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
            for (int i = 0; i < 8; i++) {
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(),
                        0.0D, 0.0D, 0.0D);
            }
        } else {
            if (!world.isRemote && (notSolid || replaceable) && !material.isLiquid()) {
                world.destroyBlock(pos, true);
            }
            world.playSound(player, pos, ModSounds.event("item.bucket.empty_axolotl"), SoundCategory.NEUTRAL, 1.0F, 1.0F);
            world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState(), 11);
        }
        if (!world.isRemote && !bucket.isEmpty()) {
            EntityAxolotl.spawnFromBucket(world, pos, bucket);
        }
        return true;
    }
}
