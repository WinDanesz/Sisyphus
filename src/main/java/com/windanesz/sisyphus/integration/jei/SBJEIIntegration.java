package com.windanesz.sisyphus.integration.jei;

import net.minecraftforge.fml.common.Loader;

public final class SBJEIIntegration {

    public static final String JEI_MOD_ID = "jei";

    private static boolean loaded;

    private SBJEIIntegration() {
    } // no instances

    public static void init() {
        loaded = Loader.isModLoaded(JEI_MOD_ID);
    }

    public static boolean enabled() {
        return loaded;
    }

    public static void updateBrushingTab() {
        // SBJEIPlugin is only touched when JEI is there, so that a game without it does not load its classes
        if (enabled()) {
            SBJEIPlugin.updateBrushingTab();
        }
    }
}
