package com.windanesz.sisyphus.client.render.layer;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.layer.AzBlockAndItemLayer;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public final class LayerPiglinHeldItem<T extends EntityLivingBase> extends AzBlockAndItemLayer<UUID, T> {

    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone) {
        ItemStack stack = itemStackForBone(bone, context.animatable());
        if (stack == null) {
            return;
        }
        context.poseStack().pushPose();
        RenderUtils.translateToPivotPoint(context.poseStack(), bone);
        renderItemForBone(context, bone, stack, context.animatable());
        context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));
        context.poseStack().popPose();
    }

    @Override
    public ItemStack itemStackForBone(AzBone bone, T entity) {
        ItemStack stack = ItemStack.EMPTY;
        if ("hand_item".equals(bone.getName())) {
            stack = entity.getHeldItemMainhand();
        } else if ("hand_item_left".equals(bone.getName())) {
            stack = entity.getHeldItemOffhand();
        }
        return stack.isEmpty() ? null : stack;
    }

    @Override
    protected ItemCameraTransforms.TransformType getTransformTypeForStack(AzBone bone, ItemStack stack, T entity) {
        return "hand_item_left".equals(bone.getName()) ? ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderItemForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone, ItemStack stack, T entity) {
        // The frame of the held item layer of the game: at the front face of the arm, with the item pointing forward from the hand
        context.poseStack().translate(0.0D, 0.0D, -2.0D / 16.0D);
        context.poseStack().mulPose(Vector3f.XP.rotationDegrees(270.0F));
        super.renderItemForBone(context, bone, stack, entity);
    }
}
