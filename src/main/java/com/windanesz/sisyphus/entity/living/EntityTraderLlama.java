package com.windanesz.sisyphus.entity.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityTraderLlama extends EntityLlama {

    private int despawnDelay = 47999;

    public EntityTraderLlama(World world) {
        super(world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        this.targetTasks.addTask(1, new AIDefendTrader(this));
    }

    @Nullable
    private EntityWanderingTrader leashedTrader() {
        Entity holder = this.getLeashHolder();
        return holder instanceof EntityWanderingTrader ? (EntityWanderingTrader) holder : null;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote || !this.isEntityAlive()) {
            return;
        }
        boolean leashedToOther = this.getLeashed() && this.leashedTrader() == null;
        if (!this.isTame() && !leashedToOther && !this.isBeingRidden()) {
            EntityWanderingTrader trader = this.leashedTrader();
            if (trader != null && trader.getDespawnDelay() <= 0) {
                // the trader stays for good (spawn egg), so its llamas do too
                return;
            }
            this.despawnDelay = trader != null ? trader.getDespawnDelay() - 1 : this.despawnDelay - 1;
            if (this.despawnDelay <= 0) {
                this.clearLeashed(true, false);
                this.setDead();
            }
        }
    }

    @Override
    public void setGrowingAge(int age) {
        super.setGrowingAge(Math.max(0, age));
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("DespawnDelay", this.despawnDelay);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("DespawnDelay", 99)) {
            this.despawnDelay = compound.getInteger("DespawnDelay");
        }
    }

    static class AIDefendTrader extends EntityAITarget {

        private final EntityTraderLlama llama;
        private EntityLivingBase attacker;
        private int timestamp;

        AIDefendTrader(EntityTraderLlama llama) {
            super(llama, false);
            this.llama = llama;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityWanderingTrader trader = this.llama.leashedTrader();
            if (trader == null || trader.getRevengeTarget() == null) {
                return false;
            }
            this.attacker = trader.getRevengeTarget();
            int stamp = trader.getRevengeTimer();
            return stamp != this.timestamp && this.isSuitableTarget(this.attacker, false);
        }

        @Override
        public void startExecuting() {
            this.taskOwner.setAttackTarget(this.attacker);
            EntityWanderingTrader trader = this.llama.leashedTrader();
            if (trader != null) {
                this.timestamp = trader.getRevengeTimer();
            }
            super.startExecuting();
        }
    }
}
