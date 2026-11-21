package com.windanesz.sisyphus.client.render.layer;

import com.windanesz.sisyphus.entity.living.EntityWanderingTrader;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.layer.AzBlockAndItemLayer;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public final class LayerTraderHeldItem extends AzBlockAndItemLayer<UUID, EntityWanderingTrader> {

    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, EntityWanderingTrader> context, AzBone bone) {
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
    public ItemStack itemStackForBone(AzBone bone, EntityWanderingTrader trader) {
        return "hand_item".equals(bone.getName()) && !trader.getHeldItemMainhand().isEmpty() ? trader.getHeldItemMainhand() : null;
    }

    @Override
    protected ItemCameraTransforms.TransformType getTransformTypeForStack(AzBone bone, ItemStack stack, EntityWanderingTrader trader) {
        return ItemCameraTransforms.TransformType.GROUND;
    }

    @Override
    protected void renderItemForBone(AzRendererPipelineContext<UUID, EntityWanderingTrader> context, AzBone bone, ItemStack stack, EntityWanderingTrader trader) {
        // Turned to face the front
        context.poseStack().mulPose(Vector3f.YP.rotationDegrees(180.0F));
        super.renderItemForBone(context, bone, stack, trader);
    }
}
