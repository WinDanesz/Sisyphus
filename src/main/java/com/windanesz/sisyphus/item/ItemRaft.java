package com.windanesz.sisyphus.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;

public class ItemRaft extends Item {

    private final Factory factory;

    public ItemRaft(Factory factory) {
        this.factory = factory;
        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new Dispense());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        float pitch = player.rotationPitch;
        float yaw = player.rotationYaw;
        Vec3d eyes = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        float cosYaw = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float sinYaw = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float cosPitch = -MathHelper.cos(-pitch * 0.017453292F);
        float sinPitch = MathHelper.sin(-pitch * 0.017453292F);
        Vec3d end = eyes.add(sinYaw * cosPitch * 5.0D, sinPitch * 5.0D, cosYaw * cosPitch * 5.0D);
        RayTraceResult hit = world.rayTraceBlocks(eyes, end, true);
        if (hit == null) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        Vec3d look = player.getLook(1.0F);
        List<Entity> near = world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(look.x * 5.0D, look.y * 5.0D,
                look.z * 5.0D).grow(1.0D));
        for (Entity entity : near) {
            if (entity.canBeCollidedWith()) {
                AxisAlignedBB box = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
                if (box.contains(eyes)) {
                    return new ActionResult<>(EnumActionResult.PASS, stack);
                }
            }
        }
        if (hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        Block block = world.getBlockState(hit.getBlockPos()).getBlock();
        boolean water = block == Blocks.WATER || block == Blocks.FLOWING_WATER;
        EntityBoat raft = this.factory.create(world, hit.hitVec.x, water ? hit.hitVec.y - 0.12D : hit.hitVec.y, hit.hitVec.z);
        raft.rotationYaw = player.rotationYaw;
        if (!world.getCollisionBoxes(raft, raft.getEntityBoundingBox().grow(-0.1D)).isEmpty()) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (!world.isRemote) {
            world.spawnEntity(raft);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public interface Factory {
        EntityBoat create(World world, double x, double y, double z);
    }

    private class Dispense extends BehaviorDefaultDispenseItem {

        private final BehaviorDefaultDispenseItem drop = new BehaviorDefaultDispenseItem();

        @Override
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
            World world = source.getWorld();
            double x = source.getX() + facing.getXOffset() * 1.125F;
            double y = source.getY() + facing.getYOffset() * 1.125F;
            double z = source.getZ() + facing.getZOffset() * 1.125F;
            BlockPos front = source.getBlockPos().offset(facing);
            Material material = world.getBlockState(front).getMaterial();
            double up;
            if (Material.WATER.equals(material)) {
                up = 1.0D;
            } else if (Material.AIR.equals(material) && Material.WATER.equals(world.getBlockState(front.down()).getMaterial())) {
                up = 0.0D;
            } else {
                return this.drop.dispense(source, stack);
            }
            EntityBoat raft = ItemRaft.this.factory.create(world, x, y + up, z);
            raft.rotationYaw = facing.getHorizontalAngle();
            world.spawnEntity(raft);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playDispenseSound(IBlockSource source) {
            source.getWorld().playEvent(1000, source.getBlockPos(), 0);
        }
    }
}
