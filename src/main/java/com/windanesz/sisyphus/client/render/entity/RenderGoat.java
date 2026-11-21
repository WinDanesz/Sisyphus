package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.GoatAnimator;
import com.windanesz.sisyphus.client.render.layer.LayerGoatHorns;
import com.windanesz.sisyphus.entity.living.EntityGoat;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class RenderGoat extends AzEntityRenderer<EntityGoat> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/goat.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/goat/goat.png");

    public RenderGoat(RenderManager manager) {
        super(AzEntityRendererConfig.<EntityGoat>builder(goat -> MODEL, goat -> TEXTURE)
                .setAnimatorProvider(GoatAnimator::new)
                .addRenderLayer(new LayerGoatHorns())
                .setScale(goat -> goat.isChild() ? 0.5F : 1.0F)
                .setShadowRadius(goat -> goat.isChild() ? 0.25F : 0.5F)
                .build(), manager);
    }
}
