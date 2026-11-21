package com.windanesz.sisyphus.integration.futuremc;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityPanda;
import com.windanesz.sisyphus.integration.fletchery.SBFletcheryIntegration;
import com.windanesz.sisyphus.integration.futuremc.ChunkConversion.Decoder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;
import java.util.function.UnaryOperator;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class SBFutureMCIntegration {

    public static final String FUTURE_MC_MOD_ID = "futuremc";

    private static final int DATA_VERSION = 1;
    private static final Decoder NO_PROPERTIES = ChunkConversion.NO_PROPERTIES;
    private static final String[] WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
    private static final String[] NEW_WOODS = {"spruce", "birch", "jungle", "acacia", "dark_oak"};
    private static final Map<String, BlockRule> BLOCKS = new HashMap<>();
    private static final Map<String, ResourceLocation> ITEMS = new HashMap<>();
    private static final Map<String, ResourceLocation> BLOCK_ITEMS = new HashMap<>();
    private static final String[] DYES = {"white_dye", "blue_dye", "brown_dye", "black_dye"};
    private static final String[] BANNER_PATTERNS = {"flower_banner_pattern", "creeper_banner_pattern", "skull_banner_pattern",
            "mojang_banner_pattern", null, "piglin_banner_pattern"};
    private static final Map<String, String> TILE_ENTITIES = new HashMap<>();
    private static final Set<String> ENTITIES = new HashSet<>(Arrays.asList("panda", "cod", "salmon", "pufferfish", "tropical_fish"));
    private static final Set<String> ENCHANTMENTS = new HashSet<>(Arrays.asList("multishot", "quick_charge", "piercing"));
    private static boolean enabled;
    private static volatile Boolean worldHasBlocks;

    static {
        for (String wood : WOODS) {
            block("stripped_" + wood + "_log", own("stripped_" + wood + "_log"), SBFutureMCIntegration::axis);
            block("stripped_" + wood + "_wood", own("stripped_" + wood + "_wood"), SBFutureMCIntegration::axis);
            block(wood + "_wood", own(wood + "_wood"), SBFutureMCIntegration::axis);
        }
        for (String wood : NEW_WOODS) {
            block(wood + "_trapdoor", new ResourceLocation("trapdoor"), null);
            block(wood + "_standing_sign", own(wood + "_standing_sign"), null);
            block(wood + "_wall_sign", own(wood + "_wall_sign"), null);
        }

        // Blocks without properties, in Future MC or in this mod
        for (String name : new String[]{"lily_of_the_valley", "cornflower", "wither_rose", "smooth_stone", "smooth_sandstone", "smooth_quartz",
                "smooth_red_sandstone", "soul_soil", "chain", "netherite_block", "ancient_debris", "nether_gold_ore", "blackstone",
                "polished_blackstone", "polished_blackstone_bricks", "cracked_polished_blackstone_bricks", "chiseled_polished_blackstone",
                "gilded_blackstone", "brick_wall", "prismarine_wall", "red_sandstone_wall", "mossy_stone_brick_wall", "granite_wall",
                "stone_brick_wall", "nether_brick_wall", "andesite_wall", "red_nether_brick_wall", "sandstone_wall", "end_stone_brick_wall",
                "diorite_wall", "blackstone_wall", "polished_blackstone_brick_wall", "polished_blackstone_wall"}) {
            block(name, own(name), NO_PROPERTIES);
        }
        block("blue_ice", new ResourceLocation("packed_ice"), NO_PROPERTIES);
        // The slabs of Future MC are whole blocks, so they become double slabs, and their items the slab
        for (String prefix : new String[]{"blackstone", "polished_blackstone", "polished_blackstone_brick"}) {
            block(prefix + "_slab", own(prefix + "_double_slab"), NO_PROPERTIES);
            BLOCK_ITEMS.put(prefix + "_slab", own(prefix + "_slab"));
        }
        for (String name : new String[]{"blackstone_stairs", "polished_blackstone_stairs", "polished_blackstone_brick_stairs", "polished_blackstone_button"}) {
            block(name, own(name), null);
        }
        block("soul_fire_torch", own("soul_torch"), null);

        block("lantern", own("lantern"), SBFutureMCIntegration::hanging);
        block("soul_fire_lantern", own("soul_lantern"), SBFutureMCIntegration::hanging);
        block("stonecutter", own("stonecutter"), (meta, p) -> p.put("facing", EnumFacing.byHorizontalIndex(meta & 3).getName()));
        block("barrel", own("barrel"), (meta, p) -> p.put("facing", EnumFacing.byIndex(meta > 5 ? meta - 6 : meta).getName()));
        block("smoker", own("smoker"), SBFutureMCIntegration::furnace);
        block("blast_furnace", own("blast_furnace"), SBFutureMCIntegration::furnace);
        block("campfire", own("campfire"), SBFutureMCIntegration::furnace);
        // The four workstations face the way that they were placed, in the encoding of all six directions
        for (String name : new String[]{"loom", "fletching_table", "smithing_table", "cartography_table"}) {
            block(name, own(name), SBFutureMCIntegration::workstation);
        }
        block("grindstone", own("grindstone"), SBFutureMCIntegration::grindstone);
        block("composter", own("composter"), (meta, p) -> p.put("level", Integer.toString(Math.min(meta, 8))));
        block("bell", own("bell"), SBFutureMCIntegration::bell);
        block("sweet_berry_bush", own("sweet_berry_bush"), (meta, p) -> p.put("age", Integer.toString(Math.min(meta, 3))));
        block("bamboo", own("bamboo"), SBFutureMCIntegration::bamboo);
        block("scaffolding", own("scaffolding"), (meta, p) -> {
            p.put("distance", Integer.toString(meta & 7));
            p.put("bottom", Boolean.toString(meta > 7));
        });

        for (String dye : DYES) {
            ITEMS.put(dye, own(dye));
        }
        for (String name : new String[]{"crossbow", "sweet_berries", "suspicious_stew", "netherite_ingot", "netherite_scrap", "netherite_axe",
                "netherite_hoe", "netherite_pickaxe", "netherite_shovel", "netherite_sword", "netherite_helmet", "netherite_chestplate",
                "netherite_leggings", "netherite_boots"}) {
            ITEMS.put(name, own(name));
        }
        ITEMS.put("record_pigstep", own("music_disc_pigstep"));
        ITEMS.put("record_otherside", own("music_disc_otherside"));
        // The fish are lost, the water is kept
        for (String fish : new String[]{"pufferfish", "cod", "tropical_fish", "salmon"}) {
            ITEMS.put(fish + "_bucket", new ResourceLocation("water_bucket"));
        }

        TILE_ENTITIES.put("barrel", SisyphusBackport.MODID + ":barrel");
        TILE_ENTITIES.put("smoker", SisyphusBackport.MODID + ":smoker");
        TILE_ENTITIES.put("blast_furnace", SisyphusBackport.MODID + ":blast_furnace");
        TILE_ENTITIES.put("campfire", SisyphusBackport.MODID + ":campfire");
        TILE_ENTITIES.put("bell", SisyphusBackport.MODID + ":bell");
        TILE_ENTITIES.put("sign", "minecraft:sign");
        TILE_ENTITIES.put("composter", null);
    }

    private SBFutureMCIntegration() {
    }

    private static ResourceLocation own(String name) {
        return new ResourceLocation(SisyphusBackport.MODID, name);
    }

    private static void block(String name, ResourceLocation target, Decoder decoder) {
        BLOCKS.put(name, new BlockRule(target, decoder));
    }

    private static void axis(int meta, Map<String, String> p) {
        ChunkConversion.logAxis(meta, p);
    }

    // The metadata of the Future MC blocks, read with the property names that the blocks of this mod use too

    private static void hanging(int meta, Map<String, String> p) {
        p.put("hanging", Boolean.toString(meta == 1));
    }

    private static void furnace(int meta, Map<String, String> p) {
        p.put("facing", EnumFacing.byHorizontalIndex(meta & 3).getName());
        p.put("lit", Boolean.toString((meta & 4) != 0));
    }

    private static void workstation(int meta, Map<String, String> p) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        p.put("facing", (facing.getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : facing).getName());
    }

    private static void grindstone(int meta, Map<String, String> p) {
        p.put("face", meta < 12 ? new String[]{"floor", "wall", "ceiling"}[meta / 4] : "floor");
        p.put("facing", meta < 12 ? new String[]{"north", "east", "south", "west"}[meta % 4] : "north");
    }

    private static void bell(int meta, Map<String, String> p) {
        p.put("attachment", new String[]{"floor", "ceiling", "single_wall", "double_wall"}[(meta >> 2) & 3]);
        p.put("facing", EnumFacing.byHorizontalIndex(meta & 3).getName());
    }

    private static void bamboo(int meta, Map<String, String> p) {
        int rest = Math.min(meta, 11) % 6;
        p.put("stage", meta >= 6 ? "1" : "0");
        p.put("age", rest >= 3 ? "1" : "0");
        p.put("leaves", new String[]{"none", "small", "large"}[rest % 3]);
    }

    public static boolean enabled() {
        return enabled;
    }

    // Setting up

    public static void init() {
        enabled = Settings.compatibility.convertFutureMCWorlds && !Loader.isModLoaded(FUTURE_MC_MOD_ID);
        if (!enabled) {
            return;
        }
        if (SBFletcheryIntegration.enabled()) {
            // This mod has no fletching table of its own then
            block("fletching_table", SBFletcheryIntegration.FLETCHING_TABLE, SBFutureMCIntegration::workstation);
        }
        ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(SisyphusBackport.MODID, DATA_VERSION);
        fixes.registerFix(FixTypes.CHUNK, new Fix(SBFutureMCIntegration::fixChunk));
        // The stacks of the containers of the game, of mobs and of players; the others are translated when they are read
        fixes.registerFix(FixTypes.ITEM_INSTANCE, new Fix(tag -> {
            fixItem(tag);
            return tag;
        }));
    }

    /**
     * Raw, so it is called for every registry. Blocks are ignored so that they stay in the chunks until those are translated.
     */
    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onMissingMappings(RegistryEvent.MissingMappings event) {
        if (!enabled) {
            return;
        }
        // The event has the registry that the world is being loaded into, not the one of ForgeRegistries, so it is known by its type
        Class<?> type = event.getRegistry().getRegistrySuperType();
        for (RegistryEvent.MissingMappings.Mapping mapping : (List<RegistryEvent.MissingMappings.Mapping>) event.getAllMappings()) {
            if (!FUTURE_MC_MOD_ID.equals(mapping.key.getNamespace())) {
                continue;
            }
            IForgeRegistryEntry target = remapTarget(type, mapping.key.getPath());
            // Only into a world that does not have it yet: one that has it keeps its number, and its stacks are translated by name
            if (target != null && !event.getRegistry().containsKey(target.getRegistryName())) {
                mapping.remap(target);
            } else {
                mapping.ignore();
            }
        }
    }

    private static IForgeRegistryEntry<?> remapTarget(Class<?> type, String name) {
        IForgeRegistry<?> registry = null;
        ResourceLocation target = null;
        if (type == Item.class) {
            registry = ForgeRegistries.ITEMS;
            // To the item of the first damage value: the others are split when the stacks are read
            target = itemTarget(name, 0);
            // Only to an item of this mod: those of the game are always in the world already
            if (target != null && !SisyphusBackport.MODID.equals(target.getNamespace())) {
                target = null;
            }
        } else if (type == EntityEntry.class && ENTITIES.contains(name)) {
            registry = ForgeRegistries.ENTITIES;
            target = own(name);
        } else if (type == Enchantment.class && ENCHANTMENTS.contains(name)) {
            registry = ForgeRegistries.ENCHANTMENTS;
            target = own(name);
        }
        // The blocks are not remapped: their metadata means something else here, so the chunks are translated instead
        return target != null && registry.containsKey(target) ? registry.getValue(target) : null;
    }

    public static void fixItem(NBTTagCompound stack) {
        if (!enabled) {
            return;
        }
        String id = stack.getString("id");
        if (!id.startsWith(FUTURE_MC_MOD_ID + ":")) {
            return;
        }
        String name = id.substring(FUTURE_MC_MOD_ID.length() + 1);
        ResourceLocation target = itemTarget(name, stack.getShort("Damage"));
        if (target == null || !ForgeRegistries.ITEMS.containsKey(target)) {
            return;
        }
        stack.setString("id", target.toString());
        stack.setShort("Damage", (short) 0);
        if (name.equals("suspicious_stew")) {
            fixStew(stack);
        }
    }

    private static ResourceLocation itemTarget(String name, int damage) {
        if (name.equals("dye")) {
            return damage >= 0 && damage < DYES.length ? own(DYES[damage]) : null;
        }
        if (name.equals("banner_pattern")) {
            return damage >= 0 && damage < BANNER_PATTERNS.length && BANNER_PATTERNS[damage] != null ? own(BANNER_PATTERNS[damage]) : null;
        }
        ResourceLocation item = ITEMS.get(name);
        if (item == null) {
            item = BLOCK_ITEMS.get(name);
        }
        if (item == null && BLOCKS.containsKey(name)) {
            item = BLOCKS.get(name).target;
        }
        return item;
    }

    // Items

    private static void fixStew(NBTTagCompound stack) {
        NBTTagCompound tag = stack.getCompoundTag("tag");
        if (!tag.hasKey("CustomPotionEffects", 9)) {
            return;
        }
        NBTTagList effects = new NBTTagList();
        NBTTagList potionEffects = tag.getTagList("CustomPotionEffects", 10);
        for (int i = 0; i < potionEffects.tagCount(); i++) {
            NBTTagCompound potion = potionEffects.getCompoundTagAt(i);
            NBTTagCompound effect = new NBTTagCompound();
            effect.setInteger("EffectId", potion.getByte("Id") & 255);
            effect.setInteger("EffectDuration", potion.getInteger("Duration"));
            effects.appendTag(effect);
        }
        tag.removeTag("CustomPotionEffects");
        tag.setTag("Effects", effects);
        stack.setTag("tag", tag);
    }

    /**
     * The next world may have other blocks in its list.
     */
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            worldHasBlocks = null;
        }
    }

    private static NBTTagCompound fixChunk(NBTTagCompound chunk) {
        Boolean hasBlocks = worldHasBlocks;
        if (hasBlocks == null) {
            hasBlocks = ChunkConversion.worldHasBlocksOf(FUTURE_MC_MOD_ID);
            worldHasBlocks = hasBlocks;
        }
        if (!hasBlocks) {
            return chunk;
        }
        NBTTagCompound level = chunk.getCompoundTag("Level");
        ChunkConversion.translateBlocks(level, SBFutureMCIntegration::translations, null);
        fixTileEntities(level.getTagList("TileEntities", 10));
        fixEntities(level.getTagList("Entities", 10));
        return chunk;
    }

    // Chunks

    private static IBlockState[] translations(int id) {
        ResourceLocation name = Block.getBlockById(id).getRegistryName();
        if (name == null || !FUTURE_MC_MOD_ID.equals(name.getNamespace())) {
            return ChunkConversion.NONE;
        }
        BlockRule rule = BLOCKS.get(name.getPath());
        // Not in this mod, or turned off in its config: it stays as it is, as with any removed mod
        if (rule == null || !ForgeRegistries.BLOCKS.containsKey(rule.target)) {
            return ChunkConversion.NONE;
        }
        Block target = ForgeRegistries.BLOCKS.getValue(rule.target);
        IBlockState[] states = new IBlockState[16];
        for (int meta = 0; meta < 16; meta++) {
            states[meta] = ChunkConversion.state(target, rule.decoder, meta);
        }
        return states;
    }

    private static void fixTileEntities(NBTTagList tiles) {
        for (int i = tiles.tagCount() - 1; i >= 0; i--) {
            NBTTagCompound tile = tiles.getCompoundTagAt(i);
            String id = tile.getString("id");
            if (!id.startsWith(FUTURE_MC_MOD_ID + ":")) {
                continue;
            }
            String name = id.substring(FUTURE_MC_MOD_ID.length() + 1);
            if (!TILE_ENTITIES.containsKey(name)) {
                continue;
            }
            // Only where the block itself is translated: a tile entity is turned on and off with its block. The signs are always there
            BlockRule owner = BLOCKS.get(name);
            if (owner != null && !ForgeRegistries.BLOCKS.containsKey(owner.target)) {
                continue;
            }
            String target = TILE_ENTITIES.get(name);
            if (target == null) {
                tiles.removeTag(i);
                continue;
            }
            tile.setString("id", target);
            switch (name) {
                case "barrel":
                    moveInventory(tile, tile.hasKey("item") ? "item" : "items");
                    break;
                case "campfire":
                    moveInventory(tile, "Buffer");
                    tile.removeTag("CookingTotalTimes");
                    break;
                case "smoker":
                case "blast_furnace":
                    fixItems(tile.getTagList("Items", 10));
                    // Future MC does not keep how long the item takes, which is always 100 ticks there
                    if (!tile.hasKey("CookTimeTotal")) {
                        tile.setInteger("CookTimeTotal", 100);
                    }
                    tile.removeTag("CurrentItemBurnTime");
                    break;
                default:
                    break;
            }
        }
    }

    private static void moveInventory(NBTTagCompound tile, String key) {
        NBTTagList stacks = tile.getCompoundTag(key).getTagList("Items", 10);
        NBTTagList items = new NBTTagList();
        for (int i = 0; i < stacks.tagCount(); i++) {
            NBTTagCompound stack = stacks.getCompoundTagAt(i).copy();
            stack.setByte("Slot", (byte) stack.getInteger("Slot"));
            items.appendTag(stack);
        }
        fixItems(items);
        tile.removeTag(key);
        tile.setTag("Items", items);
    }

    private static void fixItems(NBTTagList items) {
        for (int i = 0; i < items.tagCount(); i++) {
            fixItem(items.getCompoundTagAt(i));
        }
    }

    private static void fixEntities(NBTTagList entities) {
        for (int i = 0; i < entities.tagCount(); i++) {
            NBTTagCompound entity = entities.getCompoundTagAt(i);
            String id = entity.getString("id");
            if (!id.startsWith(FUTURE_MC_MOD_ID + ":")) {
                continue;
            }
            String name = id.substring(FUTURE_MC_MOD_ID.length() + 1);
            ResourceLocation target = own(name);
            if (!ENTITIES.contains(name) || !ForgeRegistries.ENTITIES.containsKey(target)) {
                continue;
            }
            entity.setString("id", target.toString());
            if (name.equals("panda")) {
                for (String gene : new String[]{"MainGene", "HiddenGene"}) {
                    entity.setInteger(gene, Math.max(0, Arrays.asList(EntityPanda.TYPES).indexOf(entity.getString(gene))));
                }
            }
        }
    }

    static ResourceLocation itemTargetForTest(String name, int damage) {
        return itemTarget(name, damage);
    }

    static IBlockState translateForTest(String name, Block target, int meta) {
        return ChunkConversion.state(target, BLOCKS.get(name).decoder, meta);
    }

    static ResourceLocation blockTargetForTest(String name) {
        BlockRule rule = BLOCKS.get(name);
        return rule == null ? null : rule.target;
    }

    private static final class BlockRule {
        final ResourceLocation target;
        final Decoder decoder;

        BlockRule(ResourceLocation target, Decoder decoder) {
            this.target = target;
            this.decoder = decoder;
        }
    }

    private static final class Fix implements IFixableData {
        private final UnaryOperator<NBTTagCompound> fix;

        Fix(UnaryOperator<NBTTagCompound> fix) {
            this.fix = fix;
        }

        @Override
        public int getFixVersion() {
            return DATA_VERSION;
        }

        @Override
        public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
            return this.fix.apply(compound);
        }
    }
}
