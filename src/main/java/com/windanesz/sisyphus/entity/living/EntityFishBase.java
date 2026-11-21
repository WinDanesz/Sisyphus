package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public abstract class EntityFishBase extends EntityWaterMob {

    /**
     * Vanilla's 0.4 hops about a block here, far too high for such small fish.
     */
    private static final double FLOP_MOTION = 0.25D;

    public EntityFishBase(World world) {
        super(world);
        this.moveHelper = new FishMoveHelper(this);
        // Water counts as walkable for the pathfinder
        this.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Nullable
    static Vec3d findSwimTarget(EntityFishBase fish, int horizontal, int vertical, @Nullable Vec3d awayFrom) {
        for (int i = 0; i < 12; i++) {
            double dx = fish.getRNG().nextInt(horizontal * 2 + 1) - horizontal;
            double dz = fish.getRNG().nextInt(horizontal * 2 + 1) - horizontal;
            int dy = fish.getRNG().nextInt(vertical * 2 + 1) - vertical;
            if (awayFrom != null) {
                // Bias the target to the opposite side of the threat
                Vec3d away = new Vec3d(fish.posX - awayFrom.x, 0, fish.posZ - awayFrom.z);
                if (away.lengthSquared() > 1.0E-4D) {
                    away = away.normalize();
                    if (dx * away.x + dz * away.z < 0) {
                        dx = -dx;
                        dz = -dz;
                    }
                }
            }
            BlockPos target = new BlockPos(fish.posX + dx, fish.posY + dy, fish.posZ + dz);
            if (target.getY() > 0 && fish.world.getBlockState(target).getMaterial() == Material.WATER) {
                return new Vec3d(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
            }
        }
        return null;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3.0D);
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        return new PathNavigateSwimmer(this, world);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new AIPanic(this, 1.25D));
        this.tasks.addTask(2, new AIAvoidPlayer(this, 8.0F, 1.6D, 1.4D));
        this.tasks.addTask(4, new AIRandomSwim(this, 1.0D, 40));
    }

    /**
     * Relative to {@code loot_tables/entities}.
     */
    protected abstract String getLootName();

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(SisyphusBackport.MODID, "entities/" + getLootName());
    }

    protected abstract SoundEvent getFlopSound();

    protected boolean canRandomSwim() {
        return true;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 8;
    }

    @Override
    public boolean canDespawn() {
        // Unlike squids, fish despawn when far away (unless named)
        return !this.hasCustomName();
    }

    @Override
    public boolean getCanSpawnHere() {
        int seaLevel = this.world.getSeaLevel();
        BlockPos pos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        return pos.getY() >= seaLevel - 13 && pos.getY() <= seaLevel
                && this.world.getBlockState(pos.down()).getMaterial() == Material.WATER
                && this.world.getBlockState(pos.up()).getMaterial() == Material.WATER
                && super.getCanSpawnHere();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isServerWorld() && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, 0.01F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public void onLivingUpdate() {
        // Only the server flops: the client just follows the position it is sent, and would play the sound twice
        if (!this.world.isRemote && !this.isInWater() && this.onGround && this.collidedVertically) {
            // Flop around on land
            this.motionX += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.05F;
            this.motionY += FLOP_MOTION;
            this.motionZ += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.05F;
            this.onGround = false;
            this.isAirBorne = true;
            this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getSoundPitch());
        }
        super.onLivingUpdate();
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }

    @Override
    public int getTalkInterval() {
        return 120;
    }

    public static class FishMoveHelper extends EntityMoveHelper {

        private final EntityFishBase fish;

        public FishMoveHelper(EntityFishBase fish) {
            super(fish);
            this.fish = fish;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.fish.isInsideOfMaterial(Material.WATER)) {
                this.fish.motionY += 0.005D;
            }

            if (this.action == Action.MOVE_TO && !this.fish.getNavigator().noPath()) {
                float targetSpeed = (float) (this.speed * this.fish.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float current = this.fish.getAIMoveSpeed();
                this.fish.setAIMoveSpeed(current + (targetSpeed - current) * 0.125F);

                double dx = this.posX - this.fish.posX;
                double dy = this.posY - this.fish.posY;
                double dz = this.posZ - this.fish.posZ;
                if (dy != 0.0D) {
                    double dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
                    this.fish.motionY += this.fish.getAIMoveSpeed() * (dy / dist) * 0.1D;
                }

                if (dx != 0.0D || dz != 0.0D) {
                    float yaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                    this.fish.rotationYaw = this.limitAngle(this.fish.rotationYaw, yaw, 90.0F);
                    this.fish.renderYawOffset = this.fish.rotationYaw;
                }
            } else {
                this.fish.setAIMoveSpeed(0.0F);
            }
        }
    }

    static class AIRandomSwim extends EntityAIBase {

        private final EntityFishBase fish;
        private final double speed;
        private final int chance;
        private Vec3d target;

        AIRandomSwim(EntityFishBase fish, double speed, int chance) {
            this.fish = fish;
            this.speed = speed;
            this.chance = chance;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!this.fish.canRandomSwim()) {
                return false;
            }
            if (this.fish.getIdleTime() >= 100 || this.fish.getRNG().nextInt(this.chance) != 0) {
                return false;
            }
            this.target = findSwimTarget(this.fish, 10, 7, null);
            return this.target != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.fish.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            this.fish.getNavigator().tryMoveToXYZ(this.target.x, this.target.y, this.target.z, this.speed);
        }

        @Override
        public void resetTask() {
            this.target = null;
        }
    }

    static class AIPanic extends EntityAIBase {

        private final EntityFishBase fish;
        private final double speed;
        private Vec3d target;

        AIPanic(EntityFishBase fish, double speed) {
            this.fish = fish;
            this.speed = speed;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.fish.getRevengeTarget() == null && !this.fish.isBurning()) {
                return false;
            }
            this.target = findSwimTarget(this.fish, 5, 4, null);
            return this.target != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.fish.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            this.fish.getNavigator().tryMoveToXYZ(this.target.x, this.target.y, this.target.z, this.speed);
        }
    }

    static class AIAvoidPlayer extends EntityAIBase {

        private final EntityFishBase fish;
        private final float distance;
        private final double farSpeed;
        private final double nearSpeed;
        private EntityPlayer threat;

        AIAvoidPlayer(EntityFishBase fish, float distance, double farSpeed, double nearSpeed) {
            this.fish = fish;
            this.distance = distance;
            this.farSpeed = farSpeed;
            this.nearSpeed = nearSpeed;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            List<EntityPlayer> players = this.fish.world.getEntitiesWithinAABB(EntityPlayer.class,
                    this.fish.getEntityBoundingBox().grow(this.distance, 3.0D, this.distance), EntitySelectors.NOT_SPECTATING);
            if (players.isEmpty()) {
                return false;
            }
            this.threat = null;
            double best = Double.MAX_VALUE;
            for (EntityPlayer player : players) {
                double distSq = this.fish.getDistanceSq(player);
                if (distSq < best && distSq <= this.distance * this.distance) {
                    best = distSq;
                    this.threat = player;
                }
            }
            if (this.threat == null) {
                return false;
            }
            Vec3d target = findSwimTarget(this.fish, 8, 4, new Vec3d(this.threat.posX, this.threat.posY, this.threat.posZ));
            if (target == null) {
                return false;
            }
            return this.fish.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, this.farSpeed);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.fish.getNavigator().noPath();
        }

        @Override
        public void resetTask() {
            this.threat = null;
        }

        @Override
        public void updateTask() {
            if (this.threat != null) {
                this.fish.getNavigator().setSpeed(this.fish.getDistanceSq(this.threat) < 49.0D ? this.nearSpeed : this.farSpeed);
            }
        }
    }
}
