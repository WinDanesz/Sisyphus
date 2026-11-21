package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityZoglin extends EntityMob implements HoglinLike {

    private static final DataParameter<Boolean> BABY = EntityDataManager.createKey(EntityZoglin.class, DataSerializers.BOOLEAN);

    private int attackTicks;

    public EntityZoglin(World world) {
        super(world);
        this.setSize(1.3964844F, 1.4F);
        this.experienceValue = 5;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BABY, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.6D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.4D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class, 10, true, false,
                target -> target != null && !(target instanceof EntityCreeper) && !(target instanceof EntityGhast) && !(target instanceof EntityZoglin)
                        && !(target instanceof EntityArmorStand)));
    }

    @Override
    public boolean isChild() {
        return this.dataManager.get(BABY);
    }

    public void setChild(boolean child) {
        this.dataManager.set(BABY, child);
        this.setSize(child ? 0.6982422F : 1.3964844F, child ? 0.7F : 1.4F);
    }

    @Override
    public boolean isBaby() {
        return this.isChild();
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (BABY.equals(key)) {
            this.setSize(this.isChild() ? 0.6982422F : 1.3964844F, this.isChild() ? 0.7F : 1.4F);
        }
    }

    @Override
    public int getAttackTicks() {
        return this.attackTicks;
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == HoglinCombat.HEADBUTT) {
            this.attackTicks = 10;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.attackTicks > 0) {
            this.attackTicks--;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsBaby", this.isChild());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setChild(compound.getBoolean("IsBaby"));
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        float damage = this.isChild() ? 0.5F : (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        return HoglinCombat.headbutt(this, target, damage, ModSounds.event("entity.zoglin.attack"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.event(this.getAttackTarget() != null ? "entity.zoglin.angry" : "entity.zoglin.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.event("entity.zoglin.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("entity.zoglin.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        this.playSound(ModSounds.event("entity.zoglin.step"), 0.15F, 1.0F);
    }

    @Override
    protected boolean canDropLoot() {
        return true;
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return this.isChild() ? 0 : 5;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(SisyphusBackport.MODID, "entities/zoglin");
    }
}
