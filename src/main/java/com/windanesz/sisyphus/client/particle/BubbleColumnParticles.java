package com.windanesz.sisyphus.client.particle;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockBubbleColumn;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SisyphusBackport.MODID)
public final class BubbleColumnParticles {

    private static final int POP_FRAMES = 5;
    private static final int BUBBLE_TEXTURE = 32;

    private static boolean wasInColumn = false;
    private static boolean firstTick = true;

    private BubbleColumnParticles() {
    }

    private static ResourceLocation pop(int index) {
        return new ResourceLocation(SisyphusBackport.MODID, "particle/bubble_pop_" + index);
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (BlockBubbleColumn.get() == null) {
            return;
        }
        for (int i = 0; i < POP_FRAMES; i++) {
            event.getMap().registerSprite(pop(i));
        }
    }

    private static boolean minimal() {
        return Minecraft.getMinecraft().gameSettings.particleSetting == 2;
    }

    public static void bubbleUp(World world, double x, double y, double z) {
        if (!minimal()) {
            Minecraft.getMinecraft().effectRenderer.addEffect(new BubbleUp(world, x, y, z, 0.0D, 0.04D, 0.0D));
        }
    }

    public static void currentDown(World world, double x, double y, double z) {
        if (!minimal()) {
            Minecraft.getMinecraft().effectRenderer.addEffect(new CurrentDown(world, x, y, z));
        }
    }

    public static void pop(World world, double x, double y, double z) {
        if (!minimal()) {
            Minecraft.getMinecraft().effectRenderer.addEffect(new Pop(world, x, y, z));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        BlockBubbleColumn column = BlockBubbleColumn.get();
        if (player == null || column == null) {
            wasInColumn = false;
            firstTick = true;
            return;
        }
        IBlockState found = null;
        AxisAlignedBB box = player.getEntityBoundingBox().grow(0.0D, -0.4D, 0.0D).shrink(1.0E-6D);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))) {
            IBlockState state = player.world.getBlockState(pos);
            if (state.getBlock() == column) {
                found = state;
                break;
            }
        }
        if (found != null) {
            if (!wasInColumn && !firstTick && !player.isSpectator()) {
                player.playSound(found.getValue(BlockBubbleColumn.DRAG) ? ModSounds.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE
                        : ModSounds.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 1.0F);
            }
            wasInColumn = true;
        } else {
            wasInColumn = false;
        }
        firstTick = false;
    }

    private static boolean inWater(World world, double x, double y, double z) {
        return world.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.WATER;
    }

    private static class BubbleUp extends Particle {

        BubbleUp(World world, double x, double y, double z, double speedX, double speedY, double speedZ) {
            super(world, x, y, z);
            this.setParticleTextureIndex(BUBBLE_TEXTURE);
            this.setSize(0.02F, 0.02F);
            this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F;
            this.motionX = speedX * 0.2D + (Math.random() * 2.0D - 1.0D) * 0.02D;
            this.motionY = speedY * 0.2D + (Math.random() * 2.0D - 1.0D) * 0.02D;
            this.motionZ = speedZ * 0.2D + (Math.random() * 2.0D - 1.0D) * 0.02D;
            this.particleMaxAge = (int) (40.0D / (Math.random() * 0.8D + 0.2D));
        }

        @Override
        public void onUpdate() {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionY += 0.005D;
            if (this.particleMaxAge-- <= 0) {
                this.setExpired();
                return;
            }
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.85D;
            this.motionY *= 0.85D;
            this.motionZ *= 0.85D;
            if (!inWater(this.world, this.posX, this.posY, this.posZ)) {
                this.setExpired();
            }
        }
    }

    private static class CurrentDown extends Particle {

        private float angle;

        CurrentDown(World world, double x, double y, double z) {
            super(world, x, y, z);
            this.setParticleTextureIndex(BUBBLE_TEXTURE);
            this.particleMaxAge = (int) (Math.random() * 60.0D) + 30;
            this.canCollide = false;
            this.motionX = 0.0D;
            this.motionY = -0.05D;
            this.motionZ = 0.0D;
            this.setSize(0.02F, 0.02F);
            this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F;
            this.particleGravity = 0.002F;
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
            this.motionX += 0.6F * MathHelper.cos(this.angle);
            this.motionZ += 0.6F * MathHelper.sin(this.angle);
            this.motionX *= 0.07D;
            this.motionZ *= 0.07D;
            this.move(this.motionX, this.motionY, this.motionZ);
            if (!inWater(this.world, this.posX, this.posY, this.posZ) || this.onGround) {
                this.setExpired();
            }
            this.angle += 0.08F;
        }
    }

    private static class Pop extends Particle {

        Pop(World world, double x, double y, double z) {
            super(world, x, y, z);
            this.particleMaxAge = 4;
            this.particleGravity = 0.008F;
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.updateSprite();
        }

        private void updateSprite() {
            int frame = Math.min(POP_FRAMES - 1, this.particleAge * POP_FRAMES / (this.particleMaxAge + 1));
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(pop(frame).toString());
            this.setParticleTexture(sprite);
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
            this.motionY -= this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.updateSprite();
        }
    }
}
