package com.windanesz.sisyphus.registry;

import com.google.common.base.Predicate;
import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.item.EntityBambooChestRaft;
import com.windanesz.sisyphus.entity.item.EntityBambooRaft;
import com.windanesz.sisyphus.entity.living.*;
import com.windanesz.sisyphus.entity.projectile.EntityPiercingArrow;
import com.windanesz.sisyphus.entity.projectile.EntityThrowingDisc;
import com.windanesz.sisyphus.worldgen.BiomeBambooJungle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMaterialMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;

@Mod.EventBusSubscriber
public final class ModEntities {

    public static final String[] ENDERMAN_CARRIED = {"crimson_nylium", "warped_nylium", "crimson_fungus", "warped_fungus", "crimson_roots", "warped_roots"};
    private static int networkId = 0;
    private static EnumCreatureType lavaCreature;

    private ModEntities() {
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<EntityEntry> event) {

        IForgeRegistry<EntityEntry> registry = event.getRegistry();

        registerOthers(registry);

        if (!Settings.mobs.enableFish) {
            return;
        }

        registry.register(mob(EntityCod.class, "cod").egg(12691306, 15058059).build());
        registry.register(mob(EntitySalmon.class, "salmon").egg(10489616, 951412).build());
        registry.register(mob(EntityPufferfish.class, "pufferfish").egg(16167425, 3654642).build());
        registry.register(mob(EntityTropicalFish.class, "tropical_fish").egg(15690005, 16775663).build());
    }

    private static void registerOthers(IForgeRegistry<EntityEntry> registry) {
        if (Settings.mobs.enableCats) {
            registry.register(mob(EntityCat.class, "cat").egg(15714446, 9794134).build());
        }
        if (Settings.mobs.enableFoxes) {
            registry.register(mob(EntityFox.class, "fox").egg(14005919, 13396256).build());
        }
        if (Settings.mobs.enablePandas) {
            registry.register(mob(EntityPanda.class, "panda").egg(15198183, 1776418).build());
        }
        if (Settings.mobs.enableGoats) {
            registry.register(mob(EntityGoat.class, "goat").egg(10851452, 5589310).build());
        }
        if (Settings.mobs.enableAxolotls) {
            registry.register(mob(EntityAxolotl.class, "axolotl").egg(16499171, 10890612).build());
        }
        if (Settings.mobs.enableBrownMooshroom) {
            registry.register(mob(EntityBrownMooshroom.class, "brown_mooshroom").build());
        }
        if (Settings.mobs.enableWanderingTrader) {
            registry.register(mob(EntityWanderingTrader.class, "wandering_trader").egg(5651507, 12422002).build());
            registry.register(mob(EntityTraderLlama.class, "trader_llama").egg(15377456, 4547222).build());
        }
        if (Settings.mobs.enablePiglins) {
            registry.register(mob(EntityPiglin.class, "piglin").egg(10051392, 16380836).build());
        }
        if (Settings.mobs.enableStriders) {
            registry.register(mob(EntityStrider.class, "strider").egg(10236982, 5065037).build());
        }
        if (Settings.mobs.enableHoglins) {
            registry.register(mob(EntityHoglin.class, "hoglin").egg(13004373, 6251620).build());
            registry.register(mob(EntityZoglin.class, "zoglin").egg(13004373, 44975).build());
        }
        if (Settings.items.crossbow.enabled) {
            registry.register(projectile(EntityPiercingArrow.class, "piercing_arrow").build());
        }
        if (Settings.items.throwingDiscs.enabled) {
            registry.register(projectile(EntityThrowingDisc.class, "throwing_disc").build());
        }
        if (Settings.isContentEnabled("bamboo_wood")) {
            // Tracked as the boats of the game are
            registry.register(entry(EntityBambooRaft.class, "bamboo_raft").tracker(80, 3, true).build());
            registry.register(entry(EntityBambooChestRaft.class, "bamboo_chest_raft").tracker(80, 3, true).build());
        }
    }

