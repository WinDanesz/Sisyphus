package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockWoodSign;
import com.windanesz.sisyphus.handler.ModEventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class RenderColoredSign extends TileEntitySpecialRenderer<TileEntitySign> {

    private static final ResourceLocation VANILLA_TEXTURE = new ResourceLocation("textures/entity/sign.png");

    private final ModelSign model = new ModelSign();

    private static ResourceLocation textureFor(Block block) {
        String wood = BlockWoodSign.woodOf(block);
        return wood == null ? VANILLA_TEXTURE : new ResourceLocation(SisyphusBackport.MODID, "textures/entity/signs/" + wood + ".png");
    }

    public static int getTextColor(TileEntitySign sign) {
        if (!sign.getTileData().hasKey(ModEventHandler.SIGN_COLOR_KEY)) {
            return 0;
        }
        return EnumDyeColor.byMetadata(sign.getTileData().getInteger(ModEventHandler.SIGN_COLOR_KEY)).getColorValue();
    }

    @Override
    public void render(TileEntitySign sign, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Block block = sign.getBlockType();
        GlStateManager.pushMatrix();
        float scale = 0.6666667F;

        if (block instanceof BlockStandingSign) {
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.75F * scale, (float) z + 0.5F);
            float angle = (float) (sign.getBlockMetadata() * 360) / 16.0F;
            GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
            this.model.signStick.showModel = true;
        } else {
            int meta = sign.getBlockMetadata();
            float angle = 0.0F;
            if (meta == 2) {
                angle = 180.0F;
            }
            if (meta == 4) {
                angle = 90.0F;
            }
            if (meta == 5) {
                angle = -90.0F;
            }
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.75F * scale, (float) z + 0.5F);
            GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
            this.model.signStick.showModel = false;
        }

        if (destroyStage >= 0) {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            this.bindTexture(textureFor(block));
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, -scale, -scale);
        this.model.renderSign();
        GlStateManager.popMatrix();

        FontRenderer font = this.getFontRenderer();
        float textScale = 0.010416667F;
        GlStateManager.translate(0.0F, 0.33333334F * scale, 0.046666667F * scale);
        GlStateManager.scale(textScale, -textScale, textScale);
        GlStateManager.glNormal3f(0.0F, 0.0F, -textScale);
        GlStateManager.depthMask(false);
        int color = RenderColoredSign.getTextColor(sign);

        if (destroyStage < 0) {
            for (int line = 0; line < 4; line++) {
                if (sign.signText[line] != null) {
                    ITextComponent component = sign.signText[line];
                    List<ITextComponent> lines = GuiUtilRenderComponents.splitText(component, 90, font, false, true);
                    String text = lines != null && !lines.isEmpty() ? lines.get(0).getFormattedText() : "";
                    if (line == sign.lineBeingEdited) {
                        text = "> " + text + " <";
                    }
                    font.drawString(text, -font.getStringWidth(text) / 2, line * 10 - sign.signText.length * 5, color);
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
