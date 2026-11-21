package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A trim is stored in the tag as {@code Trim: {pattern: "coast", material: "gold"}}. Missing structures are stood in for: the nether
 * fortress for the bastion, the big underwater ruin for the shipwreck and the abandoned camp for the pillager outpost. Ward, silence, flow
 * and bolt are found nowhere.
 */
@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class ArmorTrims {

    public static final String[] PATTERNS = {"sentry", "dune", "coast", "wild", "ward", "eye", "vex", "tide", "snout", "rib", "spire", "wayfinder",
            "shaper", "silence", "raiser", "host", "flow", "bolt"};

    public static final String NETHERITE_TEMPLATE = "netherite_upgrade_smithing_template";
    public static final String TEMPLATE_SUFFIX = "_armor_trim_smithing_template";

    private static final String TAG = "Trim";
    @Nullable
    private static Set<Item> trimmable;

    private ArmorTrims() {
    }

    public static boolean isEnabled() {
        return Settings.isContentEnabled("armor_trims");
    }

    private static ItemStack modItem(String name) {
        Item item = Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name));
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Nullable
    public static Item template(String pattern) {
        ItemStack stack = modItem(pattern + TEMPLATE_SUFFIX);
        return stack.isEmpty() ? null : stack.getItem();
    }

    @Nullable
    public static Item netheriteTemplate() {
        ItemStack stack = modItem(NETHERITE_TEMPLATE);
        return stack.isEmpty() ? null : stack.getItem();
    }

    public static List<Item> trimTemplates() {
        List<Item> templates = new ArrayList<>();
        for (String pattern : PATTERNS) {
            Item template = template(pattern);
            if (template != null) {
                templates.add(template);
            }
        }
        return templates;
    }

    @Nullable
    public static String patternOf(ItemStack stack) {
        return stack.getItem() instanceof ItemSmithingTemplate ? ((ItemSmithingTemplate) stack.getItem()).pattern : null;
    }

    @Nullable
    public static Material materialOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (Material material : Material.values()) {
            ItemStack item = material.item();
            if (!item.isEmpty() && item.getItem() == stack.getItem() && (!item.getHasSubtypes() || item.getMetadata() == stack.getMetadata())) {
                return material;
            }
        }
        return null;
    }

    public static List<ItemStack> materialItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Material material : Material.values()) {
            if (!material.item().isEmpty()) {
                items.add(material.item());
            }
        }
        return items;
    }

    public static Set<Item> trimmableArmor() {
        if (trimmable == null) {
            Set<Item> items = new HashSet<>();
            Collections.addAll(items, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
                    Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
                    Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                    Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
                    Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
            for (String piece : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
                ItemStack netherite = modItem("netherite_" + piece);
                if (!netherite.isEmpty()) {
                    items.add(netherite.getItem());
                }
            }
            trimmable = items;
        }
        return trimmable;
    }

    public static List<ItemStack> trimmableArmorStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (String material : new String[]{"leather", "chainmail", "iron", "golden", "diamond"}) {
            for (String piece : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
                stacks.add(new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("minecraft", material + "_" + piece))));
            }
        }
        for (String piece : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
            ItemStack netherite = modItem("netherite_" + piece);
            if (!netherite.isEmpty()) {
                stacks.add(netherite);
            }
        }
        return stacks;
    }

    public static boolean isTrimmable(ItemStack stack) {
        return isEnabled() && !stack.isEmpty() && trimmableArmor().contains(stack.getItem());
    }

    public static String pieceOf(EntityEquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return "helmet";
            case CHEST:
                return "chestplate";
            case LEGS:
                return "leggings";
            default:
                return "boots";
        }
    }

    @Nullable
    public static Trim get(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(TAG, 10) || !(stack.getItem() instanceof ItemArmor)) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag(TAG);
        String pattern = tag.getString("pattern");
        Material material = Material.byName(tag.getString("material"));
        for (String known : PATTERNS) {
            if (known.equals(pattern) && material != null) {
                return new Trim(pattern, material);
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (LootTableList.CHESTS_NETHER_BRIDGE.equals(name)) {
            if (Settings.items.netheriteNeedsTemplate) {
                addChance(event, netheriteTemplate(), 10, false);
            }
            addChance(event, template("rib"), 15, false);
            addChance(event, template("snout"), 12, false);
        } else if (LootTableList.CHESTS_DESERT_PYRAMID.equals(name)) {
            addChance(event, template("dune"), 7, false);
        } else if (LootTableList.CHESTS_JUNGLE_TEMPLE.equals(name)) {
            addChance(event, template("wild"), 3, false);
        } else if (LootTableList.CHESTS_WOODLAND_MANSION.equals(name)) {
            addChance(event, template("vex"), 2, false);
        } else if (LootTableList.ENTITIES_ELDER_GUARDIAN.equals(name)) {
            addChance(event, template("tide"), 5, true);
        } else if (LootTableList.CHESTS_STRONGHOLD_LIBRARY.equals(name)) {
            addChance(event, template("eye"), 1, false);
        } else if (LootTableList.CHESTS_STRONGHOLD_CORRIDOR.equals(name)) {
            addChance(event, template("eye"), 10, false);
        } else if (LootTableList.CHESTS_END_CITY_TREASURE.equals(name)) {
            addChance(event, template("spire"), 15, false);
        } else if (name.equals(new ResourceLocation(SisyphusBackport.MODID, "chests/underwater_ruin_big"))) {
            addChance(event, template("coast"), 6, false);
        } else if (name.equals(new ResourceLocation(SisyphusBackport.MODID, "chests/abandoned_camp"))) {
            addChance(event, template("sentry"), 4, false);
        }
    }

    private static void addChance(LootTableLoadEvent event, @Nullable Item item, int oneIn, boolean killedByPlayer) {
        if (item == null) {
            return;
        }
        String id = item.getRegistryName().getPath();
        LootCondition[] conditions = killedByPlayer ? new LootCondition[]{new KilledByPlayer(false)} : new LootCondition[0];
        LootEntry[] entries = oneIn <= 1 ? new LootEntry[]{new LootEntryItem(item, 1, 0, new LootFunction[0], new LootCondition[0], id)}
                : new LootEntry[]{new LootEntryItem(item, 1, 0, new LootFunction[0], new LootCondition[0], id),
                new LootEntryEmpty(oneIn - 1, 0, new LootCondition[0], SisyphusBackport.MODID + "_no_" + id)};
        event.getTable().addPool(new LootPool(entries, conditions, new RandomValueRange(1), new RandomValueRange(0), SisyphusBackport.MODID + "_" + id));
    }

    public static void set(ItemStack stack, Trim trim) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("pattern", trim.pattern);
        tag.setString("material", trim.material.name);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setTag(TAG, tag);
    }

    /**
     * Copper and resin are left out, as neither is in this mod.
     */
    public enum Material {
        QUARTZ("quartz", TextFormatting.WHITE),
        IRON("iron", TextFormatting.GRAY),
        NETHERITE("netherite", TextFormatting.DARK_GRAY),
        REDSTONE("redstone", TextFormatting.DARK_RED),
        GOLD("gold", TextFormatting.GOLD),
        EMERALD("emerald", TextFormatting.DARK_GREEN),
        DIAMOND("diamond", TextFormatting.AQUA),
        LAPIS("lapis", TextFormatting.BLUE),
        AMETHYST("amethyst", TextFormatting.DARK_PURPLE);

        public final String name;
        public final TextFormatting color;

        Material(String name, TextFormatting color) {
            this.name = name;
            this.color = color;
        }

        @Nullable
        public static Material byName(String name) {
            for (Material material : values()) {
                if (material.name.equals(name)) {
                    return material;
                }
            }
            return null;
        }

        public ItemStack item() {
            switch (this) {
                case QUARTZ:
                    return new ItemStack(Items.QUARTZ);
                case IRON:
                    return new ItemStack(Items.IRON_INGOT);
                case NETHERITE:
                    return modItem("netherite_ingot");
                case REDSTONE:
                    return new ItemStack(Items.REDSTONE);
                case GOLD:
                    return new ItemStack(Items.GOLD_INGOT);
                case EMERALD:
                    return new ItemStack(Items.EMERALD);
                case DIAMOND:
                    return new ItemStack(Items.DIAMOND);
                case LAPIS:
                    return new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage());
                default:
                    return modItem("amethyst_shard");
            }
        }

        /**
         * A trim on armor of its own material is drawn darker so that it still shows.
         */
        public boolean isSameAs(ItemArmor.ArmorMaterial armor) {
            switch (this) {
                case IRON:
                    return armor == ItemArmor.ArmorMaterial.IRON;
                case GOLD:
                    return armor == ItemArmor.ArmorMaterial.GOLD;
                case DIAMOND:
                    return armor == ItemArmor.ArmorMaterial.DIAMOND;
                case NETHERITE:
                    return armor.name().equals("SISYPHUS_NETHERITE");
                default:
                    return false;
            }
        }

        public boolean hasDarker() {
            return this == IRON || this == GOLD || this == DIAMOND || this == NETHERITE;
        }
    }

    public static final class Trim {
        public final String pattern;
        public final Material material;

        public Trim(String pattern, Material material) {
            this.pattern = pattern;
            this.material = material;
        }

        public String palette(ItemArmor armor) {
            return this.material.hasDarker() && this.material.isSameAs(armor.getArmorMaterial()) ? this.material.name + "_darker" : this.material.name;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Trim && ((Trim) other).pattern.equals(this.pattern) && ((Trim) other).material == this.material;
        }

        @Override
        public int hashCode() {
            return this.pattern.hashCode() * 31 + this.material.hashCode();
        }
    }
}
