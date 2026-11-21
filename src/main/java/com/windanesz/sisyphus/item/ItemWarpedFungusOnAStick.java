package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.entity.living.EntityStrider;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemWarpedFungusOnAStick extends Item {

    public ItemWarpedFungusOnAStick() {
        this.setMaxStackSize(1);
        this.setMaxDamage(100);
        this.setFull3D();
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        Entity mount = player.getRidingEntity();
        if (player.isRiding() && mount instanceof EntityStrider) {
            EntityStrider strider = (EntityStrider) mount;
            if (strider.canBeSteered() && strider.boost()) {
                stack.damageItem(1, player);
                if (stack.isEmpty()) {
                    ItemStack rod = new ItemStack(Items.FISHING_ROD);
                    rod.setTagCompound(stack.getTagCompound());
                    return new ActionResult<>(EnumActionResult.SUCCESS, rod);
                }
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
