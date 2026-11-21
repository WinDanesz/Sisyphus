package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.PiglinAnimator;
import com.windanesz.sisyphus.client.render.layer.LayerPiglinArmor;
import com.windanesz.sisyphus.client.render.layer.LayerPiglinHeldItem;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.ResourceLocation;

public final class RenderZombifiedPiglin extends AzEntityRenderer<EntityPigZombie> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/piglin.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/piglin/zombified_piglin.png");

    public RenderZombifiedPiglin(RenderManager manager) {
        super(AzEntityRendererConfig.<EntityPigZombie>builder(zombie -> MODEL, zombie -> TEXTURE)
                .setAnimatorProvider(() -> new PiglinAnimator<EntityPigZombie>(zombie -> zombie.isArmsRaised() ? "zombie_arms_angry" : "zombie_arms"))
                .addRenderLayer(new LayerPiglinHeldItem<EntityPigZombie>())
                .addRenderLayer(new LayerPiglinArmor<EntityPigZombie>())
                .setScale(zombie -> zombie.isChild() ? 0.55F : 1.0F)
                .setShadowRadius(zombie -> zombie.isChild() ? 0.25F : 0.5F)
                .build(), manager);
    }
}
