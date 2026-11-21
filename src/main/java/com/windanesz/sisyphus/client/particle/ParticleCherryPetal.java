package com.windanesz.sisyphus.client.particle;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SisyphusBackport.MODID)
public class ParticleCherryPetal extends Particle {

    private static final int SPRITES = 12;
    private static final int LIFETIME = 300;
    private static final double WIND = 0.0025D;
    private final float spinAcceleration;
    /**
     * In degrees, 0 to 60.
     */
    private final float windAngle;
    private float spin;

    public ParticleCherryPetal(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.setParticleTexture(sprite(this.rand.nextInt(SPRITES)));
        this.spin = (float) Math.toRadians(this.rand.nextBoolean() ? -30.0D : 30.0D);
        this.windAngle = this.rand.nextFloat() * 60.0F;
        this.spinAcceleration = (float) Math.toRadians(this.rand.nextBoolean() ? -5.0D : 5.0D);
        this.particleMaxAge = LIFETIME;
        this.particleGravity = 7.5E-4F;
        // Particles are drawn at a tenth of their scale
        float size = this.rand.nextBoolean() ? 0.05F : 0.075F;
        this.particleScale = size * 10.0F;
        this.setSize(size, size);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
    }

    private static ResourceLocation location(int index) {
        return new ResourceLocation(SisyphusBackport.MODID, "particle/cherry_" + index);
    }

    private static TextureAtlasSprite sprite(int index) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location(index).toString());
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        for (int i = 0; i < SPRITES; i++) {
            event.getMap().registerSprite(location(i));
        }
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
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
            return;
        }
        float progress = Math.min((float) this.particleAge / LIFETIME, 1.0F);
        double wind = 2.0D * Math.pow(progress, 1.25D);
        this.motionX += Math.cos(Math.toRadians(this.windAngle)) * wind * WIND;
        this.motionZ += Math.sin(Math.toRadians(this.windAngle)) * wind * WIND;
        this.motionY -= this.particleGravity;
        this.spin += this.spinAcceleration / 20.0F;
        this.prevParticleAngle = this.particleAngle;
        this.particleAngle += this.spin / 20.0F;
        this.move(this.motionX, this.motionY, this.motionZ);
        if (this.onGround || this.particleAge > 1 && (this.motionX == 0.0D || this.motionZ == 0.0D)) {
            this.setExpired();
        }
    }
}
