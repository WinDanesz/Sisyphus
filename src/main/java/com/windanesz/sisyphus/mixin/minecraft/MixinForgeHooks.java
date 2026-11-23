package com.windanesz.sisyphus.mixin.minecraft;

import com.google.gson.JsonObject;
import com.windanesz.sisyphus.registry.ModLoot;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryEmpty;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A loot entry for an item of ours that the config turned off becomes an empty one, instead of the whole table failing to load. Forge asks this
 * hook, which does nothing by itself, before it looks the item up.
 */
@Mixin(value = ForgeHooks.class, remap = false)
public abstract class MixinForgeHooks {

    @Inject(method = "deserializeJsonLootEntry", at = @At("HEAD"), cancellable = true)
    private static void sisyphus$skipItemsTurnedOff(String type, JsonObject json, int weight, int quality, LootCondition[] conditions,
            CallbackInfoReturnable<LootEntry> cir) {
        if (ModLoot.isItemTurnedOff(type, json)) {
            ModLoot.logSkippedEntry(json);
            cir.setReturnValue(new LootEntryEmpty(weight, quality, conditions, ForgeHooks.readLootEntryName(json, type)));
        }
    }
}
