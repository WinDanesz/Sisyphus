package com.windanesz.sisyphus.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityAIHideFromBell extends EntityAIBase {

    public static final int RANGE = 32;
    private static final int HIDE_TICKS = 300;
    private static final int GIVE_UP_TICKS = 1200;
    private static final double SPEED = 0.75D;
    private static final double CROWDING = 6.0D;

    private final EntityCreature creature;

    @Nullable
    private BlockPos hidingPlace;
    private int alarmTicks;
    private boolean arrived;
    private int hiddenTicks;
    private int pathCooldown;

    public EntityAIHideFromBell(EntityCreature creature) {
        this.creature = creature;
        this.setMutexBits(1);
    }

    @Nullable
    public static EntityAIHideFromBell of(EntityLiving entity) {
        for (EntityAITasks.EntityAITaskEntry entry : entity.tasks.taskEntries) {
            if (entry.action instanceof EntityAIHideFromBell) {
                return (EntityAIHideFromBell) entry.action;
            }
        }
        return null;
    }

    /**
     * Same priority as going indoors at night.
     */
    public static void addTo(EntityVillager villager) {
        if (of(villager) == null) {
            villager.tasks.addTask(2, new EntityAIHideFromBell(villager));
        }
    }

    public static void alarm(World world, BlockPos bell) {
        List<EntityVillager> villagers = world.getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(bell).grow(RANGE),
                villager -> villager != null && villager.isEntityAlive() && villager.getDistanceSqToCenter(bell) <= RANGE * RANGE);
        if (villagers.isEmpty()) {
            return;
        }
        List<BlockPos> houses = new ArrayList<>();
        for (Village village : world.getVillageCollection().getVillageList()) {
            for (VillageDoorInfo door : village.getVillageDoorInfoList()) {
                if (door.getInsideBlockPos().distanceSq(bell) <= (RANGE * 2) * (RANGE * 2)) {
                    houses.add(door.getInsideBlockPos());
                }
            }
        }
        Map<BlockPos, Integer> crowds = new HashMap<>();
        List<EntityAIHideFromBell> tasks = new ArrayList<>();
        for (EntityVillager villager : villagers) {
            EntityAIHideFromBell task = of(villager);
            if (task == null) {
                task = new EntityAIHideFromBell(villager);
                villager.tasks.addTask(2, task);
            }
            tasks.add(task);
            if (task.isAlarmed()) {
                crowds.merge(task.hidingPlace, 1, Integer::sum);
            }
        }
        for (EntityAIHideFromBell task : tasks) {
            if (task.isAlarmed()) {
                task.hear(task.hidingPlace);
                continue;
            }
            BlockPos best = null;
            double bestCost = Double.MAX_VALUE;
            for (BlockPos house : houses) {
                double distance = task.creature.getDistanceSqToCenter(house);
                if (distance > RANGE * RANGE) {
                    continue;
                }
                double cost = Math.sqrt(distance) + CROWDING * crowds.getOrDefault(house, 0);
                if (cost < bestCost) {
                    bestCost = cost;
                    best = house;
                }
            }
            if (best == null) {
                best = new BlockPos(task.creature);
            }
            crowds.merge(best, 1, Integer::sum);
            task.hear(best);
        }
    }

    public boolean isAlarmed() {
        return this.hidingPlace != null;
    }

    public void hear(BlockPos place) {
        if (!place.equals(this.hidingPlace)) {
            this.alarmTicks = 0;
            this.arrived = false;
        }
        this.hidingPlace = place;
        this.hiddenTicks = 0;
        this.pathCooldown = 0;
    }

    @Override
    public boolean shouldExecute() {
        return this.hidingPlace != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.hidingPlace != null;
    }

    @Override
    public void startExecuting() {
        this.pathCooldown = 0;
    }

    @Override
    public void resetTask() {
        this.creature.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        BlockPos place = this.hidingPlace;
        if (place == null) {
            return;
        }
        ++this.alarmTicks;
        double distance = this.creature.getDistanceSq(place.getX() + 0.5D, place.getY(), place.getZ() + 0.5D);
        // Only the middle of the house counts, not the doorway, where the villager would stand in the way of the others
        if (distance < 1.0D) {
            this.arrived = true;
        }
        if (this.arrived) {
            if (++this.hiddenTicks >= HIDE_TICKS) {
                this.hidingPlace = null;
                return;
            }
            // A villager that the others pushed aside stays where it is, unless it was pushed out of the house
            if (distance < 4.0D) {
                this.creature.getNavigator().clearPath();
                return;
            }
        } else if (this.alarmTicks >= GIVE_UP_TICKS) {
            this.hidingPlace = null;
            return;
        }
        // Not every tick, so that a villager that cannot find its way does not search for it all the time
        if (--this.pathCooldown <= 0) {
            this.pathCooldown = 10 + this.creature.getRNG().nextInt(10);
            Vec3d target = new Vec3d(place.getX() + 0.5D, place.getY(), place.getZ() + 0.5D);
            // The way to a house that is beyond the reach of the pathfinder is walked in steps, as villagers go indoors at night
            if (this.creature.getDistanceSq(place) > 256.0D) {
                Vec3d step = RandomPositionGenerator.findRandomTargetBlockTowards(this.creature, 14, 3, target);
                if (step != null) {
                    target = step;
                }
            }
            this.creature.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, SPEED);
        }
    }
}
