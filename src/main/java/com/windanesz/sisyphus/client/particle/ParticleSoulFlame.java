package com.windanesz.sisyphus.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSoulFlame extends Particle {

    private final float flameScale;

    public ParticleSoulFlame(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.motionX = this.motionX * 0.01D + motionX;
        this.motionY = this.motionY * 0.01D + motionY;
        this.motionZ = this.motionZ * 0.01D + motionZ;
        this.posX += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        this.posY += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        this.posZ += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        this.flameScale = this.particleScale;
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
        this.setParticleTexture(NetherParticleTextures.soulFlame());
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks,
                               float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float age = (this.particleAge + partialTicks) / this.particleMaxAge;
        this.particleScale = this.flameScale * (1.0F - age * age * 0.5F);
        super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        float age = (this.particleAge + partialTicks) / this.particleMaxAge;
        age = Math.max(0.0F, Math.min(1.0F, age));
        int base = super.getBrightnessForRender(partialTicks);
        int blockLight = base & 255;
        int skyLight = base >> 16 & 255;
        skyLight += (int) (age * age * age * age * 15.0F * 16.0F);
        return blockLight | Math.min(skyLight, 240) << 16;
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
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.96D;
        this.motionY *= 0.96D;
        this.motionZ *= 0.96D;
        if (this.onGround) {
            this.motionX *= 0.7D;
            this.motionZ *= 0.7D;
        }
    }
}
