package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockBell;
import com.windanesz.sisyphus.tileentity.TileEntityBell;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderBell extends TileEntitySpecialRenderer<TileEntityBell> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/bell/bell_body.png");

    private final ModelBell model = new ModelBell();

    @Override
    public void render(TileEntityBell bell, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!bell.hasWorld() || !(bell.getWorld().getBlockState(bell.getPos()).getBlock() instanceof BlockBell)) {
            return;
        }

        float angle = 0.0F;
        if (bell.shaking) {
            float ticks = bell.ticks + partialTicks;
            angle = MathHelper.sin(ticks / (float) Math.PI) / (4.0F + ticks / 3.0F);
        }
        float x_rot = 0.0F;
        float z_rot = 0.0F;
        EnumFacing direction = bell.direction;
        if (direction == EnumFacing.NORTH) {
            x_rot = -angle;
        } else if (direction == EnumFacing.SOUTH) {
            x_rot = angle;
        } else if (direction == EnumFacing.EAST) {
            z_rot = -angle;
        } else if (direction == EnumFacing.WEST) {
            z_rot = angle;
        }
        this.model.body.rotateAngleX = x_rot;
        this.model.body.rotateAngleZ = z_rot;

        this.bindTexture(TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 1.0F, (float) z + 1.0F);
        GlStateManager.scale(1.0F, -1.0F, -1.0F);
        GlStateManager.enableRescaleNormal();
        this.model.body.render(0.0625F);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    private static class ModelBell extends ModelBase {

        final ModelRenderer body;

        ModelBell() {
            this.textureWidth = 32;
            this.textureHeight = 32;

            this.body = new ModelRenderer(this, 0, 0);
            this.body.addBox(-3.0F, 0.0F, -3.0F, 6, 7, 6);
            this.body.setRotationPoint(8.0F, 3.0F, 8.0F);

            ModelRenderer lip = new ModelRenderer(this, 0, 13);
            lip.addBox(-4.0F, 7.0F, -4.0F, 8, 2, 8);
            lip.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.body.addChild(lip);
        }
    }
}
