package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.client.render.ArmorTrimTextures;
import com.windanesz.sisyphus.item.ArmorTrims;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the trim over worn armor before the enchantment glint, once the armor model is set up for the piece.
 */
@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase<T extends ModelBase> {

    @Shadow
    @Final
    private RenderLivingBase<?> renderer;
    @Shadow
    private float alpha;
    @Shadow
    private float colorR;
    @Shadow
    private float colorG;
    @Shadow
    private float colorB;

    @Shadow
    public abstract T getModelFromSlot(EntityEquipmentSlot slot);

    @Shadow(remap = false)
    protected abstract T getArmorModelHook(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, T model);

    // After the armor itself (the second render, as dyed armor renders once more before it for its overlay)
    @Inject(method = "renderArmorLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            ordinal = 1, shift = At.Shift.AFTER))
    private void sisyphus$renderTrim(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                     float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slot, CallbackInfo ci) {
        ItemStack stack = entity.getItemStackFromSlot(slot);
        ArmorTrims.Trim trim = ArmorTrims.get(stack);
        if (trim == null || !ArmorTrims.isTrimmable(stack)) {
            return;
        }
        T model = this.getArmorModelHook(entity, stack, slot, this.getModelFromSlot(slot));
        this.renderer.bindTexture(ArmorTrimTextures.entityTexture(trim.pattern, trim.palette((ItemArmor) stack.getItem()), slot == EntityEquipmentSlot.LEGS));
        GlStateManager.color(this.colorR, this.colorG, this.colorB, this.alpha);
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }
}
