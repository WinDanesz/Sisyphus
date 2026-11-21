package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.HoglinAnimator;
import com.windanesz.sisyphus.entity.living.EntityZoglin;
import com.windanesz.sisyphus.entity.living.HoglinLike;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public final class RenderHoglin<T extends EntityLiving & HoglinLike> extends AzEntityRenderer<T> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/hoglin.geo.json");
    private static final ResourceLocation HOGLIN = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/hoglin/hoglin.png");
    private static final ResourceLocation ZOGLIN = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/hoglin/zoglin.png");

    public RenderHoglin(RenderManager manager) {
        super(AzEntityRendererConfig.<T>builder(animal -> MODEL, animal -> animal instanceof EntityZoglin ? ZOGLIN : HOGLIN)
                .setAnimatorProvider(HoglinAnimator::new)
                .setScale(animal -> animal.isBaby() ? 0.5F : 1.0F)
                .setShadowRadius(animal -> animal.isBaby() ? 0.35F : 0.7F)
                .build(), manager);
    }
}
