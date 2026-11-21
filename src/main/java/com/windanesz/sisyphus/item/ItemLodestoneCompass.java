package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemLodestoneCompass extends Item {

    public static ItemLodestoneCompass INSTANCE;

    public ItemLodestoneCompass() {
        this.setCreativeTab(CreativeTabs.TOOLS);
        INSTANCE = this;
        this.addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
            private double rotation;
            private double rota;
            private long lastUpdateTick;

            @Override
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase holder) {
                if (holder == null && !stack.isOnItemFrame()) {
                    return 0.0F;
                }
                boolean held = holder != null;
                Entity entity = held ? holder : stack.getItemFrame();
                if (world == null) {
                    world = entity.world;
                }
                BlockPos target = trackedPos(stack, world);
                double d0;
                if (target == null) {
                    d0 = Math.random();
                } else {
                    double yaw = held ? entity.rotationYaw : 180 + ((EntityItemFrame) entity).facingDirection.getHorizontalIndex() * 90;
                    yaw = MathHelper.positiveModulo(yaw / 360.0D, 1.0D);
                    double toTarget = Math.atan2(target.getZ() + 0.5D - entity.posZ, target.getX() + 0.5D - entity.posX) / (Math.PI * 2.0D);
                    d0 = 0.5D - (yaw - 0.25D - toTarget);
                }
                if (held) {
                    d0 = this.wobble(world, d0);
                }
                return MathHelper.positiveModulo((float) d0, 1.0F);
            }

            private double wobble(World world, double angle) {
                if (world.getTotalWorldTime() != this.lastUpdateTick) {
                    this.lastUpdateTick = world.getTotalWorldTime();
                    double delta = angle - this.rotation;
                    delta = MathHelper.positiveModulo(delta + 0.5D, 1.0D) - 0.5D;
                    this.rota += delta * 0.1D;
                    this.rota *= 0.8D;
                    this.rotation = MathHelper.positiveModulo(this.rotation + this.rota, 1.0D);
                }
                return this.rotation;
            }
        });
    }

    @Nullable
    public static BlockPos trackedPos(ItemStack stack, World world) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.getBoolean("LodestoneTracked") || !tag.hasKey("LodestonePos", 10)) {
            return null;
        }
        if (tag.getInteger("LodestoneDimension") != world.provider.getDimension()) {
            return null;
        }
        NBTTagCompound p = tag.getCompoundTag("LodestonePos");
        BlockPos pos = new BlockPos(p.getInteger("X"), p.getInteger("Y"), p.getInteger("Z"));
        // A lodestone that is loaded and gone does not count, one in a chunk that is not loaded is taken to be there
        if (world.isBlockLoaded(pos) && world.getBlockState(pos).getBlock() != ModBlocks.get("lodestone")) {
            return null;
        }
        return pos;
    }

    public static void lock(ItemStack stack, World world, BlockPos pos) {
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        NBTTagCompound position = new NBTTagCompound();
        position.setInteger("X", pos.getX());
        position.setInteger("Y", pos.getY());
        position.setInteger("Z", pos.getZ());
        tag.setTag("LodestonePos", position);
        tag.setInteger("LodestoneDimension", world.provider.getDimension());
        tag.setBoolean("LodestoneTracked", true);
        stack.setTagCompound(tag);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
