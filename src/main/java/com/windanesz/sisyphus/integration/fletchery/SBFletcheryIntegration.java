package com.windanesz.sisyphus.integration.fletchery;

import com.windanesz.sisyphus.Settings;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

/**
 * When Fletchery is installed, its fletching table replaces the plain one of this mod, which has the same recipe.
 */
public final class SBFletcheryIntegration {

    public static final String FLETCHERY_MOD_ID = "fletchery";

    public static final ResourceLocation FLETCHING_TABLE = new ResourceLocation(FLETCHERY_MOD_ID, "fletching_table");

    private static boolean loaded;

    private SBFletcheryIntegration() {
    } // no instances

    public static void init() {
        loaded = Loader.isModLoaded(FLETCHERY_MOD_ID);
    }

    public static boolean enabled() {
        return Settings.compatibility.fletcheryIntegration && loaded;
    }

    public static Block fletchingTable() {
        return enabled() ? Block.REGISTRY.getObject(FLETCHING_TABLE) : Blocks.AIR;
    }
}
