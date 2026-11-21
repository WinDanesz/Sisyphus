package com.windanesz.sisyphus.recipe;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.tileentity.TileEntityDecoratedPot;
import com.windanesz.sisyphus.worldgen.ArchaeologyLoot;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.ArrayList;
import java.util.List;

public class DecoratedPotRecipe extends ShapedOreRecipe {

    private static final int[] SLOTS = {1, 3, 5, 7};

    public DecoratedPotRecipe() {
        super(new ResourceLocation(SisyphusBackport.MODID, "decorated_pot"), new ItemStack(ModBlocks.get("decorated_pot")),
                " # ", "# #", " # ", '#', ingredient());
    }

    private static Ingredient ingredient() {
        List<Item> items = new ArrayList<>();
        items.add(Items.BRICK);
        for (String name : ArchaeologyLoot.sherdNames()) {
            Item sherd = Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name + "_pottery_sherd"));
            if (sherd != null && sherd != Items.AIR) {
                items.add(sherd);
            }
        }
        return Ingredient.fromItems(items.toArray(new Item[0]));
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        if (inv.getWidth() != 3 || inv.getHeight() != 3) {
            return ItemStack.EMPTY;
        }
        Item[] decorations = new Item[SLOTS.length];
        for (int i = 0; i < SLOTS.length; i++) {
            decorations[i] = inv.getStackInSlot(SLOTS[i]).getItem();
        }
        return TileEntityDecoratedPot.createPotItem(this.output.getItem(), decorations);
    }
}
