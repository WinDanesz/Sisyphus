package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntitySalmon extends EntitySchoolingFish {

    public EntitySalmon(World world) {
        super(world);
        this.setSize(0.7F, 0.4F);
    }

    @Override
    protected String getLootName() {
        return "salmon";
    }

    @Override
    public int getMaxSchoolSize() {
        return 5;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_SALMON_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return ModSounds.ENTITY_SALMON_FLOP;
    }
}
