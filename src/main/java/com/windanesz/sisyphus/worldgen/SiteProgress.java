package com.windanesz.sisyphus.worldgen;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * For sites that are built whole once all their chunks are decorated, such as the abandoned camps.
 */
public final class SiteProgress extends WorldSavedData {

    private final Map<String, Long> decorated = new HashMap<>();
    private final Map<String, Boolean> built = new HashMap<>();

    public SiteProgress(String name) {
        super(name);
    }

    public static SiteProgress get(World world, String name) {
        MapStorage storage = world.getPerWorldStorage();
        SiteProgress progress = (SiteProgress) storage.getOrLoadData(SiteProgress.class, name);
        if (progress == null) {
            progress = new SiteProgress(name);
            storage.setData(name, progress);
        }
        return progress;
    }

    /**
     * A chunk decorates from 8 blocks into it to 8 blocks into the next. Returns the lowest chunk x and z and the number of chunks along
     * each.
     */
    public static int[] chunksUnder(int minX, int minZ, int maxX, int maxZ) {
        int chunkX = (minX - 8) >> 4;
        int chunkZ = (minZ - 8) >> 4;
        return new int[]{chunkX, chunkZ, ((maxX - 8) >> 4) - chunkX + 1, ((maxZ - 8) >> 4) - chunkZ + 1};
    }

    /**
     * True if that was the last chunk of the site, which is then forgotten.
     */
    public boolean decorate(String site, int bit, long all) {
        long done = decorated.getOrDefault(site, 0L) | 1L << bit;
        markDirty();
        if (done == all) {
            decorated.remove(site);
            return true;
        }
        decorated.put(site, done);
        return false;
    }

    public boolean isPending(String site) {
        return decorated.containsKey(site);
    }

    public void record(String site, boolean wasBuilt) {
        built.put(site, wasBuilt);
        markDirty();
    }

    /**
     * Null if not recorded: not decorated yet, or decorated before outcomes were kept.
     */
    public Boolean outcome(String site) {
        return built.get(site);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        decorated.clear();
        NBTTagCompound sites = nbt.getCompoundTag("Decorated");
        for (String key : sites.getKeySet()) {
            decorated.put(key, sites.getLong(key));
        }
        built.clear();
        NBTTagCompound outcomes = nbt.getCompoundTag("Built");
        for (String key : outcomes.getKeySet()) {
            built.put(key, outcomes.getBoolean(key));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound sites = new NBTTagCompound();
        decorated.forEach(sites::setLong);
        nbt.setTag("Decorated", sites);
        NBTTagCompound outcomes = new NBTTagCompound();
        built.forEach(outcomes::setBoolean);
        nbt.setTag("Built", outcomes);
        return nbt;
    }
}
