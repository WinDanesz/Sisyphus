package com.windanesz.sisyphus.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;

public class ItemFlowerDye extends Item {

    private final EnumDyeColor color;

    public ItemFlowerDye(EnumDyeColor color) {
        this.color = color;
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }

    @Nullable
    public static EnumDyeColor colorOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof ItemDye) {
            return EnumDyeColor.byDyeDamage(stack.getMetadata());
        }
        if (stack.getItem() instanceof ItemFlowerDye) {
            return ((ItemFlowerDye) stack.getItem()).getColor();
        }
        return null;
    }

    public EnumDyeColor getColor() {
        return this.color;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (!(target instanceof EntitySheep)) {
            return false;
        }
        EntitySheep sheep = (EntitySheep) target;
        if (!sheep.getSheared() && sheep.getFleeceColor() != this.color && !player.world.isRemote) {
            sheep.setFleeceColor(this.color);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return true;
    }
}
