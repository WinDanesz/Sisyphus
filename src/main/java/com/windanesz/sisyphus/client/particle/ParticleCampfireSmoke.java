package com.windanesz.sisyphus.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCampfireSmoke extends Particle {

    public ParticleCampfireSmoke(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.particleScale *= 3.0F;
        this.setSize(0.25F, 0.25F);
        this.particleMaxAge = this.rand.nextInt(50) + 280;
        this.particleGravity = 3.0E-6F;
        this.motionX = motionX;
        this.motionY = motionY + this.rand.nextFloat() / 500.0F;
        this.motionZ = motionZ;
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.setParticleTexture(CampfireSmokeTextures.get(this.rand.nextInt(CampfireSmokeTextures.COUNT)));
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge || this.particleAlpha <= 0.0F) {
            this.setExpired();
            return;
        }
        this.motionX += this.rand.nextFloat() / 5000.0F * (this.rand.nextBoolean() ? 1 : -1);
        this.motionZ += this.rand.nextFloat() / 5000.0F * (this.rand.nextBoolean() ? 1 : -1);
        this.motionY -= this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        if (this.particleAge >= this.particleMaxAge - 60 && this.particleAlpha > 0.01F) {
            this.particleAlpha -= 0.015F;
        }
    }
}
