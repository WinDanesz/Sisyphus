package com.windanesz.sisyphus.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelCod extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer nose;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;
    private final ModelRenderer tailFin;
    private final ModelRenderer topFin;

    public ModelCod() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-1.0F, -2.0F, 0.0F, 2, 4, 7);
        this.body.setRotationPoint(0F, 22F, 0F);

        this.head = new ModelRenderer(this, 11, 0);
        this.head.addBox(-1.0F, -2.0F, -3.0F, 2, 4, 3);
        this.head.setRotationPoint(0F, 22F, 0F);

        this.nose = new ModelRenderer(this, 0, 0);
        this.nose.addBox(-1.0F, -2.0F, -1.0F, 2, 3, 1);
        this.nose.setRotationPoint(0F, 22F, -3F);

        this.rightFin = new ModelRenderer(this, 22, 1);
        this.rightFin.addBox(-2.0F, 0.0F, -1.0F, 2, 0, 2);
        this.rightFin.setRotationPoint(-1F, 23F, 0F);
        this.rightFin.rotateAngleZ = -0.7853982F;

        this.leftFin = new ModelRenderer(this, 22, 4);
        this.leftFin.addBox(0.0F, 0.0F, -1.0F, 2, 0, 2);
        this.leftFin.setRotationPoint(1F, 23F, 0F);
        this.leftFin.rotateAngleZ = 0.7853982F;

        this.tailFin = new ModelRenderer(this, 22, 3);
        this.tailFin.addBox(0.0F, -2.0F, 0.0F, 0, 4, 4);
        this.tailFin.setRotationPoint(0F, 22F, 7F);

        this.topFin = new ModelRenderer(this, 20, -6);
        this.topFin.addBox(0.0F, -1.0F, -1.0F, 0, 1, 6);
        this.topFin.setRotationPoint(0F, 20F, 0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
        this.head.render(scale);
        this.nose.render(scale);
        this.rightFin.render(scale);
        this.leftFin.render(scale);
        this.tailFin.render(scale);
        this.topFin.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        float amplitude = entity.isInWater() ? 1.0F : 1.5F;
        this.tailFin.rotateAngleY = -amplitude * 0.45F * MathHelper.sin(0.6F * ageInTicks);
    }
}
