package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntityPufferfish extends EntityFishBase {

    public static final int STATE_SMALL = 0;
    public static final int STATE_MID = 1;
    public static final int STATE_FULL = 2;
    private static final DataParameter<Integer> PUFF_STATE = EntityDataManager.createKey(EntityPufferfish.class, DataSerializers.VARINT);
    private int inflateCounter;
    private int deflateTimer;

    public EntityPufferfish(World world) {
        super(world);
        this.updateSize();
    }

    private static float getScale(int puffState) {
        switch (puffState) {
            case STATE_SMALL:
                return 0.5F;
            case STATE_MID:
                return 0.7F;
            default:
                return 1.0F;
        }
    }

    static boolean isScary(EntityPufferfish self, EntityLivingBase other) {
        if (other == self || other instanceof EntityFishBase || other instanceof EntitySquid) {
            return false;
        }
        if (other instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) other;
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PUFF_STATE, 0);
    }

    public int getPuffState() {
        return this.dataManager.get(PUFF_STATE);
    }

    public void setPuffState(int state) {
        this.dataManager.set(PUFF_STATE, state);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (PUFF_STATE.equals(key)) {
            this.updateSize();
        }
        super.notifyDataManagerChange(key);
    }

    private void updateSize() {
        float scale = getScale(this.getPuffState());
        this.setSize(0.7F * scale, 0.7F * scale);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("PuffState", this.getPuffState());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        this.setPuffState(Math.min(tag.getInteger("PuffState"), STATE_FULL));
    }

    @Override
    protected String getLootName() {
        return "pufferfish";
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        this.tasks.addTask(1, new AIPuff(this));
    }

    @Override
    public void onUpdate() {
        if (!this.world.isRemote && this.isEntityAlive() && this.isServerWorld()) {
            if (this.inflateCounter > 0) {
                if (this.getPuffState() == STATE_SMALL) {
                    this.playSound(ModSounds.ENTITY_PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getSoundPitch());
                    this.setPuffState(STATE_MID);
                } else if (this.inflateCounter > 40 && this.getPuffState() == STATE_MID) {
                    this.playSound(ModSounds.ENTITY_PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getSoundPitch());
                    this.setPuffState(STATE_FULL);
                }
                ++this.inflateCounter;
            } else if (this.getPuffState() != STATE_SMALL) {
                if (this.deflateTimer > 60 && this.getPuffState() == STATE_FULL) {
                    this.playSound(ModSounds.ENTITY_PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getSoundPitch());
                    this.setPuffState(STATE_MID);
                } else if (this.deflateTimer > 100 && this.getPuffState() == STATE_MID) {
                    this.playSound(ModSounds.ENTITY_PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getSoundPitch());
                    this.setPuffState(STATE_SMALL);
                }
                ++this.deflateTimer;
            }
        }
        super.onUpdate();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote && this.isEntityAlive() && this.getPuffState() > 0) {
            List<EntityLiving> nearby = this.world.getEntitiesWithinAABB(EntityLiving.class, this.getEntityBoundingBox().grow(0.3D));
            for (EntityLiving mob : nearby) {
                if (mob.isEntityAlive() && isScary(this, mob)) {
                    this.touch(mob);
                }
            }
        }
    }

    private void touch(EntityLiving mob) {
        int state = this.getPuffState();
        if (mob.attackEntityFrom(DamageSource.causeMobDamage(this), 1 + state)) {
            mob.addPotionEffect(new PotionEffect(MobEffects.POISON, 60 * state, 0));
            this.playSound(ModSounds.ENTITY_PUFFER_FISH_STING, 1.0F, 1.0F);
        }
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        int state = this.getPuffState();
        if (!this.world.isRemote && this.isEntityAlive() && state > 0 && !player.capabilities.isCreativeMode
                && player.attackEntityFrom(DamageSource.causeMobDamage(this), 1 + state)) {
            if (!this.isSilent()) {
                this.playSound(ModSounds.ENTITY_PUFFER_FISH_STING, 1.0F, 1.0F);
            }
            player.addPotionEffect(new PotionEffect(MobEffects.POISON, 60 * state, 0));
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PUFFER_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_PUFFER_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return ModSounds.ENTITY_PUFFER_FISH_FLOP;
    }

    static class AIPuff extends EntityAIBase {

        private final EntityPufferfish fish;

        AIPuff(EntityPufferfish fish) {
            this.fish = fish;
        }

        @Override
        public boolean shouldExecute() {
            List<EntityLivingBase> nearby = this.fish.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    this.fish.getEntityBoundingBox().grow(2.0D), other -> other != null && isScary(this.fish, other));
            return !nearby.isEmpty();
        }

        @Override
        public void startExecuting() {
            this.fish.inflateCounter = 1;
            this.fish.deflateTimer = 0;
        }

        @Override
        public void resetTask() {
            this.fish.inflateCounter = 0;
        }
    }
}
