package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.animator.AxolotlAnimator;
import com.windanesz.sisyphus.entity.living.EntityAxolotl;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class RenderAxolotl extends AzEntityRenderer<EntityAxolotl> {

    private static final ResourceLocation MODEL = new ResourceLocation(SisyphusBackport.MODID, "geo/entity/axolotl.geo.json");
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[EntityAxolotl.Variant.values().length];

    static {
        for (EntityAxolotl.Variant variant : EntityAxolotl.Variant.values()) {
            TEXTURES[variant.ordinal()] = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/axolotl/axolotl_" + variant.getName() + ".png");
        }
    }

    public RenderAxolotl(RenderManager manager) {
        super(AzEntityRendererConfig.<EntityAxolotl>builder(axolotl -> MODEL, axolotl -> TEXTURES[axolotl.getVariant().ordinal()])
                .setAnimatorProvider(AxolotlAnimator::new)
                .setScale(axolotl -> axolotl.isChild() ? 0.5F : 1.0F)
                .setShadowRadius(axolotl -> axolotl.isChild() ? 0.25F : 0.5F)
                .build(), manager);
    }
}
