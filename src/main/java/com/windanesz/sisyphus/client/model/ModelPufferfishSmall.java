package com.windanesz.sisyphus.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelPufferfishSmall extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer rightEye;
    private final ModelRenderer leftEye;
    private final ModelRenderer backFin;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;

    public ModelPufferfishSmall() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 27);
        this.body.addBox(-1.5F, -2.0F, -1.5F, 3, 2, 3);
        this.body.setRotationPoint(0F, 23F, 0F);

        this.rightEye = new ModelRenderer(this, 24, 6);
        this.rightEye.addBox(-1.5F, 0.0F, -1.5F, 1, 1, 1);
        this.rightEye.setRotationPoint(0F, 20F, 0F);

        this.leftEye = new ModelRenderer(this, 28, 6);
        this.leftEye.addBox(0.5F, 0.0F, -1.5F, 1, 1, 1);
        this.leftEye.setRotationPoint(0F, 20F, 0F);

        this.backFin = new ModelRenderer(this, -3, 0);
        this.backFin.addBox(-1.5F, 0.0F, 0.0F, 3, 0, 3);
        this.backFin.setRotationPoint(0F, 22F, 1.5F);

        this.rightFin = new ModelRenderer(this, 25, 0);
        this.rightFin.addBox(-1.0F, 0.0F, 0.0F, 1, 0, 2);
        this.rightFin.setRotationPoint(-1.5F, 22F, -1.5F);

        this.leftFin = new ModelRenderer(this, 25, 0);
        this.leftFin.addBox(0.0F, 0.0F, 0.0F, 1, 0, 2);
        this.leftFin.setRotationPoint(1.5F, 22F, -1.5F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
        this.rightEye.render(scale);
        this.leftEye.render(scale);
        this.backFin.render(scale);
        this.rightFin.render(scale);
        this.leftFin.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        this.rightFin.rotateAngleZ = -0.2F + 0.4F * MathHelper.sin(ageInTicks * 0.2F);
        this.leftFin.rotateAngleZ = 0.2F - 0.4F * MathHelper.sin(ageInTicks * 0.2F);
    }
}
