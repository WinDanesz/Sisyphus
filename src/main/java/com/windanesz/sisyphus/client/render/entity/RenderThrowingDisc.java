package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.entity.projectile.EntityThrowingDisc;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderThrowingDisc extends Render<EntityThrowingDisc> {

    private static final float SPIN = 50.0F;

    private final RenderItem itemRenderer;

    public RenderThrowingDisc(RenderManager manager, RenderItem itemRenderer) {
        super(manager);
        this.itemRenderer = itemRenderer;
        this.shadowSize = 0.15F;
    }

    @Override
    public void doRender(EntityThrowingDisc entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2.0D, z);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((entity.ticksExisted + partialTicks) * SPIN, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.85F, 0.85F, 0.85F);
        this.bindEntityTexture(entity);
        GlStateManager.enableRescaleNormal();
        this.itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityThrowingDisc entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
