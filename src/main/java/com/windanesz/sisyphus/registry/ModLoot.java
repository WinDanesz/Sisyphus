package com.windanesz.sisyphus.registry;

import com.google.gson.JsonObject;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.item.Item;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public final class ModLoot {

    /**
     * One roll at these odds matches, on average, the stronghold corridors' 2-3 rolls at 1 in 101 and the monster rooms' 1-3 rolls at 2 in
     * 144.
     */
    public static final int STRONGHOLD_EMPTY_WEIGHT = 39;
    public static final int MONSTER_ROOM_EMPTY_WEIGHT = 35;

    private ModLoot() {
    } // No instances!

    public static boolean isItemTurnedOff(String type, JsonObject entry) {
        if (!"item".equals(type) || !JsonUtils.isString(entry, "name")) {
            return false;
        }
        ResourceLocation name = new ResourceLocation(JsonUtils.getString(entry, "name"));
        return SisyphusBackport.MODID.equals(name.getNamespace()) && !Item.REGISTRY.containsKey(name);
    }

    public static void logSkippedEntry(JsonObject entry) {
        if (SisyphusBackport.logger != null) {
            SisyphusBackport.logger.debug("Left {} out of a loot table, as the config turned it off", JsonUtils.getString(entry, "name"));
        }
    }

    /**
     * There are no bastions, so the Pigstep disc and the snout pattern are rare nether fortress chest loot instead.
     */
    @SubscribeEvent
    public static void onFortressLootLoad(LootTableLoadEvent event) {
        if (!LootTableList.CHESTS_NETHER_BRIDGE.equals(event.getName())) {
            return;
        }
        List<LootEntry> entries = new ArrayList<>();
        for (String name : new String[]{"music_disc_pigstep", "piglin_banner_pattern"}) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name));
            if (item != null) {
                entries.add(new LootEntryItem(item, 1, 0, new LootFunction[0], new LootCondition[0], SisyphusBackport.MODID + "_" + name));
            }
        }
        if (entries.isEmpty()) {
            return;
        }
        entries.add(new LootEntryEmpty(40 - entries.size(), 0, new LootCondition[0], SisyphusBackport.MODID + "_nothing"));
        event.getTable().addPool(new LootPool(entries.toArray(new LootEntry[0]), new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0),
                SisyphusBackport.MODID + "_bastion_treasure"));
    }

    @SubscribeEvent
    public static void onOthersideLootLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        int empty;
        if (LootTableList.CHESTS_STRONGHOLD_CORRIDOR.equals(name)) {
            empty = STRONGHOLD_EMPTY_WEIGHT;
        } else if (LootTableList.CHESTS_SIMPLE_DUNGEON.equals(name)) {
            empty = MONSTER_ROOM_EMPTY_WEIGHT;
        } else {
            return;
        }
        Item disc = ModItems.otherside();
        if (disc == null) {
            return;
        }
        LootEntry[] entries = {
                new LootEntryItem(disc, 1, 0, new LootFunction[0], new LootCondition[0], SisyphusBackport.MODID + "_otherside"),
                new LootEntryEmpty(empty, 0, new LootCondition[0], SisyphusBackport.MODID + "_no_otherside")};
        event.getTable().addPool(new LootPool(entries, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0),
                SisyphusBackport.MODID + "_otherside"));
    }
}
