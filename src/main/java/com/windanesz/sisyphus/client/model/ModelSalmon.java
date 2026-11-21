package com.windanesz.sisyphus.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSalmon extends ModelBase {

    private final ModelRenderer bodyFront;
    private final ModelRenderer bodyBack;
    private final ModelRenderer head;
    private final ModelRenderer backFin;
    private final ModelRenderer topFrontFin;
    private final ModelRenderer topBackFin;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;

    public ModelSalmon() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.bodyFront = new ModelRenderer(this, 0, 0);
        this.bodyFront.addBox(-1.5F, -2.5F, 0.0F, 3, 5, 8);
        this.bodyFront.setRotationPoint(0F, 20F, -7.2F);

        this.bodyBack = new ModelRenderer(this, 0, 13);
        this.bodyBack.addBox(-1.5F, -2.5F, 0.0F, 3, 5, 8);
        this.bodyBack.setRotationPoint(0F, 20F, 0.8F);

        this.head = new ModelRenderer(this, 22, 0);
        this.head.addBox(-1.0F, -2.0F, -3.0F, 2, 4, 3);
        this.head.setRotationPoint(0F, 20F, -7.2F);

        this.backFin = new ModelRenderer(this, 20, 10);
        this.backFin.addBox(0.0F, -2.5F, 0.0F, 0, 5, 6);
        this.backFin.setRotationPoint(0F, 0F, 8F);

        this.topFrontFin = new ModelRenderer(this, 2, 1);
        this.topFrontFin.addBox(0.0F, 0.0F, 0.0F, 0, 2, 3);
        this.topFrontFin.setRotationPoint(0F, -4.5F, 5F);

        this.topBackFin = new ModelRenderer(this, 0, 2);
        this.topBackFin.addBox(0.0F, 0.0F, 0.0F, 0, 2, 4);
        this.topBackFin.setRotationPoint(0F, -4.5F, -1F);

        this.rightFin = new ModelRenderer(this, -4, 0);
        this.rightFin.addBox(-2.0F, 0.0F, 0.0F, 2, 0, 2);
        this.rightFin.setRotationPoint(-1.5F, 21.5F, -7.2F);
        this.rightFin.rotateAngleZ = -0.7853982F;

        this.leftFin = new ModelRenderer(this, 0, 0);
        this.leftFin.addBox(0.0F, 0.0F, 0.0F, 2, 0, 2);
        this.leftFin.setRotationPoint(1.5F, 21.5F, -7.2F);
        this.leftFin.rotateAngleZ = 0.7853982F;

        this.bodyFront.addChild(this.topFrontFin);
        this.bodyBack.addChild(this.topBackFin);
        this.bodyBack.addChild(this.backFin);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.bodyFront.render(scale);
        this.bodyBack.render(scale);
        this.head.render(scale);
        this.rightFin.render(scale);
        this.leftFin.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        float amplitude = 1.0F;
        float frequency = 1.0F;
        if (!entity.isInWater()) {
            amplitude = 1.3F;
            frequency = 1.7F;
        }
        this.bodyBack.rotateAngleY = -amplitude * 0.25F * MathHelper.sin(frequency * 0.6F * ageInTicks);
    }
}
