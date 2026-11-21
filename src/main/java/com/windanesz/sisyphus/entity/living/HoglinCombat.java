package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;

final class HoglinCombat {

    static final byte HEADBUTT = 4;

    private HoglinCombat() {
    }

    static boolean headbutt(EntityLiving attacker, Entity target, float damage, SoundEvent sound) {
        attacker.world.setEntityState(attacker, HEADBUTT);
        attacker.playSound(sound, 1.0F, (attacker.getRNG().nextFloat() - attacker.getRNG().nextFloat()) * 0.2F + 1.0F);
        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(attacker), damage);
        if (hit && target instanceof EntityLivingBase) {
            EntityLivingBase victim = (EntityLivingBase) target;
            double strength = 1.0D - victim.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
            double dx = victim.posX - attacker.posX;
            double dz = victim.posZ - attacker.posZ;
            double length = Math.sqrt(dx * dx + dz * dz);
            if (strength > 0.0D && length > 1.0E-4D) {
                victim.addVelocity(dx / length * strength, 0.5D * strength, dz / length * strength);
                victim.velocityChanged = true;
            }
        }
        return hit;
    }

    static SoundEvent sound(String name) {
        return ModSounds.event(name);
    }
}
