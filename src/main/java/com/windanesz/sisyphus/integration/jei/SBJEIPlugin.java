package com.windanesz.sisyphus.integration.jei;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.gui.GuiGrindstone;
import com.windanesz.sisyphus.client.gui.GuiSmithing;
import com.windanesz.sisyphus.inventory.*;
import com.windanesz.sisyphus.item.ArmorTrims;
import com.windanesz.sisyphus.item.ItemBannerPattern;
import com.windanesz.sisyphus.packet.ServerSettings;
import com.windanesz.sisyphus.recipe.CookingRecipes;
import com.windanesz.sisyphus.recipe.SmithingRecipes;
import com.windanesz.sisyphus.recipe.StonecutterRecipes;
import com.windanesz.sisyphus.tileentity.TileEntityCampfire;
import com.windanesz.sisyphus.worldgen.ArchaeologyLoot;
import com.windanesz.sisyphus.worldgen.ArchaeologySites;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.*;

@JEIPlugin
public class SBJEIPlugin implements IModPlugin {

    private static final String STONECUTTING = "stonecutting";
    private static final String SMITHING = "smithing";
    private static final String LOOM = "loom";
    private static final String GRINDSTONE = "grindstone";
    private static final String CARTOGRAPHY = "cartography";
    private static final String SMOKING = "smoking";
    private static final String BLASTING = "blasting";
    private static final String CAMPFIRE = "campfire_cooking";

    private static final int[][] NO_PLUSES = new int[0][];

    @Nullable
    private static IJeiRuntime runtime;

    private static String uid(String id) {
        return SisyphusBackport.MODID + ":" + id;
    }

    private static WorkstationCategory cooking(IGuiHelper gui, String id, int ticks) {
        return new WorkstationCategory(gui, id, 78, 36, new int[][]{{0, 4}}, new int[]{60, 4}, new int[]{28, 5}, NO_PLUSES, ticks);
    }

    public static void updateBrushingTab() {
        IJeiRuntime jei = runtime;
        if (jei == null || !Settings.isContentEnabled("archaeology")) {
            return;
        }
        if (ServerSettings.showBrushingInJei()) {
            jei.getRecipeRegistry().unhideRecipeCategory(uid(BrushingCategory.ID));
        } else {
            jei.getRecipeRegistry().hideRecipeCategory(uid(BrushingCategory.ID));
        }
    }

