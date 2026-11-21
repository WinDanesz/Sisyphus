package com.windanesz.sisyphus.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSoul extends Particle {

    public ParticleSoul(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.particleScale = 1.0F;
        this.particleMaxAge = 20 + this.rand.nextInt(20);
        this.particleGravity = 0.0F;
        this.setParticleTexture(NetherParticleTextures.soulSprite(0));
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 240 | 240 << 16;
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
        int frame = Math.min(NetherParticleTextures.SOUL_FRAMES - 1, this.particleAge * NetherParticleTextures.SOUL_FRAMES / this.particleMaxAge);
        this.setParticleTexture(NetherParticleTextures.soulSprite(frame));
        this.setAlphaF(1.0F - (float) this.particleAge / this.particleMaxAge * 0.5F);
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.96D;
        this.motionY *= 0.96D;
        this.motionZ *= 0.96D;
    }
}
