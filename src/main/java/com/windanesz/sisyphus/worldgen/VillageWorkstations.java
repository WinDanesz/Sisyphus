package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.integration.fletchery.SBFletcheryIntegration;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class VillageWorkstations {

    private static final Spot[] NONE = new Spot[0];
    private static final Map<Class<?>, Spot[]> SPOTS = new HashMap<>();

    static {
        // The library: the lectern faces the room from the wall by the door, the cartography table is in the corner behind it
        SPOTS.put(StructureVillagePieces.House1.class, new Spot[]{
                new Spot("lectern", 1, 1, 3, EnumFacing.EAST),
                new Spot("cartography_table", 1, 1, 4, EnumFacing.NORTH)});
        // The blacksmith: along the forge, beside the furnaces, facing the open front
        SPOTS.put(StructureVillagePieces.House2.class, new Spot[]{
                new Spot("blast_furnace", 7, 1, 3, EnumFacing.NORTH),
                new Spot("smithing_table", 8, 1, 3, EnumFacing.NORTH),
                new Spot("grindstone", 9, 1, 3, EnumFacing.NORTH)});
        // The butcher: behind the counter
        SPOTS.put(StructureVillagePieces.Hall.class, new Spot[]{
                new Spot("smoker", 7, 1, 1, EnumFacing.SOUTH)});
        // The big house: the back corners of its wing
        SPOTS.put(StructureVillagePieces.House3.class, new Spot[]{
                new Spot("loom", 7, 1, 9, EnumFacing.WEST),
                new Spot("barrel", 3, 1, 9, EnumFacing.UP)});
        // The small hut: the corner beside the door
        SPOTS.put(StructureVillagePieces.WoodHut.class, new Spot[]{
                new Spot("fletching_table", 2, 1, 1, EnumFacing.NORTH)});
        // The cobblestone hut: the back corner away from the ladder
        SPOTS.put(StructureVillagePieces.House4Garden.class, new Spot[]{
                new Spot("stonecutter", 1, 1, 3, EnumFacing.NORTH)});
        // The fields: on the log at the end of the first row of water
        SPOTS.put(StructureVillagePieces.Field1.class, new Spot[]{
                new Spot("composter", 3, 1, 8, EnumFacing.NORTH)});
        SPOTS.put(StructureVillagePieces.Field2.class, new Spot[]{
                new Spot("composter", 3, 1, 8, EnumFacing.NORTH)});
        // The well, which is the middle of the village and the start of it: under its roof, over the water, in reach from the path
        Spot[] well = {new Spot("bell", 2, 14, 2, EnumFacing.NORTH, true)};
        SPOTS.put(StructureVillagePieces.Well.class, well);
        SPOTS.put(StructureVillagePieces.Start.class, well);
    }

    private VillageWorkstations() {
    }

    public static Spot[] spotsOf(Object piece) {
        if (!Settings.worldGeneration.enableVillageWorkstations) {
            return NONE;
        }
        Spot[] spots = SPOTS.get(piece.getClass());
        return spots == null ? NONE : spots;
    }

    /**
     * Matches how the StructureComponent offsets map the x and z of a piece.
     */
    public static EnumFacing toWorld(EnumFacing local, EnumFacing baseMode) {
        if (local.getAxis() == EnumFacing.Axis.Y) {
            return local;
        }
        // Where the x and the z of the piece go in the world
        EnumFacing plusX;
        EnumFacing plusZ;
        switch (baseMode) {
            case SOUTH:
                plusX = EnumFacing.EAST;
                plusZ = EnumFacing.SOUTH;
                break;
            case WEST:
                plusX = EnumFacing.SOUTH;
                plusZ = EnumFacing.WEST;
                break;
            case EAST:
                plusX = EnumFacing.SOUTH;
                plusZ = EnumFacing.EAST;
                break;
            case NORTH:
            default:
                plusX = EnumFacing.EAST;
                plusZ = EnumFacing.NORTH;
                break;
        }
        switch (local) {
            case EAST:
                return plusX;
            case WEST:
                return plusX.getOpposite();
            case SOUTH:
                return plusZ;
            case NORTH:
            default:
                return plusZ.getOpposite();
        }
    }

    @Nullable
    public static IBlockState stateOf(Spot spot, EnumFacing facing) {
        Block block = ModBlocks.get(spot.block);
        if (block == Blocks.AIR && "fletching_table".equals(spot.block)) {
            block = SBFletcheryIntegration.fletchingTable();
        }
        if (block == Blocks.AIR) {
            return null;
        }
        IBlockState state = block.getDefaultState();
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (property.getName().equals("facing")) {
                state = withValue(state, property, facing.getName());
            } else if (spot.hanging && property.getName().equals("attachment")) {
                state = withValue(state, property, "ceiling");
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> IBlockState withValue(IBlockState state, IProperty<T> property, String name) {
        for (T value : property.getAllowedValues()) {
            if (property.getName(value).equals(name)) {
                return state.withProperty(property, value);
            }
        }
        return state;
    }

    public static final class Spot {
        public final String block;
        public final int x;
        public final int y;
        public final int z;
        public final EnumFacing facing;
        public final boolean hanging;

        Spot(String block, int x, int y, int z, EnumFacing facing) {
            this(block, x, y, z, facing, false);
        }

        Spot(String block, int x, int y, int z, EnumFacing facing, boolean hanging) {
            this.block = block;
            this.x = x;
            this.y = y;
            this.z = z;
            this.facing = facing;
            this.hanging = hanging;
        }
    }
}
