package com.windanesz.sisyphus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentThrowingDisc extends Enchantment {

    private final Kind kind;

    public EnchantmentThrowingDisc(Kind kind, EnumEnchantmentType type) {
        super(kind.rarity, type, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND});
        this.kind = kind;
    }

    @Override
    public int getMinEnchantability(int level) {
        return this.kind.minCost + (level - 1) * this.kind.costPerLevel;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return this.kind.maxLevel;
    }

    public enum Kind {
        VELOCITY(Rarity.COMMON, 3, 1, 10),
        RICOCHET(Rarity.RARE, 3, 12, 20),
        REACH(Rarity.UNCOMMON, 4, 5, 8),
        REBOUND(Rarity.RARE, 2, 15, 20);

        final Rarity rarity;
        final int maxLevel;
        final int minCost;
        final int costPerLevel;

        Kind(Rarity rarity, int maxLevel, int minCost, int costPerLevel) {
            this.rarity = rarity;
            this.maxLevel = maxLevel;
            this.minCost = minCost;
            this.costPerLevel = costPerLevel;
        }
    }
}
