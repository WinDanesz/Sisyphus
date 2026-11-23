package com.windanesz.sisyphus.registry;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.*;
import com.windanesz.sisyphus.entity.item.EntityBambooChestRaft;
import com.windanesz.sisyphus.entity.item.EntityBambooRaft;
import com.windanesz.sisyphus.entity.living.EntityStrider;
import com.windanesz.sisyphus.item.*;
import com.windanesz.sisyphus.worldgen.ArchaeologyLoot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockSign;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber
public final class ModItems {

    private static final String OTHERSIDE = "music_disc_otherside";
    private static final String GOAT_HORN = "goat_horn";

    private static final String[] DYES = {"blue_dye", "white_dye", "black_dye", "brown_dye"};

    // Bamboo is the exception: its item is an ItemBamboo of the same name, registered with the other items
    private static final Set<String> NO_BLOCK_ITEM = new HashSet<>(Arrays.asList("lit_deepslate_redstone_ore", "soul_fire", "sweet_berry_bush",
            "bamboo", "bamboo_sapling", "cave_vines", "cave_vines_plant", "big_dripleaf_stem", "bubble_column"));

    @Nullable
    private static Item.ToolMaterial netheriteTools;

    private ModItems() {
    } // No instances!

    /**
     * Null rather than the air the registry would give when it is turned off.
     */
    public static Item otherside() {
        Item item = Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, OTHERSIDE));
        return item == null || item == Items.AIR ? null : item;
    }

    public static ItemGoatHorn goatHorn() {
        Item item = Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, GOAT_HORN));
        return item instanceof ItemGoatHorn ? (ItemGoatHorn) item : null;
    }

    /**
     * Air when the item is missing or turned off.
     */
    public static Item get(String name) {
        return Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name));
    }

    @Nullable
    public static Item getOrNull(String name) {
        Item item = get(name);
        return item == null || item == Items.AIR ? null : item;
    }

    public static List<String> cherryItemNames() {
        List<String> names = ModBlocks.cherryBlockNames();
        names.remove("cherry_double_slab");
        return names;
    }

    public static List<String> bambooWoodItemNames() {
        List<String> names = ModBlocks.bambooWoodBlockNames();
        for (String slab : ModBlocks.BAMBOO_SLABS) {
            names.remove(slab + "_double_slab");
        }
        return names;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {

        IForgeRegistry<Item> registry = event.getRegistry();

        // Blocks are registered before items, so a block that the config turned off is not here and gets no item
        for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if (SisyphusBackport.MODID.equals(block.getRegistryName().getNamespace())) {
                Item item = blockItem(block);
                if (item != null) {
                    registry.register(item.setRegistryName(block.getRegistryName()));
                }
            }
        }

        if (Settings.plants.enableFlowers) {
            for (String dye : DYES) {
                EnumDyeColor color = EnumDyeColor.valueOf(dye.substring(0, dye.indexOf('_')).toUpperCase(Locale.ROOT));
                registerItem(registry, SisyphusBackport.MODID, dye, new ItemFlowerDye(color), false);
            }
        }
        if (Settings.plants.enableSweetBerries) {
            registerItem(registry, SisyphusBackport.MODID, "sweet_berries", new ItemSweetBerries(), false);
        }
        if (Settings.items.enableSuspiciousStew) {
            // Not in the creative inventory, on purpose
            registerItem(registry, SisyphusBackport.MODID, "suspicious_stew", new ItemSuspiciousStew(), false);
        }
        if (Settings.functionalBlocks.enableLodestone) {
            registerItem(registry, SisyphusBackport.MODID, "lodestone_compass", new ItemLodestoneCompass(), false);
        }
        if (Settings.mobs.enableStriders) {
            registerItem(registry, SisyphusBackport.MODID, "warped_fungus_on_a_stick", new ItemWarpedFungusOnAStick(), false);
        }
        if (Settings.items.enablePigstep) {
            registerItem(registry, SisyphusBackport.MODID, "music_disc_pigstep", new ItemPigstep(), false);
        }
        if (Settings.buildingBlocks.enableAmethyst) {
            registerItem(registry, SisyphusBackport.MODID, "amethyst_shard", new Item().setCreativeTab(CreativeTabs.MATERIALS), false);
        }
        if (Settings.plants.enableCaveVines) {
            registerItem(registry, SisyphusBackport.MODID, "glow_berries", new ItemGlowBerries(), false);
        }
        if (Settings.items.enableOtherside) {
            registerItem(registry, SisyphusBackport.MODID, OTHERSIDE, new ItemOtherside(), false);
        }
        registerNetheriteItems(registry);
        registerSmithingTemplates(registry);
        if (Settings.plants.enableBamboo) {
            registerItem(registry, SisyphusBackport.MODID, "bamboo", new ItemBamboo(), false);
        }
        if (Settings.items.crossbow.enabled) {
            registerItem(registry, SisyphusBackport.MODID, "crossbow", new ItemCrossbow(), false);
        }
        registerWeaponItems(registry);
        registerBannerPatterns(registry);
        for (String wood : ModBlocks.signWoods()) {
            registerItem(registry, SisyphusBackport.MODID, wood + "_sign",
                    new ItemWoodSign(ModBlocks.get(wood + "_standing_sign"), ModBlocks.get(wood + "_wall_sign")), false);
        }
        if (Settings.items.enableLeatherHorseArmor) {
            ItemLeatherHorseArmor.getArmorType();
            registerItem(registry, SisyphusBackport.MODID, "leather_horse_armor", new ItemLeatherHorseArmor(), false);
        }
        if (Settings.mobs.enableAxolotls) {
            registerItem(registry, SisyphusBackport.MODID, "axolotl_bucket", new ItemAxolotlBucket(), false);
        }
        if (Settings.mobs.enableGoats && Settings.mobs.enableGoatHorns) {
            registerItem(registry, SisyphusBackport.MODID, GOAT_HORN, new ItemGoatHorn(), false);
        }
        if (Settings.archaeology.enableArchaeology) {
            registerItem(registry, SisyphusBackport.MODID, "brush", new ItemBrush(), false);
            for (String name : ArchaeologyLoot.sherdNames()) {
                registerItem(registry, SisyphusBackport.MODID, name + "_pottery_sherd", new Item().setCreativeTab(CreativeTabs.MISC), false);
            }
        }
        if (Settings.isContentEnabled("bamboo_wood")) {
            registerItem(registry, SisyphusBackport.MODID, "bamboo_raft", new ItemRaft(EntityBambooRaft::new), false);
            registerItem(registry, SisyphusBackport.MODID, "bamboo_chest_raft", new ItemRaft(EntityBambooChestRaft::new), false);
        }
        for (String wood : ModBlocks.hangingSignWoods()) {
            registerItem(registry, SisyphusBackport.MODID, wood + "_hanging_sign",
                    new ItemHangingSign(ModBlocks.get(wood + "_hanging_sign"), ModBlocks.get(wood + "_wall_hanging_sign")), false);
        }
    }

    // Signs and hanging signs get their items with the other items, as one item places two blocks. ModdedDeepslateOres gives its ores theirs
    public static boolean hasBlockItem(Block block) {
        return !NO_BLOCK_ITEM.contains(block.getRegistryName().getPath()) && !(block instanceof BlockDoubleSlabBase) && !(block instanceof BlockSign)
                && !(block instanceof BlockCandleCake) && !(block instanceof BlockDeepslateOreVariant);
    }

    @Nullable
    private static Item blockItem(Block block) {
        if (!hasBlockItem(block)) {
            return null;
        }
        String name = block.getRegistryName().getPath();
        if (block instanceof BlockSlabBase) {
            return new ItemBlockSlab(block, ModBlocks.get(name.substring(0, name.length() - "_slab".length()) + "_double_slab"));
        }
        if (block instanceof BlockDoor) {
            return new ItemDoor(block).setTranslationKey(block.getRegistryName().toString()).setCreativeTab(CreativeTabs.REDSTONE);
        }
        if (block instanceof BlockCandle) {
            return new ItemCandle(block);
        }
        switch (name) {
            case "ancient_debris":
            case "netherite_block":
                return new NetheriteItems.Block(block);
            case "scaffolding":
                return new ItemScaffolding(block);
            case "glow_lichen":
                return new ItemGlowLichen(block);
            case "small_dripleaf":
                return new ItemSmallDripleaf(block);
            case "pink_petals":
                return new ItemPinkPetals(block);
            case "decorated_pot":
                return new ItemDecoratedPot(block);
            default:
                return new ItemBlock(block);
        }
    }

    private static void registerNetheriteItems(IForgeRegistry<Item> registry) {
        if (!Settings.items.enableNetherite) {
            return;
        }
        Item ingot = new NetheriteItems.Plain(true);
        registerItem(registry, SisyphusBackport.MODID, "netherite_scrap", new NetheriteItems.Plain(false), false);
        registerItem(registry, SisyphusBackport.MODID, "netherite_ingot", ingot, false);

        // Harvest level 4, 2031 uses, speed 9, 4 points of damage more than hand, enchantability 15
        Item.ToolMaterial tools = EnumHelper.addToolMaterial("SISYPHUS_NETHERITE", 4, 2031, 9.0F, 4.0F, 15);
        tools.setRepairItem(new ItemStack(ingot));
        netheriteTools = tools;
        registerItem(registry, SisyphusBackport.MODID, "netherite_sword", new NetheriteItems.Sword(tools), false);
        registerItem(registry, SisyphusBackport.MODID, "netherite_pickaxe", new NetheriteItems.Pickaxe(tools), false);
        registerItem(registry, SisyphusBackport.MODID, "netherite_axe", new NetheriteItems.Axe(tools), false);
        registerItem(registry, SisyphusBackport.MODID, "netherite_shovel", new NetheriteItems.Shovel(tools), false);
        registerItem(registry, SisyphusBackport.MODID, "netherite_hoe", new NetheriteItems.Hoe(tools), false);

        ItemArmor.ArmorMaterial armor = EnumHelper.addArmorMaterial("SISYPHUS_NETHERITE", SisyphusBackport.MODID + ":netherite", 37,
                new int[]{3, 6, 8, 3}, 15, ModSounds.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F);
        armor.setRepairItem(new ItemStack(ingot));
        registerItem(registry, SisyphusBackport.MODID, "netherite_helmet", new NetheriteItems.Armor(armor, EntityEquipmentSlot.HEAD), false);
        registerItem(registry, SisyphusBackport.MODID, "netherite_chestplate", new NetheriteItems.Armor(armor, EntityEquipmentSlot.CHEST), false);
        registerItem(registry, SisyphusBackport.MODID, "netherite_leggings", new NetheriteItems.Armor(armor, EntityEquipmentSlot.LEGS), false);
        registerItem(registry, SisyphusBackport.MODID, "netherite_boots", new NetheriteItems.Armor(armor, EntityEquipmentSlot.FEET), false);
    }

    private static void registerSmithingTemplates(IForgeRegistry<Item> registry) {
        if (Settings.items.enableNetherite && Settings.functionalBlocks.enableWorkstations) {
            registerItem(registry, SisyphusBackport.MODID, ArmorTrims.NETHERITE_TEMPLATE, new ItemSmithingTemplate(null), false);
        }
        if (ArmorTrims.isEnabled()) {
            for (String pattern : ArmorTrims.PATTERNS) {
                registerItem(registry, SisyphusBackport.MODID, pattern + ArmorTrims.TEMPLATE_SUFFIX, new ItemSmithingTemplate(pattern), false);
            }
        }
    }

    private static void registerBannerPatterns(IForgeRegistry<Item> registry) {
        if (!Settings.functionalBlocks.enableLoom) {
            return;
        }
        if (Settings.items.enableSnoutPattern) {
            ItemBannerPattern.addSnoutPattern();
        }
        List<String> names = ItemBannerPattern.itemNames();
        for (int i = 0; i < names.size(); i++) {
            EnumRarity rarity = i == 3 ? EnumRarity.EPIC : (i == 2 ? EnumRarity.UNCOMMON : EnumRarity.COMMON);
            registerItem(registry, SisyphusBackport.MODID, names.get(i), new ItemBannerPattern(ItemBannerPattern.specialPatterns().get(i), rarity), false);
        }
    }

    private static void registerWeaponItems(IForgeRegistry<Item> registry) {
        if (!Settings.items.throwingDiscs.enabled) {
            return;
        }
        registerItem(registry, SisyphusBackport.MODID, "golden_throwing_disc", new ItemThrowingDisc(Item.ToolMaterial.GOLD, false), false);
        registerItem(registry, SisyphusBackport.MODID, "iron_throwing_disc", new ItemThrowingDisc(Item.ToolMaterial.IRON, false), false);
        registerItem(registry, SisyphusBackport.MODID, "diamond_throwing_disc", new ItemThrowingDisc(Item.ToolMaterial.DIAMOND, false), false);
        if (netheriteTools != null) {
            registerItem(registry, SisyphusBackport.MODID, "netherite_throwing_disc", new ItemThrowingDisc(netheriteTools, true), false);
        }
    }

    /**
     * Bookshelves use the names that Quark does.
     */
    public static void registerOreDictionary() {
        for (String name : ModBlocks.strippedWoodNames()) {
            anyMetaOre("logWood", name);
        }
        for (String dye : DYES) {
            String color = dye.substring(0, dye.indexOf('_'));
            ore("dye" + Character.toUpperCase(color.charAt(0)) + color.substring(1), dye);
        }
        ore("oreGold", "nether_gold_ore");
        ore("cobblestone", "blackstone");
        for (String name : ModBlocks.candleNames()) {
            ore("candle", name);
        }
        ore("gemAmethyst", "amethyst_shard");
        ore("blockAmethyst", "amethyst_block");
        for (String wood : ModBlocks.NETHER_WOODS) {
            for (String name : ModBlocks.netherLogNames(wood)) {
                anyMetaOre("logWood", name);
            }
            registerPlankOres(wood);
        }
        OreDictionary.registerOre("bookshelf", Blocks.BOOKSHELF);
        OreDictionary.registerOre("bookshelfOak", Blocks.BOOKSHELF);
        for (String name : ModBlocks.bookshelfNames()) {
            ore("bookshelf", name);
            ore(bookshelfOreName(name), name);
        }
        for (String name : ModBlocks.CHERRY_LOGS) {
            anyMetaOre("logWood", name);
        }
        registerPlankOres("cherry");
        anyMetaOre("treeLeaves", "cherry_leaves");
        ore("treeSapling", "cherry_sapling");
        // The bamboo blocks are not logs: they make two planks, not four
        registerPlankOres("bamboo");
        anyMetaOre("slabWood", "bamboo_mosaic_slab");
        ore("stairWood", "bamboo_mosaic_stairs");
        ore("stone", "deepslate");
        ore("cobblestone", "cobbled_deepslate");
        ore("oreGold", "deepslate_gold_ore");
        ore("oreIron", "deepslate_iron_ore");
        ore("oreLapis", "deepslate_lapis_ore");
        ore("oreDiamond", "deepslate_diamond_ore");
        ore("oreRedstone", "deepslate_redstone_ore");
        ore("oreEmerald", "deepslate_emerald_ore");
        ore("oreCoal", "deepslate_coal_ore");
    }

    private static void registerPlankOres(String wood) {
        ore("plankWood", wood + "_planks");
        anyMetaOre("slabWood", wood + "_slab");
        ore("stairWood", wood + "_stairs");
        ore("fenceWood", wood + "_fence");
        ore("fenceGateWood", wood + "_fence_gate");
        ore("trapdoorWood", wood + "_trapdoor");
    }

    // Items that the config turned off are not registered, so they are skipped
    private static void ore(String ore, String name) {
        registerOre(ore, name, 0);
    }

    private static void anyMetaOre(String ore, String name) {
        registerOre(ore, name, OreDictionary.WILDCARD_VALUE);
    }

    private static void registerOre(String ore, String name, int meta) {
        Item item = getOrNull(name);
        if (item != null) {
            OreDictionary.registerOre(ore, new ItemStack(item, 1, meta));
        }
    }

    static String bookshelfOreName(String name) {
        StringBuilder ore = new StringBuilder("bookshelf");
        for (String part : name.substring(0, name.length() - "_bookshelf".length()).split("_")) {
            ore.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return ore.toString();
    }

    // below registry methods are courtesy of EB
    public static void registerItem(IForgeRegistry<Item> registry, String modid, String name, Item item, boolean setTabIcon) {
        item.setRegistryName(modid, name);
        item.setTranslationKey(item.getRegistryName().toString());
        registry.register(item);
    }

    public static void registerDispenserBehaviors() {
        // Follows the axolotl's option, not the dispensers' one
        if (Settings.mobs.enableAxolotls) {
            ItemAxolotlBucket.registerDispenserBehavior();
        }
        if (!Settings.gameplay.enableDispenserBehaviors) {
            return;
        }
        registerDispenseAction(Items.SADDLE, ModItems::saddle);
        registerDispenseAction(Items.SHEARS, ModItems::shear);
        if (Settings.functionalBlocks.enableRespawnAnchor) {
            registerDispenseAction(Item.getItemFromBlock(Blocks.GLOWSTONE), BlockRespawnAnchor::dispenseCharge);
        }
    }

    private static void registerDispenseAction(Item item, DispenseAction action) {
        IBehaviorDispenseItem before = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(item);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item, (source, stack) -> {
            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
            if (action.dispense(source, source.getWorld(), source.getBlockPos().offset(facing), stack)) {
                source.getWorld().playEvent(1000, source.getBlockPos(), 0);
                source.getWorld().playEvent(2000, source.getBlockPos(), facing.getXOffset() + 1 + (facing.getZOffset() + 1) * 3);
                return stack;
            }
            return before.dispense(source, stack);
        });
    }

    private static void dispenseSound(World world, Entity entity, SoundEvent sound) {
        world.playSound(null, entity.posX, entity.posY, entity.posZ, sound, SoundCategory.NEUTRAL, 0.5F, 1.0F);
    }

    private static boolean saddle(IBlockSource source, World world, BlockPos front, ItemStack stack) {
        for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(front), e -> e != null && e.isEntityAlive() && !e.isChild())) {
            if (entity instanceof EntityPig && !((EntityPig) entity).getSaddled()) {
                ((EntityPig) entity).setSaddled(true);
                dispenseSound(world, entity, SoundEvents.ENTITY_PIG_SADDLE);
            } else if (entity instanceof EntityStrider && !((EntityStrider) entity).isSaddled()) {
                ((EntityStrider) entity).setSaddled(true);
                dispenseSound(world, entity, ModSounds.event("entity.strider.saddle"));
            } else if (entity instanceof AbstractHorse && !(entity instanceof EntityLlama) && ((AbstractHorse) entity).isTame() && !((AbstractHorse) entity).isHorseSaddled()) {
                IItemHandler inventory = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (inventory == null || !inventory.insertItem(0, new ItemStack(Items.SADDLE), false).isEmpty()) {
                    continue;
                }
                dispenseSound(world, entity, SoundEvents.ENTITY_HORSE_SADDLE);
            } else {
                continue;
            }
            stack.shrink(1);
            return true;
        }
        return false;
    }

    private static boolean shear(IBlockSource source, World world, BlockPos front, ItemStack stack) {
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(front), e -> e instanceof IShearable && e.isEntityAlive())) {
            IShearable shearable = (IShearable) entity;
            BlockPos at = entity.getPosition();
            if (!shearable.isShearable(stack, world, at)) {
                continue;
            }
            if (!world.isRemote) {
                List<ItemStack> drops = shearable.onSheared(stack, world, at, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));
                for (ItemStack drop : drops) {
                    EntityItem item = entity.entityDropItem(drop, 1.0F);
                    if (item != null) {
                        item.motionY += world.rand.nextFloat() * 0.05F;
                        item.motionX += (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
                        item.motionZ += (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
                    }
                }
                if (stack.attemptDamageItem(1, world.rand, null)) {
                    stack.setCount(0);
                }
            }
            return true;
        }
        return false;
    }

    private interface DispenseAction {
        boolean dispense(IBlockSource source, World world, BlockPos front, ItemStack stack);
    }
}
