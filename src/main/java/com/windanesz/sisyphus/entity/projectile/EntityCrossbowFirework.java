package com.windanesz.sisyphus.entity.projectile;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityCrossbowFirework extends EntityFireworkRocket {

    private static final double HORIZONTAL_BOOST = 1.15D;
    private static final double LIFT = 0.04D;

    private final Vec3d velocity;

    public EntityCrossbowFirework(World world, double x, double y, double z, ItemStack stack, Vec3d velocity) {
        super(world, x, y, z, stack);
        this.velocity = velocity;
        this.motionX = velocity.x;
        this.motionY = velocity.y;
        this.motionZ = velocity.z;
    }

    @Override
    public void onUpdate() {
        // The vanilla update speeds the rocket up horizontally and pushes it up, so the motion is set to cancel that out
        this.motionX = this.velocity.x / HORIZONTAL_BOOST;
        this.motionZ = this.velocity.z / HORIZONTAL_BOOST;
        this.motionY = this.velocity.y - LIFT;
        super.onUpdate();
        this.motionX = this.velocity.x;
        this.motionY = this.velocity.y;
        this.motionZ = this.velocity.z;
    }
}
