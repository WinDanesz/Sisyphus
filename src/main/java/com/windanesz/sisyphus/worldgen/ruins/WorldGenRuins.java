package com.windanesz.sisyphus.worldgen.ruins;

import com.windanesz.sisyphus.Settings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.fml.common.IWorldGenerator;

import javax.annotation.Nullable;
import java.util.*;

public class WorldGenRuins implements IWorldGenerator {

    public static final String LOCATE_TRAIL_RUINS = "TrailRuins";
    public static final String LOCATE_OCEAN_RUINS = "OceanRuins";
    public static final String LOCATE_RUINED_PORTALS = "RuinedPortal";

    private static final Map<World, MapGenStructure[]> GENERATORS = new WeakHashMap<>();

    public static void registerStructures() {
        MapGenStructureIO.registerStructure(MapGenTrailRuins.Start.class, MapGenTrailRuins.NAME);
        MapGenStructureIO.registerStructure(MapGenOceanRuins.Start.class, MapGenOceanRuins.NAME);
        MapGenStructureIO.registerStructure(MapGenRuinedPortals.Start.class, MapGenRuinedPortals.NAME);
        MapGenStructureIO.registerStructureComponent(RuinPiece.class, "SBRuin");
        MapGenStructureIO.registerStructureComponent(RuinedPortalPiece.class, "SBRuinedPortalPiece");
    }

    /**
     * The structure is found where it is planned, so its chunks need not be there yet.
     */
    @Nullable
    public static BlockPos locate(World world, String name, BlockPos pos, boolean findUnexplored) {
        int index;
        if (LOCATE_TRAIL_RUINS.equalsIgnoreCase(name)) {
            if (!trailRuinsIn(world)) {
                return null;
            }
            index = 0;
        } else if (LOCATE_OCEAN_RUINS.equalsIgnoreCase(name)) {
            if (!oceanRuinsIn(world)) {
                return null;
            }
            index = 1;
        } else if (LOCATE_RUINED_PORTALS.equalsIgnoreCase(name)) {
            if (!ruinedPortalsIn(world)) {
                return null;
            }
            index = 2;
        } else {
            return null;
        }
        return generators(world)[index].getNearestStructurePos(world, pos, findUnexplored);
    }

    public static List<String> locateNames() {
        boolean archaeology = Settings.archaeology.enableArchaeology;
        List<String> names = new ArrayList<>();
        if (archaeology && Settings.worldGeneration.trailRuins.enabled) {
            names.add(LOCATE_TRAIL_RUINS);
        }
        if (archaeology && Settings.worldGeneration.oceanRuins.enabled) {
            names.add(LOCATE_OCEAN_RUINS);
        }
        if (Settings.worldGeneration.ruinedPortals.enabled) {
            names.add(LOCATE_RUINED_PORTALS);
        }
        return names;
    }

    private static boolean trailRuinsIn(World world) {
        Settings.WorldGeneration settings = Settings.worldGeneration;
        return settings.trailRuins.enabled && Settings.allowsDimension(settings.trailRuins.dimensions, world) && ruinsIn(world);
    }

    private static boolean oceanRuinsIn(World world) {
        Settings.WorldGeneration settings = Settings.worldGeneration;
        return settings.oceanRuins.enabled && Settings.allowsDimension(settings.oceanRuins.dimensions, world) && ruinsIn(world);
    }

    private static boolean ruinedPortalsIn(World world) {
        Settings.RuinedPortals settings = Settings.worldGeneration.ruinedPortals;
        return settings.enabled && Settings.allowsDimension(settings.dimensions, world) && structuresIn(world);
    }

    private static boolean ruinsIn(World world) {
        return Settings.archaeology.enableArchaeology && structuresIn(world);
    }

    private static boolean structuresIn(World world) {
        return world.getWorldInfo().isMapFeaturesEnabled() && world.getWorldType() != WorldType.FLAT;
    }

    private static MapGenStructure[] generators(World world) {
        synchronized (GENERATORS) {
            return GENERATORS.computeIfAbsent(world, w -> new MapGenStructure[]{new MapGenTrailRuins(), new MapGenOceanRuins(), new MapGenRuinedPortals()});
        }
    }

    private static void build(MapGenStructure structure, World world, Random random, ChunkPos chunk) {
        // Plans what starts within eight chunks, and then builds what of it is in the chunk that is decorated
        structure.generate(world, chunk.x, chunk.z, null);
        structure.generateStructure(world, random, chunk);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        boolean trail = trailRuinsIn(world);
        boolean ocean = oceanRuinsIn(world);
        boolean portals = ruinedPortalsIn(world);
        if (!trail && !ocean && !portals) {
            return;
        }
        MapGenStructure[] structures = generators(world);
        ChunkPos chunk = new ChunkPos(chunkX, chunkZ);
        if (trail) {
            build(structures[0], world, random, chunk);
        }
        if (ocean) {
            build(structures[1], world, random, chunk);
        }
        if (portals) {
            build(structures[2], world, random, chunk);
        }
    }
}
