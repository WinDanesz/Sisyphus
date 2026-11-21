package com.windanesz.sisyphus.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SoulParticles {

    private SoulParticles() {
    }

    public static void spawn(Entity entity) {
        double x = entity.posX + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
        double z = entity.posZ + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSoul(entity.world, x, entity.getEntityBoundingBox().minY + 0.1D, z,
                entity.motionX * -0.2D, 0.1D, entity.motionZ * -0.2D));
    }
}