    private static void addBrushing(IModRegistry registry) {
        Item brushItem = Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, "brush"));
        if (brushItem == null || brushItem == Items.AIR) {
            return;
        }
        ItemStack brush = new ItemStack(brushItem);
        List<BrushingRecipe> recipes = new ArrayList<>();
        for (String table : ArchaeologyLoot.LOOT_TABLES) {
            ItemStack block = block(ArchaeologyLoot.suspiciousBlockOf(table));
            if (!ArchaeologySites.isGenerated(table) || block.isEmpty()) {
                continue;
            }
            BrushingRecipe recipe = new BrushingRecipe(table, block, brush);
            if (!recipe.isEmpty()) {
                recipes.add(recipe);
            }
        }
        String uid = uid(BrushingCategory.ID);
        registry.handleRecipes(BrushingRecipe.class, recipe -> recipe, uid);
        registry.addRecipes(recipes, uid);
        registry.addRecipeCatalyst(brush, uid);
        registry.addIngredientInfo(brush, VanillaTypes.ITEM, "jei.sisyphus.info.brush");
    }

    private static ItemStack block(String name) {
        return new ItemStack(Block.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name)));
    }

    private static void add(IModRegistry registry, String id, List<WorkstationRecipe> recipes, String... workstations) {
        String uid = uid(id);
        registry.handleRecipes(WorkstationRecipe.class, recipe -> recipe, uid);
        registry.addRecipes(recipes, uid);
        for (String name : workstations) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name));
            if (block != Blocks.AIR) {
                registry.addRecipeCatalyst(new ItemStack(block), uid);
            }
        }
    }

    private static List<WorkstationRecipe> stonecutting() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        for (StonecutterRecipes.Cut cut : StonecutterRecipes.getAll()) {
            recipes.add(new WorkstationRecipe(Collections.singletonList(stacks(cut.input)), cut.output, null));
        }
        return recipes;
    }

    private static boolean hasSmithing() {
        return Settings.functionalBlocks.enableWorkstations && (Settings.isContentEnabled("netherite") || ArmorTrims.isEnabled());
    }

    private static List<WorkstationRecipe> smithing() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        for (SmithingRecipes.Recipe recipe : SmithingRecipes.getAll()) {
            List<ItemStack> template = recipe.needsTemplate() ? stacks(recipe.template) : Collections.emptyList();
            recipes.add(new WorkstationRecipe(Arrays.asList(template, stacks(recipe.base), stacks(recipe.addition)), recipe.output, null));
        }
        // Each trim template on every piece of armor with every material, cycled together so the output follows
        List<ItemStack> armor = ArmorTrims.trimmableArmorStacks();
        List<ItemStack> materials = ArmorTrims.materialItems();
        for (Item item : ArmorTrims.trimTemplates()) {
            ItemStack template = new ItemStack(item);
            List<ItemStack> bases = new ArrayList<>();
            List<ItemStack> additions = new ArrayList<>();
            List<ItemStack> outputs = new ArrayList<>();
            for (ItemStack material : materials) {
                for (ItemStack piece : armor) {
                    bases.add(piece);
                    additions.add(material);
                    outputs.add(SmithingRecipes.result(template, piece, material));
                }
            }
            recipes.add(new WorkstationRecipe(Arrays.asList(Collections.singletonList(template), bases, additions), outputs, null));
        }
        return recipes;
    }

    private static List<WorkstationRecipe> loom() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        ItemStack banner = new ItemStack(Items.BANNER, 1, EnumDyeColor.WHITE.getDyeDamage());
        ItemStack dye = new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage());
        for (BannerPattern pattern : BannerPattern.values()) {
            if (pattern == BannerPattern.BASE) {
                continue;
            }
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Pattern", pattern.getHashname());
            entry.setInteger("Color", EnumDyeColor.BLACK.getDyeDamage());
            NBTTagList patterns = new NBTTagList();
            patterns.appendTag(entry);
            ItemStack result = ItemBanner.makeBanner(EnumDyeColor.WHITE, patterns);

            Item patternItem = ItemBannerPattern.itemFor(pattern);
            if (ItemBannerPattern.isSpecial(pattern)) {
                if (patternItem == null) {
                    continue;
                }
                recipes.add(WorkstationRecipe.of(result, null, banner, dye, new ItemStack(patternItem)));
            } else {
                recipes.add(WorkstationRecipe.of(result, null, banner, dye));
            }
        }
        return recipes;
    }

    private static List<WorkstationRecipe> grindstone() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        ItemStack enchanted = new ItemStack(Items.DIAMOND_PICKAXE);
        if (Enchantments.EFFICIENCY != null) {
            enchanted.addEnchantment(Enchantments.EFFICIENCY, 5);
        }
        if (Enchantments.UNBREAKING != null) {
            enchanted.addEnchantment(Enchantments.UNBREAKING, 3);
        }
        recipes.add(WorkstationRecipe.of(new ItemStack(Items.DIAMOND_PICKAXE), "grindstone_disenchant", enchanted));
        recipes.add(WorkstationRecipe.of(new ItemStack(Items.BOOK), "grindstone_disenchant", ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(Enchantments.SHARPNESS, 5))));

        int max = Items.DIAMOND_PICKAXE.getMaxDamage();
        ItemStack first = new ItemStack(Items.DIAMOND_PICKAXE);
        first.setItemDamage(max * 2 / 3);
        ItemStack second = new ItemStack(Items.DIAMOND_PICKAXE);
        second.setItemDamage(max * 3 / 4);
        ItemStack repaired = new ItemStack(Items.DIAMOND_PICKAXE);
        int left = (max - first.getItemDamage()) + (max - second.getItemDamage()) + max * 5 / 100;
        repaired.setItemDamage(Math.max(max - left, 0));
        recipes.add(WorkstationRecipe.of(repaired, "grindstone_repair", first, second));
        return recipes;
    }

    private static List<WorkstationRecipe> cartography() {
        ItemStack map = new ItemStack(Items.FILLED_MAP);
        return Arrays.asList(
                WorkstationRecipe.of(new ItemStack(Items.FILLED_MAP), "cartography_zoom", map, new ItemStack(Items.PAPER)),
                WorkstationRecipe.of(new ItemStack(Items.FILLED_MAP, 2), "cartography_copy", map, new ItemStack(Items.MAP)));
    }

    /**
     * Furnace recipes shadowed by a script recipe are left out.
     */
    private static List<WorkstationRecipe> cooked(CookingRecipes block, int ticks) {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        for (CookingRecipes.Recipe recipe : block.getAdded()) {
            recipes.add(new WorkstationRecipe(Collections.singletonList(stacks(recipe.input)), recipe.output, "cooking_time", ticks / 20));
        }
        for (Map.Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            ItemStack input = entry.getKey();
            if (block.takesFurnaceRecipe(input, entry.getValue()) && !block.hasOwnRecipeFor(input)) {
                recipes.add(cooking(input, entry.getValue(), ticks));
            }
        }
        return recipes;
    }

    private static List<ItemStack> stacks(Ingredient ingredient) {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            list.add(stack.copy());
        }
        return list;
    }

    private static WorkstationRecipe cooking(ItemStack input, ItemStack output, int ticks) {
        List<ItemStack> alternatives = new ArrayList<>();
        if (input.getMetadata() == OreDictionary.WILDCARD_VALUE) {
            NonNullList<ItemStack> variants = NonNullList.create();
            input.getItem().getSubItems(CreativeTabs.SEARCH, variants);
            alternatives.addAll(variants);
        }
        if (alternatives.isEmpty()) {
            ItemStack single = input.copy();
            if (single.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                single.setItemDamage(0);
            }
            alternatives.add(single);
        }
        return new WorkstationRecipe(Collections.singletonList(alternatives), output, "cooking_time", ticks / 20);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper gui = registry.getJeiHelpers().getGuiHelper();
        if (Settings.isContentEnabled("stonecutter")) {
            registry.addRecipeCategories(new WorkstationCategory(gui, STONECUTTING, 78, 26, new int[][]{{0, 4}}, new int[]{60, 4}, new int[]{28, 5}, NO_PLUSES, 0));
        }
        if (hasSmithing()) {
            registry.addRecipeCategories(new WorkstationCategory(gui, SMITHING, 130, 26, new int[][]{{0, 4}, {26, 4}, {52, 4}}, new int[]{112, 4},
                    new int[]{80, 5}, new int[][]{{20, 8}, {46, 8}}, 0));
        }
        if (Settings.isContentEnabled("loom")) {
            registry.addRecipeCategories(new WorkstationCategory(gui, LOOM, 130, 26, new int[][]{{0, 4}, {26, 4}, {52, 4}}, new int[]{112, 4}, new int[]{80, 5},
                    new int[][]{{20, 8}, {46, 8}}, 0));
        }
        if (Settings.isContentEnabled("grindstone")) {
            registry.addRecipeCategories(new WorkstationCategory(gui, GRINDSTONE, 104, 36, new int[][]{{0, 4}, {26, 4}}, new int[]{86, 4}, new int[]{54, 5},
                    new int[][]{{20, 8}}, 0));
        }
        if (Settings.isContentEnabled("cartography_table")) {
            registry.addRecipeCategories(new WorkstationCategory(gui, CARTOGRAPHY, 104, 36, new int[][]{{0, 4}, {26, 4}}, new int[]{86, 4}, new int[]{54, 5},
                    new int[][]{{20, 8}}, 0));
        }
        if (Settings.isContentEnabled("furnaces")) {
            registry.addRecipeCategories(cooking(gui, SMOKING, 100), cooking(gui, BLASTING, 100));
        }
        if (Settings.isContentEnabled("campfire")) {
            registry.addRecipeCategories(cooking(gui, CAMPFIRE, TileEntityCampfire.COOK_TIME));
        }
        if (Settings.isContentEnabled("archaeology")) {
            registry.addRecipeCategories(new BrushingCategory(gui));
        }
    }

    @Override
    public void register(IModRegistry registry) {
        if (Settings.isContentEnabled("stonecutter")) {
            add(registry, STONECUTTING, stonecutting(), "stonecutter");
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerStonecutter.class, uid(STONECUTTING), 0, 1, 2, 36);
        }
        if (hasSmithing()) {
            add(registry, SMITHING, smithing(), "smithing_table");
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerSmithing.class, uid(SMITHING), 0, 3, 4, 36);
            registry.addRecipeClickArea(GuiSmithing.class, 65, 46, 28, 21, uid(SMITHING));
        }
        if (Settings.isContentEnabled("loom")) {
            add(registry, LOOM, loom(), "loom");
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerLoom.class, uid(LOOM), 0, 3, 4, 36);
        }
        if (Settings.isContentEnabled("grindstone")) {
            add(registry, GRINDSTONE, grindstone(), "grindstone");
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerGrindstone.class, uid(GRINDSTONE), 0, 2, 3, 36);
            registry.addRecipeClickArea(GuiGrindstone.class, 92, 31, 28, 21, uid(GRINDSTONE));
        }
        if (Settings.isContentEnabled("cartography_table")) {
            add(registry, CARTOGRAPHY, cartography(), "cartography_table");
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerCartography.class, uid(CARTOGRAPHY), 0, 2, 3, 36);
        }
        if (Settings.isContentEnabled("furnaces")) {
            add(registry, SMOKING, cooked(CookingRecipes.SMOKER, 100), "smoker");
            add(registry, BLASTING, cooked(CookingRecipes.BLAST_FURNACE, 100), "blast_furnace");
        }
        if (Settings.isContentEnabled("campfire")) {
            add(registry, CAMPFIRE, cooked(CookingRecipes.CAMPFIRE, TileEntityCampfire.COOK_TIME), "campfire", "soul_campfire");
        }
        if (Settings.isContentEnabled("archaeology")) {
            addBrushing(registry);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        updateBrushingTab();
    }
}
