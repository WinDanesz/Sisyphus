package com.windanesz.sisyphus.item;

import com.google.common.collect.Multimap;
import com.windanesz.sisyphus.registry.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class NetheriteItems {

    private static final Field IMMUNE_TO_FIRE = ReflectionHelper.findField(Entity.class, "isImmuneToFire", "field_70178_ae");

    private static final Map<Item, String> UPGRADES = new LinkedHashMap<>();
    private static final Map<String, String> MOD_UPGRADES = new LinkedHashMap<>();

    static {
        UPGRADES.put(Items.DIAMOND_SWORD, "netherite_sword");
        UPGRADES.put(Items.DIAMOND_PICKAXE, "netherite_pickaxe");
        UPGRADES.put(Items.DIAMOND_AXE, "netherite_axe");
        UPGRADES.put(Items.DIAMOND_SHOVEL, "netherite_shovel");
        UPGRADES.put(Items.DIAMOND_HOE, "netherite_hoe");
        UPGRADES.put(Items.DIAMOND_HELMET, "netherite_helmet");
        UPGRADES.put(Items.DIAMOND_CHESTPLATE, "netherite_chestplate");
        UPGRADES.put(Items.DIAMOND_LEGGINGS, "netherite_leggings");
        UPGRADES.put(Items.DIAMOND_BOOTS, "netherite_boots");
    }

    static {
        MOD_UPGRADES.put("diamond_throwing_disc", "netherite_throwing_disc");
    }

    private NetheriteItems() {
    }

    public static Map<Item, Item> getUpgrades() {
        Map<Item, Item> upgrades = new LinkedHashMap<>();
        for (Map.Entry<Item, String> entry : UPGRADES.entrySet()) {
            Item result = ModItems.get(entry.getValue());
            if (result != null) {
                upgrades.put(entry.getKey(), result);
            }
        }
        for (Map.Entry<String, String> entry : MOD_UPGRADES.entrySet()) {
            Item base = ModItems.get(entry.getKey());
            Item result = ModItems.get(entry.getValue());
            // the registry gives air for what is turned off
            if (base != null && result != null && base != Items.AIR && result != Items.AIR) {
                upgrades.put(base, result);
            }
        }
        return upgrades;
    }

    public static void keepFloating(EntityItem item) {
        try {
            if (!IMMUNE_TO_FIRE.getBoolean(item)) {
                IMMUNE_TO_FIRE.setBoolean(item, true);
            }
        } catch (IllegalAccessException e) {
            // not fireproof, but still floats
        }
        BlockPos pos = new BlockPos(item);
        if (item.world.getBlockState(pos).getMaterial() == Material.LAVA) {
            while (item.world.getBlockState(pos.up()).getMaterial() == Material.LAVA) {
                pos = pos.up();
            }
            item.setPosition(item.posX, pos.getY() + 1.01D, item.posZ);
            item.motionY = 0.0D;
        }
    }

    public static class Plain extends Item {
        private final boolean beaconPayment;

        public Plain(boolean beaconPayment) {
            this.beaconPayment = beaconPayment;
            this.setCreativeTab(CreativeTabs.MATERIALS);
        }

        @Override
        public boolean isBeaconPayment(ItemStack stack) {
            return this.beaconPayment;
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem item) {
            keepFloating(item);
            return false;
        }
    }

    public static class Block extends ItemBlock {
        public Block(net.minecraft.block.Block block) {
            super(block);
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem item) {
            keepFloating(item);
            return false;
        }
    }

    public static class Sword extends ItemSword {
        public Sword(ToolMaterial material) {
            super(material);
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem item) {
            keepFloating(item);
            return false;
        }
    }

    public static class Pickaxe extends ItemPickaxe {
        public Pickaxe(ToolMaterial material) {
            super(material);
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem item) {
            keepFloating(item);
            return false;
        }
    }

    public static class Axe extends ItemAxe {
        public Axe(ToolMaterial material) {
            // the netherite axe hits with 10 points of damage, 1 more than the diamond one
            super(material, 9.0F, -3.0F);
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem item) {
            keepFloating(item);
            return false;
        }
    }

    public static class Shovel extends ItemSpade {
        public Shovel(ToolMaterial material) {
            super(material);
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem item) {
            keepFloating(item);
            return false;
        }
    }

    public static class Hoe extends ItemHoe {
        public Hoe(ToolMaterial material) {
            super(material);
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem item) {
            keepFloating(item);
            return false;
        }
    }

    public static class Armor extends ItemArmor {

        private static final UUID[] KNOCKBACK = {UUID.fromString("2d4d7a64-62d1-4a6d-8f3a-6a3c3b5e9a01"), UUID.fromString("2d4d7a64-62d1-4a6d-8f3a-6a3c3b5e9a02"),
                UUID.fromString("2d4d7a64-62d1-4a6d-8f3a-6a3c3b5e9a03"), UUID.fromString("2d4d7a64-62d1-4a6d-8f3a-6a3c3b5e9a04")};

        public Armor(ArmorMaterial material, EntityEquipmentSlot slot) {
            super(material, 0, slot);
            this.setCreativeTab(CreativeTabs.COMBAT);
        }

        @Override
        public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
            Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
            if (slot == this.armorType) {
                modifiers.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
                        new AttributeModifier(KNOCKBACK[slot.getIndex()], "Armor knockback resistance", 0.1D, 0));
            }
            return modifiers;
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem item) {
            keepFloating(item);
            return false;
        }
    }
}
