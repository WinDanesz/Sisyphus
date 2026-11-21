package com.windanesz.sisyphus.worldgen.ruins;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converted by {@code tools/structures/convert_structures.py} and read from {@code assets/sisyphus/structures}.
 */
public final class RuinTemplate {

    private static final Map<String, RuinTemplate> CACHE = new ConcurrentHashMap<>();
    private final String name;
    private final BlockPos size;
    private final NBTTagList paletteTags;
    private final List<BlockInfo> blocks;
    private final List<Jigsaw> jigsaws;
    private final List<Marker> markers;
    private volatile IBlockState[] palette;
    private RuinTemplate(String name, NBTTagCompound root) {
        this.name = name;
        this.size = readPos(root.getTagList("size", 3));
        this.paletteTags = root.getTagList("palette", 10);
        List<BlockInfo> blocks = new ArrayList<>();
        NBTTagList blockTags = root.getTagList("blocks", 10);
        for (int i = 0; i < blockTags.tagCount(); i++) {
            NBTTagCompound tag = blockTags.getCompoundTagAt(i);
            blocks.add(new BlockInfo(readPos(tag.getTagList("pos", 3)), tag.getInteger("state")));
        }
        this.blocks = Collections.unmodifiableList(blocks);
        List<Jigsaw> jigsaws = new ArrayList<>();
        NBTTagList jigsawTags = root.getTagList("jigsaws", 10);
        for (int i = 0; i < jigsawTags.tagCount(); i++) {
            NBTTagCompound tag = jigsawTags.getCompoundTagAt(i);
            jigsaws.add(new Jigsaw(readPos(tag.getTagList("pos", 3)), EnumFacing.byName(tag.getString("front")), EnumFacing.byName(tag.getString("top")),
                    tag.getString("name"), tag.getString("target"), tag.getString("pool"), "rollable".equals(tag.getString("joint"))));
        }
        this.jigsaws = Collections.unmodifiableList(jigsaws);
        List<Marker> markers = new ArrayList<>();
        NBTTagList markerTags = root.getTagList("markers", 10);
        for (int i = 0; i < markerTags.tagCount(); i++) {
            NBTTagCompound tag = markerTags.getCompoundTagAt(i);
            markers.add(new Marker(readPos(tag.getTagList("pos", 3)), tag.getString("metadata")));
        }
        this.markers = Collections.unmodifiableList(markers);
    }

    private static BlockPos readPos(NBTTagList list) {
        return new BlockPos(list.getIntAt(0), list.getIntAt(1), list.getIntAt(2));
    }

    /**
     * Cached after the first read.
     */
    public static RuinTemplate get(String path) {
        return CACHE.computeIfAbsent(path, RuinTemplate::load);
    }

    private static RuinTemplate load(String path) {
        String resource = "/assets/" + SisyphusBackport.MODID + "/structures/" + path + ".nbt";
        try (InputStream input = RuinTemplate.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("No structure template " + resource);
            }
            return new RuinTemplate(path, CompressedStreamTools.readCompressed(input));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the structure template " + resource, e);
        }
    }

    static IBlockState resolve(NBTTagCompound tag) {
        ResourceLocation name = new ResourceLocation(tag.getString("Name"));
        Block block = Block.REGISTRY.getObject(name);
        if (block == Blocks.AIR && !name.equals(Blocks.AIR.getRegistryName())) {
            return tag.hasKey("Fallback", 10) ? resolve(tag.getCompoundTag("Fallback")) : Blocks.AIR.getDefaultState();
        }
        return NBTUtil.readBlockState(tag);
    }

    public static BlockPos transform(BlockPos pos, Rotation rotation) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch (rotation) {
            case CLOCKWISE_90:
                return new BlockPos(-z, y, x);
            case COUNTERCLOCKWISE_90:
                return new BlockPos(z, y, -x);
            case CLOCKWISE_180:
                return new BlockPos(-x, y, -z);
            default:
                return pos;
        }
    }

    public static EnumFacing transform(EnumFacing facing, Rotation rotation) {
        return facing.getAxis() == EnumFacing.Axis.Y ? facing : rotation.rotate(facing);
    }

    /**
     * Some blocks of this mod do not turn themselves, so a horizontal facing they left alone is turned here.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static IBlockState rotate(IBlockState state, Rotation rotation) {
        if (rotation == Rotation.NONE) {
            return state;
        }
        IBlockState turned = state.withRotation(rotation);
        if (turned == state) {
            for (IProperty<?> property : state.getPropertyKeys()) {
                if (property instanceof PropertyDirection && property.getName().equals("facing")) {
                    EnumFacing facing = (EnumFacing) state.getValue(property);
                    EnumFacing rotated = transform(facing, rotation);
                    if (((PropertyDirection) property).getAllowedValues().contains(rotated)) {
                        return state.withProperty((PropertyDirection) property, rotated);
                    }
                }
            }
        }
        return turned;
    }

    public String getName() {
        return this.name;
    }

    public BlockPos getSize() {
        return this.size;
    }

    public List<BlockInfo> getBlocks() {
        return this.blocks;
    }

    public List<Jigsaw> getJigsaws() {
        return this.jigsaws;
    }

    public List<Marker> getMarkers() {
        return this.markers;
    }

    public IBlockState getState(int index) {
        IBlockState[] states = this.palette;
        if (states == null) {
            states = new IBlockState[this.paletteTags.tagCount()];
            for (int i = 0; i < states.length; i++) {
                states[i] = resolve(this.paletteTags.getCompoundTagAt(i));
            }
            this.palette = states;
        }
        return states[index];
    }

    // Turning

    public StructureBoundingBox getBoundingBox(BlockPos origin, Rotation rotation) {
        BlockPos far = transform(new BlockPos(this.size.getX() - 1, this.size.getY() - 1, this.size.getZ() - 1), rotation);
        BlockPos a = origin;
        BlockPos b = origin.add(far);
        return new StructureBoundingBox(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
                Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    public static final class BlockInfo {
        public final BlockPos pos;
        public final int state;

        BlockInfo(BlockPos pos, int state) {
            this.pos = pos;
            this.state = state;
        }
    }

    public static final class Jigsaw {
        public final BlockPos pos;
        public final EnumFacing front;
        public final EnumFacing top;
        public final String name;
        public final String target;
        public final String pool;
        public final boolean rollable;

        Jigsaw(BlockPos pos, EnumFacing front, EnumFacing top, String name, String target, String pool, boolean rollable) {
            this.pos = pos;
            this.front = front;
            this.top = top;
            this.name = name;
            this.target = target;
            this.pool = pool;
            this.rollable = rollable;
        }
    }

    public static final class Marker {
        public final BlockPos pos;
        public final String metadata;

        Marker(BlockPos pos, String metadata) {
            this.pos = pos;
            this.metadata = metadata;
        }
    }
}
