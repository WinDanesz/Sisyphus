package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class WanderingTraderManager {

    public static final String RULE = "doTraderSpawning";

    private static final int CHECK_INTERVAL = 1200;
    private static final int DAY = 24000;
    private static final int SPAWN_DISTANCE = 48;

    private WanderingTraderManager() {
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (!world.isRemote && world.provider.getDimension() == 0 && Settings.mobs.enableWanderingTrader) {
            GameRules rules = world.getGameRules();
            if (!rules.hasRule(RULE)) {
                rules.addGameRule(RULE, "true", GameRules.ValueType.BOOLEAN_VALUE);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END || world.isRemote || world.provider.getDimension() != 0 || !Settings.mobs.enableWanderingTrader
                || !(world instanceof WorldServer) || world.getTotalWorldTime() % CHECK_INTERVAL != 0 || !world.getGameRules().getBoolean(RULE)) {
            return;
        }
        Data data = Data.get(world);
        data.delay -= CHECK_INTERVAL;
        if (data.delay <= 0) {
            data.delay = DAY;
            int chance = data.chance;
            data.chance = MathHelper.clamp(data.chance + 25, 25, 75);
            if (world.rand.nextInt(100) <= chance && spawn((WorldServer) world)) {
                data.chance = 25;
            }
        }
        data.markDirty();
    }

    public static boolean spawn(WorldServer world) {
        List<EntityPlayer> players = world.playerEntities;
        if (players.isEmpty()) {
            return false;
        }
        EntityPlayer player = players.get(world.rand.nextInt(players.size()));
        BlockPos where = findSpawnPositionNear(world, player.getPosition(), SPAWN_DISTANCE);
        if (where == null) {
            return false;
        }
        EntityWanderingTrader trader = new EntityWanderingTrader(world);
        trader.setLocationAndAngles(where.getX() + 0.5D, where.getY(), where.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        trader.onInitialSpawn(world.getDifficultyForLocation(where), null);
        trader.setDespawnDelay(EntityWanderingTrader.STAY_TICKS);
        trader.setWanderTarget(player.getPosition());
        world.spawnEntity(trader);
        return true;
    }

    public static void spawnLlamas(WorldServer world, EntityWanderingTrader trader) {
        spawnLlama(world, trader, 4);
        spawnLlama(world, trader, 4);
    }

    private static void spawnLlama(WorldServer world, EntityWanderingTrader trader, int radius) {
        BlockPos where = findSpawnPositionNear(world, trader.getPosition(), radius);
        if (where == null) {
            return;
        }
        EntityTraderLlama llama = new EntityTraderLlama(world);
        llama.setLocationAndAngles(where.getX() + 0.5D, where.getY(), where.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        llama.onInitialSpawn(world.getDifficultyForLocation(where), null);
        // leashed after spawning, or clients tracking it would never get the lead
        world.spawnEntity(llama);
        llama.setLeashHolder(trader, true);
    }

    @Nullable
    private static BlockPos findSpawnPositionNear(World world, BlockPos around, int radius) {
        Random rand = world.rand;
        for (int i = 0; i < 10; i++) {
            int x = around.getX() + rand.nextInt(radius * 2) - radius;
            int z = around.getZ() + rand.nextInt(radius * 2) - radius;
            if (!world.isBlockLoaded(new BlockPos(x, 64, z))) {
                continue;
            }
            BlockPos pos = world.getHeight(new BlockPos(x, 0, z));
            if (world.isAirBlock(pos) && world.isAirBlock(pos.up()) && world.getBlockState(pos.down()).isTopSolid()
                    && !world.getBlockState(pos.down()).getMaterial().isLiquid()) {
                return pos;
            }
        }
        return null;
    }

    public static class Data extends WorldSavedData {

        private static final String NAME = "sisyphus_wandering_trader";

        int delay = DAY;
        int chance = 25;

        public Data() {
            super(NAME);
        }

        public Data(String name) {
            super(name);
        }

        static Data get(World world) {
            MapStorage storage = world.getMapStorage();
            Data data = (Data) storage.getOrLoadData(Data.class, NAME);
            if (data == null) {
                data = new Data();
                storage.setData(NAME, data);
            }
            return data;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            this.delay = nbt.hasKey("Delay") ? nbt.getInteger("Delay") : DAY;
            this.chance = nbt.hasKey("Chance") ? nbt.getInteger("Chance") : 25;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger("Delay", this.delay);
            compound.setInteger("Chance", this.chance);
            return compound;
        }
    }
}
