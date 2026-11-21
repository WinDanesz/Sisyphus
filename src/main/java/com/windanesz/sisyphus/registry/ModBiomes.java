package com.windanesz.sisyphus.registry;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.worldgen.BiomeBambooJungle;
import com.windanesz.sisyphus.worldgen.BiomeCherryGrove;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class ModBiomes {

    private ModBiomes() {
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        if (Settings.isContentEnabled("cherry_grove")) {
            Biome grove = new BiomeCherryGrove();
            grove.setRegistryName(SisyphusBackport.MODID, "cherry_grove");
            event.getRegistry().register(grove);
            BiomeDictionary.addTypes(grove, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.LUSH);
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(grove, Settings.worldGeneration.biomes.cherryGroveWeight));
        }
        if (!Settings.worldGeneration.biomes.enableBambooJungle || !Settings.plants.enableBamboo) {
            return;
        }
        Biome biome = new BiomeBambooJungle();
        biome.setRegistryName(SisyphusBackport.MODID, "bamboo_jungle");
        event.getRegistry().register(biome);

        BiomeDictionary.addTypes(biome, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.HOT, BiomeDictionary.Type.WET,
                BiomeDictionary.Type.DENSE, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.LUSH);
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(biome, Settings.worldGeneration.biomes.bambooJungleWeight));
    }
}
