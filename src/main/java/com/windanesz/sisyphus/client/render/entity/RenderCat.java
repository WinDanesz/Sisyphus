package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityCat;
import net.minecraft.client.model.ModelOcelot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderCat extends RenderLiving<EntityCat> {

    private static final ResourceLocation COLLAR_TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/cat/cat_collar.png");

    public RenderCat(RenderManager renderManager) {
        super(renderManager, new ModelOcelot(), 0.4F);
        this.addLayer(new CollarLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCat entity) {
        return entity.getTexture();
    }

    @Override
    protected void preRenderCallback(EntityCat entity, float partialTickTime) {
        super.preRenderCallback(entity, partialTickTime);
        if (entity.isTamed()) {
            GlStateManager.scale(0.8F, 0.8F, 0.8F);
        }
    }

    private static class CollarLayer implements LayerRenderer<EntityCat> {

        private final RenderCat renderer;

        CollarLayer(RenderCat renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityCat cat, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale) {
            if (!cat.isTamed() || cat.isInvisible()) {
                return;
            }
            this.renderer.bindTexture(COLLAR_TEXTURE);
            float[] color = cat.getCollarColor().getColorComponentValues();
            GlStateManager.color(color[0], color[1], color[2], 1.0F);
            this.renderer.getMainModel().render(cat, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
