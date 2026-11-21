package com.windanesz.sisyphus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentCrossbow extends Enchantment {

    private final Kind kind;
    private Enchantment exclusive;
    public EnchantmentCrossbow(Kind kind, EnumEnchantmentType type) {
        super(kind.rarity, type, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND});
        this.kind = kind;
    }

    public void setExclusive(Enchantment other) {
        this.exclusive = other;
    }

    @Override
    public int getMinEnchantability(int level) {
        switch (this.kind) {
            case MULTISHOT:
                return 20;
            case QUICK_CHARGE:
                return 12 + (level - 1) * 20;
            default:
                return 1 + (level - 1) * 10;
        }
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.kind == Kind.MULTISHOT ? 50 : this.getMinEnchantability(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return this.kind.maxLevel;
    }

    @Override
    public boolean canApplyTogether(Enchantment other) {
        return super.canApplyTogether(other) && other != this.exclusive;
    }

    public enum Kind {
        MULTISHOT(Rarity.RARE, 1),
        QUICK_CHARGE(Rarity.UNCOMMON, 3),
        PIERCING(Rarity.COMMON, 4);

        final Rarity rarity;
        final int maxLevel;

        Kind(Rarity rarity, int maxLevel) {
            this.rarity = rarity;
            this.maxLevel = maxLevel;
        }
    }
}
