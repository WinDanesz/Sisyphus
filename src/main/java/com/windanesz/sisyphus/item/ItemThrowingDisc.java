package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.entity.projectile.EntityThrowingDisc;
import com.windanesz.sisyphus.packet.ServerSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemThrowingDisc extends Item {

    private final ToolMaterial material;
    private final boolean fireproof;

    public ItemThrowingDisc(ToolMaterial material, boolean fireproof) {
        this.material = material;
        this.fireproof = fireproof;
        this.setMaxStackSize(1);
        this.setMaxDamage(material.getMaxUses());
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.setFull3D();
    }

    /**
     * 0 to 1, or -1 when not drawing one back. Works for other players too.
     */
    public static float getWindUp(EntityLivingBase entity, float partialTicks) {
        if (!entity.isHandActive() || !(entity.getActiveItemStack().getItem() instanceof ItemThrowingDisc)) {
            return -1.0F;
        }
        int windUp = ServerSettings.discWindUp();
        if (windUp <= 0) {
            return 1.0F;
        }
        return Math.min((entity.getItemInUseMaxCount() + partialTicks) / windUp, 1.0F);
    }

    public float getDamage() {
        return (float) Settings.items.throwingDiscs.damage + this.material.getAttackDamage();
    }

    public boolean isFireproof() {
        return this.fireproof;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (ServerSettings.discWindUp() > 0) {
            // Drawn back while use is held, and thrown when it is let go, as a bow is
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        this.throwDisc(stack, world, player, hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
        if (!(entity instanceof EntityPlayer) || this.getMaxItemUseDuration(stack) - timeLeft < ServerSettings.discWindUp()) {
            // Let go before it was drawn all the way back: it stays in the hand
            return;
        }
        this.throwDisc(stack, world, (EntityPlayer) entity, entity.getActiveHand());
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        // The arms are posed by RenderDiscHand and MixinModelBiped
        return EnumAction.NONE;
    }

    private void throwDisc(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            // The arm follows through. The swing is sent to the server, which shows it to the players around
            player.swingArm(hand);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.8F,
                1.4F + world.rand.nextFloat() * 0.2F);
        // So that a creative player cannot throw a stream of them
        player.getCooldownTracker().setCooldown(this, Settings.items.throwingDiscs.cooldown);
        if (!world.isRemote) {
            ItemStack thrown = stack.copy();
            thrown.damageItem(1, player);
            if (!thrown.isEmpty()) {
                int slot = hand == EnumHand.OFF_HAND ? EntityThrowingDisc.OFF_HAND : player.inventory.currentItem;
                EntityThrowingDisc disc = new EntityThrowingDisc(world, player, thrown, slot);
                world.spawnEntity(disc);
            }
        }
        player.addStat(StatList.getObjectUseStats(this));
        if (!player.capabilities.isCreativeMode) {
            // The disc carries the item while it flies, and puts it back when it comes home
            stack.shrink(1);
        }
    }

    @Override
    public int getItemEnchantability() {
        return this.material.getEnchantability();
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        ItemStack material = this.material.getRepairItemStack();
        if (!material.isEmpty() && OreDictionary.itemMatches(material, repair, false)) {
            return true;
        }
        return super.getIsRepairable(toRepair, repair);
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem item) {
        if (this.fireproof) {
            NetheriteItems.keepFloating(item);
        }
        return false;
    }
}
