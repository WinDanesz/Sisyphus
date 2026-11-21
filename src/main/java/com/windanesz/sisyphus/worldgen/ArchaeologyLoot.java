package com.windanesz.sisyphus.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.item.ArmorTrims;
import com.windanesz.sisyphus.item.ItemSuspiciousStew;
import com.windanesz.sisyphus.registry.ModItems;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.ObjIntConsumer;

@Mod.EventBusSubscriber
public final class ArchaeologyLoot {

    public static final String[] LOOT_TABLES = {"desert_pyramid", "desert_well", "ocean_ruin_cold", "ocean_ruin_warm", "trail_ruins_common",
            "trail_ruins_rare"};
    /**
     * Each is an item {@code <name>_pottery_sherd}, with the pattern {@code textures/entity/decorated_pot/<name>_pottery_pattern.png}.
     * Wizard, dragon and full moon are this mod's own.
     */
    private static final String[] SHERDS = {"angler", "archer", "arms_up", "blade", "brewer", "burn", "danger", "explorer", "friend", "heart",
            "heartbreak", "howl", "miner", "mourner", "plenty", "prize", "sheaf", "shelter", "skull", "snort",
            // Not in the game: drawn for this mod
            "wizard", "dragon", "full_moon"};
    private static final String SHERD_SUFFIX = "_pottery_sherd";

    private ArchaeologyLoot() {
    }

    public static String[] sherdNames() {
        return SHERDS.clone();
    }

    public static boolean isSherd(Item item) {
        ResourceLocation name = item.getRegistryName();
        return name != null && name.getNamespace().equals(SisyphusBackport.MODID) && name.getPath().endsWith(SHERD_SUFFIX);
    }

    public static String patternOf(Item item) {
        return isSherd(item) ? item.getRegistryName().getPath().substring(0, item.getRegistryName().getPath().length() - SHERD_SUFFIX.length()) : null;
    }

    public static ResourceLocation lootTable(String name) {
        return new ResourceLocation(SisyphusBackport.MODID, "archaeology/" + name);
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (!name.getNamespace().equals(SisyphusBackport.MODID) || !name.getPath().startsWith("archaeology/")) {
            return;
        }
        LootPool pool = event.getTable().getPool("main");
        if (pool == null) {
            return;
        }
        Item stew = ModItems.get("suspicious_stew");
        forEachAddedEntry(name.getPath().substring("archaeology/".length()), (item, weight) -> pool.addEntry(new LootEntryItem(item, weight, 0,
                item == stew ? new LootFunction[]{new StewEffect()} : new LootFunction[0], new LootCondition[0], item.getRegistryName().getPath())));
    }

    private static void forEachAddedEntry(String table, ObjIntConsumer<Item> entry) {
        if (table.equals("trail_ruins_common") && Settings.buildingBlocks.enableCandles) {
            for (String color : new String[]{"red", "green", "purple", "brown"}) {
                Item candle = Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, color + "_candle"));
                if (candle != null && candle != Items.AIR) {
                    entry.accept(candle, 2);
                }
            }
        }
        if (table.equals("trail_ruins_rare")) {
            // the four armor trims of the trail ruins
            for (String pattern : new String[]{"wayfinder", "raiser", "shaper", "host"}) {
                Item template = ArmorTrims.template(pattern);
                if (template != null) {
                    entry.accept(template, 1);
                }
            }
        }
        if (table.equals("desert_well")) {
            Item stew = ModItems.get("suspicious_stew");
            if (stew != null && stew != Items.AIR) {
                entry.accept(stew, 1);
            }
        }
    }

    public static String suspiciousBlockOf(String table) {
        return table.startsWith("desert_") || table.equals("ocean_ruin_warm") ? "suspicious_sand" : "suspicious_gravel";
    }

    /**
     * An item listed twice is one find with both weights.
     */
    public static List<Find> finds(String table) {
        Map<String, Find> finds = new LinkedHashMap<>();
        String path = "/assets/" + SisyphusBackport.MODID + "/loot_tables/archaeology/" + table + ".json";
        try (InputStream input = ArchaeologyLoot.class.getResourceAsStream(path)) {
            if (input == null) {
                return new ArrayList<>();
            }
            JsonObject json = new JsonParser().parse(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement pool : json.getAsJsonArray("pools")) {
                for (JsonElement element : pool.getAsJsonObject().getAsJsonArray("entries")) {
                    JsonObject entry = element.getAsJsonObject();
                    if (!"item".equals(entry.get("type").getAsString())) {
                        continue;
                    }
                    int meta = 0;
                    if (entry.has("functions")) {
                        for (JsonElement function : entry.getAsJsonArray("functions")) {
                            JsonObject object = function.getAsJsonObject();
                            if ("set_data".equals(object.get("function").getAsString()) && object.get("data").isJsonPrimitive()) {
                                meta = object.get("data").getAsInt();
                            }
                        }
                    }
                    add(finds, new ResourceLocation(entry.get("name").getAsString()), meta, entry.has("weight") ? entry.get("weight").getAsInt() : 1);
                }
            }
        } catch (IOException | RuntimeException e) {
            SisyphusBackport.logger.warn("Could not read the loot table {}", path, e);
        }
        forEachAddedEntry(table, (item, weight) -> add(finds, item.getRegistryName(), 0, weight));
        return new ArrayList<>(finds.values());
    }

    private static void add(Map<String, Find> finds, ResourceLocation item, int meta, int weight) {
        finds.merge(item + "@" + meta, new Find(item, meta, weight), (old, added) -> new Find(item, meta, old.weight + added.weight));
    }

    public static final class Find {
        public final ResourceLocation item;
        public final int meta;
        public final int weight;

        Find(ResourceLocation item, int meta, int weight) {
            this.item = item;
            this.meta = meta;
            this.weight = weight;
        }
    }

    private static final class StewEffect extends LootFunction {

        private static final Object[][] EFFECTS = {{MobEffects.NIGHT_VISION, 7, 10}, {MobEffects.JUMP_BOOST, 7, 10}, {MobEffects.WEAKNESS, 6, 8},
                {MobEffects.BLINDNESS, 5, 7}, {MobEffects.POISON, 10, 20}, {MobEffects.SATURATION, 7, 10}};

        StewEffect() {
            super(new LootCondition[0]);
        }

        @Override
        public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
            Object[] effect = EFFECTS[rand.nextInt(EFFECTS.length)];
            Potion potion = (Potion) effect[0];
            int min = (Integer) effect[1];
            int max = (Integer) effect[2];
            int seconds = min + rand.nextInt(max - min + 1);
            // An instant effect, as saturation, lasts ticks and not seconds
            ItemSuspiciousStew.addEffect(stack, potion, potion.isInstant() ? seconds : seconds * 20);
            return stack;
        }
    }
}
