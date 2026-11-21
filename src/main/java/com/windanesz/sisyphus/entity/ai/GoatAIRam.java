package com.windanesz.sisyphus.entity.ai;

import com.windanesz.sisyphus.entity.living.EntityGoat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;
import java.util.List;

public class GoatAIRam extends EntityAIBase {

    public static final int PREPARE_TICKS = 20;
    public static final int MIN_DISTANCE = 4;
    public static final int MAX_DISTANCE = 7;
    private static final double RAM_SPEED = 3.0D;
    private static final int RAM_TICKS = 200;
    private static final double OVERSHOOT = 2.0D;

    private final EntityGoat goat;
    private EntityLivingBase target;
    private boolean ramming;
    private boolean done;
    private int timer;
    private double endX;
    private double endZ;
    private double dirX;
    private double dirZ;

    public GoatAIRam(EntityGoat goat) {
        this.goat = goat;
        this.setMutexBits(3);
    }

    public static boolean isRammable(EntityGoat goat, Entity entity) {
        if (entity == goat || entity instanceof EntityGoat || entity instanceof EntityArmorStand || !(entity instanceof EntityLivingBase)) {
            return false;
        }
        if (entity instanceof EntityPlayer && (((EntityPlayer) entity).isCreative() || ((EntityPlayer) entity).isSpectator())) {
            return false;
        }
        return entity.isEntityAlive();
    }

    private static int level(PotionEffect effect) {
        return effect == null ? 0 : effect.getAmplifier() + 1;
    }

    @Override
    public boolean shouldExecute() {
        EntityGoat g = this.goat;
        if (g.getRamCooldown() > 0 || !g.onGround || g.isInWater() || g.isRiding() || g.isInLove() || g.getLeashed() || g.getRNG().nextInt(4) != 0) {
            return false;
        }
        this.target = this.findTarget();
        return this.target != null;
    }

    private EntityLivingBase findTarget() {
        AxisAlignedBB around = this.goat.getEntityBoundingBox().grow(MAX_DISTANCE, 2.0D, MAX_DISTANCE);
        List<Entity> candidates = this.goat.world.getEntitiesWithinAABB(Entity.class, around, entity -> isRammable(this.goat, entity));
        candidates.sort(Comparator.comparingDouble(this.goat::getDistanceSq));
        for (Entity entity : candidates) {
            double dx = entity.posX - this.goat.posX;
            double dz = entity.posZ - this.goat.posZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance >= MIN_DISTANCE && distance <= MAX_DISTANCE && Math.abs(entity.posY - this.goat.posY) <= 1.5D
                    && this.goat.canEntityBeSeen(entity) && this.lineIsClear(dx, dz, distance)) {
                return (EntityLivingBase) entity;
            }
        }
        return null;
    }

    private boolean lineIsClear(double dx, double dz, double distance) {
        AxisAlignedBB box = this.goat.getEntityBoundingBox();
        int steps = MathHelper.ceil(distance);
        for (int i = 1; i <= steps; i++) {
            double f = (double) i / steps;
            if (!this.goat.world.getCollisionBoxes(this.goat, box.offset(dx * f, 0.0D, dz * f)).isEmpty()) {
                return false;
            }
            BlockPos below = new BlockPos(this.goat.posX + dx * f, box.minY - 0.5D, this.goat.posZ + dz * f);
            if (this.goat.world.isAirBlock(below) || this.goat.world.getBlockState(below).getMaterial().isLiquid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void startExecuting() {
        this.ramming = false;
        this.done = false;
        this.timer = PREPARE_TICKS;
        this.goat.getNavigator().clearPath();
        this.goat.setLoweringHead(true);
        this.goat.playPrepareRamSound();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.done && this.target != null && this.goat.isEntityAlive() && !this.goat.isInWater();
    }

    @Override
    public void updateTask() {
        if (!this.ramming) {
            this.prepare();
        } else {
            this.charge();
        }
    }

    private void prepare() {
        this.goat.getNavigator().clearPath();
        if (!this.target.isEntityAlive()) {
            this.done = true;
            return;
        }
        this.goat.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
        if (--this.timer > 0) {
            return;
        }
        double dx = this.target.posX - this.goat.posX;
        double dz = this.target.posZ - this.goat.posZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 0.01D) {
            this.done = true;
            return;
        }
        this.dirX = dx / length;
        this.dirZ = dz / length;
        this.endX = this.target.posX + this.dirX * OVERSHOOT;
        this.endZ = this.target.posZ + this.dirZ * OVERSHOOT;
        this.ramming = true;
        this.timer = RAM_TICKS;
    }

    private void charge() {
        this.goat.getMoveHelper().setMoveTo(this.endX, this.goat.posY, this.endZ, RAM_SPEED);
        AxisAlignedBB hitBox = this.goat.getEntityBoundingBox().grow(0.15D, 0.0D, 0.15D);
        List<Entity> hit = this.goat.world.getEntitiesWithinAABB(Entity.class, hitBox, entity -> isRammable(this.goat, entity));
        if (!hit.isEmpty()) {
            this.hit((EntityLivingBase) hit.get(0));
            this.done = true;
            return;
        }
        if (this.hasRammedHornBreakingBlock()) {
            // It missed, and ran into a tree or a rock instead, which can break off a horn
            this.goat.playRamImpactSound();
            if (this.goat.dropHorn()) {
                this.goat.playHornBreakSound();
            }
            this.done = true;
            return;
        }
        double dx = this.endX - this.goat.posX;
        double dz = this.endZ - this.goat.posZ;
        if (--this.timer <= 0 || this.goat.collidedHorizontally || dx * dx + dz * dz < 0.0625D) {
            this.done = true;
        }
    }

    private boolean hasRammedHornBreakingBlock() {
        BlockPos ahead = new BlockPos(this.goat.posX + this.dirX, this.goat.posY, this.goat.posZ + this.dirZ);
        return EntityGoat.snapsHorn(this.goat.world.getBlockState(ahead)) || EntityGoat.snapsHorn(this.goat.world.getBlockState(ahead.up()));
    }

    private void hit(EntityLivingBase victim) {
        float damage = this.goat.isChild() ? 1.0F : 2.0F;
        boolean blocked = victim.isActiveItemStackBlocking();
        victim.attackEntityFrom(DamageSource.causeMobDamage(this.goat), damage);
        // It is not angry at the goat, and does not go for it
        victim.setRevengeTarget(null);

        int effectLevels = level(this.goat.getActivePotionEffect(MobEffects.SPEED)) - level(this.goat.getActivePotionEffect(MobEffects.SLOWNESS));
        float strength = EntityGoat.ramKnockback(this.goat.getAIMoveSpeed(), effectLevels, this.goat.isChild(), blocked);
        // The ratios point from the victim to the goat, so that it goes the other way: on along the line of the charge
        victim.knockBack(this.goat, strength, -this.dirX, -this.dirZ);
        victim.velocityChanged = true;
        this.goat.playRamImpactSound();
    }

    @Override
    public void resetTask() {
        this.goat.setLoweringHead(false);
        this.goat.setRamCooldown(EntityGoat.ramCooldown(this.goat.getRNG(), this.goat.isScreaming()));
        this.goat.getNavigator().clearPath();
        this.target = null;
        this.ramming = false;
    }
}
