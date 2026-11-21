package com.windanesz.sisyphus.registry;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class ModSounds {

    private static final String[] KINDS = {"break", "step", "place", "hit", "fall"};

    private static final Map<String, SoundEvent> EVENTS = new LinkedHashMap<>();
    public static final SoundEvent ENTITY_COD_AMBIENT = sound("entity.cod.ambient");
    public static final SoundEvent ENTITY_COD_DEATH = sound("entity.cod.death");
    public static final SoundEvent ENTITY_COD_FLOP = sound("entity.cod.flop");
    public static final SoundEvent ENTITY_COD_HURT = sound("entity.cod.hurt");
    public static final SoundEvent ENTITY_SALMON_AMBIENT = sound("entity.salmon.ambient");
    public static final SoundEvent ENTITY_SALMON_DEATH = sound("entity.salmon.death");
    public static final SoundEvent ENTITY_SALMON_FLOP = sound("entity.salmon.flop");
    public static final SoundEvent ENTITY_SALMON_HURT = sound("entity.salmon.hurt");
    public static final SoundEvent ENTITY_TROPICAL_FISH_AMBIENT = sound("entity.tropical_fish.ambient");
    public static final SoundEvent ENTITY_TROPICAL_FISH_DEATH = sound("entity.tropical_fish.death");
    public static final SoundEvent ENTITY_TROPICAL_FISH_FLOP = sound("entity.tropical_fish.flop");
    public static final SoundEvent ENTITY_TROPICAL_FISH_HURT = sound("entity.tropical_fish.hurt");
    public static final SoundEvent ENTITY_PUFFER_FISH_BLOW_OUT = sound("entity.puffer_fish.blow_out");
    public static final SoundEvent ENTITY_PUFFER_FISH_BLOW_UP = sound("entity.puffer_fish.blow_up");
    public static final SoundEvent ENTITY_PUFFER_FISH_DEATH = sound("entity.puffer_fish.death");
    public static final SoundEvent ENTITY_PUFFER_FISH_FLOP = sound("entity.puffer_fish.flop");
    public static final SoundEvent ENTITY_PUFFER_FISH_HURT = sound("entity.puffer_fish.hurt");
    public static final SoundEvent ENTITY_PUFFER_FISH_STING = sound("entity.puffer_fish.sting");
    public static final SoundEvent ITEM_AXE_STRIP = sound("item.axe.strip");
    public static final SoundEvent BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES = sound("block.sweet_berry_bush.pick_berries");
    public static final SoundEvent BLOCK_NOTE_BLOCK_IRON_XYLOPHONE = sound("block.note_block.iron_xylophone");
    public static final SoundEvent BLOCK_NOTE_BLOCK_COW_BELL = sound("block.note_block.cow_bell");
    public static final SoundEvent BLOCK_NOTE_BLOCK_DIDGERIDOO = sound("block.note_block.didgeridoo");
    public static final SoundEvent BLOCK_NOTE_BLOCK_BIT = sound("block.note_block.bit");
    public static final SoundEvent BLOCK_NOTE_BLOCK_BANJO = sound("block.note_block.banjo");
    public static final SoundEvent BLOCK_NOTE_BLOCK_PLING = sound("block.note_block.pling");
    public static final SoundEvent BLOCK_COMPOSTER_EMPTY = sound("block.composter.empty");
    public static final SoundEvent BLOCK_COMPOSTER_FILL = sound("block.composter.fill");
    public static final SoundEvent BLOCK_COMPOSTER_FILL_SUCCESS = sound("block.composter.fill_success");
    public static final SoundEvent BLOCK_COMPOSTER_READY = sound("block.composter.ready");
    public static final SoundEvent BLOCK_BARREL_OPEN = sound("block.barrel.open");
    public static final SoundEvent BLOCK_BARREL_CLOSE = sound("block.barrel.close");
    public static final SoundEvent BLOCK_LANTERN_BREAK = sound("block.lantern.break");
    public static final SoundEvent BLOCK_LANTERN_FALL = sound("block.lantern.fall");
    public static final SoundEvent BLOCK_LANTERN_HIT = sound("block.lantern.hit");
    public static final SoundEvent BLOCK_LANTERN_PLACE = sound("block.lantern.place");
    public static final SoundEvent BLOCK_LANTERN_STEP = sound("block.lantern.step");
    public static final SoundEvent BLOCK_BAMBOO_BREAK = sound("block.bamboo.break");
    public static final SoundEvent BLOCK_BAMBOO_FALL = sound("block.bamboo.fall");
    public static final SoundEvent BLOCK_BAMBOO_HIT = sound("block.bamboo.hit");
    public static final SoundEvent BLOCK_BAMBOO_PLACE = sound("block.bamboo.place");
    public static final SoundEvent BLOCK_BAMBOO_STEP = sound("block.bamboo.step");
    public static final SoundEvent BLOCK_BAMBOO_SAPLING_BREAK = sound("block.bamboo_sapling.break");
    public static final SoundEvent BLOCK_BAMBOO_SAPLING_HIT = sound("block.bamboo_sapling.hit");
    public static final SoundEvent BLOCK_BAMBOO_SAPLING_PLACE = sound("block.bamboo_sapling.place");
    public static final SoundEvent BLOCK_SCAFFOLDING_BREAK = sound("block.scaffolding.break");
    public static final SoundEvent BLOCK_SCAFFOLDING_FALL = sound("block.scaffolding.fall");
    public static final SoundEvent BLOCK_SCAFFOLDING_HIT = sound("block.scaffolding.hit");
    public static final SoundEvent BLOCK_SCAFFOLDING_PLACE = sound("block.scaffolding.place");
    public static final SoundEvent BLOCK_SCAFFOLDING_STEP = sound("block.scaffolding.step");
    public static final SoundEvent BLOCK_CAMPFIRE_CRACKLE = sound("block.campfire.crackle");
    public static final SoundEvent BLOCK_BUBBLE_COLUMN_BUBBLE_POP = sound("block.bubble_column.bubble_pop");
    public static final SoundEvent BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT = sound("block.bubble_column.upwards_ambient");
    public static final SoundEvent BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE = sound("block.bubble_column.upwards_inside");
    public static final SoundEvent BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT = sound("block.bubble_column.whirlpool_ambient");
    public static final SoundEvent BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE = sound("block.bubble_column.whirlpool_inside");
    public static final SoundEvent BLOCK_GRINDSTONE_USE = sound("block.grindstone.use");
    public static final SoundEvent UI_STONECUTTER_TAKE_RESULT = sound("ui.stonecutter.take_result");
    public static final SoundEvent UI_STONECUTTER_SELECT_RECIPE = sound("ui.stonecutter.select_recipe");
    public static final SoundEvent ITEM_CROSSBOW_LOADING_START = sound("item.crossbow.loading_start");
    public static final SoundEvent ITEM_CROSSBOW_LOADING_MIDDLE = sound("item.crossbow.loading_middle");
    public static final SoundEvent ITEM_CROSSBOW_LOADING_END = sound("item.crossbow.loading_end");
    public static final SoundEvent ITEM_CROSSBOW_SHOOT = sound("item.crossbow.shoot");
    public static final SoundEvent ITEM_CROSSBOW_HIT = sound("item.crossbow.hit");
    public static final SoundEvent ITEM_CROSSBOW_QUICK_CHARGE_1 = sound("item.crossbow.quick_charge_1");
    public static final SoundEvent ITEM_CROSSBOW_QUICK_CHARGE_2 = sound("item.crossbow.quick_charge_2");
    public static final SoundEvent ITEM_CROSSBOW_QUICK_CHARGE_3 = sound("item.crossbow.quick_charge_3");
    public static final SoundEvent UI_LOOM_SELECT_PATTERN = sound("ui.loom.select_pattern");
    public static final SoundEvent UI_LOOM_TAKE_RESULT = sound("ui.loom.take_result");
    public static final SoundEvent UI_CARTOGRAPHY_TABLE_TAKE_RESULT = sound("ui.cartography_table.take_result");
    public static final SoundEvent BLOCK_BELL_USE = sound("block.bell.use");
    public static final SoundEvent ITEM_BOOK_PUT = sound("item.book.put");
    public static final SoundEvent ENTITY_FOX_AMBIENT = sound("entity.fox.ambient");
    public static final SoundEvent ENTITY_FOX_BITE = sound("entity.fox.bite");
    public static final SoundEvent ENTITY_FOX_DEATH = sound("entity.fox.death");
    public static final SoundEvent ENTITY_FOX_EAT = sound("entity.fox.eat");
    public static final SoundEvent ENTITY_FOX_HURT = sound("entity.fox.hurt");
    public static final SoundEvent ENTITY_FOX_SLEEP = sound("entity.fox.sleep");
    public static final SoundEvent ENTITY_PANDA_AMBIENT = sound("entity.panda.ambient");
    public static final SoundEvent ENTITY_PANDA_BITE = sound("entity.panda.bite");
    public static final SoundEvent ENTITY_PANDA_DEATH = sound("entity.panda.death");
    public static final SoundEvent ENTITY_PANDA_EAT = sound("entity.panda.eat");
    public static final SoundEvent ENTITY_PANDA_HURT = sound("entity.panda.hurt");
    public static final SoundEvent ENTITY_PANDA_SNEEZE = sound("entity.panda.sneeze");
    public static final SoundEvent ENTITY_PANDA_PRE_SNEEZE = sound("entity.panda.pre_sneeze");
    public static final SoundEvent ENTITY_PANDA_CANT_BREED = sound("entity.panda.cant_breed");
    public static final SoundEvent ENTITY_PANDA_AGGRESSIVE_AMBIENT = sound("entity.panda.aggressive_ambient");
    public static final SoundEvent ENTITY_PANDA_WORRIED_AMBIENT = sound("entity.panda.worried_ambient");
    public static final SoundEvent ENTITY_PANDA_STEP = sound("entity.panda.step");
    public static final SoundEvent BLOCK_SMITHING_TABLE_USE = sound("block.smithing_table.use");
    public static final SoundEvent BLOCK_RESPAWN_ANCHOR_AMBIENT = sound("block.respawn_anchor.ambient");
    public static final SoundEvent BLOCK_RESPAWN_ANCHOR_CHARGE = sound("block.respawn_anchor.charge");
    public static final SoundEvent BLOCK_RESPAWN_ANCHOR_DEPLETE = sound("block.respawn_anchor.deplete");
    public static final SoundEvent BLOCK_RESPAWN_ANCHOR_SET_SPAWN = sound("block.respawn_anchor.set_spawn");
    public static final SoundEvent ITEM_LODESTONE_COMPASS_LOCK = sound("item.lodestone_compass.lock");
    public static final SoundEvent ENTITY_WANDERING_TRADER_AMBIENT = sound("entity.wandering_trader.ambient");
    public static final SoundEvent ENTITY_WANDERING_TRADER_DEATH = sound("entity.wandering_trader.death");
    public static final SoundEvent ENTITY_WANDERING_TRADER_DISAPPEARED = sound("entity.wandering_trader.disappeared");
    public static final SoundEvent ENTITY_WANDERING_TRADER_DRINK_MILK = sound("entity.wandering_trader.drink_milk");
    public static final SoundEvent ENTITY_WANDERING_TRADER_DRINK_POTION = sound("entity.wandering_trader.drink_potion");
    public static final SoundEvent ENTITY_WANDERING_TRADER_HURT = sound("entity.wandering_trader.hurt");
    public static final SoundEvent ENTITY_WANDERING_TRADER_NO = sound("entity.wandering_trader.no");
    public static final SoundEvent ENTITY_WANDERING_TRADER_REAPPEARED = sound("entity.wandering_trader.reappeared");
    public static final SoundEvent ENTITY_WANDERING_TRADER_TRADE = sound("entity.wandering_trader.trade");
    public static final SoundEvent ENTITY_WANDERING_TRADER_YES = sound("entity.wandering_trader.yes");
    public static final SoundEvent RECORD_PIGSTEP = sound("record.pigstep");
    public static final SoundEvent RECORD_OTHERSIDE = sound("record.otherside");
    public static final SoundEvent BLOCK_NETHER_WOOD_DOOR_OPEN = sound("block.nether_wood_door.open");
    public static final SoundEvent BLOCK_NETHER_WOOD_DOOR_CLOSE = sound("block.nether_wood_door.close");
    public static final SoundEvent BLOCK_NETHER_WOOD_TRAPDOOR_OPEN = sound("block.nether_wood_trapdoor.open");
    public static final SoundEvent BLOCK_NETHER_WOOD_TRAPDOOR_CLOSE = sound("block.nether_wood_trapdoor.close");
    public static final SoundEvent BLOCK_NETHER_WOOD_FENCE_GATE_OPEN = sound("block.nether_wood_fence_gate.open");
    public static final SoundEvent BLOCK_NETHER_WOOD_FENCE_GATE_CLOSE = sound("block.nether_wood_fence_gate.close");
    public static final SoundEvent BLOCK_NETHER_WOOD_BUTTON_CLICK_ON = sound("block.nether_wood_button.click_on");
    public static final SoundEvent BLOCK_NETHER_WOOD_BUTTON_CLICK_OFF = sound("block.nether_wood_button.click_off");
    public static final SoundEvent BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_ON = sound("block.nether_wood_pressure_plate.click_on");
    public static final SoundEvent BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF = sound("block.nether_wood_pressure_plate.click_off");
    public static final SoundEvent ITEM_ARMOR_EQUIP_NETHERITE = sound("item.armor.equip_netherite");
    private static final Map<String, SoundType> TYPES = new HashMap<>();

    static {
        blockSounds("basalt", "ancient_debris", "netherite_block", "chain", "nether_gold_ore", "shroomlight", "soul_soil", "nether_bricks", "nylium",
                "fungus", "roots", "stem", "wart_block", "weeping_vines", "nether_sprouts", "gilded_blackstone", "lodestone", "nether_wood");

        blockSounds("amethyst_block", "amethyst_cluster", "calcite", "candle", "azalea", "flowering_azalea", "azalea_leaves", "cave_vines",
                "big_dripleaf", "small_dripleaf", "spore_blossom", "hanging_roots", "rooted_dirt");
        // Buds only have their own break and place sounds
        for (String size : new String[]{"small", "medium", "large"}) {
            String name = size + "_amethyst_bud";
            TYPES.put(name, new SoundType(1.0F, 1.0F, sound("block." + name + ".break"), sound("block.amethyst_block.step"),
                    sound("block." + name + ".place"), sound("block.amethyst_block.hit"), sound("block.amethyst_block.fall")));
        }
        sounds("block.candle.extinguish", "block.candle.ambient", "block.cave_vines.pick_berries", "block.big_dripleaf.tilt_down",
                "block.big_dripleaf.tilt_up");

        blockSounds("cherry_wood", "cherry_leaves", "cherry_sapling", "pink_petals");
        woodSounds("cherry_wood");
        blockSounds("bamboo_wood");
        woodSounds("bamboo_wood");

        blockSounds("hanging_sign", "nether_wood_hanging_sign", "cherry_wood_hanging_sign", "bamboo_wood_hanging_sign");

        blockSounds("suspicious_sand", "suspicious_gravel", "decorated_pot", "mud", "packed_mud", "mud_bricks");
        sounds("block.decorated_pot.shatter", "block.decorated_pot.insert", "block.decorated_pot.insert_fail", "item.brush.brushing.generic",
                "item.brush.brushing.sand", "item.brush.brushing.gravel", "item.brush.brushing.sand.complete", "item.brush.brushing.gravel.complete");

        mobSounds("axolotl", "attack", "death", "hurt", "idle_air", "idle_water", "splash", "swim");
        sounds("item.bucket.fill_axolotl", "item.bucket.empty_axolotl");
        mobSounds("goat", "ambient", "death", "eat", "horn_break", "hurt", "long_jump", "milk", "prepare_ram", "ram_impact", "step");
        for (int i = 0; i < 8; i++) {
            sound("item.goat_horn.sound." + i);
        }
        mobSounds("goat.screaming", "ambient", "death", "eat", "hurt", "long_jump", "milk", "prepare_ram", "ram_impact");
        mobSounds("hoglin", "ambient", "angry", "attack", "converted_to_zombified", "death", "hurt", "retreat", "step");
        mobSounds("piglin", "admiring_item", "ambient", "angry", "celebrate", "converted_to_zombified", "death", "hurt", "jealous", "retreat", "step");
        mobSounds("strider", "ambient", "death", "eat", "happy", "hurt", "retreat", "saddle", "step", "step_lava");
        mobSounds("zoglin", "ambient", "angry", "attack", "death", "hurt", "step");

        // Polished deepslate and the tiles play the deepslate and bricks recordings, set up in sounds.json as in vanilla
        blockSounds("deepslate", "deepslate_bricks", "polished_deepslate", "deepslate_tiles");
    }

    private ModSounds() {
    } // no instances

    private static SoundEvent sound(String name) {
        return EVENTS.computeIfAbsent(name, n -> new SoundEvent(new ResourceLocation(SisyphusBackport.MODID, n)).setRegistryName(n));
    }

    private static void sounds(String... names) {
        for (String name : names) {
            sound(name);
        }
    }

    private static void blockSounds(String... blocks) {
        for (String block : blocks) {
            SoundEvent[] events = new SoundEvent[KINDS.length];
            for (int i = 0; i < KINDS.length; i++) {
                events[i] = sound("block." + block + "." + KINDS[i]);
            }
            TYPES.put(block, new SoundType(1.0F, 1.0F, events[0], events[1], events[2], events[3], events[4]));
        }
    }

    private static void woodSounds(String wood) {
        for (String block : new String[]{"door", "trapdoor", "fence_gate"}) {
            sounds("block." + wood + "_" + block + ".open", "block." + wood + "_" + block + ".close");
        }
        for (String block : new String[]{"button", "pressure_plate"}) {
            sounds("block." + wood + "_" + block + ".click_on", "block." + wood + "_" + block + ".click_off");
        }
    }

    private static void mobSounds(String mob, String... kinds) {
        for (String kind : kinds) {
            sound("entity." + mob + "." + kind);
        }
    }

    public static SoundEvent event(String name) {
        SoundEvent event = EVENTS.get(name);
        if (event == null) {
            throw new IllegalArgumentException("No sound called " + name);
        }
        return event;
    }

    public static SoundType type(String block) {
        SoundType type = TYPES.get(block);
        if (type == null) {
            throw new IllegalArgumentException("No sound type called " + block);
        }
        return type;
    }

    public static SoundType hangingSignType(String wood) {
        if (wood.equals("crimson") || wood.equals("warped")) {
            return TYPES.get("nether_wood_hanging_sign");
        }
        if (wood.equals("cherry") || wood.equals("bamboo")) {
            return TYPES.get(wood + "_wood_hanging_sign");
        }
        return TYPES.get("hanging_sign");
    }

    public static Set<String> eventNames() {
        return Collections.unmodifiableSet(EVENTS.keySet());
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(EVENTS.values().toArray(new SoundEvent[0]));
    }
}
