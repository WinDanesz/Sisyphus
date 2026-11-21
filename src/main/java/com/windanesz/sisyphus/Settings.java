package com.windanesz.sisyphus;

import com.windanesz.sisyphus.integration.fletchery.SBFletcheryIntegration;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = SisyphusBackport.MODID, name = "SisyphusBackport")
public class Settings {

    @Config.Name("Building Blocks")
    @Config.Comment("Blocks to build with: stones, bricks, wood, lights and the like.")
    @Config.LangKey("settings.sisyphus:building_blocks")
    public static BuildingBlocks buildingBlocks = new BuildingBlocks();
    @Config.Name("Deepslate")
    @Config.Comment("Deepslate, its ores and where it generates.")
    @Config.LangKey("settings.sisyphus:deepslate")
    public static Deepslate deepslate = new Deepslate();
    @Config.Name("Plants")
    @Config.Comment("Flowers, bushes, bamboo and the plants of the Nether and of the caves.")
    @Config.LangKey("settings.sisyphus:plants")
    public static Plants plants = new Plants();
    @Config.Name("Functional Blocks")
    @Config.Comment("Blocks that do something: workstations, furnaces, storage, fire and redstone.")
    @Config.LangKey("settings.sisyphus:functional_blocks")
    public static FunctionalBlocks functionalBlocks = new FunctionalBlocks();
    @Config.Name("Items and Effects")
    @Config.Comment("Weapons, armor, food, potions, enchantments, banner patterns and music discs.")
    @Config.LangKey("settings.sisyphus:items")
    public static ItemsAndEffects items = new ItemsAndEffects();
    @Config.Name("Archaeology")
    @Config.Comment("The brush, suspicious blocks, pottery sherds and decorated pots. The places they are found are under World Generation.")
    @Config.LangKey("settings.sisyphus:archaeology")
    public static Archaeology archaeology = new Archaeology();
    @Config.Name("Mobs")
    @Config.Comment("Animals, monsters and the wandering trader.")
    @Config.LangKey("settings.sisyphus:mobs")
    public static Mobs mobs = new Mobs();
    @Config.Name("World Generation")
    @Config.Comment("Biomes and features of the world. They are only in new chunks.")
    @Config.LangKey("settings.sisyphus:world_generation")
    public static WorldGeneration worldGeneration = new WorldGeneration();
    @Config.Name("Gameplay Changes")
    @Config.Comment("Changes to how the game plays, which do not add blocks, items or mobs.")
    @Config.LangKey("settings.sisyphus:gameplay")
    public static Gameplay gameplay = new Gameplay();
    @Config.Name("Advancements, Commands and Splash Texts")
    @Config.Comment("Advancements, commands and the texts of the title screen.")
    @Config.LangKey("settings.sisyphus:extras")
    public static Extras extras = new Extras();
    @Config.Name("Compatibility")
    @Config.Comment("How this mod gives way to other mods that have some of its content too.")
    @Config.LangKey("settings.sisyphus:compatibility")
    public static Compatibility compatibility = new Compatibility();

    public static void restoreDefaults() {
        buildingBlocks = new BuildingBlocks();
        deepslate = new Deepslate();
        plants = new Plants();
        functionalBlocks = new FunctionalBlocks();
        items = new ItemsAndEffects();
        archaeology = new Archaeology();
        mobs = new Mobs();
        worldGeneration = new WorldGeneration();
        gameplay = new Gameplay();
        extras = new Extras();
        compatibility = new Compatibility();
    }

    /**
     * An empty list allows every dimension.
     */
    public static boolean allowsDimension(int[] dimensions, World world) {
        return allowsDimension(dimensions, world.provider.getDimension());
    }

    /**
     * An empty list allows every dimension.
     */
    public static boolean allowsDimension(int[] dimensions, int dimension) {
        if (dimensions == null || dimensions.length == 0) {
            return true;
        }
        for (int allowed : dimensions) {
            if (allowed == dimension) {
                return true;
            }
        }
        return false;
    }

