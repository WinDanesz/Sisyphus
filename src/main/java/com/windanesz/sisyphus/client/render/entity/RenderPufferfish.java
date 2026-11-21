package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.model.ModelPufferfishBig;
import com.windanesz.sisyphus.client.model.ModelPufferfishMedium;
import com.windanesz.sisyphus.client.model.ModelPufferfishSmall;
import com.windanesz.sisyphus.entity.living.EntityPufferfish;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderPufferfish extends RenderLiving<EntityPufferfish> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/fish/pufferfish.png");

    private final ModelBase small = new ModelPufferfishSmall();
    private final ModelBase medium = new ModelPufferfishMedium();
    private final ModelBase big = new ModelPufferfishBig();

    public RenderPufferfish(RenderManager renderManager) {
        super(renderManager, new ModelPufferfishBig(), 0.2F);
    }

    @Override
    public void doRender(EntityPufferfish entity, double x, double y, double z, float entityYaw, float partialTicks) {
        int state = entity.getPuffState();
        this.shadowSize = 0.1F + 0.1F * state;
        this.mainModel = state == EntityPufferfish.STATE_SMALL ? this.small : state == EntityPufferfish.STATE_MID ? this.medium : this.big;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPufferfish entity) {
        return TEXTURE;
    }

    @Override
    protected void applyRotations(EntityPufferfish entity, float ageInTicks, float rotationYaw, float partialTicks) {
        GlStateManager.translate(0.0F, MathHelper.cos(ageInTicks * 0.05F) * 0.08F, 0.0F);
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
    }
}
