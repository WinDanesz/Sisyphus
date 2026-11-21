package com.windanesz.sisyphus.recipe;

import com.windanesz.sisyphus.item.ItemFlowerDye;
import com.windanesz.sisyphus.item.ItemLeatherHorseArmor;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class LeatherHorseArmorDyeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Nullable
    private static ItemStack craft(InventoryCrafting inv) {
        ItemStack armor = ItemStack.EMPTY;
        EnumDyeColor dye = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof ItemLeatherHorseArmor && armor.isEmpty()) {
                armor = stack;
            } else if (dye == null && ItemFlowerDye.colorOf(stack) != null) {
                dye = ItemFlowerDye.colorOf(stack);
            } else {
                return null;
            }
        }
        if (armor.isEmpty() || dye == null) {
            return null;
        }
        ItemStack result = armor.copy();
        result.setCount(1);
        ItemLeatherHorseArmor.setDye(result, dye);
        return result;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return craft(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack result = craft(inv);
        return result == null ? ItemStack.EMPTY : result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
