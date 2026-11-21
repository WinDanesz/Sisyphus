package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.StriderAnimator;
import com.windanesz.sisyphus.entity.living.EntityStrider;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class RenderStrider extends AzEntityRenderer<EntityStrider> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/strider.geo.json");

    public RenderStrider(RenderManager manager) {
        super(AzEntityRendererConfig.<EntityStrider>builder(strider -> MODEL, strider -> StriderTextures.get(strider.isSuffocating(), strider.isSaddled()))
                .setAnimatorProvider(StriderAnimator::new)
                .setScale(strider -> strider.isChild() ? 0.5F : 1.0F)
                .setShadowRadius(strider -> strider.isChild() ? 0.25F : 0.5F)
                .build(), manager);
    }
}
