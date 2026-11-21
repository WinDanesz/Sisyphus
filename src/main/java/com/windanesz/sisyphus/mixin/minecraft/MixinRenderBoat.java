package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.entity.item.BubbleColumnBoat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.entity.item.EntityBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A boat leans from side to side while it rocks over a bubble column.
 */
@Mixin(RenderBoat.class)
public abstract class MixinRenderBoat {

    @Inject(method = "setupRotation", at = @At("TAIL"))
    private void sisyphus$rock(EntityBoat boat, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (boat instanceof BubbleColumnBoat) {
            float angle = ((BubbleColumnBoat) boat).sisyphus$getRockingAngle(partialTicks);
            if (angle != 0.0F) {
                GlStateManager.rotate(angle, 1.0F, 0.0F, 1.0F);
            }
        }
    }
}
