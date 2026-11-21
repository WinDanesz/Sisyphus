package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;

import java.util.Random;

public final class PiglinBartering {

    private static final int TOTAL = 459;

    private PiglinBartering() {
    }

    public static ItemStack roll(Random rand) {
        for (int attempt = 0; attempt < 20; attempt++) {
            ItemStack stack = pick(rand.nextInt(TOTAL), rand);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return new ItemStack(Items.IRON_NUGGET, 9 + rand.nextInt(28));
    }

    private static int count(Random rand, int min, int max) {
        return min + rand.nextInt(max - min + 1);
    }

    private static ItemStack mod(String block, int count) {
        Block found = ModBlocks.get(block);
        return found == Blocks.AIR ? ItemStack.EMPTY : new ItemStack(found, count);
    }

    private static ItemStack pick(int roll, Random rand) {
        Enchantment soulSpeed = ModEnchantments.SOUL_SPEED;
        int level = 1 + rand.nextInt(3);
        // the weights, in the order of the table of the game
        if ((roll -= 5) < 0) {
            return soulSpeed == null ? ItemStack.EMPTY : ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(soulSpeed, level));
        }
        if ((roll -= 8) < 0) {
            if (soulSpeed == null) {
                return ItemStack.EMPTY;
            }
            ItemStack boots = new ItemStack(Items.IRON_BOOTS);
            boots.addEnchantment(soulSpeed, level);
            return boots;
        }
        if ((roll -= 8) < 0) {
            return PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), PotionTypes.FIRE_RESISTANCE);
        }
        if ((roll -= 8) < 0) {
            return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.FIRE_RESISTANCE);
        }
        if ((roll -= 10) < 0) {
            return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
        }
        if ((roll -= 10) < 0) {
            return new ItemStack(Items.IRON_NUGGET, count(rand, 9, 36));
        }
        if ((roll -= 10) < 0) {
            return new ItemStack(Items.ENDER_PEARL, count(rand, 2, 4));
        }
        if ((roll -= 20) < 0) {
            return new ItemStack(Items.STRING, count(rand, 3, 9));
        }
        if ((roll -= 20) < 0) {
            return new ItemStack(Items.QUARTZ, count(rand, 5, 12));
        }
        if ((roll -= 40) < 0) {
            return new ItemStack(Blocks.OBSIDIAN);
        }
        if ((roll -= 40) < 0) {
            return mod("crying_obsidian", count(rand, 1, 3));
        }
        if ((roll -= 40) < 0) {
            return new ItemStack(Items.FIRE_CHARGE);
        }
        if ((roll -= 40) < 0) {
            return new ItemStack(Items.LEATHER, count(rand, 2, 4));
        }
        if ((roll -= 40) < 0) {
            return new ItemStack(Blocks.SOUL_SAND, count(rand, 2, 8));
        }
        if ((roll -= 40) < 0) {
            return new ItemStack(Items.NETHERBRICK, count(rand, 2, 8));
        }
        if ((roll -= 40) < 0) {
            return new ItemStack(Items.SPECTRAL_ARROW, count(rand, 6, 12));
        }
        if ((roll -= 40) < 0) {
            return new ItemStack(Blocks.GRAVEL, count(rand, 8, 16));
        }
        return mod("blackstone", count(rand, 8, 16));
    }
}
