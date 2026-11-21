package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.PiglinAnimator;
import com.windanesz.sisyphus.client.render.layer.LayerPiglinArmor;
import com.windanesz.sisyphus.client.render.layer.LayerPiglinHeldItem;
import com.windanesz.sisyphus.entity.living.EntityPiglin;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class RenderPiglin extends AzEntityRenderer<EntityPiglin> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/piglin.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/piglin/piglin.png");

    public RenderPiglin(RenderManager manager) {
        super(AzEntityRendererConfig.<EntityPiglin>builder(piglin -> MODEL, piglin -> TEXTURE)
                .setAnimatorProvider(() -> new PiglinAnimator<EntityPiglin>(RenderPiglin::arms))
                .addRenderLayer(new LayerPiglinHeldItem<EntityPiglin>())
                .addRenderLayer(new LayerPiglinArmor<EntityPiglin>())
                .setScale(piglin -> piglin.isChild() ? 0.55F : 1.0F)
                .setShadowRadius(piglin -> piglin.isChild() ? 0.25F : 0.5F)
                .build(), manager);
    }

    private static String arms(EntityPiglin piglin) {
        if (piglin.isAdmiring()) {
            return "admire";
        }
        if (piglin.isChargingCrossbow()) {
            return "crossbow_charge";
        }
        if (piglin.isAggressive()) {
            return piglin.holdsCrossbow() ? "crossbow_hold" : "melee_ready";
        }
        return "";
    }
}
