package com.windanesz.sisyphus.worldgen.ruins;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class MapGenRuinedPortals extends MapGenStructure {

    public static final String NAME = "SBRuinedPortal";
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(SisyphusBackport.MODID, "chests/ruined_portal");
    public static final Setup[] STANDARD = {new Setup(RuinedPortalPiece.Placement.UNDERGROUND, 1.0F, 0.2F, false, false, true, false, 0.5F),
            new Setup(RuinedPortalPiece.Placement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)};
    public static final Setup[] DESERT = {new Setup(RuinedPortalPiece.Placement.PARTLY_BURIED, 0.0F, 0.0F, false, false, false, false, 1.0F)};
    public static final Setup[] JUNGLE = {new Setup(RuinedPortalPiece.Placement.ON_LAND_SURFACE, 0.5F, 0.8F, true, true, false, false, 1.0F)};
    public static final Setup[] SWAMP = {new Setup(RuinedPortalPiece.Placement.ON_OCEAN_FLOOR, 0.0F, 0.5F, false, true, false, false, 1.0F)};
    public static final Setup[] MOUNTAIN = {new Setup(RuinedPortalPiece.Placement.IN_MOUNTAIN, 1.0F, 0.2F, false, false, true, false, 0.5F),
            new Setup(RuinedPortalPiece.Placement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)};
    public static final Setup[] OCEAN = {new Setup(RuinedPortalPiece.Placement.ON_OCEAN_FLOOR, 0.0F, 0.8F, false, false, true, false, 1.0F)};
    public static final Setup[] NETHER = {new Setup(RuinedPortalPiece.Placement.IN_NETHER, 0.5F, 0.0F, false, false, false, true, 1.0F)};
    private static final int SALT = 34222645;
    private static final float GIANT = 0.05F;
    private static final String[] PORTALS = names("portal_", 10);
    private static final String[] GIANT_PORTALS = names("giant_portal_", 3);

    private static String[] names(String prefix, int count) {
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = "ruined_portal/" + prefix + (i + 1);
        }
        return names;
    }

    /**
     * For tests.
     */
    public static String[] templates() {
        String[] all = new String[PORTALS.length + GIANT_PORTALS.length];
        System.arraycopy(PORTALS, 0, all, 0, PORTALS.length);
        System.arraycopy(GIANT_PORTALS, 0, all, PORTALS.length, GIANT_PORTALS.length);
        return all;
    }

    /**
     * Hill biomes count as the biomes around them. Extreme hills, badlands, savanna plateaus and the stone beach count as mountains.
     */
    @Nullable
    public static Setup[] setupsFor(Biome biome) {
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)) {
            return NETHER;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.END) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.VOID)) {
            return null;
        }
        if (MapGenOceanRuins.isOcean(biome)) {
            return OCEAN;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {
            return SWAMP;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) {
            return JUNGLE;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.MESA)
                || biome == Biomes.SAVANNA_PLATEAU || biome == Biomes.MUTATED_SAVANNA_ROCK || biome == Biomes.STONE_BEACH) {
            return MOUNTAIN;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY) && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)) {
            return DESERT;
        }
        return STANDARD;
    }

    private static Biome biomeAt(World world, int chunkX, int chunkZ) {
        return world.getBiomeProvider().getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
    }

    private static int spacing() {
        return Settings.worldGeneration.ruinedPortals.spacing;
    }

    private static int separation() {
        return Math.min(Settings.worldGeneration.ruinedPortals.separation, spacing() - 1);
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!event.getName().equals(LOOT_TABLE)) {
            return;
        }
        LootPool pool = event.getTable().getPool("main");
        Item bell = Item.getItemFromBlock(ModBlocks.get("bell"));
        if (pool != null && bell != Item.getItemFromBlock(Blocks.AIR)) {
            pool.addEntry(new LootEntryItem(bell, 1, 0, new LootFunction[0], new LootCondition[0], "bell"));
        }
    }

    @Override
    public String getStructureName() {
        return NAME;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int[] cell = MapGenTrailRuins.cell(this.world, chunkX, chunkZ, spacing(), separation(), SALT);
        if (chunkX != cell[0] || chunkZ != cell[1]) {
            return false;
        }
        return setupsFor(biomeAt(this.world, chunkX, chunkZ)) != null;
    }

    @Override
    public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored) {
        this.world = world;
        return findNearestStructurePosBySpacing(world, this, pos, spacing(), separation(), SALT, false, 100, findUnexplored);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        Setup[] setups = setupsFor(biomeAt(this.world, chunkX, chunkZ));
        return new Start(this.rand, chunkX, chunkZ, setups == null ? STANDARD : setups);
    }

    public static final class Setup {
        public final RuinedPortalPiece.Placement placement;
        public final float airPocket;
        public final float mossiness;
        public final boolean overgrown;
        public final boolean vines;
        public final boolean canBeCold;
        public final boolean blackstone;
        public final float weight;

        Setup(RuinedPortalPiece.Placement placement, float airPocket, float mossiness, boolean overgrown, boolean vines, boolean canBeCold, boolean blackstone,
              float weight) {
            this.placement = placement;
            this.airPocket = airPocket;
            this.mossiness = mossiness;
            this.overgrown = overgrown;
            this.vines = vines;
            this.canBeCold = canBeCold;
            this.blackstone = blackstone;
            this.weight = weight;
        }
    }

    public static class Start extends StructureStart {

        public Start() {
        }

        public Start(Random random, int chunkX, int chunkZ, Setup[] setups) {
            super(chunkX, chunkZ);
            Setup setup = pick(random, setups);
            boolean airPocket = sample(random, setup.airPocket);
            String template = random.nextFloat() < GIANT ? GIANT_PORTALS[random.nextInt(GIANT_PORTALS.length)] : PORTALS[random.nextInt(PORTALS.length)];
            Rotation rotation = Rotation.values()[random.nextInt(4)];
            Mirror mirror = random.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
            this.components.add(new RuinedPortalPiece(template, new BlockPos(chunkX * 16, 64, chunkZ * 16), rotation, mirror, setup, airPocket, random.nextLong()));
            this.updateBoundingBox();
        }

        private static Setup pick(Random random, Setup[] setups) {
            if (setups.length == 1) {
                return setups[0];
            }
            float total = 0.0F;
            for (Setup setup : setups) {
                total += setup.weight;
            }
            float roll = random.nextFloat();
            for (Setup setup : setups) {
                roll -= setup.weight / total;
                if (roll < 0.0F) {
                    return setup;
                }
            }
            return setups[setups.length - 1];
        }

        private static boolean sample(Random random, float chance) {
            return chance != 0.0F && (chance == 1.0F || random.nextFloat() < chance);
        }

        @Override
        public void generateStructure(World world, Random random, StructureBoundingBox box) {
            for (StructureComponent component : this.components) {
                if (component instanceof RuinedPortalPiece) {
                    ((RuinedPortalPiece) component).buildIfReady(world, box);
                }
            }
            this.updateBoundingBox();
            super.generateStructure(world, random, box);
        }
    }
}
