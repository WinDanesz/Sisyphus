package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.item.ItemGoatHorn;
import com.windanesz.sisyphus.item.ItemThrowingDisc;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A disc is drawn back across the chest, as it is thrown backhand. A goat horn is held to the mouth.
 */
@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {

    @Shadow
    public ModelRenderer bipedRightArm;
    @Shadow
    public ModelRenderer bipedLeftArm;
    @Shadow
    public ModelRenderer bipedHead;

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }

    @Inject(method = "setRotationAngles", at = @At("TAIL"))
    private void sisyphus$drawBackDisc(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                       float scaleFactor, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase living = (EntityLivingBase) entity;
        if (ItemGoatHorn.isTooting(living)) {
            this.tootHorn(living);
            return;
        }
        // The age that the model is posed at is the ticks the entity has lived and the part of the tick that has passed
        float partialTicks = MathHelper.clamp(ageInTicks - entity.ticksExisted, 0.0F, 1.0F);
        float windUp = ItemThrowingDisc.getWindUp(living, partialTicks);
        if (windUp < 0.0F) {
            return;
        }
        float drawn = 1.0F - (1.0F - windUp) * (1.0F - windUp);
        EnumHandSide side = living.getActiveHand() == EnumHand.MAIN_HAND ? living.getPrimaryHand() : living.getPrimaryHand().opposite();
        boolean right = side == EnumHandSide.RIGHT;
        ModelRenderer arm = right ? this.bipedRightArm : this.bipedLeftArm;
        // Raised to the chest, swung across it, and the elbow out
        float across = right ? -1.0F : 1.0F;
        arm.rotateAngleX = lerp(arm.rotateAngleX, -1.35F, drawn);
        arm.rotateAngleY = lerp(arm.rotateAngleY, across * 0.95F, drawn);
        arm.rotateAngleZ = lerp(arm.rotateAngleZ, across * -0.25F, drawn);
    }

    private void tootHorn(EntityLivingBase living) {
        EnumHandSide side = living.getActiveHand() == EnumHand.MAIN_HAND ? living.getPrimaryHand() : living.getPrimaryHand().opposite();
        boolean right = side == EnumHandSide.RIGHT;
        ModelRenderer arm = right ? this.bipedRightArm : this.bipedLeftArm;
        arm.rotateAngleX = MathHelper.clamp(this.bipedHead.rotateAngleX, -1.2F, 1.2F) - 1.4835298F;
        arm.rotateAngleY = this.bipedHead.rotateAngleY + (right ? -0.2617994F : 0.2617994F);
        arm.rotateAngleZ = 0.0F;
    }
}
