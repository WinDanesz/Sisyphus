package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.PandaAnimator;
import com.windanesz.sisyphus.entity.living.EntityPanda;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class RenderPanda extends AzEntityRenderer<EntityPanda> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/panda.geo.json");

    public RenderPanda(RenderManager manager) {
        super(AzEntityRendererConfig.<EntityPanda>builder(panda -> MODEL, EntityPanda::getTexture)
                .setAnimatorProvider(PandaAnimator::new)
                .setScale(panda -> panda.isChild() ? 0.5F : 1.0F)
                .setShadowRadius(panda -> panda.isChild() ? 0.45F : 0.9F)
                .build(), manager);
    }
}
