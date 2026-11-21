package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.WanderingTraderAnimator;
import com.windanesz.sisyphus.client.render.layer.LayerTraderHeldItem;
import com.windanesz.sisyphus.entity.living.EntityWanderingTrader;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class RenderWanderingTrader extends AzEntityRenderer<EntityWanderingTrader> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/wandering_trader.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/wandering_trader.png");

    public RenderWanderingTrader(RenderManager manager) {
        super(AzEntityRendererConfig.<EntityWanderingTrader>builder(trader -> MODEL, trader -> TEXTURE)
                .setAnimatorProvider(WanderingTraderAnimator::new)
                .addRenderLayer(new LayerTraderHeldItem())
                .setScale(0.9375F)
                .setShadowRadius(0.5F)
                .build(), manager);
    }
}
