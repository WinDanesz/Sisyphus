package com.windanesz.sisyphus.registry;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@GameRegistry.ObjectHolder(SisyphusBackport.MODID)
@Mod.EventBusSubscriber
public final class ModBlocks {

    public static final Block deepslate = placeholder();
    public static final Block cobbled_deepslate = placeholder();
    public static final Block moss_block = placeholder();
    public static final Block moss_carpet = placeholder();
    public static final Block deepslate_coal_ore = placeholder();
    public static final Block deepslate_diamond_ore = placeholder();
    public static final Block deepslate_emerald_ore = placeholder();
    public static final Block deepslate_gold_ore = placeholder();
    public static final Block deepslate_iron_ore = placeholder();
    public static final Block deepslate_lapis_ore = placeholder();
    public static final Block deepslate_redstone_ore = placeholder();
    public static final Block lit_deepslate_redstone_ore = placeholder();

    public static final Block polished_deepslate = placeholder();
    public static final Block deepslate_bricks = placeholder();
    public static final Block deepslate_tiles = placeholder();
    public static final Block cracked_deepslate_bricks = placeholder();
    public static final Block cracked_deepslate_tiles = placeholder();
    public static final Block reinforced_deepslate = placeholder();
    public static final Block deepslate_tile_stairs = placeholder();
    public static final Block deepslate_brick_stairs = placeholder();
    public static final Block cobbled_deepslate_stairs = placeholder();
    public static final Block deepslate_brick_slab = placeholder();
    public static final Block deepslate_brick_double_slab = placeholder();
    public static final Block deepslate_tile_slab = placeholder();
    public static final Block deepslate_tile_double_slab = placeholder();
    public static final Block deepslate_brick_wall = placeholder();
    public static final Block deepslate_tile_wall = placeholder();
    public static final Block cobbled_deepslate_slab = placeholder();
    public static final Block cobbled_deepslate_double_slab = placeholder();
    public static final Block cobbled_deepslate_wall = placeholder();
    public static final Block polished_deepslate_stairs = placeholder();
    public static final Block polished_deepslate_slab = placeholder();
    public static final Block polished_deepslate_double_slab = placeholder();
    public static final Block polished_deepslate_wall = placeholder();
    public static final Block chiseled_deepslate = placeholder();

