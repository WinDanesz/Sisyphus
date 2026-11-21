package com.windanesz.sisyphus.integration;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockDeepslateOreVariant;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class ModdedDeepslateOres {

    private static final Map<Block, Block> VARIANTS = new LinkedHashMap<>();

    private ModdedDeepslateOres() {
    }

    public static boolean isEnabled() {
        return Settings.deepslate.enableDeepslate && Settings.deepslate.enableDeepslateModOres;
    }

    @Nullable
    public static Block variantOf(Block original) {
        return VARIANTS.get(original);
    }

    public static Map<Block, Block> all() {
        return Collections.unmodifiableMap(VARIANTS);
    }

    public static String variantName(ResourceLocation original) {
        return "deepslate_" + original.getNamespace() + "_" + original.getPath().replace('/', '_');
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        if (!isEnabled()) {
            return;
        }
        IForgeRegistry<Block> registry = event.getRegistry();
        List<Block> found = new ArrayList<>();
        for (Block block : registry.getValuesCollection()) {
            if (!(block instanceof BlockDeepslateOreVariant)
                    && OreDetector.isCandidate(block, Settings.deepslate.deepslateOreWhitelist, Settings.deepslate.deepslateOreBlacklist)) {
                found.add(block);
            }
        }
        for (Block original : found) {
            String name = variantName(original.getRegistryName());
            if (registry.containsKey(new ResourceLocation(SisyphusBackport.MODID, name))) {
                continue;
            }
            BlockDeepslateOreVariant variant = new BlockDeepslateOreVariant(original);
            variant.setRegistryName(SisyphusBackport.MODID, name);
            variant.setTranslationKey(SisyphusBackport.MODID + "." + name);
            registry.register(variant);
            VARIANTS.put(original, variant);
        }
        if (!found.isEmpty()) {
            SisyphusBackport.logger.info("Made deepslate versions of {} ores of other mods", VARIANTS.size());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Map.Entry<Block, Block> entry : VARIANTS.entrySet()) {
            Block variant = entry.getValue();
            event.getRegistry().register(new VariantItem(variant, entry.getKey()).setRegistryName(variant.getRegistryName()));
        }
    }

    /**
     * Runs after every mod has loaded.
     */
    public static void registerRecipes() {
        for (Map.Entry<Block, Block> entry : VARIANTS.entrySet()) {
            ItemStack ore = new ItemStack(entry.getKey());
            ItemStack variant = new ItemStack(entry.getValue());
            if (ore.isEmpty()) {
                continue;
            }
            for (int id : OreDictionary.getOreIDs(ore)) {
                OreDictionary.registerOre(OreDictionary.getOreName(id), variant);
            }
            ItemStack smelted = FurnaceRecipes.instance().getSmeltingResult(ore);
            if (!smelted.isEmpty()) {
                FurnaceRecipes.instance().addSmeltingRecipe(variant, smelted.copy(), FurnaceRecipes.instance().getSmeltingExperience(ore));
            }
        }
    }

    public static class VariantItem extends ItemBlock {
        private final Block original;

        public VariantItem(Block variant, Block original) {
            super(variant);
            this.original = original;
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            return I18n.translateToLocalFormatted("sisyphus.deepslate_variant", new ItemStack(original).getDisplayName());
        }
    }
}
