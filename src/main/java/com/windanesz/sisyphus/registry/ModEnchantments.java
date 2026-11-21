package com.windanesz.sisyphus.registry;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.enchantment.EnchantmentCrossbow;
import com.windanesz.sisyphus.enchantment.EnchantmentSoulSpeed;
import com.windanesz.sisyphus.enchantment.EnchantmentThrowingDisc;
import com.windanesz.sisyphus.item.ItemCrossbow;
import com.windanesz.sisyphus.item.ItemThrowingDisc;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mod.EventBusSubscriber
public final class ModEnchantments {

    @Nullable
    public static Enchantment MULTISHOT;
    @Nullable
    public static Enchantment QUICK_CHARGE;
    @Nullable
    public static Enchantment PIERCING;
    @Nullable
    public static Enchantment SOUL_SPEED;
    @Nullable
    public static Enchantment VELOCITY;
    @Nullable
    public static Enchantment RICOCHET;
    @Nullable
    public static Enchantment REACH;
    @Nullable
    public static Enchantment REBOUND;
    @Nullable
    public static EnumEnchantmentType diggerType;

    private ModEnchantments() {
    }

    public static int level(@Nullable Enchantment enchantment, ItemStack stack) {
        return enchantment == null ? 0 : EnchantmentHelper.getEnchantmentLevel(enchantment, stack);
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Enchantment> event) {
        registerCrossbowEnchantments(event);
        if (Settings.items.enableSoulSpeed) {
            SOUL_SPEED = register(event, new EnchantmentSoulSpeed(), "soul_speed");
        }
        registerThrowingDiscEnchantments(event);
    }

    private static void registerCrossbowEnchantments(RegistryEvent.Register<Enchantment> event) {
        if (!Settings.items.crossbow.enabled) {
            return;
        }
        EnumEnchantmentType type = EnumHelper.addEnchantmentType("sisyphus_crossbow", item -> item instanceof ItemCrossbow);

        Settings.Crossbow settings = Settings.items.crossbow;
        EnchantmentCrossbow multishot = settings.enableMultishot ? new EnchantmentCrossbow(EnchantmentCrossbow.Kind.MULTISHOT, type) : null;
        EnchantmentCrossbow quickCharge = settings.enableQuickCharge ? new EnchantmentCrossbow(EnchantmentCrossbow.Kind.QUICK_CHARGE, type) : null;
        EnchantmentCrossbow piercing = settings.enablePiercing ? new EnchantmentCrossbow(EnchantmentCrossbow.Kind.PIERCING, type) : null;
        if (multishot != null && piercing != null) {
            multishot.setExclusive(piercing);
            piercing.setExclusive(multishot);
        }

        MULTISHOT = register(event, multishot, "multishot");
        QUICK_CHARGE = register(event, quickCharge, "quick_charge");
        PIERCING = register(event, piercing, "piercing");
        if (MULTISHOT != null || QUICK_CHARGE != null || PIERCING != null) {
            showInCombatTab(type);
        }
    }

    private static void registerThrowingDiscEnchantments(RegistryEvent.Register<Enchantment> event) {
        if (!Settings.items.throwingDiscs.enabled) {
            return;
        }
        Settings.ThrowingDiscs settings = Settings.items.throwingDiscs;
        if (!settings.enableVelocity && !settings.enableRicochet && !settings.enableReach && !settings.enableRebound) {
            return;
        }
        EnumEnchantmentType type = EnumHelper.addEnchantmentType("sisyphus_throwing_disc", item -> item instanceof ItemThrowingDisc);
        VELOCITY = register(event, settings.enableVelocity ? new EnchantmentThrowingDisc(EnchantmentThrowingDisc.Kind.VELOCITY, type) : null, "velocity");
        RICOCHET = register(event, settings.enableRicochet ? new EnchantmentThrowingDisc(EnchantmentThrowingDisc.Kind.RICOCHET, type) : null, "ricochet");
        REACH = register(event, settings.enableReach ? new EnchantmentThrowingDisc(EnchantmentThrowingDisc.Kind.REACH, type) : null, "reach");
        REBOUND = register(event, settings.enableRebound ? new EnchantmentThrowingDisc(EnchantmentThrowingDisc.Kind.REBOUND, type) : null, "rebound");
        showInCombatTab(type);
    }

    // So that the creative combat tab lists enchanted books of the type
    private static void showInCombatTab(EnumEnchantmentType type) {
        EnumEnchantmentType[] existing = CreativeTabs.COMBAT.getRelevantEnchantmentTypes();
        EnumEnchantmentType[] extended = Arrays.copyOf(existing, existing.length + 1);
        extended[existing.length] = type;
        CreativeTabs.COMBAT.setRelevantEnchantmentTypes(extended);
    }

    @Nullable
    private static Enchantment register(RegistryEvent.Register<Enchantment> event, @Nullable Enchantment enchantment, String name) {
        if (enchantment != null) {
            enchantment.setRegistryName(SisyphusBackport.MODID, name);
            enchantment.setName(SisyphusBackport.MODID + ":" + name);
            event.getRegistry().register(enchantment);
        }
        return enchantment;
    }

    /**
     * Efficiency, Fortune and Silk Touch only take an ItemTool, which a hoe is not, so they get an enchantment type that takes hoes too.
     * The table still does not offer them, as hoes have no enchantability, but an anvil does.
     */
    public static void registerHoeEnchanting() {
        if (!Settings.gameplay.enableHoeEnchanting) {
            return;
        }
        diggerType = EnumHelper.addEnchantmentType("sisyphus_digger", item -> item instanceof ItemTool || item instanceof ItemHoe);
        Enchantments.EFFICIENCY.type = diggerType;
        Enchantments.FORTUNE.type = diggerType;
        Enchantments.SILK_TOUCH.type = diggerType;
    }
}
