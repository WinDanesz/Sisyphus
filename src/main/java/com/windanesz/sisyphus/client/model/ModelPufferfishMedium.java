package com.windanesz.sisyphus.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelPufferfishMedium extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;
    private final ModelRenderer topFrontFin;
    private final ModelRenderer topBackFin;
    private final ModelRenderer rightFrontFin;
    private final ModelRenderer rightBackFin;
    private final ModelRenderer leftBackFin;
    private final ModelRenderer leftFrontFin;
    private final ModelRenderer bottomBackFin;
    private final ModelRenderer bottomFrontFin;

    public ModelPufferfishMedium() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 12, 22);
        this.body.addBox(-2.5F, -5.0F, -2.5F, 5, 5, 5);
        this.body.setRotationPoint(0F, 22F, 0F);

        this.rightFin = new ModelRenderer(this, 24, 0);
        this.rightFin.addBox(-2.0F, 0.0F, 0.0F, 2, 0, 2);
        this.rightFin.setRotationPoint(-2.5F, 18F, -1.5F);

        this.leftFin = new ModelRenderer(this, 24, 3);
        this.leftFin.addBox(0.0F, 0.0F, 0.0F, 2, 0, 2);
        this.leftFin.setRotationPoint(2.5F, 18F, -1.5F);

        this.topFrontFin = new ModelRenderer(this, 19, 17);
        this.topFrontFin.addBox(-2.5F, -1.0F, 0.0F, 5, 1, 0);
        this.topFrontFin.setRotationPoint(0F, 17F, -2.5F);
        this.topFrontFin.rotateAngleX = 0.7853982F;

        this.topBackFin = new ModelRenderer(this, 11, 17);
        this.topBackFin.addBox(-2.5F, -1.0F, 0.0F, 5, 1, 0);
        this.topBackFin.setRotationPoint(0F, 17F, 2.5F);
        this.topBackFin.rotateAngleX = -0.7853982F;

        this.rightFrontFin = new ModelRenderer(this, 5, 17);
        this.rightFrontFin.addBox(-1.0F, -5.0F, 0.0F, 1, 5, 0);
        this.rightFrontFin.setRotationPoint(-2.5F, 22F, -2.5F);
        this.rightFrontFin.rotateAngleY = -0.7853982F;

        this.rightBackFin = new ModelRenderer(this, 9, 17);
        this.rightBackFin.addBox(-1.0F, -5.0F, 0.0F, 1, 5, 0);
        this.rightBackFin.setRotationPoint(-2.5F, 22F, 2.5F);
        this.rightBackFin.rotateAngleY = 0.7853982F;

        this.leftBackFin = new ModelRenderer(this, 1, 17);
        this.leftBackFin.addBox(0.0F, -5.0F, 0.0F, 1, 5, 0);
        this.leftBackFin.setRotationPoint(2.5F, 22F, 2.5F);
        this.leftBackFin.rotateAngleY = -0.7853982F;

        this.leftFrontFin = new ModelRenderer(this, 1, 17);
        this.leftFrontFin.addBox(0.0F, -5.0F, 0.0F, 1, 5, 0);
        this.leftFrontFin.setRotationPoint(2.5F, 22F, -2.5F);
        this.leftFrontFin.rotateAngleY = 0.7853982F;

        this.bottomBackFin = new ModelRenderer(this, 18, 20);
        this.bottomBackFin.addBox(0.0F, 0.0F, 0.0F, 5, 1, 0);
        this.bottomBackFin.setRotationPoint(-2.5F, 22F, 2.5F);
        this.bottomBackFin.rotateAngleX = 0.7853982F;

        this.bottomFrontFin = new ModelRenderer(this, 17, 19);
        this.bottomFrontFin.addBox(-2.5F, 0.0F, 0.0F, 5, 1, 1);
        this.bottomFrontFin.setRotationPoint(0F, 22F, -2.5F);
        this.bottomFrontFin.rotateAngleX = -0.7853982F;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
        this.rightFin.render(scale);
        this.leftFin.render(scale);
        this.topFrontFin.render(scale);
        this.topBackFin.render(scale);
        this.rightFrontFin.render(scale);
        this.rightBackFin.render(scale);
        this.leftBackFin.render(scale);
        this.leftFrontFin.render(scale);
        this.bottomBackFin.render(scale);
        this.bottomFrontFin.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        this.rightFin.rotateAngleZ = -0.2F + 0.4F * MathHelper.sin(ageInTicks * 0.2F);
        this.leftFin.rotateAngleZ = 0.2F - 0.4F * MathHelper.sin(ageInTicks * 0.2F);
    }
}
