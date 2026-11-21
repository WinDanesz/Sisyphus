package com.windanesz.sisyphus.util;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public final class AdvancementHelper {

    private AdvancementHelper() {
    }

    /**
     * Vanilla advancements have their own criteria, not the "done" of ours.
     */
    public static void grantVanilla(EntityPlayerMP player, String path, String criterion) {
        if (!Settings.extras.enableAdvancements) {
            return;
        }
        Advancement advancement = player.getServerWorld().getAdvancementManager().getAdvancement(new ResourceLocation(path));
        if (advancement != null) {
            player.getAdvancements().grantCriterion(advancement, criterion);
        }
    }

    public static void grant(EntityPlayerMP player, String name) {
        if (!Settings.extras.enableAdvancements) {
            return;
        }
        Advancement advancement = player.getServerWorld().getAdvancementManager().getAdvancement(new ResourceLocation(SisyphusBackport.MODID, name));
        if (advancement != null) {
            player.getAdvancements().grantCriterion(advancement, "done");
        }
    }
}
