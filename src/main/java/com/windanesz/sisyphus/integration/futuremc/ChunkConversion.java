package com.windanesz.sisyphus.integration.futuremc;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

final class ChunkConversion {

    static final Decoder NO_PROPERTIES = (meta, properties) -> {
    };
    static final IBlockState[] NONE = new IBlockState[0];

    private ChunkConversion() {
    }

    static void translateBlocks(NBTTagCompound level, IntFunction<IBlockState[]> translations, @Nullable PositionedState positioned) {
        NBTTagList sections = level.getTagList("Sections", 10);
        int chunkX = level.getInteger("xPos") << 4;
        int chunkZ = level.getInteger("zPos") << 4;
        Map<Integer, IBlockState[]> cache = new HashMap<>();
        for (int s = 0; s < sections.tagCount(); s++) {
            NBTTagCompound section = sections.getCompoundTagAt(s);
            byte[] blocks = section.getByteArray("Blocks");
            byte[] metadata = section.getByteArray("Data");
            if (blocks.length != 4096 || metadata.length != 2048) {
                continue;
            }
            int sectionY = section.getByte("Y") << 4;
            NibbleArray data = new NibbleArray(metadata);
            NibbleArray add = section.hasKey("Add", 7) ? new NibbleArray(section.getByteArray("Add")) : null;
            boolean changed = false;
            for (int i = 0; i < 4096; i++) {
                int id = (add == null ? 0 : add.getFromIndex(i) << 8) | (blocks[i] & 255);
                IBlockState[] states = cache.computeIfAbsent(id, translations::apply);
                if (states == NONE) {
                    continue;
                }
                IBlockState state = states[data.getFromIndex(i)];
                if (positioned != null) {
                    // The index is y, z, x, four bits each
                    state = positioned.at(id, new BlockPos(chunkX + (i & 15), sectionY + (i >> 8), chunkZ + ((i >> 4) & 15)), state);
                }
                int newId = Block.getIdFromBlock(state.getBlock());
                blocks[i] = (byte) (newId & 255);
                if (newId > 255 && add == null) {
                    add = new NibbleArray();
                }
                if (add != null) {
                    add.setIndex(i, (newId >> 8) & 15);
                }
                data.setIndex(i, state.getBlock().getMetaFromState(state));
                changed = true;
            }
            if (changed) {
                section.setByteArray("Blocks", blocks);
                section.setByteArray("Data", data.getData());
                if (add != null) {
                    section.setByteArray("Add", add.getData());
                }
            }
        }
    }

    /**
     * Without a decoder both blocks share a vanilla class, so the metadata is kept.
     */
    @SuppressWarnings("deprecation")
    static IBlockState state(Block target, @Nullable Decoder decoder, int meta) {
        if (decoder == null) {
            try {
                return target.getStateFromMeta(meta);
            } catch (RuntimeException e) {
                return target.getDefaultState();
            }
        }
        Map<String, String> properties = new HashMap<>();
        decoder.decode(meta, properties);
        IBlockState state = target.getDefaultState();
        for (IProperty<?> property : state.getPropertyKeys()) {
            String value = properties.get(property.getName());
            if (value != null) {
                state = withValue(state, property, value);
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> IBlockState withValue(IBlockState state, IProperty<T> property, String value) {
        Optional<T> parsed = property.parseValue(value);
        return parsed.isPresent() ? state.withProperty(property, parsed.get()) : state;
    }

    /**
     * Forge only reports missing blocks on the first load. After that they stay in the registry as air, under their names.
     */
    static boolean worldHasBlocksOf(String modid) {
        for (ResourceLocation name : ForgeRegistries.BLOCKS.getKeys()) {
            if (modid.equals(name.getNamespace())) {
                return true;
            }
        }
        return false;
    }

    static void logAxis(int meta, Map<String, String> p) {
        p.put("axis", (meta & 12) == 4 ? "x" : (meta & 12) == 8 ? "z" : (meta & 12) == 12 ? "none" : "y");
    }

    interface Decoder {
        void decode(int meta, Map<String, String> properties);
    }

    interface PositionedState {
        IBlockState at(int id, BlockPos pos, IBlockState state);
    }
}
