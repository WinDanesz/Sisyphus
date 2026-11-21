package com.windanesz.sisyphus.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelTropicalFishA extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;
    private final ModelRenderer topFin;

    public ModelTropicalFishA(float deformation) {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-1.0F, -1.5F, -3.0F, 2, 3, 6, deformation);
        this.body.setRotationPoint(0F, 22F, 0F);

        this.tail = new ModelRenderer(this, 22, -6);
        this.tail.addBox(0.0F, -1.5F, 0.0F, 0, 3, 6, deformation);
        this.tail.setRotationPoint(0F, 22F, 3F);

        this.rightFin = new ModelRenderer(this, 2, 16);
        this.rightFin.addBox(-2.0F, -1.0F, 0.0F, 2, 2, 0, deformation);
        this.rightFin.setRotationPoint(-1F, 22.5F, 0F);
        this.rightFin.rotateAngleY = 0.7853982F;

        this.leftFin = new ModelRenderer(this, 2, 12);
        this.leftFin.addBox(0.0F, -1.0F, 0.0F, 2, 2, 0, deformation);
        this.leftFin.setRotationPoint(1F, 22.5F, 0F);
        this.leftFin.rotateAngleY = -0.7853982F;

        this.topFin = new ModelRenderer(this, 10, -5);
        this.topFin.addBox(0.0F, -3.0F, 0.0F, 0, 3, 6, deformation);
        this.topFin.setRotationPoint(0F, 20.5F, -3F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
        this.tail.render(scale);
        this.rightFin.render(scale);
        this.leftFin.render(scale);
        this.topFin.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        float amplitude = entity.isInWater() ? 1.0F : 1.5F;
        this.tail.rotateAngleY = -amplitude * 0.45F * MathHelper.sin(0.6F * ageInTicks);
    }
}
