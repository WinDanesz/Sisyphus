package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityWanderingTrader;
import net.minecraft.client.model.ModelLlama;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderLlama;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerLlamaDecor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.List;

public class RenderTraderLlama extends RenderLlama {

    private static final ResourceLocation CARPET = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/llama/decor/trader_llama.png");

    @SuppressWarnings("unchecked")
    public RenderTraderLlama(RenderManager renderManager) {
        super(renderManager);
        // The llama renderer adds a layer for the carpet that the player puts on it, which this one never wears
        List<LayerRenderer<EntityLlama>> layers = ReflectionHelper.getPrivateValue(RenderLivingBase.class, this, "layerRenderers", "field_177097_h");
        layers.removeIf(layer -> layer instanceof LayerLlamaDecor);
        this.addLayer(new CarpetLayer(this));
    }

    private static double lerp(double start, double end, float partialTicks) {
        return start + (end - start) * partialTicks;
    }

    /**
     * Vanilla ends the leash where a player would hold it. A trader holds it in front, at 0.7 of its eye height.
     */
    @Override
    protected void renderLeash(EntityLlama llama, double x, double y, double z, float entityYaw, float partialTicks) {
        Entity holder = llama.getLeashHolder();
        if (!(holder instanceof EntityWanderingTrader)) {
            super.renderLeash(llama, x, y, z, entityYaw, partialTicks);
            return;
        }
        y -= (1.6D - llama.height) * 0.5D;
        double holderX = lerp(holder.prevPosX, holder.posX, partialTicks);
        // The rope ends a quarter block above the point it is given, as in the vanilla leash
        double holderY = lerp(holder.prevPosY, holder.posY, partialTicks) + holder.getEyeHeight() * 0.7D - 0.25D;
        double holderZ = lerp(holder.prevPosZ, holder.posZ, partialTicks);
        double yaw = lerp(llama.prevRenderYawOffset, llama.renderYawOffset, partialTicks) * 0.017453292D + Math.PI / 2.0D;
        double offsetX = Math.cos(yaw) * llama.width * 0.4D;
        double offsetZ = Math.sin(yaw) * llama.width * 0.4D;
        double dx = (float) (holderX - (lerp(llama.prevPosX, llama.posX, partialTicks) + offsetX));
        double dy = (float) (holderY - lerp(llama.prevPosY, llama.posY, partialTicks));
        double dz = (float) (holderZ - (lerp(llama.prevPosZ, llama.posZ, partialTicks) + offsetZ));
        x += offsetX;
        z += offsetZ;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        for (int side = 0; side < 2; side++) {
            buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);
            for (int j = 0; j <= 24; j++) {
                float shade = j % 2 == 0 ? 0.7F : 1.0F;
                float r = 0.5F * shade, g = 0.4F * shade, b = 0.3F * shade;
                float t = j / 24.0F;
                double px = x + dx * t;
                double py = y + dy * (t * t + t) * 0.5D + ((24.0F - j) / 18.0F + 0.125F);
                double pz = z + dz * t;
                if (side == 0) {
                    buffer.pos(px, py, pz).color(r, g, b, 1.0F).endVertex();
                    buffer.pos(px + 0.025D, py + 0.025D, pz).color(r, g, b, 1.0F).endVertex();
                } else {
                    buffer.pos(px, py + 0.025D, pz).color(r, g, b, 1.0F).endVertex();
                    buffer.pos(px + 0.025D, py, pz + 0.025D).color(r, g, b, 1.0F).endVertex();
                }
            }
            tessellator.draw();
        }
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
    }

    private static class CarpetLayer implements LayerRenderer<EntityLlama> {

        private final RenderLlama renderer;
        private final ModelLlama model = new ModelLlama(0.5F);

        CarpetLayer(RenderLlama renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityLlama llama, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale) {
            this.renderer.bindTexture(CARPET);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.render(llama, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
