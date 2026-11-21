package com.windanesz.sisyphus.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleObsidianTear extends Particle {

    private int hangTicks;

    public ParticleObsidianTear(World world, double x, double y, double z) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.particleRed = 0.45F;
        this.particleGreen = 0.12F;
        this.particleBlue = 0.8F;
        this.setSize(0.01F, 0.01F);
        this.particleGravity = 0.06F;
        this.particleMaxAge = 100;
        this.particleScale = 0.9F;
        this.hangTicks = 20 + this.rand.nextInt(25);
        // The water drop of the particle sheet, which is the shape of a tear
        this.setParticleTextureIndex(113);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
            return;
        }
        if (this.hangTicks > 0) {
            this.hangTicks--;
            return;
        }
        this.motionY -= this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionY *= 0.98D;
        if (this.onGround) {
            this.setExpired();
        }
    }
}