    /**
     * Also backs the {@code sisyphus:content_enabled} recipe condition.
     */
    public static boolean isContentEnabled(String set) {
        switch (set) {
            case "deepslate":
                return deepslate.enableDeepslate;
            case "moss":
                return buildingBlocks.enableMoss;
            case "fish":
                return mobs.enableFish;
            case "stripped_wood":
                return buildingBlocks.enableStrippedWood;
            case "prismarine":
                return buildingBlocks.enablePrismarineStairsAndSlabs;
            case "smooth_blocks":
                return buildingBlocks.enableSmoothBlocks;
            case "slow_falling":
                return items.enableSlowFalling;
            case "stone_variants":
                return buildingBlocks.enableStoneVariants;
            case "flowers":
                return plants.enableFlowers;
            case "sweet_berries":
                return plants.enableSweetBerries;
            case "lantern":
                return buildingBlocks.enableLantern;
            case "note_block_sounds":
                return functionalBlocks.enableNoteBlockSounds;
            case "suspicious_stew":
                return items.enableSuspiciousStew;
            case "composter":
                return functionalBlocks.enableComposter;
            case "barrel":
                return functionalBlocks.enableBarrel;
            case "workstations":
                return functionalBlocks.enableWorkstations;
            case "furnaces":
                return functionalBlocks.enableFurnaces;
            case "bamboo":
                return plants.enableBamboo;
            case "scaffolding":
                return functionalBlocks.enableScaffolding;
            case "campfire":
                return functionalBlocks.enableCampfire;
            case "grindstone":
                return functionalBlocks.enableGrindstone;
            case "stonecutter":
                return functionalBlocks.enableStonecutter;
            case "crossbow":
                return items.crossbow.enabled;
            case "throwing_disc":
                return items.throwingDiscs.enabled;
            case "loom":
                return functionalBlocks.enableLoom;
            case "cartography_table":
                return functionalBlocks.enableCartographyTable;
            case "lectern":
                return functionalBlocks.enableLectern;
            case "bell":
                return functionalBlocks.enableBell;
            case "signs":
                return buildingBlocks.enableSigns;
            case "hanging_signs":
                return buildingBlocks.enableHangingSigns;
            case "wood_bookshelves":
                return buildingBlocks.enableWoodBookshelves;
            case "mooshroom":
                return mobs.enableBrownMooshroom;
            case "cat":
                return mobs.enableCats;
            case "fox":
                return mobs.enableFoxes;
            case "goat":
                return mobs.enableGoats;
            case "axolotl":
                return mobs.enableAxolotls;
            case "panda":
                return mobs.enablePandas;
            case "bamboo_jungle":
                return worldGeneration.biomes.enableBambooJungle;
            case "horse_armor":
                return items.enableLeatherHorseArmor;
            case "advancements":
                return extras.enableAdvancements;
            case "commands":
                return extras.enableCommands;
            case "wandering_trader":
                return mobs.enableWanderingTrader;
            case "splash_texts":
                return extras.enableSplashTexts;
            case "netherite":
                return items.enableNetherite;
            case "armor_trims":
                return items.enableArmorTrims && functionalBlocks.enableWorkstations;
            case "blackstone":
                return buildingBlocks.enableBlackstone;
            case "basalt":
                return buildingBlocks.enableBasalt;
            case "nether_gold":
                return buildingBlocks.enableNetherGold;
            case "chain":
                return buildingBlocks.enableChain;
            case "crying_obsidian":
                return buildingBlocks.enableCryingObsidian;
            case "soul_soil":
                return buildingBlocks.enableSoulSoil;
            case "shroomlight":
                return buildingBlocks.enableShroomlight;
            case "nether_brick_variants":
                return buildingBlocks.enableNetherBrickVariants;
            case "soul_fire":
                return functionalBlocks.enableSoulFire;
            case "respawn_anchor":
                return functionalBlocks.enableRespawnAnchor;
            case "lodestone":
                return functionalBlocks.enableLodestone;
            case "target":
                return functionalBlocks.enableTarget;
            case "nether_fungi":
                return plants.enableNetherFungi;
            case "nether_wood":
                return buildingBlocks.enableNetherWood;
            case "cherry_wood":
                return buildingBlocks.enableCherryWood;
            case "cherry_trees":
                return buildingBlocks.enableCherryWood && plants.enableCherryTrees;
            case "cherry_grove":
                return buildingBlocks.enableCherryWood && plants.enableCherryTrees && worldGeneration.biomes.enableCherryGrove;
            case "bamboo_wood":
                return buildingBlocks.enableBambooWood && plants.enableBamboo;
            case "soul_speed":
                return items.enableSoulSpeed;
            case "snout_pattern":
                return items.enableSnoutPattern;
            case "pigstep":
                return items.enablePigstep;
            case "hoglin":
                return mobs.enableHoglins;
            case "piglin":
                return mobs.enablePiglins;
            case "strider":
                return mobs.enableStriders;
            case "fletching_table":
                return functionalBlocks.enableWorkstations && !SBFletcheryIntegration.enabled();
            case "candles":
                return buildingBlocks.enableCandles;
            case "amethyst":
                return buildingBlocks.enableAmethyst;
            case "lush_plants":
                return plants.enableLushPlants;
            case "cave_vines":
                return plants.enableCaveVines;
            case "dripleaf":
                return plants.enableDripleaf;
            case "archaeology":
                return archaeology.enableArchaeology;
            case "mud":
                return buildingBlocks.enableMud;
            case "ruined_portals":
                return worldGeneration.ruinedPortals.enabled;
            default:
                throw new IllegalArgumentException("Unknown content set: " + set);
        }
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(SisyphusBackport.MODID)) {
                ConfigManager.sync(SisyphusBackport.MODID, Config.Type.INSTANCE);
            }
        }
    }

    public static class BuildingBlocks {

        @Config.Comment("Master toggle for the moss block and moss carpet. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Moss")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_moss")
        public boolean enableMoss = true;

        @Config.Comment("Master toggle for wood, stripped logs and stripped wood, including stripping with an axe. When disabled, the blocks are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Stripped Logs and Wood")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_stripped_wood")
        public boolean enableStrippedWood = true;

        @Config.Comment("Extra blocks that can be stripped with an axe, for modded woods, in the format 'modid:log=modid:stripped_log'. The rotation of the log is kept. Blocks named 'stripped_<name>' next to a block called '<name>' are found automatically. Requires a game restart.")
        @Config.Name("Strippable Blocks")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.strippable_blocks")
        public String[] strippableBlocks = new String[0];

        @Config.Comment("Master toggle for prismarine, prismarine brick and dark prismarine stairs and slabs. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Prismarine Stairs and Slabs")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_prismarine_stairs_and_slabs")
        public boolean enablePrismarineStairsAndSlabs = true;

        @Config.Comment("Master toggle for the smooth stone, smooth sandstone, smooth red sandstone and smooth quartz blocks. When disabled, they are not registered and their smelting recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Smooth Blocks")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_smooth_blocks")
        public boolean enableSmoothBlocks = true;

        @Config.Comment("Master toggle for the stairs, slabs and walls of stone, andesite, diorite, granite, mossy blocks, red nether brick, end stone brick and the other stone types added in 1.14. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Stone Stairs, Slabs and Walls")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_stone_variants")
        public boolean enableStoneVariants = true;

        @Config.Comment("Master toggle for blackstone and its polished, chiseled and brick variants, with their stairs, slabs, walls, buttons and pressure plates. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Blackstone")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_blackstone")
        public boolean enableBlackstone = true;

        @Config.Comment("Master toggle for basalt and polished basalt. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Basalt")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_basalt")
        public boolean enableBasalt = true;

        @Config.Comment("Master toggle for chiseled nether bricks, cracked nether bricks and quartz bricks. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Nether Brick Variants")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_nether_brick_variants")
        public boolean enableNetherBrickVariants = true;

        @Config.Comment("Master toggle for crying obsidian. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Crying Obsidian")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_crying_obsidian")
        public boolean enableCryingObsidian = true;

        @Config.Comment("Master toggle for soul soil. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Soul Soil")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_soul_soil")
        public boolean enableSoulSoil = true;

        @Config.Comment("Master toggle for the shroomlight. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Shroomlight")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_shroomlight")
        public boolean enableShroomlight = true;

        @Config.Comment("Master toggle for nether gold ore. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Nether Gold Ore")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_nether_gold")
        public boolean enableNetherGold = true;

        @Config.Comment("Master toggle for the crimson and warped wood: stems, hyphae, planks, slabs, stairs, fences, gates, buttons, pressure plates, doors, trapdoors and signs. Huge fungi need it. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Nether Wood")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_nether_wood")
        public boolean enableNetherWood = true;

        @Config.Comment("Master toggle for the cherry wood: logs, wood, stripped logs and wood, planks, slabs, stairs, fences, gates, buttons, pressure plates, doors, trapdoors, the sign and the bookshelf. The cherry trees under Plants need it. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Cherry Wood")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_cherry_wood")
        public boolean enableCherryWood = true;

        @Config.Comment("Master toggle for the bamboo wood: blocks of bamboo and of stripped bamboo, planks, bamboo mosaic, slabs, stairs, fences, gates, buttons, pressure plates, doors, trapdoors, the sign, the bookshelf, the raft and the chest raft. A block of bamboo is crafted from nine bamboo, so it needs Bamboo in Plants. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Bamboo Wood")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_bamboo_wood")
        public boolean enableBambooWood = true;

        @Config.Comment("Master toggle for the spruce, birch, jungle, acacia and dark oak signs, and dyeing sign text. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("New Signs")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_signs")
        public boolean enableSigns = true;

        @Config.Comment("Makes a sign for each wood of another mod that has none: a mod with a <wood>_log and a <wood>_planks block and no sign of that wood gets one, crafted from six of the planks over a stick, which sounds like the planks. Its pictures are made when the game loads, from those of the oak sign in the colours of the planks. It needs the new signs to be on. Requires a game restart. Default: true")
        @Config.Name("Signs of Modded Woods")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_modded_wood_signs")
        public boolean enableModdedWoodSigns = true;

        @Config.Comment("Finds the woods of other mods that get a sign by the names of their blocks: a <wood>_log and a <wood>_planks block of the same mod, and no sign of that wood. When disabled, only the woods in Modded Sign Woods get one. Requires a game restart. Default: true")
        @Config.Name("Detect Modded Woods Automatically")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.detect_modded_woods")
        public boolean detectModdedWoods = true;

        @Config.Comment("Woods of other mods that get a sign although their blocks are not named <wood>_log and <wood>_planks, in the format 'modid:log=modid:planks'. Add '@meta' after the planks when they are one kind of a block, and '=name' at the end to name the sign sisyphus:<modid>_<name>_sign; otherwise the name is made from those of the planks or the log and the meta. Example: 'examplemod:log_fir=examplemod:planks@2=fir'. Requires a game restart.")
        @Config.Name("Modded Sign Woods")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.modded_sign_woods")
        public String[] moddedSignWoods = new String[0];

        @Config.Comment("Master toggle for the hanging signs of 1.20, of oak, spruce, birch, jungle, acacia and dark oak, and of crimson, warped, cherry and bamboo when their wood is on. Each is crafted from two chains and six stripped logs of its wood (stripped blocks of bamboo for bamboo), so it needs the chain and the stripped logs too. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Hanging Signs")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_hanging_signs")
        public boolean enableHangingSigns = true;

        @Config.Comment("Master toggle for the bookshelves of spruce, birch, jungle, acacia and dark oak, and of crimson and warped when the nether wood is on, as other mods have them. Each is crafted like the oak bookshelf from the planks of its wood, so the oak bookshelf then needs oak planks. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Wood Bookshelves")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_wood_bookshelves")
        public boolean enableWoodBookshelves = true;

        @Config.Comment("Master toggle for the chain. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Chain")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_chain")
        public boolean enableChain = true;

        @Config.Comment("Master toggle for the lantern. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Lantern")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_lantern")
        public boolean enableLantern = true;

        @Config.Comment("Master toggle for candles of the 17 colors and the cakes with a candle on them. When disabled, they are not registered and their recipes are removed. A candle is made of string and honeycomb from another mod, or of string and a slime ball. Requires a game restart. Default: true")
        @Config.Name("Candles")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_candles")
        public boolean enableCandles = true;

        @Config.Comment("Master toggle for amethyst: the block, budding amethyst, the buds and clusters that grow on it, the shard, tinted glass, calcite and smooth basalt. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Amethyst")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_amethyst")
        public boolean enableAmethyst = true;

        @Config.Comment("Master toggle for mud, packed mud and mud bricks of 1.19, with the stairs, slab and wall of mud bricks, and making mud with a water bottle on dirt. The trail ruins are built of mud bricks, and are built of bricks when it is off. Requires a game restart. Default: true")
        @Config.Name("Mud")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:building_blocks.enable_mud")
        public boolean enableMud = true;
    }

    public static class Deepslate {

        @Config.Comment("Master toggle for all deepslate content. When disabled, no deepslate blocks, items, recipes or ore dictionary entries are registered and deepslate world generation is skipped. Requires a game restart. Default: true")
        @Config.Name("Deepslate")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:deepslate.enable_deepslate")
        public boolean enableDeepslate = true;

        @Config.Comment("Enable deepslate world generation. When enabled, stone below the Y-threshold will be replaced with deepslate.")
        @Config.Name("Deepslate Generation")
        @Config.LangKey("settings.sisyphus:deepslate.enable_deepslate_generation")
        public boolean enableDeepslateGeneration = true;

        @Config.Comment("The Y-level at which deepslate generation starts. Blocks below this level will be replaced with deepslate variants. Default: 20")
        @Config.Name("Y-Level Threshold")
        @Config.RangeInt(min = 0, max = 255)
        @Config.LangKey("settings.sisyphus:deepslate.deepslate_y_threshold")
        public int deepslateYThreshold = 20;

        @Config.Comment("The range (in blocks) over which deepslate gradually replaces stone around the Y-threshold. 0 = instant transition, higher values = more gradual. Default: 4")
        @Config.Name("Transition Range")
        @Config.RangeInt(min = 0, max = 16)
        @Config.LangKey("settings.sisyphus:deepslate.deepslate_transition_range")
        public int deepslateTransitionRange = 4;

        @Config.Comment("The dimensions, by number, that stone is turned to deepslate in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. Default: 0")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:deepslate.deepslate_dimensions")
        public int[] deepslateDimensions = new int[]{0};

        @Config.Comment("Replace vanilla stone ores with deepslate ore variants below the Y-threshold")
        @Config.Name("Replace Ores in Deepslate")
        @Config.LangKey("settings.sisyphus:deepslate.replace_ores_with_deepslate")
        public boolean replaceOresWithDeepslate = true;

        @Config.Comment("Makes a deepslate version of each ore of another mod that has a stone background: a block of rock with a single state and no tile entity, called an ore or in the ore dictionary as one. Where its picture shows the stone of the game, deepslate is drawn instead. It needs deepslate to be on, and drops, experience and smelting are those of the ore. Requires a game restart. Default: true")
        @Config.Name("Modded Deepslate Ores")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:deepslate.enable_deepslate_mod_ores")
        public boolean enableDeepslateModOres = true;

        @Config.Comment("Ores that get a deepslate version whatever their name is, as 'modid:block' or 'modid:*', if they are simple blocks of rock. Requires a game restart.")
        @Config.Name("Ore Whitelist")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:deepslate.deepslate_ore_whitelist")
        public String[] deepslateOreWhitelist = new String[0];

        @Config.Comment("Ores that do not get a deepslate version, as 'modid:block' or 'modid:*'. Requires a game restart.")
        @Config.Name("Ore Blacklist")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:deepslate.deepslate_ore_blacklist")
        public String[] deepslateOreBlacklist = new String[0];
    }

    public static class Plants {

        @Config.Comment("Master toggle for the cornflower, lily of the valley and wither rose, and the blue, white, black and brown dyes. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Flowers and Dyes")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:plants.enable_flowers")
        public boolean enableFlowers = true;

        @Config.Comment("Master toggle for sweet berries and sweet berry bushes. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Sweet Berries")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:plants.enable_sweet_berries")
        public boolean enableSweetBerries = true;

        @Config.Comment("Master toggle for bamboo and bamboo shoots, including the sticks that can be crafted from it. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Bamboo")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:plants.enable_bamboo")
        public boolean enableBamboo = true;

        @Config.Comment("Master toggle for the crimson and warped plants: the two nylium blocks, fungi, roots, nether sprouts, weeping and twisting vines and the warped wart block. Where they grow in the Nether is under World Generation. When disabled, they are not registered. Requires a game restart. Default: true")
        @Config.Name("Nether Fungi")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:plants.enable_nether_fungi")
        public boolean enableNetherFungi = true;

        @Config.Comment("Master toggle for the plants of the lush caves: azalea and its leaves, glow lichen, spore blossom, hanging roots and rooted dirt. When disabled, they are not registered. Requires a game restart. Default: true")
        @Config.Name("Lush Cave Plants")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:plants.enable_lush_plants")
        public boolean enableLushPlants = true;

        @Config.Comment("Master toggle for the cherry trees: cherry leaves, which shed pink petals, the cherry sapling and pink petals. Needs Cherry Wood in Building Blocks. Where they grow is under World Generation. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Cherry Trees")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:plants.enable_cherry_trees")
        public boolean enableCherryTrees = true;

        @Config.Comment("Master toggle for cave vines and the glow berries that grow on them. When disabled, they are not registered. Requires a game restart. Default: true")
        @Config.Name("Cave Vines")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:plants.enable_cave_vines")
        public boolean enableCaveVines = true;

        @Config.Comment("Master toggle for the big and small dripleaf. When disabled, they are not registered. Requires a game restart. Default: true")
        @Config.Name("Dripleaf")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:plants.enable_dripleaf")
        public boolean enableDripleaf = true;

        @Config.Comment("Let flower pots hold the small plants of other mods and of this mod, besides those of the game: any plant that grows on the ground and is one block tall, such as a flower, a sapling, a mushroom, a fungus or roots, and the Extra Potted Plants. It is drawn at three quarters of its size, like the plants of the game in a pot. Needs MixinBooter or FermiumBooter. Default: true")
        @Config.Name("Pot Any Small Plant")
        @Config.LangKey("settings.sisyphus:plants.pot_any_small_plant")
        public boolean potAnySmallPlant = true;

        @Config.Comment("More plants that flower pots hold with Pot Any Small Plant, as 'modid:block' or 'modid:*': plants that do not grow like a flower, such as the azaleas. Default: sisyphus:azalea, sisyphus:flowering_azalea")
        @Config.Name("Extra Potted Plants")
        @Config.LangKey("settings.sisyphus:plants.extra_potted_plants")
        public String[] extraPottedPlants = {"sisyphus:azalea", "sisyphus:flowering_azalea"};

        @Config.Comment("Plants that flower pots do not hold with Pot Any Small Plant, as 'modid:block' or 'modid:*', for plants that look wrong in a pot. The plants of the game that fit in a pot always do. Default: sisyphus:nether_sprouts, sisyphus:pink_petals")
        @Config.Name("Unpottable Plants")
        @Config.LangKey("settings.sisyphus:plants.unpottable_plants")
        public String[] unpottablePlants = {"sisyphus:nether_sprouts", "sisyphus:pink_petals"};
    }

    public static class FunctionalBlocks {

        @Config.Comment("Master toggle for the composter. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Composter")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_composter")
        public boolean enableComposter = true;

        @Config.Comment("Master toggle for the barrel. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Barrel")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_barrel")
        public boolean enableBarrel = true;

        @Config.Comment("Master toggle for the fletching table and the smithing table. When disabled, they are not registered and their recipes are removed. The fletching table is a decorative block, as it is in the game; when the Fletchery mod is installed, its fletching table, which has a screen, is used in place of this one. Requires a game restart. Default: true")
        @Config.Name("Fletching and Smithing Tables")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_workstations")
        public boolean enableWorkstations = true;

        @Config.Comment("Master toggle for the blast furnace and the smoker. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Blast Furnace and Smoker")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_furnaces")
        public boolean enableFurnaces = true;

        @Config.Comment("Master toggle for the grindstone. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Grindstone")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_grindstone")
        public boolean enableGrindstone = true;

        @Config.Comment("Master toggle for the stonecutter. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Stonecutter")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_stonecutter")
        public boolean enableStonecutter = true;

        @Config.Comment("Master toggle for the loom and banner patterns. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Loom")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_loom")
        public boolean enableLoom = true;

        @Config.Comment("Master toggle for the cartography table. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Cartography Table")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_cartography_table")
        public boolean enableCartographyTable = true;

        @Config.Comment("Master toggle for the lectern. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Lectern")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_lectern")
        public boolean enableLectern = true;

        @Config.Comment("Lets a filled map be put on the lectern instead of a book. It is drawn on the top of the lectern, and using the lectern shows it big. Default: true")
        @Config.Name("Lectern Holds Maps")
        @Config.LangKey("settings.sisyphus:functional_blocks.lectern_holds_maps")
        public boolean lecternHoldsMaps = true;

        @Config.Comment("Other items that can be put on the lectern, as registry names where * stands for anything: 'modid:item', 'modid:*' or '*book*'. Using the lectern then uses the item, as if it were held, which opens the screen of a guide book. Blocks are left out. Default: *book*")
        @Config.Name("Lectern Items")
        @Config.LangKey("settings.sisyphus:functional_blocks.lectern_items")
        public String[] lecternItems = {"*book*"};

        @Config.Comment("Master toggle for the bell. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Bell")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_bell")
        public boolean enableBell = true;

        @Config.Comment("Ringing a bell sends the villagers within 32 blocks of it into the houses of their village, as in 1.14. Each one runs to the nearest house that is not yet crowded and stays inside for 15 seconds; one with no house within 32 blocks stays where it is. Default: true")
        @Config.Name("Bell Sends Villagers Indoors")
        @Config.LangKey("settings.sisyphus:functional_blocks.bell_sends_villagers_indoors")
        public boolean bellSendsVillagersIndoors = true;

        @Config.Comment("Master toggle for scaffolding. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Scaffolding")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_scaffolding")
        public boolean enableScaffolding = true;

        @Config.Comment("Master toggle for the campfire. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Campfire")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_campfire")
        public boolean enableCampfire = true;

        @Config.Comment("Master toggle for soul fire, the soul torch, the soul lantern and the soul campfire. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Soul Fire")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_soul_fire")
        public boolean enableSoulFire = true;

        @Config.Comment("Master toggle for the respawn anchor. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Respawn Anchor")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_respawn_anchor")
        public boolean enableRespawnAnchor = true;

        @Config.Comment("Master toggle for the lodestone and the lodestone compass. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Lodestone")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_lodestone")
        public boolean enableLodestone = true;

        @Config.Comment("Master toggle for the target block. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Target Block")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_target")
        public boolean enableTarget = true;

        @Config.Comment("Master toggle for the six new note block instruments. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("New Note Block Sounds")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:functional_blocks.enable_note_block_sounds")
        public boolean enableNoteBlockSounds = true;
    }

    public static class ItemsAndEffects {

        @Config.Comment("The crossbow: whether it is there, how many uses it has, how long it takes to load, how hard its arrows hit and which of its enchantments are there.")
        @Config.Name("Crossbow")
        @Config.LangKey("settings.sisyphus:crossbow")
        public Crossbow crossbow = new Crossbow();

        @Config.Comment("The throwing discs: whether they are there, how hard they hit, how fast and far they fly, how often they bounce and which of their enchantments are there. The enchantments add to these as they say.")
        @Config.Name("Throwing Discs")
        @Config.LangKey("settings.sisyphus:throwing_discs")
        public ThrowingDiscs throwingDiscs = new ThrowingDiscs();

        @Config.Comment("Master toggle for netherite: ancient debris, scrap, ingots, blocks, tools and armor, and upgrading diamond gear in the smithing table. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Netherite")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_netherite")
        public boolean enableNetherite = true;

        @Config.Comment("Upgrading diamond gear to netherite in the smithing table needs a netherite upgrade smithing template, as in 1.20. The template is found in the chests of nether fortresses, in place of the bastion remnants, and copied with diamonds and netherrack. When disabled, the netherite ingot alone upgrades it, as in 1.16, and the template is not found in chests. Default: true")
        @Config.Name("Netherite Upgrade Needs Template")
        @Config.LangKey("settings.sisyphus:items.netherite_needs_template")
        public boolean netheriteNeedsTemplate = true;

        @Config.Comment("Master toggle for the armor trims: the armor trim smithing templates, which are found in structures and give armor a pattern in the smithing table, in the color of the ingot or crystal it is made with. Needs the smithing table (Enable Fletching and Smithing Tables). When disabled, the templates are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Armor Trims")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_armor_trims")
        public boolean enableArmorTrims = true;

        @Config.Comment("Master toggle for leather horse armor. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Leather Horse Armor")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_leather_horse_armor")
        public boolean enableLeatherHorseArmor = true;

        @Config.Comment("Master toggle for suspicious stew. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Suspicious Stew")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_suspicious_stew")
        public boolean enableSuspiciousStew = true;

        @Config.Comment("Master toggle for the Slow Falling effect and the potions and tipped arrows that use it. When disabled, none of it is registered. Requires a game restart. Default: true")
        @Config.Name("Slow Falling")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_slow_falling")
        public boolean enableSlowFalling = true;

        @Config.Comment("Master toggle for the Soul Speed enchantment. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Soul Speed")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_soul_speed")
        public boolean enableSoulSpeed = true;

        @Config.Comment("Master toggle for the Snout banner pattern and its banner pattern item. It also needs the loom. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Snout Banner Pattern")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_snout_pattern")
        public boolean enableSnoutPattern = true;

        @Config.Comment("Master toggle for the Pigstep music disc. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Pigstep")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_pigstep")
        public boolean enablePigstep = true;

        @Config.Comment("Master toggle for the Otherside music disc, by Lena Raine, which is found in the chests of stronghold corridors and now and then in those of monster rooms, as in 1.18. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Otherside")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:items.enable_otherside")
        public boolean enableOtherside = true;
    }

    public static class Crossbow {

        @Config.Comment("Master toggle for the crossbow. When disabled, it and its enchantments are not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Enabled")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:crossbow.enabled")
        public boolean enabled = true;

        @Config.Comment("How many times the crossbow can be used before it breaks. 1.18 has 465, and 1.14 had 326. Requires a game restart. Default: 465")
        @Config.Name("Durability")
        @Config.RangeInt(min = 1, max = 100000)
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:crossbow.durability")
        public int durability = 465;

        @Config.Comment("How many ticks it takes to load the crossbow. 20 ticks are a second. Each level of Quick Charge takes a fifth of it off, so Quick Charge III loads in 10 of the 25. Default: 25")
        @Config.Name("Reload Time")
        @Config.RangeInt(min = 1, max = 200)
        @Config.LangKey("settings.sisyphus:crossbow.reload_time")
        public int reloadTime = 25;

        @Config.Comment("How much harder than an arrow from a bow an arrow from the crossbow hits, as a multiple of the damage of the arrow. At 1, a plain arrow hits for 7 to 11 points, as in 1.14. Firework rockets are not changed. Default: 1.0")
        @Config.Name("Damage Multiplier")
        @Config.RangeDouble(min = 0.0D, max = 10.0D)
        @Config.LangKey("settings.sisyphus:crossbow.damage_multiplier")
        public double damageMultiplier = 1.0D;

        @Config.Comment("Toggle for the Multishot enchantment, which loads three arrows or rockets at once. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Multishot")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:crossbow.enable_multishot")
        public boolean enableMultishot = true;

        @Config.Comment("Toggle for the Quick Charge enchantment, which loads the crossbow faster. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Quick Charge")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:crossbow.enable_quick_charge")
        public boolean enableQuickCharge = true;

        @Config.Comment("Toggle for the Piercing enchantment, which lets arrows fly through mobs. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Piercing")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:crossbow.enable_piercing")
        public boolean enablePiercing = true;
    }

    public static class ThrowingDiscs {

        @Config.Comment("Master toggle for the throwing discs of gold, iron and diamond, and of netherite when netherite is on. A thrown disc flies straight, cuts through mobs, bounces off hard blocks and comes back to the thrower. When disabled, they are not registered and their recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Enabled")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:throwing_discs.enabled")
        public boolean enabled = true;

        @Config.Comment("How much harder than the attack damage of its material a disc hits each mob. A sword adds 4, so 3 makes a disc one point weaker than a sword of the same material: 4 for gold, 5 for iron, 6 for diamond and 7 for netherite. Default: 3.0")
        @Config.Name("Damage")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:throwing_discs.damage")
        public double damage = 3.0D;

        @Config.Comment("How fast a disc flies when it is thrown, in blocks per tick. Each level of Velocity adds 20 percent. Bounces make it faster, up to 2.4 or this speed, if that is more. Default: 1.1")
        @Config.Name("Speed")
        @Config.RangeDouble(min = 0.1D, max = 5.0D)
        @Config.LangKey("settings.sisyphus:throwing_discs.speed")
        public double speed = 1.1D;

        @Config.Comment("How many blocks a disc flies out before it turns back. Each level of Reach adds 2, and each bounce 5. Default: 12.0")
        @Config.Name("Range")
        @Config.RangeDouble(min = 1.0D, max = 64.0D)
        @Config.LangKey("settings.sisyphus:throwing_discs.range")
        public double range = 12.0D;

        @Config.Comment("At which bounce off a block a disc turns back. Each level of Ricochet adds 2. 1 sends it back from the first block it hits. Default: 3")
        @Config.Name("Bounces")
        @Config.RangeInt(min = 1, max = 20)
        @Config.LangKey("settings.sisyphus:throwing_discs.bounces")
        public int bounces = 3;

        @Config.Comment("The durability that each bounce costs. Each throw and each mob hit cost one. Unbreaking can save some of it. Default: 2")
        @Config.Name("Bounce Wear")
        @Config.RangeInt(min = 0, max = 20)
        @Config.LangKey("settings.sisyphus:throwing_discs.bounce_wear")
        public int bounceWear = 2;

        @Config.Comment("How many ticks a player waits after a throw before they can throw a disc again. 20 ticks are a second. Default: 10")
        @Config.Name("Cooldown")
        @Config.RangeInt(min = 0, max = 200)
        @Config.LangKey("settings.sisyphus:throwing_discs.cooldown")
        public int cooldown = 10;

        @Config.Comment("How many ticks a player holds use to draw a disc back before it can be thrown, as with a bow: it is thrown when use is let go, and letting go sooner throws nothing. 20 ticks are a second. 0 throws it at once, without drawing it back. On a server, the server's value counts. Default: 10")
        @Config.Name("Wind-up")
        @Config.RangeInt(min = 0, max = 100)
        @Config.LangKey("settings.sisyphus:throwing_discs.wind_up")
        public int windUp = 10;

        @Config.Comment("Toggle for the Velocity enchantment, which makes a disc fly faster. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Velocity")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:throwing_discs.enable_velocity")
        public boolean enableVelocity = true;

        @Config.Comment("Toggle for the Ricochet enchantment, which lets a disc bounce more often and speeds up its bounces. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Ricochet")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:throwing_discs.enable_ricochet")
        public boolean enableRicochet = true;

        @Config.Comment("Toggle for the Reach enchantment, which lets a disc fly further before it turns back. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Reach")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:throwing_discs.enable_reach")
        public boolean enableReach = true;

        @Config.Comment("Toggle for the Rebound enchantment, which lets a disc on its way back hurt the mobs it already hit. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Rebound")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:throwing_discs.enable_rebound")
        public boolean enableRebound = true;
    }

    public static class Archaeology {

        @Config.Comment("Master toggle for the archaeology of 1.20: the brush, suspicious sand and suspicious gravel, the pottery sherds and the decorated pot. When disabled, none of it is registered and its recipes and advancements are removed. Requires a game restart. Default: true")
        @Config.Name("Archaeology")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:archaeology.enable_archaeology")
        public boolean enableArchaeology = true;
    }

    public static class Mobs {

        @Config.Comment("Master toggle for cod, salmon, pufferfish and tropical fish. When disabled, the fish are not registered, so they do not spawn and have no spawn eggs. Requires a game restart. Default: true")
        @Config.Name("Fish")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_fish")
        public boolean enableFish = true;

        @Config.Comment("Master toggle for the cat and the cat spawn egg. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Cats")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_cats")
        public boolean enableCats = true;

        @Config.Comment("Master toggle for the fox and the fox spawn egg. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Foxes")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_foxes")
        public boolean enableFoxes = true;

        @Config.Comment("Master toggle for the panda and the panda spawn egg. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Pandas")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_pandas")
        public boolean enablePandas = true;

        @Config.Comment("Master toggle for the brown mooshroom and milking mooshrooms for suspicious stew. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Brown Mooshroom")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_brown_mooshroom")
        public boolean enableBrownMooshroom = true;

        @Config.Comment("Master toggle for the goat of 1.17 and the goat spawn egg. When disabled, it is not registered and its advancement is removed. Requires a game restart. Default: true")
        @Config.Name("Goats")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_goats")
        public boolean enableGoats = true;

        @Config.Comment("Master toggle for the goat horn of 1.19, which a goat drops when it rams a log, stone, packed ice or a coal, iron or emerald ore instead of what it aimed at. Each goat has two horns, which play one of eight calls; screaming goats have calls of their own. Needs the goats. When disabled, goats keep their horns. Requires a game restart. Default: true")
        @Config.Name("Goat Horns")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_goat_horns")
        public boolean enableGoatHorns = true;

        @Config.Comment("Master toggle for the axolotl of 1.17, the bucket of axolotl and the axolotl spawn egg. When disabled, they are not registered and their advancements are removed. Requires a game restart. Default: true")
        @Config.Name("Axolotls")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_axolotls")
        public boolean enableAxolotls = true;

        @Config.Comment("Master toggle for the wandering trader and his trader llamas, including their spawning and spawn eggs. When disabled, they are not registered. Requires a game restart. Default: true")
        @Config.Name("Wandering Trader")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_wandering_trader")
        public boolean enableWanderingTrader = true;

        @Config.Comment("Master toggle for the piglin: bartering, hunting and turning into zombified piglins, which are drawn with their new model. When disabled, it is not registered and does not spawn. Requires a game restart. Default: true")
        @Config.Name("Piglins")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_piglins")
        public boolean enablePiglins = true;

        @Config.Comment("Master toggle for the hoglin and the zoglin. When disabled, they are not registered and do not spawn, and piglins have nothing to hunt. Requires a game restart. Default: true")
        @Config.Name("Hoglins")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_hoglins")
        public boolean enableHoglins = true;

        @Config.Comment("Master toggle for the strider and the warped fungus on a stick. When disabled, they are not registered and do not spawn, and their recipe is removed. Requires a game restart. Default: true")
        @Config.Name("Striders")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:mobs.enable_striders")
        public boolean enableStriders = true;
    }

    public static class WorldGeneration {

        @Config.Comment("Put the workstations of 1.14 in the villages, in new chunks: the lectern and the cartography table in the library, the blast furnace, smithing table and grindstone at the blacksmith, the smoker at the butcher, the loom and the barrel in the big house, the fletching table and the stonecutter in the small huts, a composter at each field and a bell under the roof of the well. Each one needs its block to be turned on. Default: true")
        @Config.Name("Workstations in Villages")
        @Config.LangKey("settings.sisyphus:world_generation.enable_village_workstations")
        public boolean enableVillageWorkstations = true;

        @Config.Comment("Generate suspicious sand in desert pyramids and desert wells, as in 1.20: a hidden room of sand under each pyramid, and two blocks under the water of each well. Only in new chunks, and only with Archaeology on. Default: true")
        @Config.Name("Suspicious Sand in Deserts")
        @Config.LangKey("settings.sisyphus:world_generation.enable_desert_archaeology")
        public boolean enableDesertArchaeology = true;

        @Config.Comment("The biomes added to the Overworld and how often they are picked. Only in new chunks.")
        @Config.Name("Biomes")
        @Config.LangKey("settings.sisyphus:biomes")
        public Biomes biomes = new Biomes();

        @Config.Comment("Whether trail ruins are made, how far apart they are and the dimensions they are made in. For new chunks, and none of it needs a restart.")
        @Config.Name("Trail Ruins")
        @Config.LangKey("settings.sisyphus:trail_ruins")
        public TrailRuins trailRuins = new TrailRuins();

        @Config.Comment("Whether ocean ruins are made, how far apart they are, the dimensions they are made in and how many are large. For new chunks, and none of it needs a restart.")
        @Config.Name("Ocean Ruins")
        @Config.LangKey("settings.sisyphus:ocean_ruins")
        public OceanRuins oceanRuins = new OceanRuins();

        @Config.Comment("Whether abandoned camps are made, how often and where. For new chunks, and none of it needs a restart.")
        @Config.Name("Abandoned Camps")
        @Config.LangKey("settings.sisyphus:abandoned_camps")
        public AbandonedCamps abandonedCamps = new AbandonedCamps();

        @Config.Comment("Whether ruined portals are made, how far apart they are and the dimensions they are made in. For new chunks, and none of it needs a restart.")
        @Config.Name("Ruined Portals")
        @Config.LangKey("settings.sisyphus:ruined_portals")
        public RuinedPortals ruinedPortals = new RuinedPortals();

        @Config.Comment("Whether amethyst geodes are made, how often and at what heights. For new chunks, and none of it needs a restart.")
        @Config.Name("Amethyst Geodes")
        @Config.LangKey("settings.sisyphus:geodes")
        public Geodes geodes = new Geodes();

        @Config.Comment("Whether lush caves are made, and how: how much of the world they are, their size, their big caves and how thickly they are covered. All of it is for new chunks, and none of it needs a restart.")
        @Config.Name("Lush Caves")
        @Config.LangKey("settings.sisyphus:lush_caves")
        public LushCaves lushCaves = new LushCaves();

        @Config.Comment("The bamboo, sweet berry bushes and flowers that grow in the wild in the biomes of the game. Each also needs its plant to be turned on. For new chunks, and none of it needs a restart.")
        @Config.Name("Wild Plants")
        @Config.LangKey("settings.sisyphus:wild_plants")
        public WildPlants wildPlants = new WildPlants();

        @Config.Comment("The ores, stones and crimson and warped patches that are put into the Nether. Each also needs its block to be turned on. For new chunks, and none of it needs a restart.")
        @Config.Name("Nether")
        @Config.LangKey("settings.sisyphus:nether_generation")
        public NetherGeneration nether = new NetherGeneration();
    }

    public static class Biomes {

        @Config.Comment("Master toggle for the bamboo jungle biome. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("Bamboo Jungle")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:biomes.enable_bamboo_jungle")
        public boolean enableBambooJungle = true;

        @Config.Comment("How often the bamboo jungle is picked among the warm biomes. A jungle has 10 and a desert 30. Requires a game restart. Default: 10")
        @Config.Name("Bamboo Jungle Weight")
        @Config.RangeInt(min = 1, max = 1000)
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:biomes.bamboo_jungle_weight")
        public int bambooJungleWeight = 10;

        @Config.Comment("Master toggle for the cherry grove biome: hills of pale green grass under cherry trees, with pink petals on the ground. Needs Cherry Trees in Plants. When disabled, it is not registered. Requires a game restart. Default: true")
        @Config.Name("Cherry Grove")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:biomes.enable_cherry_grove")
        public boolean enableCherryGrove = true;

        @Config.Comment("How often the cherry grove is picked among the cool biomes. A forest, the extreme hills, a taiga and the plains have 10 each. Requires a game restart. Default: 5")
        @Config.Name("Cherry Grove Weight")
        @Config.RangeInt(min = 1, max = 1000)
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:biomes.cherry_grove_weight")
        public int cherryGroveWeight = 5;
    }

    public static class TrailRuins {

        @Config.Comment("Generate the trail ruins of 1.20 in new chunks: buried towers, halls, houses and roads in taigas, birch forests and jungles, with suspicious gravel that holds pottery sherds. Only with Archaeology on. Default: true")
        @Config.Name("Enabled")
        @Config.LangKey("settings.sisyphus:trail_ruins.enabled")
        public boolean enabled = true;

        @Config.Comment("How far apart the trail ruins are: the world is cut into squares of this many chunks, and each square can have one, if its place is in a taiga, a birch forest or a jungle. Only places that are not planned yet change. Default: 34")
        @Config.Name("Spacing")
        @Config.RangeInt(min = 2, max = 256)
        @Config.LangKey("settings.sisyphus:trail_ruins.spacing")
        public int spacing = 34;

        @Config.Comment("How many chunks apart two trail ruins are at least. It is kept below the spacing. Default: 8")
        @Config.Name("Separation")
        @Config.RangeInt(min = 1, max = 255)
        @Config.LangKey("settings.sisyphus:trail_ruins.separation")
        public int separation = 8;

        @Config.Comment("The dimensions, by number, that trail ruins are made in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. They are never made in flat worlds or with structures turned off. Default: 0")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:trail_ruins.dimensions")
        public int[] dimensions = new int[]{0};
    }

    public static class OceanRuins {

        @Config.Comment("Generate the ocean ruins of 1.13 in new chunks: ruins of stone bricks or sandstone on the floor of the oceans, with chests and with suspicious gravel or sand that holds pottery sherds. Whether a ruin is cold or warm depends on the region, as 1.12 has only one kind of ocean. Only with Archaeology on. Default: true")
        @Config.Name("Enabled")
        @Config.LangKey("settings.sisyphus:ocean_ruins.enabled")
        public boolean enabled = true;

        @Config.Comment("How far apart the ocean ruins are: the world is cut into squares of this many chunks, and each square can have one, if its place is in an ocean. Only places that are not planned yet change. Default: 20")
        @Config.Name("Spacing")
        @Config.RangeInt(min = 2, max = 256)
        @Config.LangKey("settings.sisyphus:ocean_ruins.spacing")
        public int spacing = 20;

        @Config.Comment("How many chunks apart two ocean ruins are at least. It is kept below the spacing. Default: 8")
        @Config.Name("Separation")
        @Config.RangeInt(min = 1, max = 255)
        @Config.LangKey("settings.sisyphus:ocean_ruins.separation")
        public int separation = 8;

        @Config.Comment("The dimensions, by number, that ocean ruins are made in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. They are never made in flat worlds or with structures turned off. Default: 0")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:ocean_ruins.dimensions")
        public int[] dimensions = new int[]{0};

        @Config.Comment("How many of the ocean ruins are large, in percent. Default: 30")
        @Config.Name("Large Ruins")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:ocean_ruins.large_ruins")
        public double largeRuins = 30.0D;

        @Config.Comment("How many of the large ocean ruins have small ruins around them, in percent. Default: 90")
        @Config.Name("Clusters")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:ocean_ruins.clusters")
        public double clusters = 90.0D;
    }

    public static class RuinedPortals {

        @Config.Comment("Generate the ruined portals of 1.16 in new chunks: broken frames of obsidian and crying obsidian on a patch of netherrack, with a chest of gold. They lie on the ground, half buried in deserts, overgrown in jungles, sunk in oceans and swamps, deep in mountains or under the ground, and of blackstone in the Nether. Turned off when Unseen's Nether Backport is installed, which has them too, unless Yield to Unseen's Nether Backport is off. Default: true")
        @Config.Name("Enabled")
        @Config.LangKey("settings.sisyphus:ruined_portals.enabled")
        public boolean enabled = true;

        @Config.Comment("How far apart the ruined portals are: the world is cut into squares of this many chunks, and each square can have one. Only places that are not planned yet change. Default: 40")
        @Config.Name("Spacing")
        @Config.RangeInt(min = 2, max = 256)
        @Config.LangKey("settings.sisyphus:ruined_portals.spacing")
        public int spacing = 40;

        @Config.Comment("How many chunks apart two ruined portals are at least. It is kept below the spacing. Default: 15")
        @Config.Name("Separation")
        @Config.RangeInt(min = 1, max = 255)
        @Config.LangKey("settings.sisyphus:ruined_portals.separation")
        public int separation = 15;

        @Config.Comment("The dimensions, by number, that ruined portals are made in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. In a dimension of Nether biomes they are of blackstone; there are none in the End. They are never made in flat worlds or with structures turned off. Default: 0, -1")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:ruined_portals.dimensions")
        public int[] dimensions = new int[]{0, -1};
    }

    public static class AbandonedCamps {

        @Config.Comment("Make abandoned camps in new chunks, from Wilderlands: a white tent with one slope fallen in, on birch poles and hung with cobwebs, in a worn clearing with a chest of the campers' supplies, a bench and a bedroll, on flat grassy ground in plains, savannas, forests and taigas. Default: true")
        @Config.Name("Enabled")
        @Config.LangKey("settings.sisyphus:abandoned_camps.enabled")
        public boolean enabled = true;

        @Config.Comment("How far apart the camps are: the world is cut into squares of this many chunks, and each square can have one, where it has flat ground in a fitting biome. Default: 18")
        @Config.Name("Spacing")
        @Config.RangeInt(min = 4, max = 128)
        @Config.LangKey("settings.sisyphus:abandoned_camps.spacing")
        public int spacing = 18;

        @Config.Comment("How many of the squares get a camp where they have room for one, in percent. Default: 50")
        @Config.Name("Chance")
        @Config.RangeInt(min = 0, max = 100)
        @Config.LangKey("settings.sisyphus:abandoned_camps.chance")
        public int chance = 50;

        @Config.Comment("How much higher the highest ground under the clearing of a camp may be than the lowest, in blocks. Default: 4")
        @Config.Name("Steepest Ground")
        @Config.RangeInt(min = 0, max = 8)
        @Config.LangKey("settings.sisyphus:abandoned_camps.max_relief")
        public int maxRelief = 4;

        @Config.Comment("How close to the world spawn a camp may be, in blocks: none is made nearer than this. Default: 250")
        @Config.Name("Distance From Spawn")
        @Config.RangeInt(min = 0, max = 10000)
        @Config.LangKey("settings.sisyphus:abandoned_camps.min_spawn_distance")
        public int minSpawnDistance = 250;

        @Config.Comment("The dimensions, by number, that camps are made in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. They are never made in flat worlds or with structures turned off. Default: 0")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:abandoned_camps.dimensions")
        public int[] dimensions = new int[]{0};
    }

    public static class Geodes {

        @Config.Comment("Generate amethyst geodes underground in the Overworld, in new chunks. Needs Amethyst in Building Blocks. Default: true")
        @Config.Name("Enabled")
        @Config.LangKey("settings.sisyphus:geodes.enabled")
        public boolean enabled = true;

        @Config.Comment("One chunk in this many has a geode, on average. 1 puts one in every chunk. Default: 24")
        @Config.Name("Rarity")
        @Config.RangeInt(min = 1, max = 10000)
        @Config.LangKey("settings.sisyphus:geodes.rarity")
        public int rarity = 24;

        @Config.Comment("The lowest height of the middle of a geode. A geode reaches up to seven blocks from its middle. Default: 9")
        @Config.Name("Lowest Height")
        @Config.RangeInt(min = 8, max = 240)
        @Config.LangKey("settings.sisyphus:geodes.min_height")
        public int minHeight = 9;

        @Config.Comment("The highest height of the middle of a geode. Default: 30")
        @Config.Name("Highest Height")
        @Config.RangeInt(min = 8, max = 240)
        @Config.LangKey("settings.sisyphus:geodes.max_height")
        public int maxHeight = 30;

        @Config.Comment("The dimensions, by number, that geodes are made in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. Default: 0")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:geodes.dimensions")
        public int[] dimensions = new int[]{0};
    }

    public static class WildPlants {

        @Config.Comment("Generate thickets of bamboo in jungles. Needs Bamboo in Plants. Default: true")
        @Config.Name("Bamboo in Jungles")
        @Config.LangKey("settings.sisyphus:wild_plants.enable_bamboo")
        public boolean enableBamboo = true;

        @Config.Comment("One jungle chunk in this many has a thicket of bamboo, on average. The bamboo jungle has its own thickets. Default: 3")
        @Config.Name("Bamboo Rarity")
        @Config.RangeInt(min = 1, max = 1000)
        @Config.LangKey("settings.sisyphus:wild_plants.bamboo_rarity")
        public int bambooRarity = 3;

        @Config.Comment("Generate patches of sweet berry bushes in taigas. Needs Sweet Berries in Plants. Default: true")
        @Config.Name("Sweet Berries in Taigas")
        @Config.LangKey("settings.sisyphus:wild_plants.enable_sweet_berries")
        public boolean enableSweetBerries = true;

        @Config.Comment("One taiga chunk in this many has a patch of sweet berry bushes, on average. Default: 6")
        @Config.Name("Sweet Berry Rarity")
        @Config.RangeInt(min = 1, max = 1000)
        @Config.LangKey("settings.sisyphus:wild_plants.sweet_berry_rarity")
        public int sweetBerryRarity = 6;

        @Config.Comment("One snowy taiga chunk in this many has a patch of sweet berry bushes, on average. Default: 25")
        @Config.Name("Sweet Berry Rarity in Snowy Taigas")
        @Config.RangeInt(min = 1, max = 1000)
        @Config.LangKey("settings.sisyphus:wild_plants.snowy_sweet_berry_rarity")
        public int snowySweetBerryRarity = 25;

        @Config.Comment("Generate cornflowers in plains and lilies of the valley in forests. Needs Flowers and Dyes in Plants. Default: true")
        @Config.Name("Wild Flowers")
        @Config.LangKey("settings.sisyphus:wild_plants.enable_flowers")
        public boolean enableFlowers = true;

        @Config.Comment("One plains chunk in this many has a patch of cornflowers, on average. Default: 3")
        @Config.Name("Cornflower Rarity")
        @Config.RangeInt(min = 1, max = 1000)
        @Config.LangKey("settings.sisyphus:wild_plants.cornflower_rarity")
        public int cornflowerRarity = 3;

        @Config.Comment("One forest chunk in this many has a patch of lilies of the valley, on average. Default: 3")
        @Config.Name("Lily of the Valley Rarity")
        @Config.RangeInt(min = 1, max = 1000)
        @Config.LangKey("settings.sisyphus:wild_plants.lily_of_the_valley_rarity")
        public int lilyOfTheValleyRarity = 3;

        @Config.Comment("The dimensions, by number, that the wild plants grow in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. They are only in biomes they belong to, jungles, taigas, plains and forests, wherever those are. Default: empty")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:wild_plants.dimensions")
        public int[] dimensions = new int[]{};
    }

    public static class NetherGeneration {

        @Config.Comment("Generate patches of crimson or warped nylium in the caves of the Nether, with roots, fungi and vines on them, and huge fungi where the nether wood is turned on. Needs Nether Fungi in Plants. Default: true")
        @Config.Name("Crimson and Warped Patches")
        @Config.LangKey("settings.sisyphus:nether_generation.enable_fungi_patches")
        public boolean enableFungiPatches = true;

        @Config.Comment("One Nether chunk in this many has a crimson or warped patch, on average. Default: 6")
        @Config.Name("Crimson and Warped Patch Rarity")
        @Config.RangeInt(min = 1, max = 1000)
        @Config.LangKey("settings.sisyphus:nether_generation.fungi_patch_rarity")
        public int fungiPatchRarity = 6;

        @Config.Comment("Generate veins of nether gold ore in the netherrack. Needs Nether Gold Ore in Building Blocks. Default: true")
        @Config.Name("Nether Gold Ore")
        @Config.LangKey("settings.sisyphus:nether_generation.enable_nether_gold")
        public boolean enableNetherGold = true;

        @Config.Comment("How many veins of nether gold ore each chunk tries to make. Default: 10")
        @Config.Name("Nether Gold Ore Veins")
        @Config.RangeInt(min = 0, max = 64)
        @Config.LangKey("settings.sisyphus:nether_generation.nether_gold_veins")
        public int netherGoldVeins = 10;

        @Config.Comment("How many blocks a vein of nether gold ore has at most. Default: 10")
        @Config.Name("Nether Gold Ore Vein Size")
        @Config.RangeInt(min = 1, max = 64)
        @Config.LangKey("settings.sisyphus:nether_generation.nether_gold_vein_size")
        public int netherGoldVeinSize = 10;

        @Config.Comment("Generate patches of blackstone in the netherrack. Needs Blackstone in Building Blocks. Default: true")
        @Config.Name("Blackstone")
        @Config.LangKey("settings.sisyphus:nether_generation.enable_blackstone")
        public boolean enableBlackstone = true;

        @Config.Comment("How many patches of blackstone each chunk tries to make. Default: 2")
        @Config.Name("Blackstone Patches")
        @Config.RangeInt(min = 0, max = 64)
        @Config.LangKey("settings.sisyphus:nether_generation.blackstone_patches")
        public int blackstonePatches = 2;

        @Config.Comment("How many blocks a patch of blackstone has at most. Default: 25")
        @Config.Name("Blackstone Patch Size")
        @Config.RangeInt(min = 1, max = 64)
        @Config.LangKey("settings.sisyphus:nether_generation.blackstone_patch_size")
        public int blackstonePatchSize = 25;

        @Config.Comment("Generate patches of basalt in the netherrack. Needs Basalt in Building Blocks. Default: true")
        @Config.Name("Basalt")
        @Config.LangKey("settings.sisyphus:nether_generation.enable_basalt")
        public boolean enableBasalt = true;

        @Config.Comment("How many patches of basalt each chunk tries to make. Default: 3")
        @Config.Name("Basalt Patches")
        @Config.RangeInt(min = 0, max = 64)
        @Config.LangKey("settings.sisyphus:nether_generation.basalt_patches")
        public int basaltPatches = 3;

        @Config.Comment("How many blocks a patch of basalt has at most. Default: 14")
        @Config.Name("Basalt Patch Size")
        @Config.RangeInt(min = 1, max = 64)
        @Config.LangKey("settings.sisyphus:nether_generation.basalt_patch_size")
        public int basaltPatchSize = 14;

        @Config.Comment("Hide ancient debris in the netherrack, where it never touches air. Needs Netherite in Items and Effects. Default: true")
        @Config.Name("Ancient Debris")
        @Config.LangKey("settings.sisyphus:nether_generation.enable_ancient_debris")
        public boolean enableAncientDebris = true;

        @Config.Comment("How many times each chunk tries to hide ancient debris. Each try hides a vein of up to three blocks low down, between heights 8 and 24, and one of up to two blocks at any height from 8 up. Default: 1")
        @Config.Name("Ancient Debris Tries")
        @Config.RangeInt(min = 0, max = 16)
        @Config.LangKey("settings.sisyphus:nether_generation.ancient_debris_tries")
        public int ancientDebrisTries = 1;

        @Config.Comment("The dimensions, by number, that the ores, stones and crimson and warped patches are put in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. They only replace netherrack. Default: -1")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:nether_generation.dimensions")
        public int[] dimensions = new int[]{-1};
    }

    public static class LushCaves {

        @Config.Comment("Generate lush caves in the Overworld, in new chunks: zones about a hundred blocks across, about one part in fourteen of the world, with big open caves carved in them, which are covered in moss, cave vines, spore blossoms, azaleas, pools and dripleaves, with rooted azalea trees. They are zones of the caves that are there, and not biomes. Each part is only there when its content is turned on. Default: true")
        @Config.Name("Enabled")
        @Config.LangKey("settings.sisyphus:lush_caves.enabled")
        public boolean enabled = true;

        @Config.Comment("How much of the Overworld is in lush zones, in percent. 0 makes none, 100 makes all of it lush. Default: 7.5")
        @Config.Name("Zone Coverage")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.zone_coverage")
        public double zoneCoverage = 7.5D;

        @Config.Comment("How big the lush zones are: 1 is about a hundred blocks across, 2 twice as wide. They cover as much of the world as before, so bigger zones are fewer. Default: 1.0")
        @Config.Name("Zone Size")
        @Config.RangeDouble(min = 0.25D, max = 4.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.zone_size")
        public double zoneSize = 1.0D;

        @Config.Comment("The lowest height of the caves that are covered in moss, vines and the rest in a zone. Default: 8")
        @Config.Name("Lowest Height")
        @Config.RangeInt(min = 1, max = 250)
        @Config.LangKey("settings.sisyphus:lush_caves.min_height")
        public int minHeight = 8;

        @Config.Comment("The highest height of the caves that are covered in moss, vines and the rest in a zone. Only caves with rock above them are covered. Default: 56")
        @Config.Name("Highest Height")
        @Config.RangeInt(min = 1, max = 250)
        @Config.LangKey("settings.sisyphus:lush_caves.max_height")
        public int maxHeight = 56;

        @Config.Comment("The dimensions, by number, that lush caves are made in: 0 is the Overworld, -1 the Nether and 1 the End. Leave it empty for every dimension. The big caves are only carved in dimensions that make their caves the way the Overworld does. Default: 0")
        @Config.Name("Dimensions")
        @Config.LangKey("settings.sisyphus:lush_caves.dimensions")
        public int[] dimensions = new int[]{0};

        @Config.Comment("Carve big open caves in the lush zones, like the caves of 1.18, as well as the narrow tunnels of 1.12. When off, the lush zones only cover the tunnels. Default: true")
        @Config.Name("Big Caves")
        @Config.LangKey("settings.sisyphus:lush_caves.enable_big_caves")
        public boolean enableBigCaves = true;

        @Config.Comment("How much of the rock in the middle of a zone the big caves take, in percent, before they close off toward their floor and roof. More makes bigger halls with fewer pillars. Default: 45")
        @Config.Name("Big Cave Openness")
        @Config.RangeDouble(min = 0.0D, max = 90.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.big_cave_openness")
        public double bigCaveOpenness = 45.0D;

        @Config.Comment("How big the halls of the big caves are: 1 makes halls of forty to sixty blocks across and up to about thirty high, 2 twice that. Default: 1.2")
        @Config.Name("Big Cave Size")
        @Config.RangeDouble(min = 0.5D, max = 3.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.big_cave_size")
        public double bigCaveSize = 1.2D;

        @Config.Comment("The lowest height of the big caves. Below 11 the caves of 1.12 are filled with lava, so lower big caves can meet lava. Default: 14")
        @Config.Name("Big Cave Lowest Height")
        @Config.RangeInt(min = 1, max = 250)
        @Config.LangKey("settings.sisyphus:lush_caves.big_cave_min_height")
        public int bigCaveMinHeight = 14;

        @Config.Comment("The highest height of the big caves. Default: 54")
        @Config.Name("Big Cave Highest Height")
        @Config.RangeInt(min = 1, max = 250)
        @Config.LangKey("settings.sisyphus:lush_caves.big_cave_max_height")
        public int bigCaveMaxHeight = 54;

        @Config.Comment("How many blocks of rock are left at least between the big caves and the ground above them, or the sea floor. Default: 10")
        @Config.Name("Big Cave Roof")
        @Config.RangeInt(min = 3, max = 64)
        @Config.LangKey("settings.sisyphus:lush_caves.big_cave_roof")
        public int bigCaveRoof = 10;

        @Config.Comment("How much of the roof of a lush cave is moss, in percent. Default: 92")
        @Config.Name("Roof Moss")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.roof_moss")
        public double roofMoss = 92.0D;

        @Config.Comment("How many of the blocks of the roof have a cave vine under them, in percent. Default: 32")
        @Config.Name("Cave Vines")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.cave_vines")
        public double caveVines = 32.0D;

        @Config.Comment("How many of the blocks of a cave vine have glow berries, in percent. Default: 30")
        @Config.Name("Glow Berries")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.glow_berries")
        public double glowBerries = 30.0D;

        @Config.Comment("How long the longest cave vines are, in blocks. Most vines are one to seven blocks long, and a few are up to this long. A vine always stops a block or two above the floor. Default: 20")
        @Config.Name("Longest Cave Vine")
        @Config.RangeInt(min = 1, max = 64)
        @Config.LangKey("settings.sisyphus:lush_caves.longest_cave_vine")
        public int longestCaveVine = 20;

        @Config.Comment("How many of the blocks of the roof have a spore blossom under them, in percent. Default: 2.5")
        @Config.Name("Spore Blossoms")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.spore_blossoms")
        public double sporeBlossoms = 2.5D;

        @Config.Comment("How many of the blocks of the roof have glow lichen under them, and how many of the floors have some on a wall above them, in percent. Default: 3")
        @Config.Name("Glow Lichen")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.glow_lichen")
        public double glowLichen = 3.0D;

        @Config.Comment("How much of the floor of a lush cave is moss, in percent. Default: 95")
        @Config.Name("Floor Moss")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.floor_moss")
        public double floorMoss = 95.0D;

        @Config.Comment("How much of the moss on the floor has a plant on it, in percent: grass, moss carpet, tall grass and azaleas. Default: 80")
        @Config.Name("Floor Plants")
        @Config.RangeDouble(min = 0.0D, max = 100.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.floor_plants")
        public double floorPlants = 80.0D;

        @Config.Comment("How many of the floors are the middle of a pool of water on clay, in percent. A pool covers a lot of floor, so a little goes a long way. Default: 0.8")
        @Config.Name("Clay Pools")
        @Config.RangeDouble(min = 0.0D, max = 10.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.clay_pools")
        public double clayPools = 0.8D;

        @Config.Comment("How far the largest pools of clay reach from their middle, in blocks. A pool reaches from two blocks less than this to this. Default: 5")
        @Config.Name("Largest Clay Pool")
        @Config.RangeInt(min = 2, max = 7)
        @Config.LangKey("settings.sisyphus:lush_caves.largest_clay_pool")
        public int largestClayPool = 5;

        @Config.Comment("How many rooted azalea trees there are in each chunk of a zone, on average. Default: 1.5")
        @Config.Name("Rooted Azalea Trees")
        @Config.RangeDouble(min = 0.0D, max = 8.0D)
        @Config.LangKey("settings.sisyphus:lush_caves.rooted_azalea_trees")
        public double rootedAzaleaTrees = 1.5D;
    }

    public static class Gameplay {

        @Config.Comment("Hoes can be enchanted with Efficiency, Fortune and Silk Touch (with books at an anvil, as the enchanting table only offers what has an enchantability), and mine leaves, hay bales, sponges, wart blocks, targets and shroomlights faster, as in 1.16. Requires a game restart. Default: true")
        @Config.Name("Enchanted and Faster Hoes")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:gameplay.enable_hoe_enchanting")
        public boolean enableHoeEnchanting = true;

        @Config.Comment("Mending, as in 1.16: experience repairs every worn Mending item you carry, one after the other, before what is left fills the experience bar. When disabled, the game picks one Mending item at random and the experience goes to the bar if it needs no repair. Default: true")
        @Config.Name("Mending Repairs First")
        @Config.LangKey("settings.sisyphus:gameplay.enable_mending_repair_first")
        public boolean enableMendingRepairFirst = true;

        @Config.Comment("The enchanting table gives light of level 7, as in 1.18. This needs a mixin, so it needs MixinBooter or FermiumBooter, and nothing changes without one of them. A table that is already there gives light once the light around it is worked out again, which happens when a block near it changes. Default: true")
        @Config.Name("Enchanting Table Light")
        @Config.LangKey("settings.sisyphus:gameplay.enable_enchanting_table_light")
        public boolean enableEnchantingTableLight = true;

        @Config.Comment("Bubble columns, as in 1.13: water sources over soul sand push up whatever is in them, and over a magma block pull it down, a boat on top rocks and is then thrown up or pulled under, and a column gives back air. Magma blocks no longer dry up the water over them. This needs a mixin, so it needs MixinBooter or FermiumBooter, and no column is made without one of them. Water that is already over soul sand or a magma block becomes a column once a block next to it changes. Requires a game restart. Default: true")
        @Config.Name("Bubble Columns")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:gameplay.enable_bubble_columns")
        public boolean enableBubbleColumns = true;

        @Config.Comment("Dispensers, as in 1.16: put saddles on pigs, striders and tamed horses, donkeys and mules, shear sheep, mooshrooms and snow golems, and charge respawn anchors with glowstone. Requires a game restart. Default: true")
        @Config.Name("Dispenser Behaviors")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:gameplay.enable_dispenser_behaviors")
        public boolean enableDispenserBehaviors = true;

        @Config.Comment("A burning arrow that hits an unlit campfire or soul campfire lights it, as in 1.16. Default: true")
        @Config.Name("Burning Arrows Light Campfires")
        @Config.LangKey("settings.sisyphus:gameplay.enable_campfire_arrows")
        public boolean enableCampfireArrows = true;

        @Config.Comment("The wither can be built on soul soil as well as soul sand, as in 1.16. It needs soul soil to be turned on. Requires a game restart. Default: true")
        @Config.Name("Wither on Soul Soil")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:gameplay.enable_soul_soil_wither")
        public boolean enableSoulSoilWither = true;

        @Config.Comment("Endermen can pick up nylium, roots and fungi of the Nether and no longer pick up netherrack, as in 1.16. Requires a game restart. Default: true")
        @Config.Name("Endermen Take Nether Plants")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:gameplay.enable_endermen_nether_plants")
        public boolean enableEndermenNetherPlants = true;

        @Config.Comment("Horses, donkeys and mules follow a player who holds a golden carrot, a golden apple or an enchanted golden apple, and llamas follow one who holds a hay bale, as in 1.18. Default: true")
        @Config.Name("Animals Follow Food")
        @Config.LangKey("settings.sisyphus:gameplay.enable_animals_following_food")
        public boolean enableAnimalsFollowingFood = true;

        @Config.Comment("Evokers, vindicators and illusioners no longer attack baby villagers, as in 1.18. Default: true")
        @Config.Name("Illagers Spare Baby Villagers")
        @Config.LangKey("settings.sisyphus:gameplay.enable_illagers_sparing_babies")
        public boolean enableIllagersSparingBabies = true;

        @Config.Comment("Hostile mobs need a light level of 0 from blocks to spawn naturally in the Overworld, as in 1.18, where they used to spawn up to level 7. A single torch keeps them away from its surroundings. It makes the game easier. Mob spawners are not changed. Default: false")
        @Config.Name("Hostile Mobs Spawn Only in Darkness")
        @Config.LangKey("settings.sisyphus:gameplay.enable_dark_only_spawning")
        public boolean enableDarkOnlySpawning = false;
    }

    public static class Extras {

        @Config.Comment("Master toggle for the advancements added in 1.14 that are not about pillagers. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("New Advancements")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:extras.enable_advancements")
        public boolean enableAdvancements = true;

        @Config.Comment("Master toggle for the /teammsg, /schedule and /loot commands. When disabled, it is not registered and its recipes are removed. Requires a game restart. Default: true")
        @Config.Name("New Commands")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:extras.enable_commands")
        public boolean enableCommands = true;

        @Config.Comment("Master toggle for the new splash texts on the title screen. When disabled, only the splashes of the game are shown. Requires a game restart. Default: true")
        @Config.Name("New Splash Texts")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:extras.enable_splash_texts")
        public boolean enableSplashTexts = true;
    }

    public static class Compatibility {

        @Config.Comment("When Unseen's Nether Backport is installed, turn off the content of the Nether Update that it has too, so that there are not two of each: netherite, blackstone, basalt, nether gold ore, chains, crying obsidian, soul soil, shroomlight, soul fire (with the soul torch, lantern and campfire), the respawn anchor, the lodestone, the crimson and warped plants and wood, Soul Speed, Pigstep, piglins, hoglins and striders. Your own options are not changed in the file. The target block, the snout banner pattern, the nether brick variants and the smaller changes (hoes, wither, dispensers, endermen, Mending) stay on. Set to false to keep both, with the doubles. Requires a game restart. Default: true")
        @Config.Name("Yield to Unseen's Nether Backport")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:compatibility.yield_to_unseens_nether_backport")
        public boolean yieldToUnseensNetherBackport = true;

        @Config.Comment("When Depths Update is installed, turn off the content of the caves of 1.17 that it has too, so that there are not two of each and two deepslate layers in the same rock: deepslate (the blocks, their recipes, the way it is made in the world, and the deepslate versions of the ores of other mods), moss, amethyst (with calcite, smooth basalt, tinted glass and the geodes), the plants of the lush caves (with glow lichen), cave vines with glow berries, the dripleaf and lush caves. Your own options are not changed in the file. The candles and everything that is not from the caves of 1.17 stay on. If you turn one of those off in Depths Update, turn this off too, or neither mod has it. Set to false to keep both, with the doubles. Requires a game restart. Default: true")
        @Config.Name("Yield to Depths Update")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:compatibility.yield_to_depths_update")
        public boolean yieldToDepthsUpdate = true;

        @Config.Comment("When Quark is installed, leave the spruce, birch, jungle, acacia and dark oak bookshelves to it, so that there are not two of each with the same recipe. The crimson and warped bookshelves stay. If you turn its bookshelves off in Quark, turn this off too, or neither mod has them. Requires a game restart. Default: true")
        @Config.Name("Yield to Quark")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:compatibility.yield_to_quark")
        public boolean yieldToQuark = true;

        @Config.Comment("Open worlds made with Future MC with this mod in its place: take Future MC out of the mods folder, and its blocks, items, tile entities (with their contents) and mobs become those of this mod, each chunk when it is first loaded. Content that this mod does not have (bees, honey, the trident, the scute, the nautilus shell, the globe banner pattern) stays in the save as with any removed mod; blue ice becomes packed ice, the new wooden trapdoors the oak one, and the fish buckets water buckets. Nothing is done while Future MC is installed. Back up the world first. Requires a game restart. Default: true")
        @Config.Name("Convert Future MC Worlds")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:compatibility.convert_future_mc_worlds")
        public boolean convertFutureMCWorlds = true;

        @Config.Comment("Show the Brushing tab in Just Enough Items, with everything that the suspicious sand and gravel of each place can hold and the chance of each. Turn it off to keep what is buried a surprise. On a server, the server's setting counts, and players get it when they join. Default: true")
        @Config.Name("Show Brushing in JEI")
        @Config.LangKey("settings.sisyphus:compatibility.show_brushing_in_jei")
        public boolean showBrushingInJei = true;

        @Config.Comment("When Fletchery is installed, its fletching table, which has a screen, takes the place of the plain one of this mod, with the same recipe, in the recipes and in the villages. Set to false to keep the plain one. Requires a game restart. Default: true")
        @Config.Name("Fletchery Integration")
        @Config.RequiresMcRestart
        @Config.LangKey("settings.sisyphus:compatibility.fletchery_integration")
        public boolean fletcheryIntegration = true;

        @Config.Comment("When Oceanic Expanse is installed, ocean ruins get its drowned, which stay where they are put. Set to false to leave them out. Default: true")
        @Config.Name("Oceanic Expanse Integration")
        @Config.LangKey("settings.sisyphus:compatibility.oceanic_expanse_integration")
        public boolean oceanicExpanseIntegration = true;
    }
}
