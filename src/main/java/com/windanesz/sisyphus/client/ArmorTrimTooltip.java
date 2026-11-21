package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.item.ArmorTrims;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SisyphusBackport.MODID)
public final class ArmorTrimTooltip {

    private ArmorTrimTooltip() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ArmorTrims.Trim trim = ArmorTrims.get(event.getItemStack());
        if (trim == null) {
            return;
        }
        List<String> tooltip = event.getToolTip();
        int at = Math.min(1, tooltip.size());
        tooltip.add(at, " " + trim.material.color + I18n.format("trim_material.sisyphus." + trim.material.name));
        tooltip.add(at, " " + trim.material.color + I18n.format("trim_pattern.sisyphus." + trim.pattern));
        tooltip.add(at, TextFormatting.GRAY + I18n.format("item.sisyphus.smithing_template.upgrade"));
    }
}
