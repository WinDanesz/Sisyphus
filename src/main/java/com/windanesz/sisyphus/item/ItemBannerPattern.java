package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraftforge.common.util.EnumHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemBannerPattern extends Item {

    // The first four are vanilla patterns that need a special item; the snout is added to the game when it is turned on
    private static final List<String> NAMES = new ArrayList<>(Arrays.asList("flower_banner_pattern", "creeper_banner_pattern", "skull_banner_pattern", "mojang_banner_pattern"));
    private static final List<BannerPattern> PATTERNS = new ArrayList<>(Arrays.asList(BannerPattern.FLOWER, BannerPattern.CREEPER, BannerPattern.SKULL, BannerPattern.MOJANG));

    private final BannerPattern pattern;
    private final EnumRarity rarity;

    public ItemBannerPattern(BannerPattern pattern, EnumRarity rarity) {
        this.pattern = pattern;
        this.rarity = rarity;
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.MISC);
    }

    public static void addSnoutPattern() {
        if (NAMES.contains("piglin_banner_pattern")) {
            return;
        }
        try {
            // the name is the name of the texture, the id is what is saved in banners
            BannerPattern snout = EnumHelper.addEnum(BannerPattern.class, "PIGLIN", new Class<?>[]{String.class, String.class}, "piglin", "pig");
            NAMES.add("piglin_banner_pattern");
            PATTERNS.add(snout);
        } catch (Throwable e) {
            SisyphusBackport.logger.warn("Could not add the snout banner pattern, it will not be in the game", e);
        }
    }

    public static List<String> itemNames() {
        return Collections.unmodifiableList(NAMES);
    }

    public static List<BannerPattern> specialPatterns() {
        return Collections.unmodifiableList(PATTERNS);
    }

    @Nullable
    public static BannerPattern patternOf(ItemStack stack) {
        return stack.getItem() instanceof ItemBannerPattern ? ((ItemBannerPattern) stack.getItem()).getPattern() : null;
    }

    @Nullable
    public static Item itemFor(BannerPattern pattern) {
        int index = PATTERNS.indexOf(pattern);
        return index < 0 ? null : ModItems.get(NAMES.get(index));
    }

    public static boolean isSpecial(BannerPattern pattern) {
        return PATTERNS.contains(pattern);
    }

    public static List<BannerPattern> genericPatterns() {
        List<BannerPattern> list = new ArrayList<>();
        for (BannerPattern pattern : BannerPattern.values()) {
            if (pattern != BannerPattern.BASE && !isSpecial(pattern)) {
                list.add(pattern);
            }
        }
        return list;
    }

    public BannerPattern getPattern() {
        return this.pattern;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return this.rarity;
    }
}
