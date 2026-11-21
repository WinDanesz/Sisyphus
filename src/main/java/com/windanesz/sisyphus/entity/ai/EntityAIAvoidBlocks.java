package com.windanesz.sisyphus.entity.ai;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public class EntityAIAvoidBlocks extends EntityAIBase {

    private final EntityCreature creature;
    private final Predicate<IBlockState> disliked;
    private final int radius;
    private final double speed;
    private final Predicate<EntityCreature> active;

    private BlockPos found;
    private int cooldown;
    private boolean running;

    public EntityAIAvoidBlocks(EntityCreature creature, Predicate<IBlockState> disliked, int radius, double speed, Predicate<EntityCreature> active) {
        this.creature = creature;
        this.disliked = disliked;
        this.radius = radius;
        this.speed = speed;
        this.active = active;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.cooldown-- > 0 || !this.active.test(this.creature)) {
            return false;
        }
        this.cooldown = 20 + this.creature.getRNG().nextInt(20);
        this.found = this.search();
        if (this.found == null) {
            return false;
        }
        Vec3d away = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 10, 5, new Vec3d(this.found));
        return away != null && this.creature.getNavigator().tryMoveToXYZ(away.x, away.y, away.z, this.speed);
    }

    @Override
    public void startExecuting() {
        this.running = true;
    }

    @Override
    public void resetTask() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }

    private BlockPos search() {
        BlockPos origin = new BlockPos(this.creature);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-this.radius, -this.radius / 2, -this.radius), origin.add(this.radius, this.radius / 2, this.radius))) {
            if (this.disliked.test(this.creature.world.getBlockState(pos))) {
                double distance = pos.distanceSq(origin);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = pos.toImmutable();
                }
            }
        }
        return best;
    }
}
