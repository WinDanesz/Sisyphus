package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.block.BlockLectern;
import com.windanesz.sisyphus.tileentity.TileEntityLectern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import org.lwjgl.opengl.GL11;

public class RenderLectern extends TileEntitySpecialRenderer<TileEntityLectern> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private static final ResourceLocation MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");

    private static final float MAP_PAPER = 0.75F;

    private final ModelBook book = new ModelBook();

    @Override
    public void render(TileEntityLectern lectern, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!lectern.hasWorld() || !lectern.hasBook() || !(lectern.getWorld().getBlockState(lectern.getPos()).getBlock() instanceof BlockLectern)) {
            return;
        }
        EnumFacing facing = lectern.getWorld().getBlockState(lectern.getPos()).getValue(BlockLectern.FACING);
        if (lectern.hasMap()) {
            this.renderMap(lectern, facing, x, y, z);
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.0625F, (float) z + 0.5F);
        GlStateManager.rotate(-facing.rotateY().getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(67.5F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.0F, -0.125F, 0.0F);
        this.bindTexture(TEXTURE);
        GlStateManager.enableCull();
        this.book.render(null, 0.0F, 0.1F, 0.9F, 1.2F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }

    private void renderMap(TileEntityLectern lectern, EnumFacing facing, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        // The frame of the block model facing north, and the tilt of its top board
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180.0F - facing.getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-22.5F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);

        // From the corner of the map, x going right and y going down the map, at the middle of the board
        float scale = MAP_PAPER / 142.0F;
        GlStateManager.translate(0.5F + 64.0F * scale, 1.005F, 0.59375F + 64.0F * scale);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // The paper under the map, as when it is held
        this.bindTexture(MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-7.0D, 135.0D, 0.6D).tex(0.0D, 1.0D).endVertex();
        buffer.pos(135.0D, 135.0D, 0.6D).tex(1.0D, 1.0D).endVertex();
        buffer.pos(135.0D, -7.0D, 0.6D).tex(1.0D, 0.0D).endVertex();
        buffer.pos(-7.0D, -7.0D, 0.6D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();

        MapData data = Items.FILLED_MAP.getMapData(lectern.getBook(), lectern.getWorld());
        if (data != null) {
            Minecraft.getMinecraft().entityRenderer.getMapItemRenderer().renderMap(data, true);
        }

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
