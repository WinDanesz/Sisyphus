package com.windanesz.sisyphus.client.render.layer;

import com.windanesz.sisyphus.entity.living.EntityGoat;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.layer.AzRenderLayer;

import java.util.UUID;

/**
 * All goats share the model, so each one shows or hides both horns before it is drawn.
 */
public final class LayerGoatHorns implements AzRenderLayer<UUID, EntityGoat> {

    private static void setShown(AzBone bone, boolean shown) {
        if (bone != null) {
            bone.setHidden(!shown);
        }
    }

    @Override
    public void preRender(AzRendererPipelineContext<UUID, EntityGoat> context) {
        AzBakedModel model = context.bakedModel();
        if (model == null) {
            return;
        }
        EntityGoat goat = context.animatable();
        setShown(model.getBoneOrNull("left_horn"), goat.hasLeftHorn());
        setShown(model.getBoneOrNull("right_horn"), goat.hasRightHorn());
    }

    @Override
    public void render(AzRendererPipelineContext<UUID, EntityGoat> context) {
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, EntityGoat> context, AzBone bone) {
    }
}
