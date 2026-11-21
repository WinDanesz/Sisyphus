package com.windanesz.sisyphus.client.render.layer;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.layer.AzArmorLayer;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.UUID;

public final class LayerPiglinArmor<T extends EntityLivingBase> extends AzArmorLayer<T> {

    private T wearer;
    private boolean bootsPass;

    private static boolean isLeg(AzBone bone) {
        return "right_leg".equals(bone.getName()) || "left_leg".equals(bone.getName());
    }

    @Override
    public void preRender(AzRendererPipelineContext<UUID, T> context) {
        super.preRender(context);
        this.wearer = context.animatable();
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone) {
        this.bootsPass = false;
        super.renderForBone(context, bone);
        if (isLeg(bone)) {
            this.bootsPass = true;
            super.renderForBone(context, bone);
            this.bootsPass = false;
        }
    }

    @Override
    protected ModelBiped getModelForItem(ItemStack stack, EntityEquipmentSlot slot) {
        ModelBiped generic = super.getModelForItem(stack, slot);
        return this.wearer == null ? generic : ForgeHooksClient.getArmorModel(this.wearer, stack, slot, generic);
    }

    @Override
    protected void renderVanillaArmorPiece(AzRendererPipelineContext<UUID, T> context, AzBone bone, EntityEquipmentSlot slot, ItemStack stack, ModelRenderer part) {
        if (context.multiBufferSource() instanceof AzBufferSource) {
            ((AzBufferSource) context.multiBufferSource()).endBatch();
        }
        GlStateManager.disableCull();
        try {
            super.renderVanillaArmorPiece(context, bone, slot, stack, part);
            ModelBiped model = getModelForItem(stack, slot);
            if (part == model.bipedHead) {
                ModelRenderer headwear = model.bipedHeadwear;
                headwear.setRotationPoint(part.rotationPointX, part.rotationPointY, part.rotationPointZ);
                headwear.rotateAngleX = headwear.rotateAngleY = headwear.rotateAngleZ = 0.0F;
                headwear.showModel = true;
                headwear.isHidden = false;
                super.renderVanillaArmorPiece(context, bone, slot, stack, headwear);
            }
        } finally {
            GlStateManager.enableCull();
        }
    }

    @Override
    protected ItemStack getArmorItemForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone) {
        T entity = context.animatable();
        switch (bone.getName()) {
            case "head":
                return entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            case "body":
            case "right_arm":
            case "left_arm":
                return entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            case "right_leg":
            case "left_leg":
                return entity.getItemStackFromSlot(this.bootsPass ? EntityEquipmentSlot.FEET : EntityEquipmentSlot.LEGS);
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    protected void prepModelPartForRender(AzRendererPipelineContext<UUID, T> context, AzBone bone, ModelRenderer part) {
        float x = 0.0F;
        float y = bone.getPivotY();
        float z = 0.0F;
        switch (bone.getName()) {
            case "right_arm":
                x = -5.0F;
                break;
            case "left_arm":
                x = 5.0F;
                break;
            case "right_leg":
                x = -1.9F;
                break;
            case "left_leg":
                x = 1.9F;
                break;
            default:
                break;
        }
        // Model space runs y down and the layer flips x and y, so the rotation point is the negated position.
        part.setRotationPoint(x, -y, z);
        part.showModel = true;
        part.isHidden = false;
        part.rotateAngleX = 0.0F;
        part.rotateAngleY = 0.0F;
        part.rotateAngleZ = 0.0F;
    }

    @Override
    protected ModelRenderer getModelPartForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone, ModelBiped model) {
        switch (bone.getName()) {
            case "head":
                return model.bipedHead;
            case "right_arm":
                return model.bipedRightArm;
            case "left_arm":
                return model.bipedLeftArm;
            case "right_leg":
                return model.bipedRightLeg;
            case "left_leg":
                return model.bipedLeftLeg;
            default:
                return model.bipedBody;
        }
    }
}
