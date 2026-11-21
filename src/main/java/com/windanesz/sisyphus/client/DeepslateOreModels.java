package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.integration.ModdedDeepslateOres;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SisyphusBackport.MODID)
public final class DeepslateOreModels {

    private DeepslateOreModels() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Block variant : ModdedDeepslateOres.all().values()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(variant), 0, new ModelResourceLocation(variant.getRegistryName(), "inventory"));
        }
    }
}
