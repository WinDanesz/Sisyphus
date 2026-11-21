package com.windanesz.sisyphus.registry;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.recipe.DecoratedPotRecipe;
import com.windanesz.sisyphus.recipe.LeatherHorseArmorDyeRecipe;
import com.windanesz.sisyphus.recipe.SuspiciousStewRecipe;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public final class ModRecipes {

    private ModRecipes() {
    } // no instances

    @SubscribeEvent
    public static void register(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> registry = event.getRegistry();
        if (Settings.archaeology.enableArchaeology) {
            registry.register(new DecoratedPotRecipe().setRegistryName(SisyphusBackport.MODID, "decorated_pot"));
        }
        if (Settings.items.enableSuspiciousStew) {
            registry.register(new SuspiciousStewRecipe().setRegistryName(SisyphusBackport.MODID, "suspicious_stew"));
        }
        if (Settings.items.enableLeatherHorseArmor) {
            registry.register(new LeatherHorseArmorDyeRecipe().setRegistryName(SisyphusBackport.MODID, "leather_horse_armor_dyeing"));
        }
        registerBookshelfRecipes(registry);
    }

    private static void registerBookshelfRecipes(IForgeRegistry<IRecipe> registry) {
        if (!Settings.buildingBlocks.enableWoodBookshelves) {
            return;
        }
        // The vanilla recipe takes planks of any wood, and being registered first it would win for all of them
        registry.register(bookshelfRecipe(new ItemStack(Blocks.BOOKSHELF), new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.OAK.getMetadata()))
                .setRegistryName(new ResourceLocation("minecraft", "bookshelf")));
        for (String name : ModBlocks.bookshelfNames()) {
            String wood = name.substring(0, name.length() - "_bookshelf".length());
            ItemStack planks = new ItemStack(ModBlocks.get(wood + "_planks"));
            for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
                if (type.getName().equals(wood)) {
                    planks = new ItemStack(Blocks.PLANKS, 1, type.getMetadata());
                }
            }
            registry.register(bookshelfRecipe(new ItemStack(ModBlocks.get(name)), planks).setRegistryName(new ResourceLocation(SisyphusBackport.MODID, name)));
        }
    }

    static IRecipe bookshelfRecipe(ItemStack result, ItemStack planks) {
        return new ShapedOreRecipe(null, result, "###", "XXX", "###", '#', planks, 'X', Items.BOOK);
    }

    public static void registerSmelting() {
        FurnaceRecipes recipes = FurnaceRecipes.instance();
        ItemStack charcoal = new ItemStack(Items.COAL, 1, 1);
        if (Settings.buildingBlocks.enableStrippedWood) {
            for (String name : ModBlocks.strippedWoodNames()) {
                recipes.addSmeltingRecipeForBlock(ModBlocks.get(name), charcoal, 0.15F);
            }
        }
        if (Settings.isContentEnabled("cherry_wood")) {
            for (String name : ModBlocks.CHERRY_LOGS) {
                recipes.addSmeltingRecipeForBlock(ModBlocks.get(name), charcoal, 0.15F);
            }
        }
        if (Settings.buildingBlocks.enableSmoothBlocks) {
            recipes.addSmeltingRecipe(new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.STONE.getMetadata()), new ItemStack(ModBlocks.get("smooth_stone")), 0.1F);
            recipes.addSmeltingRecipe(new ItemStack(Blocks.SANDSTONE, 1, 0), new ItemStack(ModBlocks.get("smooth_sandstone")), 0.1F);
            recipes.addSmeltingRecipe(new ItemStack(Blocks.RED_SANDSTONE, 1, 0), new ItemStack(ModBlocks.get("smooth_red_sandstone")), 0.1F);
            recipes.addSmeltingRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, BlockQuartz.EnumType.DEFAULT.getMetadata()), new ItemStack(ModBlocks.get("smooth_quartz")), 0.1F);
        }
        registerNetherSmelting(recipes);
        if (Settings.buildingBlocks.enableAmethyst && Settings.buildingBlocks.enableBasalt) {
            recipes.addSmeltingRecipe(new ItemStack(ModBlocks.get("basalt")), new ItemStack(ModBlocks.get("smooth_basalt")), 0.1F);
        }
        if (Settings.deepslate.enableDeepslate) {
            registerDeepslateSmelting(recipes);
        }
    }

    private static void registerNetherSmelting(FurnaceRecipes recipes) {
        if (Settings.buildingBlocks.enableNetherBrickVariants) {
            recipes.addSmeltingRecipe(new ItemStack(Blocks.NETHER_BRICK), new ItemStack(ModBlocks.get("cracked_nether_bricks")), 0.1F);
        }
        if (Settings.buildingBlocks.enableNetherGold) {
            recipes.addSmeltingRecipe(new ItemStack(ModBlocks.get("nether_gold_ore")), new ItemStack(Items.GOLD_INGOT), 1.0F);
        }
        if (Settings.buildingBlocks.enableBlackstone) {
            recipes.addSmeltingRecipe(new ItemStack(ModBlocks.get("polished_blackstone_bricks")), new ItemStack(ModBlocks.get("cracked_polished_blackstone_bricks")), 0.1F);
        }
        if (Settings.items.enableNetherite) {
            recipes.addSmeltingRecipe(new ItemStack(ModBlocks.get("ancient_debris")), new ItemStack(ModItems.get("netherite_scrap")), 2.0F);
        }
    }

    private static void registerDeepslateSmelting(FurnaceRecipes recipes) {
        recipes.addSmeltingRecipeForBlock(ModBlocks.cobbled_deepslate, new ItemStack(Item.getItemFromBlock(ModBlocks.deepslate)), 0.1F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_gold_ore, new ItemStack(Items.GOLD_INGOT), 1.0F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_diamond_ore, new ItemStack(Items.DIAMOND), 1.0F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_iron_ore, new ItemStack(Items.IRON_INGOT), 0.7F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_redstone_ore, new ItemStack(Items.REDSTONE), 0.7F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_lapis_ore, new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), 0.2F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_coal_ore, new ItemStack(Items.COAL), 0.1F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_emerald_ore, new ItemStack(Items.EMERALD), 1.0F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_bricks, new ItemStack(Item.getItemFromBlock(ModBlocks.cracked_deepslate_bricks)), 0.1F);
        recipes.addSmeltingRecipeForBlock(ModBlocks.deepslate_tiles, new ItemStack(Item.getItemFromBlock(ModBlocks.cracked_deepslate_tiles)), 0.1F);
    }
}
