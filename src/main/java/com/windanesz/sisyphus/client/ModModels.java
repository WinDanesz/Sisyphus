package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.RenderDecoratedPotItem;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class ModModels {

    private ModModels() {
    }

    @SubscribeEvent
    public static void register(ModelRegistryEvent event) {
        // The item of the decorated pot is drawn by its model of the game, with the sides it was made from
        Item pot = Item.getItemFromBlock(ModBlocks.get("decorated_pot"));
        if (pot != Items.AIR) {
            pot.setTileEntityItemStackRenderer(new RenderDecoratedPotItem());
        }

        // The bubble column is drawn as water is, by the fluid renderer of the game, so it has no model
        Block bubbleColumn = ModBlocks.get("bubble_column");
        if (bubbleColumn != Blocks.AIR) {
            ModelLoader.setCustomStateMapper(bubbleColumn, block -> Collections.emptyMap());
        }

        // Every item of the mod uses the inventory model named after it, whatever its metadata
        for (Item item : Item.REGISTRY) {
            if (SisyphusBackport.MODID.equals(item.getRegistryName().getNamespace())) {
                ModelResourceLocation model = new ModelResourceLocation(item.getRegistryName(), "inventory");
                ModelBakery.registerItemVariants(item, model);
                ModelLoader.setCustomMeshDefinition(item, stack -> model);
            }
        }
    }
}
