package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SisyphusBackport.MODID)
public final class GhostSlotTextures {

    public static final String[] NAMES = {"dye", "banner", "pattern",
            // the smithing table
            "smithing_template_armor_trim", "smithing_template_netherite_upgrade", "helmet", "chestplate", "leggings", "boots", "sword", "pickaxe",
            "axe", "shovel", "hoe", "ingot", "redstone_dust", "lapis_lazuli", "quartz", "diamond", "emerald", "amethyst_shard"};

    private GhostSlotTextures() {
    }

    public static String spriteOf(String name) {
        return SisyphusBackport.MODID + ":gui/slot_" + name;
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        for (String name : NAMES) {
            event.getMap().registerSprite(new ResourceLocation(spriteOf(name)));
        }
    }
}