    private static <T extends Entity> EntityEntryBuilder<T> mob(Class<T> type, String name) {
        return entry(type, name).tracker(80, 3, true);
    }

    private static <T extends Entity> EntityEntryBuilder<T> projectile(Class<T> type, String name) {
        return entry(type, name).tracker(64, 1, true);
    }

    private static <T extends Entity> EntityEntryBuilder<T> entry(Class<T> type, String name) {
        ResourceLocation key = new ResourceLocation(SisyphusBackport.MODID, name);
        return EntityEntryBuilder.<T>create().entity(type).id(key, networkId++).name(key.toString());
    }

    /**
     * Must run before anything calls Biome.getSpawnableList, whose switch caches the number of creature types on first use. Its own type
     * rather than CREATURE, as animals only spawn every 400 ticks.
     */
    public static void addCreatureTypes() {
        lavaCreature = EnumHelper.addCreatureType("SISYPHUS_LAVA_CREATURE", EntityStrider.class, 10, Material.LAVA, true, false);
    }

    public static void registerSpawns() {
        registerFishSpawns();
        if (Settings.mobs.enableAxolotls) {
            // Without this, 1.12 checks the place as one on the ground, and no water is ever good enough
            EntitySpawnPlacementRegistry.setPlacementType(EntityAxolotl.class, EntityLiving.SpawnPlacementType.IN_WATER);
        }
        if (Settings.mobs.enableStriders) {
            EntitySpawnPlacementRegistry.setPlacementType(EntityStrider.class, EnumHelper.addSpawnPlacementType("SISYPHUS_IN_LAVA",
                    (world, pos) -> world.getBlockState(pos).getMaterial() == Material.LAVA && world.isAirBlock(pos.up())));
        }
        for (Biome biome : ForgeRegistries.BIOMES) {
            boolean otherworld = BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.VOID);
            if (Settings.mobs.enableAxolotls && !otherworld) {
                // In the water of the caves under any biome, see EntityAxolotl.getCanSpawnHere
                EntityRegistry.addSpawn(EntityAxolotl.class, 10, 4, 6, EnumCreatureType.WATER_CREATURE, biome);
            }
            boolean taiga = BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS) && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN);

            if (Settings.mobs.enableFoxes && taiga) {
                EntityRegistry.addSpawn(EntityFox.class, 8, 2, 4, EnumCreatureType.CREATURE, biome);
            }
            if (Settings.mobs.enableGoats && BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN)) {
                EntityRegistry.addSpawn(EntityGoat.class, 5, 1, 3, EnumCreatureType.CREATURE, biome);
            }
            if (Settings.mobs.enablePandas) {
                if (biome instanceof BiomeBambooJungle) {
                    EntityRegistry.addSpawn(EntityPanda.class, 80, 1, 2, EnumCreatureType.CREATURE, biome);
                } else if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE) && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPARSE)) {
                    // Far rarer in the other jungles
                    EntityRegistry.addSpawn(EntityPanda.class, 1, 1, 2, EnumCreatureType.CREATURE, biome);
                }
            }
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)) {
                if (Settings.mobs.enablePiglins) {
                    EntityRegistry.addSpawn(EntityPiglin.class, 15, 2, 4, EnumCreatureType.MONSTER, biome);
                }
                if (Settings.mobs.enableHoglins) {
                    EntityRegistry.addSpawn(EntityHoglin.class, 9, 3, 4, EnumCreatureType.MONSTER, biome);
                }
                if (Settings.mobs.enableStriders) {
                    EntityRegistry.addSpawn(EntityStrider.class, 60, 1, 2, lavaCreature, biome);
                }
            }
        }
    }

    private static void registerFishSpawns() {
        if (!Settings.mobs.enableFish) {
            return;
        }

        // 1.12 checks the place of any mob it does not know as one on the ground, where water is never good enough, so without
        // this no fish ever spawned on its own
        for (Class<? extends EntityLiving> fish : Arrays.asList(EntityCod.class, EntitySalmon.class, EntityPufferfish.class, EntityTropicalFish.class)) {
            EntitySpawnPlacementRegistry.setPlacementType(fish, EntityLiving.SpawnPlacementType.IN_WATER);
        }

        for (Biome biome : ForgeRegistries.BIOMES) {
            float temperature = biome.getDefaultTemperature();
            boolean ocean = BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN);
            boolean river = BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER);

            if (ocean) {
                if (temperature < 0.9F) {
                    EntityRegistry.addSpawn(EntityCod.class, temperature < 0.3F ? 15 : 10, 3, 6, EnumCreatureType.WATER_CREATURE, biome);
                }
                if (temperature < 0.3F) {
                    EntityRegistry.addSpawn(EntitySalmon.class, 15, 1, 5, EnumCreatureType.WATER_CREATURE, biome);
                }
                if (temperature >= 0.5F) {
                    EntityRegistry.addSpawn(EntityPufferfish.class, temperature >= 0.9F ? 15 : 5, 1, 3, EnumCreatureType.WATER_CREATURE, biome);
                    EntityRegistry.addSpawn(EntityTropicalFish.class, 25, 8, 8, EnumCreatureType.WATER_CREATURE, biome);
                }
            } else if (river) {
                EntityRegistry.addSpawn(EntitySalmon.class, 5, 1, 5, EnumCreatureType.WATER_CREATURE, biome);
            }
        }
    }

    public static boolean isWitherBase(IBlockState state) {
        return state != null && (state.getBlock() == Blocks.SOUL_SAND || ModBlocks.isSoulSoil(state.getBlock()));
    }

    private static boolean isWitherSkull(BlockWorldState state) {
        return state.getBlockState() != null && state.getBlockState().getBlock() == Blocks.SKULL && state.getTileEntity() instanceof TileEntitySkull
                && ((TileEntitySkull) state.getTileEntity()).getSkullType() == 1;
    }

    public static BlockPattern[] witherPatterns() {
        Predicate<IBlockState> base = ModEntities::isWitherBase;
        Predicate<BlockWorldState> skull = ModEntities::isWitherSkull;
        BlockPattern body = FactoryBlockPattern.start().aisle("   ", "###", "~#~").where('#', BlockWorldState.hasState(base))
                .where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
        BlockPattern full = FactoryBlockPattern.start().aisle("^^^", "###", "~#~").where('#', BlockWorldState.hasState(base)).where('^', skull)
                .where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
        return new BlockPattern[]{body, full};
    }

    /**
     * The skull block builds its patterns the first time it needs them, so they are set before that.
     */
    public static void registerWitherPatterns() {
        if (!Settings.gameplay.enableSoulSoilWither || ModBlocks.get("soul_soil") == Blocks.AIR) {
            return;
        }
        try {
            BlockPattern[] patterns = witherPatterns();
            ReflectionHelper.setPrivateValue(BlockSkull.class, (BlockSkull) Blocks.SKULL, patterns[0], "witherBasePattern", "field_176420_N");
            ReflectionHelper.setPrivateValue(BlockSkull.class, (BlockSkull) Blocks.SKULL, patterns[1], "witherPattern", "field_176421_O");
        } catch (RuntimeException e) {
            SisyphusBackport.logger.warn("Could not make the wither buildable on soul soil", e);
        }
    }

    public static void registerEndermanCarriables() {
        if (!Settings.gameplay.enableEndermenNetherPlants) {
            return;
        }
        for (String name : ENDERMAN_CARRIED) {
            Block block = ModBlocks.getOrNull(name);
            if (block != null) {
                EntityEnderman.setCarriable(block, true);
            }
        }
        EntityEnderman.setCarriable(Blocks.NETHERRACK, false);
    }
}
