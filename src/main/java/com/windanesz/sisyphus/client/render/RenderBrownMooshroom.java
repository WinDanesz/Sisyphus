package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityBrownMooshroom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class RenderBrownMooshroom extends RenderLiving<EntityBrownMooshroom> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/brown_mooshroom.png");

    public RenderBrownMooshroom(RenderManager renderManager) {
        super(renderManager, new ModelCow(), 0.7F);
        this.addLayer(new MushroomLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBrownMooshroom entity) {
        return TEXTURE;
    }

    private static class MushroomLayer implements LayerRenderer<EntityBrownMooshroom> {

        private final RenderBrownMooshroom renderer;

        MushroomLayer(RenderBrownMooshroom renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityBrownMooshroom entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale) {
            if (entity.isChild() || entity.isInvisible()) {
                return;
            }
            BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            this.renderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.enableCull();
            GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F, -1.0F, 1.0F);
            GlStateManager.translate(0.2F, 0.35F, 0.5F);
            GlStateManager.rotate(42.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, 0.5F);
            dispatcher.renderBlockBrightness(Blocks.BROWN_MUSHROOM.getDefaultState(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.1F, 0.0F, -0.6F);
            GlStateManager.rotate(42.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.5F);
            dispatcher.renderBlockBrightness(Blocks.BROWN_MUSHROOM.getDefaultState(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            ((ModelCow) this.renderer.getMainModel()).head.postRender(0.0625F);
            GlStateManager.scale(1.0F, -1.0F, 1.0F);
            GlStateManager.translate(0.0F, 0.7F, -0.2F);
            GlStateManager.rotate(12.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.5F);
            dispatcher.renderBlockBrightness(Blocks.BROWN_MUSHROOM.getDefaultState(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.disableCull();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
