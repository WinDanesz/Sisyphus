package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.FoxAnimator;
import com.windanesz.sisyphus.client.render.layer.LayerFoxHeldItem;
import com.windanesz.sisyphus.entity.living.EntityFox;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class RenderFox extends AzEntityRenderer<EntityFox> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/fox.geo.json");
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fox/fox.png"),
            new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fox/snow_fox.png")};
    private static final ResourceLocation[] SLEEPING = {
            new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fox/fox_sleep.png"),
            new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fox/snow_fox_sleep.png")};

    public RenderFox(RenderManager manager) {
        super(AzEntityRendererConfig.<EntityFox>builder(fox -> MODEL, RenderFox::texture)
                .setAnimatorProvider(FoxAnimator::new)
                .addRenderLayer(new LayerFoxHeldItem())
                .setScale(fox -> fox.isChild() ? 0.6F : 1.0F)
                .setShadowRadius(fox -> fox.isChild() ? 0.25F : 0.4F)
                .build(), manager);
    }

    private static ResourceLocation texture(EntityFox fox) {
        int type = Math.max(0, Math.min(1, fox.getFoxType()));
        return fox.isSleeping() ? SLEEPING[type] : TEXTURES[type];
    }
}
