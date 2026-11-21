package com.windanesz.sisyphus.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSmithingTemplate extends Item {

    /**
     * Null for the netherite upgrade.
     */
    @Nullable
    public final String pattern;

    public ItemSmithingTemplate(@Nullable String pattern) {
        this.pattern = pattern;
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        if ("silence".equals(this.pattern)) {
            return EnumRarity.EPIC;
        }
        if ("ward".equals(this.pattern) || "eye".equals(this.pattern) || "vex".equals(this.pattern) || "spire".equals(this.pattern)) {
            return EnumRarity.RARE;
        }
        return EnumRarity.UNCOMMON;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        String kind = this.pattern == null ? "netherite_upgrade" : "armor_trim";
        String prefix = "item.sisyphus.smithing_template.";
        tooltip.add(TextFormatting.GRAY + I18n.format(this.pattern == null ? "upgrade.sisyphus.netherite_upgrade" : "trim_pattern.sisyphus." + this.pattern));
        tooltip.add("");
        tooltip.add(TextFormatting.GRAY + I18n.format(prefix + "applies_to"));
        tooltip.add(" " + TextFormatting.BLUE + I18n.format(prefix + kind + ".applies_to"));
        tooltip.add(TextFormatting.GRAY + I18n.format(prefix + "ingredients"));
        tooltip.add(" " + TextFormatting.BLUE + I18n.format(prefix + kind + ".ingredients"));
    }
}
