package com.windanesz.sisyphus.recipe;

import com.windanesz.sisyphus.item.ItemSuspiciousStew;
import com.windanesz.sisyphus.registry.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class SuspiciousStewRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Nullable
    private static ItemStack craft(InventoryCrafting inv) {
        boolean red = false;
        boolean brown = false;
        boolean bowl = false;
        Object[] effect = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            Item item = stack.getItem();
            if (item == Item.getItemFromBlock(Blocks.RED_MUSHROOM) && !red) {
                red = true;
            } else if (item == Item.getItemFromBlock(Blocks.BROWN_MUSHROOM) && !brown) {
                brown = true;
            } else if (item == Items.BOWL && !bowl) {
                bowl = true;
            } else if (effect == null && ItemSuspiciousStew.effectOf(stack) != null) {
                effect = ItemSuspiciousStew.effectOf(stack);
            } else {
                return null;
            }
        }
        if (!(red && brown && bowl && effect != null)) {
            return null;
        }
        ItemStack stew = new ItemStack(ModItems.get("suspicious_stew"));
        ItemSuspiciousStew.addEffect(stew, (Potion) effect[0], (Integer) effect[1]);
        return stew;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return craft(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack stew = craft(inv);
        return stew == null ? ItemStack.EMPTY : stew;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
