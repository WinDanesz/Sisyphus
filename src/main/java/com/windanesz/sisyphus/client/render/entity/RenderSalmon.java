package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.model.ModelSalmon;
import com.windanesz.sisyphus.entity.living.EntitySalmon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderSalmon extends RenderLiving<EntitySalmon> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fish/salmon.png");

    public RenderSalmon(RenderManager renderManager) {
        super(renderManager, new ModelSalmon(), 0.4F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySalmon entity) {
        return TEXTURE;
    }

    @Override
    protected void applyRotations(EntitySalmon entity, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        float amplitude = 1.0F;
        float frequency = 1.0F;
        if (!entity.isInWater()) {
            amplitude = 1.3F;
            frequency = 1.7F;
        }
        GlStateManager.rotate(amplitude * 4.3F * MathHelper.sin(frequency * 0.6F * ageInTicks), 0.0F, 1.0F, 0.0F);
        if (!entity.isInWater()) {
            GlStateManager.translate(0.2F, 0.1F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        }
    }
}
