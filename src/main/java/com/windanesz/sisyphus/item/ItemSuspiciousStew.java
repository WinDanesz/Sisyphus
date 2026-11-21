package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemSuspiciousStew extends ItemFood {

    public ItemSuspiciousStew() {
        // 7.2 saturation: the modifier is the saturation divided by twice the hunger
        super(6, 0.6F, false);
        this.setMaxStackSize(1);
    }

    /**
     * Returns {potion, duration in ticks}.
     */
    @Nullable
    public static Object[] effectOf(ItemStack flower) {
        Item item = flower.getItem();
        int meta = flower.getMetadata();
        if (item == Item.getItemFromBlock(Blocks.YELLOW_FLOWER)) {
            return new Object[]{MobEffects.SATURATION, 7}; // dandelion
        }
        if (item == Item.getItemFromBlock(Blocks.RED_FLOWER)) {
            switch (meta) {
                case 0:
                    return new Object[]{MobEffects.SPEED, 100}; // poppy
                case 1:
                    return new Object[]{MobEffects.SATURATION, 7}; // blue orchid
                case 2:
                    return new Object[]{MobEffects.FIRE_RESISTANCE, 80}; // allium
                case 3:
                    return new Object[]{MobEffects.BLINDNESS, 160}; // azure bluet
                case 4:
                case 5:
                case 6:
                case 7:
                    return new Object[]{MobEffects.WEAKNESS, 180}; // tulips
                case 8:
                    return new Object[]{MobEffects.REGENERATION, 160}; // oxeye daisy
                default:
                    return null;
            }
        }
        if (Settings.plants.enableFlowers) {
            Block block = Block.getBlockFromItem(item);
            if (block == ModBlocks.get("cornflower")) {
                return new Object[]{MobEffects.JUMP_BOOST, 120};
            } else if (block == ModBlocks.get("lily_of_the_valley")) {
                return new Object[]{MobEffects.POISON, 240};
            } else if (block == ModBlocks.get("wither_rose")) {
                return new Object[]{MobEffects.WITHER, 160};
            }
        }
        return null;
    }

    public static void addEffect(ItemStack stack, Potion potion, int duration) {
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        NBTTagList list = tag.getTagList("Effects", 10);
        NBTTagCompound effect = new NBTTagCompound();
        effect.setInteger("EffectId", Potion.getIdFromPotion(potion));
        effect.setInteger("EffectDuration", duration);
        list.appendTag(effect);
        tag.setTag("Effects", list);
        stack.setTagCompound(tag);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        super.onFoodEaten(stack, world, player);
        if (world.isRemote || !stack.hasTagCompound()) {
            return;
        }
        NBTTagList list = stack.getTagCompound().getTagList("Effects", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound effect = list.getCompoundTagAt(i);
            Potion potion = Potion.getPotionById(effect.getInteger("EffectId"));
            if (potion != null) {
                player.addPotionEffect(new PotionEffect(potion, effect.getInteger("EffectDuration")));
            }
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        ItemStack result = super.onItemUseFinish(stack, world, entity);
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
            return result;
        }
        // The bowl is left behind
        return result.isEmpty() ? new ItemStack(Items.BOWL) : result;
    }
}
