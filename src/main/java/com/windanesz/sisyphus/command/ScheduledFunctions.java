package com.windanesz.sisyphus.command;

import net.minecraft.command.CommandSenderWrapper;
import net.minecraft.command.FunctionObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Saved with the overworld, so they survive a restart.
 */
public class ScheduledFunctions extends WorldSavedData {

    private static final String NAME = "sisyphus_scheduled_functions";
    private final List<Entry> entries = new ArrayList<>();

    public ScheduledFunctions() {
        super(NAME);
    }

    public ScheduledFunctions(String name) {
        super(name);
    }

    @Nullable
    public static ScheduledFunctions get(MinecraftServer server) {
        World world = server.getWorld(0);
        if (world == null) {
            return null;
        }
        MapStorage storage = world.getMapStorage();
        if (storage == null) {
            return null;
        }
        ScheduledFunctions data = (ScheduledFunctions) storage.getOrLoadData(ScheduledFunctions.class, NAME);
        if (data == null) {
            data = new ScheduledFunctions();
            storage.setData(NAME, data);
        }
        return data;
    }

    public void schedule(ResourceLocation function, long time, boolean replace) {
        if (replace) {
            clear(function);
        }
        entries.add(new Entry(function, time));
        markDirty();
    }

    public int clear(ResourceLocation function) {
        int removed = 0;
        for (Iterator<Entry> it = entries.iterator(); it.hasNext(); ) {
            if (it.next().function.equals(function)) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            markDirty();
        }
        return removed;
    }

    public void runDue(MinecraftServer server, long now) {
        if (entries.isEmpty()) {
            return;
        }
        List<Entry> due = new ArrayList<>();
        for (Iterator<Entry> it = entries.iterator(); it.hasNext(); ) {
            Entry entry = it.next();
            if (entry.time <= now) {
                due.add(entry);
                it.remove();
            }
        }
        if (due.isEmpty()) {
            return;
        }
        markDirty();
        for (Entry entry : due) {
            FunctionObject function = server.getFunctionManager().getFunction(entry.function);
            if (function != null) {
                server.getFunctionManager().execute(function, CommandSenderWrapper.create(server).computePositionVector().withPermissionLevel(2).withSendCommandFeedback(false));
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        entries.clear();
        NBTTagList list = nbt.getTagList("Scheduled", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            entries.add(new Entry(new ResourceLocation(tag.getString("Function")), tag.getLong("Time")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Entry entry : entries) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Function", entry.function.toString());
            tag.setLong("Time", entry.time);
            list.appendTag(tag);
        }
        compound.setTag("Scheduled", list);
        return compound;
    }

    private static class Entry {
        final ResourceLocation function;
        final long time;

        Entry(ResourceLocation function, long time) {
            this.function = function;
            this.time = time;
        }
    }
}
