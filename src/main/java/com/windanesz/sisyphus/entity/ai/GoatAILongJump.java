package com.windanesz.sisyphus.entity.ai;

import com.windanesz.sisyphus.entity.living.EntityGoat;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class GoatAILongJump extends EntityAIBase {

    private static final int WIDTH = 5;
    private static final int HEIGHT = 5;
    private static final double MAX_SPEED_FACTOR = 3.5714288D;
    private static final int TRIES = 8;

    private final EntityGoat goat;
    private EntityGoat.GoatJump.Launch launch;
    private double landX;
    private double landZ;
    private int airTicks;

    public GoatAILongJump(EntityGoat goat) {
        this.goat = goat;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityGoat g = this.goat;
        if (g.getLongJumpCooldown() > 0 || !g.onGround || g.isInWater() || g.isRiding() || g.isInLove() || !g.getNavigator().noPath()) {
            return false;
        }
        this.launch = this.findLeap();
        if (this.launch == null) {
            // Nowhere to go from here: look again in a few seconds
            g.setLongJumpCooldown(60 + g.getRNG().nextInt(60));
            return false;
        }
        return true;
    }

    private EntityGoat.GoatJump.Launch findLeap() {
        Random random = this.goat.getRNG();
        double maxSpeed = this.goat.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * MAX_SPEED_FACTOR;
        BlockPos here = new BlockPos(this.goat);
        for (int i = 0; i < TRIES; i++) {
            BlockPos spot = here.add(random.nextInt(2 * WIDTH + 1) - WIDTH, random.nextInt(2 * HEIGHT + 1) - HEIGHT, random.nextInt(2 * WIDTH + 1) - WIDTH);
            double x = spot.getX() + 0.5D;
            double z = spot.getZ() + 0.5D;
            double dx = x - this.goat.posX;
            double dz = z - this.goat.posZ;
            double dy = spot.getY() - this.goat.posY;
            if (dx * dx + dz * dz < 6.25D || !this.canStandAt(spot, x, z)) {
                continue;
            }
            EntityGoat.GoatJump.Launch plan = EntityGoat.GoatJump.plan(dx, dy, dz, maxSpeed);
            if (plan != null && this.wayIsFree(plan)) {
                this.landX = x;
                this.landZ = z;
                return plan;
            }
        }
        return null;
    }

    private boolean canStandAt(BlockPos spot, double x, double z) {
        BlockPos floor = spot.down();
        if (!this.goat.world.getBlockState(floor).isSideSolid(this.goat.world, floor, EnumFacing.UP)) {
            return false;
        }
        AxisAlignedBB box = this.boxAt(x - this.goat.posX, spot.getY() - this.goat.posY, z - this.goat.posZ);
        return this.goat.world.getCollisionBoxes(this.goat, box).isEmpty() && !this.goat.world.containsAnyLiquid(box);
    }

    private AxisAlignedBB boxAt(double dx, double dy, double dz) {
        return this.goat.getEntityBoundingBox().offset(dx, dy, dz);
    }

    private boolean wayIsFree(EntityGoat.GoatJump.Launch plan) {
        for (int tick = 1; tick < plan.ticks; tick += 2) {
            double[] offset = plan.offsetAfter(tick);
            if (!this.goat.world.getCollisionBoxes(this.goat, this.boxAt(offset[0], offset[1], offset[2])).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void startExecuting() {
        EntityGoat g = this.goat;
        this.airTicks = 0;
        g.getNavigator().clearPath();
        float yaw = (float) (MathHelper.atan2(this.landZ - g.posZ, this.landX - g.posX) * (180.0D / Math.PI)) - 90.0F;
        g.rotationYaw = yaw;
        g.renderYawOffset = yaw;
        g.rotationYawHead = yaw;
        g.motionX = this.launch.motionX;
        g.motionY = this.launch.motionY;
        g.motionZ = this.launch.motionZ;
        g.isAirBorne = true;
        g.playLongJumpSound();
        g.setLongJumpCooldown(EntityGoat.longJumpCooldown(g.getRNG()));
    }

    @Override
    public boolean shouldContinueExecuting() {
        // The first ticks are still on the ground, and then it goes on until the goat is down
        return this.airTicks++ < 3 || (!this.goat.onGround && !this.goat.isInWater() && this.airTicks < 100);
    }

    @Override
    public void resetTask() {
        this.launch = null;
    }
}
