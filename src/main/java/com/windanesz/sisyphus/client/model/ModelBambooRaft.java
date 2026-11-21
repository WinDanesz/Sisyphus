package com.windanesz.sisyphus.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;

public class ModelBambooRaft extends ModelBase {

    private final ModelRenderer bottom;
    private final ModelRenderer[] paddles = new ModelRenderer[2];
    private final ModelRenderer[] chest;

    public ModelBambooRaft(boolean withChest) {
        this.textureWidth = 128;
        this.textureHeight = withChest ? 128 : 64;

        this.bottom = new ModelRenderer(this);
        this.bottom.setTextureOffset(0, 0).addBox(-14.0F, -11.0F, -4.0F, 28, 20, 4);
        this.bottom.setTextureOffset(0, 0).addBox(-14.0F, -9.0F, -8.0F, 28, 16, 4);
        this.bottom.setRotationPoint(0.0F, -2.0F, 1.0F);
        this.bottom.rotateAngleX = 1.5708F;

        this.paddles[0] = paddle(0, 9.0F, -1.001F);
        this.paddles[1] = paddle(40, -9.0F, 0.001F);
        this.paddles[1].rotateAngleY = (float) Math.PI;

        if (withChest) {
            ModelRenderer chestBottom = chestPart(76, 12, 8, 12, -2.0F, -10.0F, -6.0F);
            ModelRenderer lid = chestPart(59, 12, 4, 12, -2.0F, -14.0F, -6.0F);
            ModelRenderer lock = chestPart(59, 2, 4, 1, -1.0F, -11.0F, -1.0F);
            this.chest = new ModelRenderer[]{chestBottom, lid, lock};
        } else {
            this.chest = new ModelRenderer[0];
        }
    }

    private ModelRenderer paddle(int textureX, float z, float bladeX) {
        ModelRenderer paddle = new ModelRenderer(this, textureX, 24);
        paddle.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
        paddle.addBox(bladeX, -3.0F, 8.0F, 1, 6, 7);
        paddle.setRotationPoint(3.0F, -4.0F, z);
        paddle.rotateAngleZ = 0.19634955F;
        return paddle;
    }

    private ModelRenderer chestPart(int textureY, int width, int height, int depth, float x, float y, float z) {
        ModelRenderer part = new ModelRenderer(this, 0, textureY);
        part.addBox(0.0F, 0.0F, 0.0F, width, height, depth);
        part.setRotationPoint(x, y, z);
        part.rotateAngleY = -(float) Math.PI / 2F;
        return part;
    }

    /**
     * The boat renderer passes the partial ticks as the limb swing.
     */
    @Override
    public void render(Entity entity, float partialTicks, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        this.bottom.render(scale);
        for (ModelRenderer part : this.chest) {
            part.render(scale);
        }
        EntityBoat boat = (EntityBoat) entity;
        for (int side = 0; side < 2; side++) {
            // As the paddles of the game's boat and raft swing
            float rowing = boat.getRowingTime(side, partialTicks);
            ModelRenderer paddle = this.paddles[side];
            paddle.rotateAngleX = (float) MathHelper.clampedLerp(-Math.PI / 3.0D, -0.2617993950843811D, (MathHelper.sin(-rowing) + 1.0F) / 2.0F);
            paddle.rotateAngleY = (float) MathHelper.clampedLerp(-Math.PI / 4.0D, Math.PI / 4.0D, (MathHelper.sin(-rowing + 1.0F) + 1.0F) / 2.0F);
            if (side == 1) {
                paddle.rotateAngleY = (float) Math.PI - paddle.rotateAngleY;
            }
            paddle.render(scale);
        }
    }
}
