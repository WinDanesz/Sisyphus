package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockHangingSign;
import com.windanesz.sisyphus.tileentity.TileEntityHangingSign;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class RenderHangingSign extends TileEntitySpecialRenderer<TileEntityHangingSign> {

    private static final float TEXT_SCALE = 0.015625F * 0.9F;

    private final Model model = new Model();
    private final Map<String, ResourceLocation> textures = new HashMap<>();

    private ResourceLocation texture(String wood) {
        return this.textures.computeIfAbsent(wood, w -> new ResourceLocation(SisyphusBackport.MODID, "textures/entity/signs/hanging/" + w + ".png"));
    }

    @Override
    public void render(TileEntityHangingSign sign, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Block block = sign.getBlockType();
        if (!(block instanceof BlockHangingSign) || !sign.hasWorld()) {
            return;
        }
        IBlockState state = sign.getWorld().getBlockState(sign.getPos());
        if (state.getBlock() != block) {
            return;
        }
        float angle;
        if (block instanceof BlockHangingSign.Ceiling) {
            angle = state.getValue(BlockHangingSign.Ceiling.ROTATION) * 22.5F;
            this.model.show(false, sign.isAttached());
        } else {
            angle = state.getValue(BlockHangingSign.Wall.FACING).getHorizontalAngle();
            this.model.show(true, false);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.9375F, (float) z + 0.5F);
        GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -0.3125F, 0.0F);

        if (destroyStage >= 0) {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            this.bindTexture(this.texture(((BlockHangingSign) block).wood));
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0F, -1.0F, -1.0F);
        // The chains are flat, with their links only on the front of the texture, so both sides of each face are drawn
        GlStateManager.disableCull();
        this.model.render();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        if (destroyStage < 0) {
            this.renderText(sign);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }

    private void renderText(TileEntityHangingSign sign) {
        FontRenderer font = this.getFontRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -0.32F, 0.073F);
        GlStateManager.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        GlStateManager.glNormal3f(0.0F, 0.0F, -TEXT_SCALE);
        GlStateManager.depthMask(false);
        int color = RenderColoredSign.getTextColor(sign);
        int top = 4 * TileEntityHangingSign.LINE_HEIGHT / 2;
        for (int line = 0; line < 4; line++) {
            ITextComponent component = sign.signText[line];
            if (component == null) {
                continue;
            }
            List<ITextComponent> lines = GuiUtilRenderComponents.splitText(component, TileEntityHangingSign.MAX_LINE_WIDTH, font, false, true);
            String text = lines != null && !lines.isEmpty() ? lines.get(0).getFormattedText() : "";
            font.drawString(text, -font.getStringWidth(text) / 2, line * TileEntityHangingSign.LINE_HEIGHT - top, color);
        }
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    static final class Model extends ModelBase {

        private final ModelRenderer board;
        private final ModelRenderer plank;
        private final ModelRenderer[] normalChains = new ModelRenderer[4];
        private final ModelRenderer vChains;

        Model() {
            this.textureWidth = 64;
            this.textureHeight = 32;
            this.board = new ModelRenderer(this, 0, 12);
            this.board.addBox(-7.0F, 0.0F, -1.0F, 14, 10, 2);
            this.plank = new ModelRenderer(this, 0, 0);
            this.plank.addBox(-8.0F, -6.0F, -2.0F, 16, 2, 4);
            float quarter = (float) Math.PI / 4.0F;
            float[] sides = {-5.0F, -5.0F, 5.0F, 5.0F};
            float[] turns = {-quarter, quarter, -quarter, quarter};
            int[] u = {0, 6, 0, 6};
            for (int i = 0; i < 4; i++) {
                ModelRenderer chain = new ModelRenderer(this, u[i], 6);
                chain.addBox(-1.5F, 0.0F, 0.0F, 3, 6, 0);
                chain.setRotationPoint(sides[i], -6.0F, 0.0F);
                chain.rotateAngleY = turns[i];
                this.normalChains[i] = chain;
            }
            this.vChains = new ModelRenderer(this, 14, 6);
            this.vChains.addBox(-6.0F, -6.0F, 0.0F, 12, 6, 0);
        }

        void show(boolean wall, boolean attached) {
            this.plank.showModel = wall;
            this.vChains.showModel = !wall && attached;
            for (ModelRenderer chain : this.normalChains) {
                chain.showModel = wall || !attached;
            }
        }

        void render() {
            float scale = 0.0625F;
            this.board.render(scale);
            this.plank.render(scale);
            for (ModelRenderer chain : this.normalChains) {
                chain.render(scale);
            }
            this.vChains.render(scale);
        }
    }
}
