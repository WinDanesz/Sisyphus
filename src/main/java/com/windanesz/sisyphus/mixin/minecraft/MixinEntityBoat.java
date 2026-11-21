package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.block.BlockBubbleColumn;
import com.windanesz.sisyphus.entity.item.BubbleColumnBoat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Each side finds the column under the boat itself: the side that steers the boat throws it, and only the server throws the riders out.
 */
@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat extends Entity implements BubbleColumnBoat {

    @Unique
    private static final int ROCKING_TICKS = 60;

    @Unique
    private int sisyphus$rockingTicks;
    @Unique
    private boolean sisyphus$downwards;
    @Unique
    private float sisyphus$rockingIntensity;
    @Unique
    private float sisyphus$rockingAngle;
    @Unique
    private float sisyphus$prevRockingAngle;

    private MixinEntityBoat(World world) {
        super(world);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void sisyphus$rock(CallbackInfo ci) {
        if (!BlockBubbleColumn.isEnabled()) {
            return;
        }
        Boolean drag = this.sisyphus$columnUnder();
        if (drag != null) {
            this.sisyphus$downwards = drag;
            if (this.sisyphus$rockingTicks == 0) {
                this.sisyphus$rockingTicks = ROCKING_TICKS;
            }
            if (this.world.isRemote) {
                this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + this.rand.nextFloat(), this.posY + 0.7D, this.posZ + this.rand.nextFloat(),
                        0.0D, 0.0D, 0.0D);
                if (this.rand.nextInt(20) == 0) {
                    this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BOAT_PADDLE_WATER, this.getSoundCategory(), 1.0F,
                            0.8F + 0.4F * this.rand.nextFloat(), false);
                }
            }
        } else {
            this.sisyphus$rockingTicks = 0;
        }

        if (this.world.isRemote) {
            this.sisyphus$rockingIntensity += this.sisyphus$rockingTicks > 0 ? 0.05F : -0.1F;
            this.sisyphus$rockingIntensity = MathHelper.clamp(this.sisyphus$rockingIntensity, 0.0F, 1.0F);
            this.sisyphus$prevRockingAngle = this.sisyphus$rockingAngle;
            this.sisyphus$rockingAngle = 10.0F * (float) Math.sin(0.5F * this.world.getTotalWorldTime()) * this.sisyphus$rockingIntensity;
        }

        if (this.sisyphus$rockingTicks > 0 && --this.sisyphus$rockingTicks == 0) {
            if (this.sisyphus$downwards) {
                if (this.canPassengerSteer()) {
                    this.motionY -= 0.7D;
                }
                if (!this.world.isRemote) {
                    this.removePassengers();
                }
            } else if (this.canPassengerSteer()) {
                boolean player = false;
                for (Entity passenger : this.getPassengers()) {
                    player |= passenger instanceof EntityPlayer;
                }
                this.motionY = player ? 2.7D : 0.6D;
            }
        }
    }

    @Unique
    private Boolean sisyphus$columnUnder() {
        BlockBubbleColumn column = BlockBubbleColumn.get();
        AxisAlignedBB box = this.getEntityBoundingBox().shrink(0.001D);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))) {
            IBlockState state = this.world.getBlockState(pos);
            if (state.getBlock() == column && this.world.isAirBlock(pos.up())) {
                return state.getValue(BlockBubbleColumn.DRAG);
            }
        }
        return null;
    }

    @Override
    public float sisyphus$getRockingAngle(float partialTicks) {
        return this.sisyphus$prevRockingAngle + (this.sisyphus$rockingAngle - this.sisyphus$prevRockingAngle) * partialTicks;
    }
}
