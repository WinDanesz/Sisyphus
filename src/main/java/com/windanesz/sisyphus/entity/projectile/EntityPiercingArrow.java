package com.windanesz.sisyphus.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class EntityPiercingArrow extends EntityTippedArrow {

    private static final DataParameter<Byte> PIERCE_LEVEL = EntityDataManager.createKey(EntityPiercingArrow.class, DataSerializers.BYTE);

    private final Set<Integer> pierced = new HashSet<>();

    public EntityPiercingArrow(World world) {
        super(world);
    }

    public EntityPiercingArrow(World world, EntityLivingBase shooter) {
        super(world, shooter);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PIERCE_LEVEL, (byte) 0);
    }

    public int getPierceLevel() {
        return this.dataManager.get(PIERCE_LEVEL);
    }

    public void setPierceLevel(int level) {
        this.dataManager.set(PIERCE_LEVEL, (byte) level);
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (result.entityHit == null || this.getPierceLevel() <= 0) {
            super.onHit(result);
            return;
        }

        int id = result.entityHit.getEntityId();
        if (this.pierced.contains(id)) {
            // Already went through this one
            return;
        }
        if (this.pierced.size() > this.getPierceLevel()) {
            this.setDead();
            return;
        }
        this.pierced.add(id);

        // The normal hit does the damage and the effects, but also stops the arrow, which is undone here
        double mx = this.motionX;
        double my = this.motionY;
        double mz = this.motionZ;
        super.onHit(result);
        this.isDead = false;
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("PierceLevel", (byte) this.getPierceLevel());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setPierceLevel(compound.getByte("PierceLevel"));
    }
}
