package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.*;
import java.util.function.Supplier;

public final class WanderingTraderTrades {

    private static final List<Trade> COMMON = Collections.unmodifiableList(Arrays.asList(
            new Trade(1, () -> dye("white_dye", 15, 3), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 14), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 13), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 12), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 11), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 10), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 9), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 8), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 7), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 6), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 5), 12),
            new Trade(1, () -> dye("blue_dye", 4, 3), 12),
            new Trade(1, () -> dye("brown_dye", 3, 3), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 2), 12),
            new Trade(1, () -> new ItemStack(Items.DYE, 3, 1), 12),
            new Trade(1, () -> dye("black_dye", 0, 3), 12),
            new Trade(4, () -> new ItemStack(Items.SLIME_BALL, 1), 5),
            new Trade(2, () -> new ItemStack(Blocks.GLOWSTONE, 1), 5),
            new Trade(1, () -> new ItemStack(Blocks.TALLGRASS, 1, 2), 12),
            new Trade(1, () -> new ItemStack(Items.REEDS, 1), 8),
            new Trade(1, () -> new ItemStack(Blocks.PUMPKIN, 1), 4),
            new Trade(3, () -> new ItemStack(Blocks.CACTUS, 1), 8),
            new Trade(1, () -> new ItemStack(Blocks.YELLOW_FLOWER, 1), 12),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 0), 12),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 1), 8),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 2), 12),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 3), 12),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 4), 12),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 5), 12),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 6), 12),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 7), 12),
            new Trade(1, () -> new ItemStack(Blocks.RED_FLOWER, 1, 8), 12),
            new Trade(1, () -> flower("cornflower", 1), 12),
            new Trade(1, () -> flower("lily_of_the_valley", 1), 7),
            new Trade(1, () -> new ItemStack(Items.WHEAT_SEEDS, 1), 12),
            new Trade(1, () -> new ItemStack(Items.BEETROOT_SEEDS, 1), 12),
            new Trade(1, () -> new ItemStack(Items.PUMPKIN_SEEDS, 1), 12),
            new Trade(1, () -> new ItemStack(Items.MELON_SEEDS, 1), 12),
            new Trade(5, () -> new ItemStack(Blocks.SAPLING, 1, 4), 8),
            new Trade(5, () -> new ItemStack(Blocks.SAPLING, 1, 2), 8),
            new Trade(5, () -> new ItemStack(Blocks.SAPLING, 1, 5), 8),
            new Trade(5, () -> new ItemStack(Blocks.SAPLING, 1, 3), 8),
            new Trade(5, () -> new ItemStack(Blocks.SAPLING, 1, 0), 8),
            new Trade(5, () -> new ItemStack(Blocks.SAPLING, 1, 1), 8),
            new Trade(1, () -> new ItemStack(Blocks.VINE, 3), 4),
            new Trade(1, () -> new ItemStack(Blocks.BROWN_MUSHROOM, 3), 4),
            new Trade(1, () -> new ItemStack(Blocks.RED_MUSHROOM, 3), 4),
            new Trade(1, () -> new ItemStack(Blocks.WATERLILY, 5), 2),
            new Trade(1, () -> new ItemStack(Blocks.SAND, 8, 0), 8),
            new Trade(1, () -> new ItemStack(Blocks.SAND, 4, 1), 6)));
    private static final List<Trade> UNCOMMON = Collections.unmodifiableList(Arrays.asList(
            new Trade(1, () -> new ItemStack(Blocks.PACKED_ICE, 1), 6),
            new Trade(1, () -> new ItemStack(Items.GUNPOWDER, 4), 2),
            new Trade(3, () -> new ItemStack(Blocks.DIRT, 3, 2), 6),
            new Trade(5, () -> potion(PotionTypes.LONG_INVISIBILITY), 1)));

    private WanderingTraderTrades() {
    }

    private static ItemStack flower(String name, int count) {
        Block block = Settings.plants.enableFlowers ? ModBlocks.get(name) : null;
        return block == null ? ItemStack.EMPTY : new ItemStack(block, count);
    }

    private static ItemStack dye(String name, int vanillaMeta, int count) {
        Item dye = Settings.plants.enableFlowers ? ModItems.get(name) : null;
        return dye != null ? new ItemStack(dye, count) : new ItemStack(Items.DYE, count, vanillaMeta);
    }

    private static ItemStack potion(PotionType type) {
        return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), type);
    }

    public static MerchantRecipeList generate(Random rand) {
        MerchantRecipeList list = new MerchantRecipeList();
        add(list, COMMON, 5, rand);
        add(list, UNCOMMON, 1, rand);
        return list;
    }

    private static void add(MerchantRecipeList list, List<Trade> trades, int amount, Random rand) {
        List<Trade> pool = new ArrayList<>(trades);
        Collections.shuffle(pool, rand);
        int added = 0;
        for (Trade trade : pool) {
            if (added >= amount) {
                break;
            }
            MerchantRecipe recipe = trade.toRecipe();
            if (recipe != null) {
                list.add(recipe);
                added++;
            }
        }
    }

    static final class Trade {
        final int emeralds;
        final Supplier<ItemStack> goods;
        final int maxUses;

        Trade(int emeralds, Supplier<ItemStack> goods, int maxUses) {
            this.emeralds = emeralds;
            this.goods = goods;
            this.maxUses = maxUses;
        }

        MerchantRecipe toRecipe() {
            ItemStack stack = this.goods.get();
            if (stack.isEmpty()) {
                return null;
            }
            return new MerchantRecipe(new ItemStack(Items.EMERALD, this.emeralds), ItemStack.EMPTY, stack, 0, this.maxUses);
        }
    }
}
