package com.windanesz.sisyphus.util;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.integration.OreDetector;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public final class PottedPlants {

    private PottedPlants() {
    }

    public static boolean canPot(ItemStack stack) {
        if (!Settings.plants.potAnySmallPlant || stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
            return false;
        }
        Block block = ((ItemBlock) stack.getItem()).getBlock();
        ResourceLocation name = block.getRegistryName();
        if (name == null || OreDetector.matchesAny(Settings.plants.unpottablePlants, name)) {
            return false;
        }
        if (OreDetector.matchesAny(Settings.plants.extraPottedPlants, name)) {
            return true;
        }
        // The rest of the plants of the game, tall grass, lily pads and the like, stay out of pots as in every version
        return !"minecraft".equals(name.getNamespace()) && isSmallPlant(block);
    }

    static boolean isSmallPlant(Block block) {
        if (!(block instanceof BlockBush) || block instanceof BlockDoublePlant || block instanceof BlockCrops || block instanceof BlockStem
                || block instanceof BlockLilyPad) {
            return false;
        }
        // Tall plants of other mods are mostly bushes with an upper and a lower half
        for (IProperty<?> property : block.getBlockState().getProperties()) {
            if ("half".equals(property.getName())) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public static IBlockState plantOf(@Nullable Item item, int data) {
        if (!(item instanceof ItemBlock)) {
            return null;
        }
        try {
            return ((ItemBlock) item).getBlock().getStateFromMeta(item.getMetadata(data));
        } catch (RuntimeException e) {
            // Some blocks throw on a metadata that they do not use
            return ((ItemBlock) item).getBlock().getDefaultState();
        }
    }
}
