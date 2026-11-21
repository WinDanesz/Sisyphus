package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityCod extends EntitySchoolingFish {

    public EntityCod(World world) {
        super(world);
        this.setSize(0.5F, 0.3F);
    }

    @Override
    protected String getLootName() {
        return "cod";
    }

    @Override
    public int getMaxSchoolSize() {
        return 8;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_COD_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_COD_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_COD_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return ModSounds.ENTITY_COD_FLOP;
    }
}
