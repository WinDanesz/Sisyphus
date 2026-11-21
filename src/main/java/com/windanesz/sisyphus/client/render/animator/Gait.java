package com.windanesz.sisyphus.client.render.animator;

import net.minecraft.entity.Entity;

public final class Gait {

    private Gait() {
    }

    public static double speed(Entity entity) {
        double dx = entity.posX - entity.prevPosX;
        double dz = entity.posZ - entity.prevPosZ;
        return Math.sqrt(dx * dx + dz * dz) * 20.0D;
    }
}
