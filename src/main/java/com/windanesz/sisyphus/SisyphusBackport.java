package com.windanesz.sisyphus;

import com.windanesz.sisyphus.command.CommandRegistry;
import com.windanesz.sisyphus.integration.ModdedDeepslateOres;
import com.windanesz.sisyphus.integration.fletchery.SBFletcheryIntegration;
import com.windanesz.sisyphus.integration.futuremc.SBFutureMCIntegration;
import com.windanesz.sisyphus.integration.jei.SBJEIIntegration;
import com.windanesz.sisyphus.integration.oceanicexpanse.SBOceanicExpanseIntegration;
import com.windanesz.sisyphus.inventory.ModGuiHandler;
import com.windanesz.sisyphus.packet.ModPacketHandler;
import com.windanesz.sisyphus.recipe.StrippingRecipes;
import com.windanesz.sisyphus.registry.*;
import com.windanesz.sisyphus.sisyphus.Tags;
import com.windanesz.sisyphus.worldgen.*;
import com.windanesz.sisyphus.worldgen.camps.WorldGenAbandonedCamps;
import com.windanesz.sisyphus.worldgen.ruins.WorldGenRuins;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptedMinecraftVersions = "1.12.2",
        dependencies = "required-after:azurelib@[1.1.5,)")
public class SisyphusBackport {

    public static final String MODID = Tags.MOD_ID;
    public static final String NAME = Tags.MOD_NAME;

    public static Logger logger;

    @Mod.Instance(Tags.MOD_ID)
    public static SisyphusBackport instance;

    @SidedProxy(clientSide = "com.windanesz.sisyphus.client.ClientProxy", serverSide = "com.windanesz.sisyphus.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        ModEntities.addCreatureTypes();
        proxy.construct();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        // Before anything is registered (content yields to other mods) and before any world is read (Future MC worlds)
        SBFletcheryIntegration.init();
        SBFutureMCIntegration.init();
        SBOceanicExpanseIntegration.init();
        SBJEIIntegration.init();
        ModTileEntities.register();
        MinecraftForge.TERRAIN_GEN_BUS.register(MapGenLushCaverns.class);
        WorldGenRuins.registerStructures();
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new WorldGenRuins(), 4);
        GameRegistry.registerWorldGenerator(new WorldGenNetherOres(), 5);
        GameRegistry.registerWorldGenerator(new WorldGenNetherFungi(), 6);
        GameRegistry.registerWorldGenerator(new WorldGenGeodes(), 7);
        GameRegistry.registerWorldGenerator(new WorldGenLushCaves(), 8);
        GameRegistry.registerWorldGenerator(new WorldGenAbandonedCamps(), Integer.MAX_VALUE - 1);
        // Runs last, so the ores of other mods are already there to be swapped
        GameRegistry.registerWorldGenerator(new WorldGenDeepslate(), Integer.MAX_VALUE);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
        ModPacketHandler.initPackets();
        proxy.registerParticles();
        // Before the proxy, which puts the new kind of enchantment into the creative tab
        ModEnchantments.registerHoeEnchanting();
        proxy.init();
        ModRecipes.registerSmelting();
        ModBlocks.registerFlammability();
        ModPotions.registerBrewing();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModItems.registerOreDictionary();
        ModdedDeepslateOres.registerRecipes();
        StrippingRecipes.registerDefaults();
        ModEntities.registerSpawns();
        ModEntities.registerWitherPatterns();
        ModEntities.registerEndermanCarriables();
        ModItems.registerDispenserBehaviors();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        CommandRegistry.register(event);
    }
}
