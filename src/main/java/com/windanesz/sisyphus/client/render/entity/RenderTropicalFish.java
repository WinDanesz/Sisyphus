package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.model.ModelTropicalFishA;
import com.windanesz.sisyphus.client.model.ModelTropicalFishB;
import com.windanesz.sisyphus.entity.living.EntityTropicalFish;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderTropicalFish extends RenderLiving<EntityTropicalFish> {

    private static final ResourceLocation TEXTURE_A = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fish/tropical_a.png");
    private static final ResourceLocation TEXTURE_B = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fish/tropical_b.png");
    private static final ResourceLocation[] PATTERNS_A = new ResourceLocation[6];
    private static final ResourceLocation[] PATTERNS_B = new ResourceLocation[6];

    static {
        for (int i = 0; i < 6; i++) {
            PATTERNS_A[i] = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fish/tropical_a_pattern_" + (i + 1) + ".png");
            PATTERNS_B[i] = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fish/tropical_b_pattern_" + (i + 1) + ".png");
        }
    }

    private final ModelBase modelA = new ModelTropicalFishA(0.0F);
    private final ModelBase modelB = new ModelTropicalFishB(0.0F);

    public RenderTropicalFish(RenderManager renderManager) {
        super(renderManager, new ModelTropicalFishA(0.0F), 0.15F);
        this.addLayer(new PatternLayer(this));
    }

    @Override
    public void doRender(EntityTropicalFish entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.mainModel = entity.getShape() == 0 ? this.modelA : this.modelB;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTropicalFish entity) {
        return entity.getShape() == 0 ? TEXTURE_A : TEXTURE_B;
    }

    @Override
    protected void renderModel(EntityTropicalFish entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        // The base texture is greyscale and gets tinted with the base colour
        float[] color = entity.getBaseColor().getColorComponentValues();
        GlStateManager.color(color[0], color[1], color[2], 1.0F);
        super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void applyRotations(EntityTropicalFish entity, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        GlStateManager.rotate(4.3F * MathHelper.sin(0.6F * ageInTicks), 0.0F, 1.0F, 0.0F);
        if (!entity.isInWater()) {
            GlStateManager.translate(0.2F, 0.1F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    private static class PatternLayer implements LayerRenderer<EntityTropicalFish> {

        private final RenderTropicalFish renderer;
        private final ModelBase modelA = new ModelTropicalFishA(0.008F);
        private final ModelBase modelB = new ModelTropicalFishB(0.008F);

        PatternLayer(RenderTropicalFish renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityTropicalFish entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible()) {
                return;
            }
            boolean small = entity.getShape() == 0;
            ModelBase model = small ? this.modelA : this.modelB;
            int index = Math.max(0, Math.min(5, entity.getPatternIndex()));
            this.renderer.bindTexture(small ? PATTERNS_A[index] : PATTERNS_B[index]);

            float[] color = entity.getPatternColor().getColorComponentValues();
            GlStateManager.color(color[0], color[1], color[2], 1.0F);
            model.setModelAttributes(this.renderer.getMainModel());
            model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