    // The name tables are not public, as the object holder of this class fills in the public static final fields
    static final String[] PRISMARINE = {"prismarine", "prismarine_brick", "dark_prismarine"};
    static final String[] SHAPED_PREFIX = {"blackstone", "polished_blackstone", "polished_blackstone_brick"};
    static final String[] WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
    static final String[] NETHER_WOODS = {"crimson", "warped"};
    static final String[] CHERRY_LOGS = {"cherry_log", "stripped_cherry_log", "cherry_wood", "stripped_cherry_wood"};
    static final String[] CHERRY_PLANK_BLOCKS = {"cherry_planks", "cherry_stairs", "cherry_fence", "cherry_fence_gate", "cherry_button",
            "cherry_pressure_plate", "cherry_trapdoor"};
    static final String[] BAMBOO_BLOCKS = {"bamboo_block", "stripped_bamboo_block"};
    static final String[] BAMBOO_PLANK_BLOCKS = {"bamboo_planks", "bamboo_stairs", "bamboo_mosaic", "bamboo_mosaic_stairs", "bamboo_fence",
            "bamboo_fence_gate", "bamboo_button", "bamboo_pressure_plate", "bamboo_trapdoor"};
    static final String[] BAMBOO_SLABS = {"bamboo", "bamboo_mosaic"};
    static final String[] SIGN_WOODS = {"spruce", "birch", "jungle", "acacia", "dark_oak", "crimson", "warped", "cherry", "bamboo"};
    static final String[] FLOWERS = {"cornflower", "lily_of_the_valley", "wither_rose"};
    private static final String[] SHAPED = {"blackstone", "polished_blackstone", "polished_blackstone_bricks"};
    private static final float[][] SHAPED_STRENGTH = {{1.5F, 6.0F}, {2.0F, 6.0F}, {1.5F, 6.0F}};
    private static final String[] AMETHYST = {"amethyst_block", "budding_amethyst", "small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud",
            "amethyst_cluster", "tinted_glass", "calcite", "smooth_basalt"};
    private static final String[] LUSH = {"rooted_dirt", "hanging_roots", "spore_blossom", "azalea", "flowering_azalea", "azalea_leaves", "flowering_azalea_leaves"};
    private static final MapColor[] NETHER_WOOD_COLORS = {MapColor.NETHERRACK, MapColor.CYAN};
    private static final MapColor CHERRY_COLOR = MapColor.WHITE_STAINED_HARDENED_CLAY;
    private static final BlockPlanks.EnumType[] BOOKSHELF_WOODS = {BlockPlanks.EnumType.SPRUCE, BlockPlanks.EnumType.BIRCH,
            BlockPlanks.EnumType.JUNGLE, BlockPlanks.EnumType.ACACIA, BlockPlanks.EnumType.DARK_OAK};
    private static final String[] HANGING_SIGN_WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "crimson", "warped", "cherry", "bamboo"};
    private static final AxisAlignedBB ROOTS = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.8125D, 0.875D);
    private static final AxisAlignedBB SPROUTS = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.1875D, 0.875D);

    private static final String[] SMOOTH_VARIANTS = {"smooth_sandstone", "smooth_red_sandstone", "smooth_quartz"};

    private ModBlocks() {
    } // no instances

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T placeholder() {
        return null;
    }

    public static String[] amethystNames() {
        return AMETHYST.clone();
    }

    public static String[] lushPlantNames() {
        return LUSH.clone();
    }

    public static String[] candleNames() {
        String[] names = new String[BlockCandle.COLORS.length + 1];
        names[0] = "candle";
        for (int i = 0; i < BlockCandle.COLORS.length; i++) {
            names[i + 1] = BlockCandle.COLORS[i] + "_candle";
        }
        return names;
    }

    /**
     * Air when the block is missing or turned off.
     */
    public static Block get(String name) {
        return Block.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name));
    }

    @Nullable
    public static Block getOrNull(String name) {
        Block block = get(name);
        return block == Blocks.AIR ? null : block;
    }

    public static String[] strippedWoodNames() {
        String[] names = new String[WOODS.length * 3];
        for (int i = 0; i < WOODS.length; i++) {
            names[i * 3] = WOODS[i] + "_wood";
            names[i * 3 + 1] = "stripped_" + WOODS[i] + "_log";
            names[i * 3 + 2] = "stripped_" + WOODS[i] + "_wood";
        }
        return names;
    }

    static String[] netherLogNames(String wood) {
        return new String[]{wood + "_stem", "stripped_" + wood + "_stem", wood + "_hyphae", "stripped_" + wood + "_hyphae"};
    }

    public static List<String> cherryBlockNames() {
        List<String> names = new ArrayList<>(Arrays.asList(CHERRY_LOGS));
        names.addAll(Arrays.asList(CHERRY_PLANK_BLOCKS));
        names.addAll(Arrays.asList("cherry_slab", "cherry_double_slab", "cherry_door", "cherry_leaves", "cherry_sapling", "pink_petals"));
        return names;
    }

    public static List<String> bambooWoodBlockNames() {
        List<String> names = new ArrayList<>(Arrays.asList(BAMBOO_BLOCKS));
        names.addAll(Arrays.asList(BAMBOO_PLANK_BLOCKS));
        for (String slab : BAMBOO_SLABS) {
            names.add(slab + "_slab");
            names.add(slab + "_double_slab");
        }
        names.add("bamboo_door");
        return names;
    }

    public static List<String> bookshelfNames() {
        List<String> names = new ArrayList<>();
        if (!Settings.buildingBlocks.enableWoodBookshelves) {
            return names;
        }
        for (BlockPlanks.EnumType wood : BOOKSHELF_WOODS) {
            names.add(wood.getName() + "_bookshelf");
        }
        if (Settings.buildingBlocks.enableNetherWood) {
            for (String wood : NETHER_WOODS) {
                names.add(wood + "_bookshelf");
            }
        }
        if (Settings.isContentEnabled("cherry_wood")) {
            names.add("cherry_bookshelf");
        }
        if (Settings.isContentEnabled("bamboo_wood")) {
            names.add("bamboo_bookshelf");
        }
        return names;
    }

    // Crimson, warped, cherry and bamboo signs need their wood
    static boolean hasWood(String wood) {
        if (wood.equals("crimson") || wood.equals("warped")) {
            return Settings.buildingBlocks.enableNetherWood;
        }
        if (wood.equals("cherry") || wood.equals("bamboo")) {
            return Settings.isContentEnabled(wood + "_wood");
        }
        return true;
    }

    public static List<String> signWoods() {
        List<String> woods = new ArrayList<>();
        if (Settings.buildingBlocks.enableSigns) {
            for (String wood : SIGN_WOODS) {
                if (hasWood(wood)) {
                    woods.add(wood);
                }
            }
        }
        return woods;
    }

    public static List<String> hangingSignWoods() {
        List<String> woods = new ArrayList<>();
        if (Settings.isContentEnabled("hanging_signs")) {
            for (String wood : HANGING_SIGN_WOODS) {
                if (hasWood(wood)) {
                    woods.add(wood);
                }
            }
        }
        return woods;
    }

    public static boolean isSoulSoil(Block block) {
        Block soil = get("soul_soil");
        return soil != Blocks.AIR && block == soil;
    }

    public static void registerBlock(IForgeRegistry<Block> registry, String name, Block block) {
        block.setRegistryName(SisyphusBackport.MODID, name);
        block.setTranslationKey(block.getRegistryName().toString());
        registry.register(block);
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        if (Settings.buildingBlocks.enableMoss) {
            registerBlock(registry, "moss_block", (new BlockMoss()).setHardness(0.1F).setResistance(0.1F));
            registerBlock(registry, "moss_carpet", (new BlockMossCarpet()).setHardness(0.1F).setResistance(0.1F));
        }
        if (Settings.buildingBlocks.enableStrippedWood) {
            for (String name : strippedWoodNames()) {
                registerBlock(registry, name, new BlockWoodLog());
            }
        }
        registerStoneBlocks(registry);
        registerStoneVariantBlocks(registry);
        if (Settings.plants.enableFlowers) {
            registerBlock(registry, "cornflower", new BlockFlower());
            registerBlock(registry, "lily_of_the_valley", new BlockFlower());
            registerBlock(registry, "wither_rose", new BlockWitherRose());
        }
        if (Settings.plants.enableSweetBerries) {
            registerBlock(registry, "sweet_berry_bush", new BlockSweetBerryBush());
        }
        registerFunctionalBlocks(registry);
        registerNetherBlocks(registry);
        registerCaveBlocks(registry);
        registerNetherPlantBlocks(registry);
        registerNetherWoodBlocks(registry);
        if (Settings.plants.enableBamboo) {
            registerBlock(registry, "bamboo", new BlockBamboo());
            registerBlock(registry, "bamboo_sapling", new BlockBambooSapling());
        }
        for (String wood : signWoods()) {
            registerBlock(registry, wood + "_standing_sign", new BlockWoodSign.Standing(wood).setHardness(1.0F));
            registerBlock(registry, wood + "_wall_sign", new BlockWoodSign.Wall(wood).setHardness(1.0F));
        }
        registerBookshelfBlocks(registry);
        registerDeepslateBlocks(registry);
        registerArchaeologyBlocks(registry);
        registerMudBlocks(registry);
        registerCherryBlocks(registry);
        registerBambooWoodBlocks(registry);
        for (String wood : hangingSignWoods()) {
            registerBlock(registry, wood + "_hanging_sign", new BlockHangingSign.Ceiling(wood));
            registerBlock(registry, wood + "_wall_hanging_sign", new BlockHangingSign.Wall(wood));
        }
        if (Settings.gameplay.enableBubbleColumns) {
            // No item, as in the game: it is made by soul sand and magma blocks under water
            registerBlock(registry, "bubble_column", new BlockBubbleColumn());
        }
    }

    private static void registerStoneVariantBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.buildingBlocks.enableStoneVariants) {
            return;
        }
        for (StoneVariant v : stoneStairsAndSlabs()) {
            registerBlock(registry, v.name + "_stairs", new BlockDeepslateTileStairs(v.base, SoundType.STONE));
            registerSlabs(registry, v.name, SoundType.STONE, v.hardness, v.resistance);
        }
        for (StoneVariant v : stoneWalls()) {
            registerBlock(registry, v.name + "_wall", new BlockDeepslateWall(v.base, SoundType.STONE));
        }
    }

    private static List<StoneVariant> stoneStairsAndSlabs() {
        List<StoneVariant> list = new ArrayList<>();
        list.add(new StoneVariant("stone", Blocks.STONE.getStateFromMeta(0), 1.5F, 6.0F));
        list.add(new StoneVariant("andesite", Blocks.STONE.getStateFromMeta(5), 1.5F, 6.0F));
        list.add(new StoneVariant("polished_andesite", Blocks.STONE.getStateFromMeta(6), 1.5F, 6.0F));
        list.add(new StoneVariant("diorite", Blocks.STONE.getStateFromMeta(3), 1.5F, 6.0F));
        list.add(new StoneVariant("polished_diorite", Blocks.STONE.getStateFromMeta(4), 1.5F, 6.0F));
        list.add(new StoneVariant("granite", Blocks.STONE.getStateFromMeta(1), 1.5F, 6.0F));
        list.add(new StoneVariant("polished_granite", Blocks.STONE.getStateFromMeta(2), 1.5F, 6.0F));
        list.add(new StoneVariant("mossy_stone_brick", Blocks.STONEBRICK.getStateFromMeta(1), 1.5F, 6.0F));
        list.add(new StoneVariant("mossy_cobblestone", Blocks.MOSSY_COBBLESTONE.getDefaultState(), 2.0F, 6.0F));
        list.add(new StoneVariant("red_nether_brick", Blocks.RED_NETHER_BRICK.getDefaultState(), 2.0F, 6.0F));
        list.add(new StoneVariant("end_stone_brick", Blocks.END_BRICKS.getDefaultState(), 0.8F, 9.0F));
        if (Settings.buildingBlocks.enableSmoothBlocks) {
            for (String name : SMOOTH_VARIANTS) {
                list.add(new StoneVariant(name, get(name).getDefaultState(), 2.0F, 6.0F));
            }
        }
        return list;
    }

    private static List<StoneVariant> stoneWalls() {
        List<StoneVariant> list = new ArrayList<>();
        list.add(new StoneVariant("brick", Blocks.BRICK_BLOCK.getDefaultState(), 2.0F, 6.0F));
        list.add(new StoneVariant("andesite", Blocks.STONE.getStateFromMeta(5), 1.5F, 6.0F));
        list.add(new StoneVariant("diorite", Blocks.STONE.getStateFromMeta(3), 1.5F, 6.0F));
        list.add(new StoneVariant("granite", Blocks.STONE.getStateFromMeta(1), 1.5F, 6.0F));
        list.add(new StoneVariant("prismarine", Blocks.PRISMARINE.getStateFromMeta(0), 1.5F, 30.0F));
        list.add(new StoneVariant("stone_brick", Blocks.STONEBRICK.getStateFromMeta(0), 1.5F, 6.0F));
        list.add(new StoneVariant("mossy_stone_brick", Blocks.STONEBRICK.getStateFromMeta(1), 1.5F, 6.0F));
        list.add(new StoneVariant("sandstone", Blocks.SANDSTONE.getStateFromMeta(0), 0.8F, 0.8F));
        list.add(new StoneVariant("red_sandstone", Blocks.RED_SANDSTONE.getStateFromMeta(0), 0.8F, 0.8F));
        list.add(new StoneVariant("nether_brick", Blocks.NETHER_BRICK.getDefaultState(), 2.0F, 6.0F));
        list.add(new StoneVariant("red_nether_brick", Blocks.RED_NETHER_BRICK.getDefaultState(), 2.0F, 6.0F));
        list.add(new StoneVariant("end_stone_brick", Blocks.END_BRICKS.getDefaultState(), 0.8F, 9.0F));
        return list;
    }

    private static void registerNetherPlantBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.plants.enableNetherFungi) {
            return;
        }
        registerBlock(registry, "crimson_nylium", new BlockNylium(false));
        registerBlock(registry, "warped_nylium", new BlockNylium(true));
        registerBlock(registry, "crimson_fungus", new BlockNetherFungus(false));
        registerBlock(registry, "warped_fungus", new BlockNetherFungus(true));
        registerBlock(registry, "crimson_roots", new BlockNetherPlant(ROOTS, true, ModSounds.type("roots")));
        registerBlock(registry, "warped_roots", new BlockNetherPlant(ROOTS, true, ModSounds.type("roots")));
        registerBlock(registry, "nether_sprouts", new BlockNetherPlant(SPROUTS, true, ModSounds.type("nether_sprouts")));
        registerBlock(registry, "weeping_vines", new BlockNetherVines(false));
        registerBlock(registry, "twisting_vines", new BlockNetherVines(true));
        registerBlock(registry, "warped_wart_block", new BlockSimple(Material.GRASS, MapColor.CYAN, ModSounds.type("wart_block"), 1.0F, 1.0F));
    }

    private static void registerNetherWoodBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.buildingBlocks.enableNetherWood) {
            return;
        }
        WoodSounds sounds = WoodSounds.nether();
        for (int i = 0; i < NETHER_WOODS.length; i++) {
            String wood = NETHER_WOODS[i];
            for (String name : netherLogNames(wood)) {
                registerBlock(registry, name, new BlockNetherLog());
            }
            registerPlankBlocks(registry, wood, NETHER_WOOD_COLORS[i], sounds);
        }
    }

    // Planks, stairs, slabs, fence, fence gate, button, pressure plate, trapdoor and door
    private static void registerPlankBlocks(IForgeRegistry<Block> registry, String wood, MapColor color, WoodSounds sounds) {
        Block planks = new BlockSimple(Material.WOOD, color, sounds.type, 2.0F, 3.0F);
        registerBlock(registry, wood + "_planks", planks);
        registerBlock(registry, wood + "_stairs", new BlockDeepslateTileStairs(planks.getDefaultState(), sounds.type));
        registerSlabs(registry, wood, sounds.type, 2.0F, 3.0F, Material.WOOD);
        registerBlock(registry, wood + "_fence", new BlockWoodFence(color, sounds));
        registerBlock(registry, wood + "_fence_gate", new BlockWoodFenceGate(sounds));
        registerBlock(registry, wood + "_button", new BlockWoodButton(sounds));
        registerBlock(registry, wood + "_pressure_plate", new BlockWoodPlate(sounds));
        registerBlock(registry, wood + "_trapdoor", new BlockWoodTrapDoor(sounds));
        registerBlock(registry, wood + "_door", new BlockWoodDoor(sounds));
    }

    private static void registerBookshelfBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.buildingBlocks.enableWoodBookshelves) {
            return;
        }
        for (BlockPlanks.EnumType wood : BOOKSHELF_WOODS) {
            registerBlock(registry, wood.getName() + "_bookshelf", new BlockWoodBookshelf(wood.getMapColor(), SoundType.WOOD));
        }
        if (Settings.buildingBlocks.enableNetherWood) {
            SoundType sound = ModSounds.type("nether_wood");
            registerBlock(registry, "crimson_bookshelf", new BlockWoodBookshelf(MapColor.NETHERRACK, sound));
            registerBlock(registry, "warped_bookshelf", new BlockWoodBookshelf(MapColor.CYAN, sound));
        }
        if (Settings.isContentEnabled("cherry_wood")) {
            registerBlock(registry, "cherry_bookshelf", new BlockWoodBookshelf(CHERRY_COLOR, ModSounds.type("cherry_wood")));
        }
        if (Settings.isContentEnabled("bamboo_wood")) {
            registerBlock(registry, "bamboo_bookshelf", new BlockWoodBookshelf(MapColor.YELLOW, ModSounds.type("bamboo_wood")));
        }
    }

    private static void registerCherryBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.isContentEnabled("cherry_wood")) {
            return;
        }
        WoodSounds sounds = WoodSounds.cherry();
        for (String name : CHERRY_LOGS) {
            registerBlock(registry, name, new BlockWoodLog(sounds.type));
        }
        registerPlankBlocks(registry, "cherry", CHERRY_COLOR, sounds);
        if (Settings.isContentEnabled("cherry_trees")) {
            registerBlock(registry, "cherry_leaves", new BlockCherryLeaves());
            registerBlock(registry, "cherry_sapling", new BlockCherrySapling());
            registerBlock(registry, "pink_petals", new BlockPinkPetals());
        }
    }

    private static void registerBambooWoodBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.isContentEnabled("bamboo_wood")) {
            return;
        }
        WoodSounds sounds = WoodSounds.bamboo();
        SoundType wood = sounds.type;
        for (String name : BAMBOO_BLOCKS) {
            registerBlock(registry, name, new BlockWoodLog(wood));
        }
        Block planks = new BlockSimple(Material.WOOD, MapColor.YELLOW, wood, 2.0F, 3.0F);
        registerBlock(registry, "bamboo_planks", planks);
        registerBlock(registry, "bamboo_stairs", new BlockDeepslateTileStairs(planks.getDefaultState(), wood));
        Block mosaic = new BlockSimple(Material.WOOD, MapColor.YELLOW, wood, 2.0F, 3.0F);
        registerBlock(registry, "bamboo_mosaic", mosaic);
        registerBlock(registry, "bamboo_mosaic_stairs", new BlockDeepslateTileStairs(mosaic.getDefaultState(), wood));
        for (String slab : BAMBOO_SLABS) {
            registerSlabs(registry, slab, wood, 2.0F, 3.0F, Material.WOOD);
        }
        registerBlock(registry, "bamboo_fence", new BlockWoodFence(MapColor.YELLOW, sounds));
        registerBlock(registry, "bamboo_fence_gate", new BlockWoodFenceGate(sounds));
        registerBlock(registry, "bamboo_button", new BlockWoodButton(sounds));
        registerBlock(registry, "bamboo_pressure_plate", new BlockWoodPlate(sounds));
        registerBlock(registry, "bamboo_trapdoor", new BlockWoodTrapDoor(sounds));
        registerBlock(registry, "bamboo_door", new BlockWoodDoor(sounds));
    }

    private static void registerMudBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.buildingBlocks.enableMud) {
            return;
        }
        registerBlock(registry, "mud", new BlockMud());
        Block packed = new BlockSimple(Material.GROUND, MapColor.DIRT, ModSounds.type("packed_mud"), 1.0F, 3.0F).setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        registerBlock(registry, "packed_mud", packed);
        Block bricks = rock(MapColor.SILVER_STAINED_HARDENED_CLAY, ModSounds.type("mud_bricks"), 1.5F, 3.0F).setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        registerBlock(registry, "mud_bricks", bricks);
        registerBlock(registry, "mud_brick_stairs", new BlockDeepslateTileStairs(bricks.getDefaultState(), ModSounds.type("mud_bricks")));
        registerSlabs(registry, "mud_brick", ModSounds.type("mud_bricks"), 1.5F, 3.0F);
        registerBlock(registry, "mud_brick_wall", new BlockDeepslateWall(bricks.getDefaultState(), ModSounds.type("mud_bricks")));
    }

    private static void registerArchaeologyBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.archaeology.enableArchaeology) {
            return;
        }
        // The dust that falls from them is that of sand and gravel
        registerBlock(registry, "suspicious_sand", new BlockSuspicious(Blocks.SAND.getDefaultState(), -2370656, ModSounds.type("suspicious_sand"),
                ModSounds.event("item.brush.brushing.sand"), ModSounds.event("item.brush.brushing.sand.complete")));
        registerBlock(registry, "suspicious_gravel", new BlockSuspicious(Blocks.GRAVEL.getDefaultState(), -8356741, ModSounds.type("suspicious_gravel"),
                ModSounds.event("item.brush.brushing.gravel"), ModSounds.event("item.brush.brushing.gravel.complete")));
        registerBlock(registry, "decorated_pot", new BlockDecoratedPot());
    }

    private static Block rock(MapColor color, SoundType sound, float hardness, float resistance) {
        Block block = new BlockSimple(Material.ROCK, color, sound, hardness, resistance);
        block.setHarvestLevel("pickaxe", 0);
        return block;
    }

    private static Block smoothBlock(MapColor color) {
        Block block = new Block(Material.ROCK, color);
        block.setHardness(2.0F).setResistance(6.0F).setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        return block;
    }

    private static Block workstation() {
        Block block = new Block(Material.WOOD);
        block.setHardness(2.5F).setCreativeTab(CreativeTabs.DECORATIONS);
        return block;
    }

    private static void registerStoneBlocks(IForgeRegistry<Block> registry) {
        if (Settings.buildingBlocks.enableSmoothBlocks) {
            registerBlock(registry, "smooth_stone", smoothBlock(MapColor.STONE));
            registerBlock(registry, "smooth_sandstone", smoothBlock(MapColor.SAND));
            registerBlock(registry, "smooth_red_sandstone", smoothBlock(MapColor.ADOBE));
            registerBlock(registry, "smooth_quartz", smoothBlock(MapColor.QUARTZ));
        }

        if (Settings.buildingBlocks.enablePrismarineStairsAndSlabs) {
            for (int meta = 0; meta < PRISMARINE.length; meta++) {
                // The stairs take their hardness, resistance and sound from the vanilla block they are based on
                registerBlock(registry, PRISMARINE[meta] + "_stairs", new BlockDeepslateTileStairs(Blocks.PRISMARINE.getStateFromMeta(meta), SoundType.STONE));
                registerSlabs(registry, PRISMARINE[meta], SoundType.STONE, 1.5F, 30.0F);
            }
        }
    }

    private static void registerFunctionalBlocks(IForgeRegistry<Block> registry) {
        if (Settings.buildingBlocks.enableLantern) {
            registerBlock(registry, "lantern", new BlockLantern());
        }
        if (Settings.functionalBlocks.enableScaffolding) {
            registerBlock(registry, "scaffolding", new BlockScaffolding());
        }
        if (Settings.functionalBlocks.enableCampfire) {
            registerBlock(registry, "campfire", new BlockCampfire());
        }
        if (Settings.functionalBlocks.enableGrindstone) {
            registerBlock(registry, "grindstone", new BlockGrindstone());
        }
        if (Settings.functionalBlocks.enableStonecutter) {
            registerBlock(registry, "stonecutter", new BlockStonecutter());
        }
        if (Settings.functionalBlocks.enableLoom) {
            registerBlock(registry, "loom", new BlockLoom());
        }
        if (Settings.functionalBlocks.enableCartographyTable) {
            registerBlock(registry, "cartography_table", new BlockCartographyTable());
        }
        if (Settings.functionalBlocks.enableLectern) {
            registerBlock(registry, "lectern", new BlockLectern());
        }
        if (Settings.functionalBlocks.enableBell) {
            registerBlock(registry, "bell", new BlockBell());
        }
        if (Settings.functionalBlocks.enableComposter) {
            registerBlock(registry, "composter", new BlockComposter());
        }
        if (Settings.functionalBlocks.enableBarrel) {
            registerBlock(registry, "barrel", new BlockBarrel());
        }
        if (Settings.isContentEnabled("fletching_table")) {
            registerBlock(registry, "fletching_table", workstation());
        }
        if (Settings.functionalBlocks.enableWorkstations) {
            registerBlock(registry, "smithing_table", Settings.items.enableNetherite || Settings.items.enableArmorTrims ? new BlockSmithingTable() : workstation());
        }
        if (Settings.functionalBlocks.enableFurnaces) {
            registerBlock(registry, "blast_furnace", new BlockFurnaceVariant(true));
            registerBlock(registry, "smoker", new BlockFurnaceVariant(false));
        }
    }

    private static void registerNetherBlocks(IForgeRegistry<Block> registry) {
        if (Settings.items.enableNetherite) {
            Block debris = new BlockSimple(Material.IRON, MapColor.BLACK, ModSounds.type("ancient_debris"), 30.0F, 1200.0F);
            debris.setHarvestLevel("pickaxe", 3);
            registerBlock(registry, "ancient_debris", debris);
            registerBlock(registry, "netherite_block", new BlockNetherite());
        }
        if (Settings.buildingBlocks.enableBlackstone) {
            registerBlock(registry, "blackstone", rock(MapColor.BLACK, SoundType.STONE, 1.5F, 6.0F));
            registerBlock(registry, "polished_blackstone", rock(MapColor.BLACK, SoundType.STONE, 2.0F, 6.0F));
            registerBlock(registry, "chiseled_polished_blackstone", rock(MapColor.BLACK, SoundType.STONE, 1.5F, 6.0F));
            registerBlock(registry, "polished_blackstone_bricks", rock(MapColor.BLACK, SoundType.STONE, 1.5F, 6.0F));
            registerBlock(registry, "cracked_polished_blackstone_bricks", rock(MapColor.BLACK, SoundType.STONE, 1.5F, 6.0F));
            registerBlock(registry, "gilded_blackstone", new BlockGildedBlackstone());
            for (int i = 0; i < SHAPED.length; i++) {
                Block base = get(SHAPED[i]);
                registerBlock(registry, SHAPED_PREFIX[i] + "_stairs", new BlockDeepslateTileStairs(base.getDefaultState(), SoundType.STONE));
                registerSlabs(registry, SHAPED_PREFIX[i], SoundType.STONE, SHAPED_STRENGTH[i][0], SHAPED_STRENGTH[i][1]);
                registerBlock(registry, SHAPED_PREFIX[i] + "_wall", new BlockDeepslateWall(base.getDefaultState(), SoundType.STONE));
            }
            registerBlock(registry, "polished_blackstone_button", new BlockStoneButton());
            registerBlock(registry, "polished_blackstone_pressure_plate", new BlockStonePlate());
        }
        if (Settings.buildingBlocks.enableBasalt) {
            registerBlock(registry, "basalt", new BlockAxis(Material.ROCK, MapColor.BLACK, ModSounds.type("basalt"), 1.25F, 4.2F));
            registerBlock(registry, "polished_basalt", new BlockAxis(Material.ROCK, MapColor.BLACK, ModSounds.type("basalt"), 1.25F, 4.2F));
        }
        if (Settings.buildingBlocks.enableNetherGold) {
            registerBlock(registry, "nether_gold_ore", new BlockNetherGoldOre());
        }
        if (Settings.buildingBlocks.enableChain) {
            registerBlock(registry, "chain", new BlockChain());
        }
        if (Settings.buildingBlocks.enableCryingObsidian) {
            registerBlock(registry, "crying_obsidian", new BlockCryingObsidian());
        }
        if (Settings.buildingBlocks.enableSoulSoil) {
            Block soil = new BlockSimple(Material.SAND, MapColor.BROWN, ModSounds.type("soul_soil"), 0.5F, 0.5F);
            soil.setHarvestLevel("shovel", 0);
            registerBlock(registry, "soul_soil", soil);
        }
        if (Settings.buildingBlocks.enableShroomlight) {
            Block light = new BlockSimple(Material.GRASS, MapColor.ADOBE, ModSounds.type("shroomlight"), 1.0F, 1.0F);
            light.setLightLevel(1.0F);
            registerBlock(registry, "shroomlight", light);
        }
        if (Settings.functionalBlocks.enableSoulFire) {
            registerBlock(registry, "soul_fire", new BlockSoulFire());
            registerBlock(registry, "soul_torch", new BlockSoulTorch());
            registerBlock(registry, "soul_lantern", new BlockLantern(10.0F / 15.0F));
            if (Settings.functionalBlocks.enableCampfire) {
                registerBlock(registry, "soul_campfire", new BlockCampfire(true));
            }
        }
        if (Settings.functionalBlocks.enableRespawnAnchor) {
            registerBlock(registry, "respawn_anchor", new BlockRespawnAnchor());
        }
        if (Settings.functionalBlocks.enableLodestone) {
            registerBlock(registry, "lodestone", new BlockLodestone());
        }
        if (Settings.functionalBlocks.enableTarget) {
            registerBlock(registry, "target", new BlockTarget());
        }
        if (Settings.buildingBlocks.enableNetherBrickVariants) {
            registerBlock(registry, "chiseled_nether_bricks", rock(MapColor.NETHERRACK, ModSounds.type("nether_bricks"), 2.0F, 6.0F));
            registerBlock(registry, "cracked_nether_bricks", rock(MapColor.NETHERRACK, ModSounds.type("nether_bricks"), 2.0F, 6.0F));
            registerBlock(registry, "quartz_bricks", rock(MapColor.QUARTZ, SoundType.STONE, 0.8F, 0.8F));
        }
    }

    private static void registerCaveBlocks(IForgeRegistry<Block> registry) {
        if (Settings.buildingBlocks.enableCandles) {
            for (String name : candleNames()) {
                registerBlock(registry, name, new BlockCandle());
                registerBlock(registry, name + "_cake", new BlockCandleCake(name));
            }
        }
        if (Settings.buildingBlocks.enableAmethyst) {
            registerBlock(registry, "amethyst_block", rock(MapColor.PURPLE, ModSounds.type("amethyst_block"), 1.5F, 1.5F));
            registerBlock(registry, "budding_amethyst", new BlockBuddingAmethyst());
            registerBlock(registry, "small_amethyst_bud", new BlockAmethystCluster("small_amethyst_bud", 3, 4, 1));
            registerBlock(registry, "medium_amethyst_bud", new BlockAmethystCluster("medium_amethyst_bud", 4, 3, 2));
            registerBlock(registry, "large_amethyst_bud", new BlockAmethystCluster("large_amethyst_bud", 5, 3, 4));
            registerBlock(registry, "amethyst_cluster", new BlockAmethystCluster("amethyst_cluster", 7, 3, 5));
            registerBlock(registry, "tinted_glass", new BlockTintedGlass());
            registerBlock(registry, "calcite", rock(MapColor.QUARTZ, ModSounds.type("calcite"), 0.75F, 0.75F));
            registerBlock(registry, "smooth_basalt", rock(MapColor.BLACK, ModSounds.type("basalt"), 1.25F, 4.2F));
        }
        if (Settings.plants.enableLushPlants) {
            registerBlock(registry, "rooted_dirt", new BlockRootedDirt());
            registerBlock(registry, "hanging_roots", new BlockHangingPlant("hanging_roots", true));
            registerBlock(registry, "spore_blossom", new BlockHangingPlant("spore_blossom", false));
            registerBlock(registry, "azalea", new BlockAzalea("azalea"));
            registerBlock(registry, "flowering_azalea", new BlockAzalea("flowering_azalea"));
            registerBlock(registry, "azalea_leaves", new BlockAzaleaLeaves("azalea_leaves"));
            registerBlock(registry, "flowering_azalea_leaves", new BlockAzaleaLeaves("azalea_leaves"));
            registerBlock(registry, "glow_lichen", new BlockGlowLichen());
        }
        if (Settings.plants.enableCaveVines) {
            registerBlock(registry, "cave_vines", new BlockCaveVines(true));
            registerBlock(registry, "cave_vines_plant", new BlockCaveVines(false));
        }
        if (Settings.plants.enableDripleaf) {
            registerBlock(registry, "big_dripleaf", new BlockBigDripleaf());
            registerBlock(registry, "big_dripleaf_stem", new BlockBigDripleafStem());
            registerBlock(registry, "small_dripleaf", new BlockSmallDripleaf());
        }
    }

    private static void registerDeepslateBlocks(IForgeRegistry<Block> registry) {
        if (!Settings.deepslate.enableDeepslate) {
            return;
        }

        // Deepslate blocks now have custom sounds set in their constructors
        registerBlock(registry, "deepslate", (new BlockDeepslate()).setHardness(3F).setResistance(10.0F));
        registerBlock(registry, "cobbled_deepslate", (new BlockCobbledDeepslate()).setHardness(3F).setResistance(10.0F));

        registerBlock(registry, "deepslate_coal_ore", (new BlockDeepslateOre(MapColor.GRAY).setHardness(3.0F).setResistance(10.0F)));
        registerBlock(registry, "deepslate_diamond_ore", (new BlockDeepslateOre(MapColor.GRAY).setHardness(3.0F).setResistance(10.0F)));
        registerBlock(registry, "deepslate_emerald_ore", (new BlockDeepslateOre(MapColor.GRAY).setHardness(3.0F).setResistance(10.0F)));
        registerBlock(registry, "deepslate_gold_ore", (new BlockDeepslateOre(MapColor.GRAY).setHardness(3.0F).setResistance(10.0F)));
        registerBlock(registry, "deepslate_iron_ore", (new BlockDeepslateOre(MapColor.GRAY).setHardness(3.0F).setResistance(10.0F)));
        registerBlock(registry, "deepslate_lapis_ore", (new BlockDeepslateOre(MapColor.GRAY).setHardness(3.0F).setResistance(10.0F)));
        registerBlock(registry, "deepslate_redstone_ore", (new BlockDeepslateRedstoneOre(false).setHardness(3.0F).setResistance(10.0F)));
        registerBlock(registry, "lit_deepslate_redstone_ore", (new BlockDeepslateRedstoneOre(true).setHardness(3.0F).setResistance(10.0F)));
        SoundType bricks = ModSounds.type("deepslate_bricks");
        SoundType tiles = ModSounds.type("deepslate_tiles");
        SoundType polished = ModSounds.type("polished_deepslate");
        SoundType base = ModSounds.type("deepslate");

        registerBlock(registry, "polished_deepslate", (new BlockDeepslateVariant(polished)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "deepslate_bricks", (new BlockDeepslateVariant(bricks)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "deepslate_tiles", (new BlockDeepslateVariant(tiles)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "cracked_deepslate_bricks", (new BlockDeepslateVariant(bricks)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "cracked_deepslate_tiles", (new BlockDeepslateVariant(tiles)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "chiseled_deepslate", (new BlockDeepslateVariant(bricks)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "reinforced_deepslate", (new BlockDeepslateVariant(base)).setHardness(55F).setResistance(1200.0F));

        Block block = (new Block(Material.ROCK)).setHardness(2.0F).setResistance(10.0F).setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        registerBlock(registry, "cobbled_deepslate_stairs", (new BlockDeepslateTileStairs(block.getDefaultState(), base)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "polished_deepslate_stairs", (new BlockDeepslateTileStairs(block.getDefaultState(), polished)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "deepslate_brick_stairs", (new BlockDeepslateTileStairs(block.getDefaultState(), bricks)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "deepslate_tile_stairs", (new BlockDeepslateTileStairs(block.getDefaultState(), tiles)).setHardness(3.5F).setResistance(10.0F));

        registerSlabs(registry, "cobbled_deepslate", base);
        registerSlabs(registry, "polished_deepslate", polished);
        registerSlabs(registry, "deepslate_brick", bricks);
        registerSlabs(registry, "deepslate_tile", tiles);

        registerBlock(registry, "cobbled_deepslate_wall", (new BlockDeepslateWall(block.getDefaultState(), base)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "polished_deepslate_wall", (new BlockDeepslateWall(block.getDefaultState(), polished)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "deepslate_brick_wall", (new BlockDeepslateWall(block.getDefaultState(), bricks)).setHardness(3.5F).setResistance(10.0F));
        registerBlock(registry, "deepslate_tile_wall", (new BlockDeepslateWall(block.getDefaultState(), tiles)).setHardness(3.5F).setResistance(10.0F));
    }

    /**
     * The slab instance is passed to the double slab directly, since the object holder fields are not populated yet.
     */
    private static void registerSlabs(IForgeRegistry<Block> registry, String prefix, SoundType soundType) {
        registerSlabs(registry, prefix, soundType, 3.5F, 10.0F);
    }

    static void registerSlabs(IForgeRegistry<Block> registry, String prefix, SoundType soundType, float hardness, float resistance) {
        registerSlabs(registry, prefix, soundType, hardness, resistance, Material.ROCK);
    }

    static void registerSlabs(IForgeRegistry<Block> registry, String prefix, SoundType soundType, float hardness, float resistance, Material material) {
        Block slab = new BlockSlabBase(material, soundType).setHardness(hardness).setResistance(resistance).setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        registerBlock(registry, prefix + "_slab", slab);
        registerBlock(registry, prefix + "_double_slab", new BlockDoubleSlabBase(material, slab, soundType).setHardness(hardness).setResistance(resistance));
    }

    public static void registerFlammability() {
        if (Settings.buildingBlocks.enableStrippedWood) {
            for (String name : strippedWoodNames()) {
                Blocks.FIRE.setFireInfo(get(name), 5, 5);
            }
        }
        for (String name : bookshelfNames()) {
            if (!name.startsWith("crimson") && !name.startsWith("warped")) {
                Blocks.FIRE.setFireInfo(get(name), 30, 20);
            }
        }
        if (Settings.isContentEnabled("cherry_wood")) {
            for (String name : CHERRY_LOGS) {
                Blocks.FIRE.setFireInfo(get(name), 5, 5);
            }
            for (String name : new String[]{"cherry_planks", "cherry_stairs", "cherry_slab", "cherry_double_slab", "cherry_fence", "cherry_fence_gate"}) {
                Blocks.FIRE.setFireInfo(get(name), 5, 20);
            }
            if (Settings.isContentEnabled("cherry_trees")) {
                Blocks.FIRE.setFireInfo(get("cherry_leaves"), 30, 60);
                Blocks.FIRE.setFireInfo(get("pink_petals"), 60, 100);
            }
        }
        if (Settings.isContentEnabled("bamboo_wood")) {
            for (String name : BAMBOO_BLOCKS) {
                Blocks.FIRE.setFireInfo(get(name), 5, 5);
            }
            for (String name : new String[]{"bamboo_planks", "bamboo_stairs", "bamboo_slab", "bamboo_double_slab", "bamboo_mosaic", "bamboo_mosaic_stairs",
                    "bamboo_mosaic_slab", "bamboo_mosaic_double_slab", "bamboo_fence", "bamboo_fence_gate"}) {
                Blocks.FIRE.setFireInfo(get(name), 5, 20);
            }
        }
        if (Settings.plants.enableLushPlants) {
            Blocks.FIRE.setFireInfo(get("azalea_leaves"), 30, 60);
            Blocks.FIRE.setFireInfo(get("flowering_azalea_leaves"), 30, 60);
            Blocks.FIRE.setFireInfo(get("hanging_roots"), 30, 60);
            Blocks.FIRE.setFireInfo(get("spore_blossom"), 60, 100);
        }
    }

    private static final class StoneVariant {
        final String name;
        final IBlockState base;
        final float hardness;
        final float resistance;

        StoneVariant(String name, IBlockState base, float hardness, float resistance) {
            this.name = name;
            this.base = base;
            this.hardness = hardness;
            this.resistance = resistance;
        }
    }

}
