package com.windanesz.sisyphus.client.gui;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.render.RenderColoredSign;
import com.windanesz.sisyphus.tileentity.TileEntityHangingSign;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiEditHangingSign extends GuiScreen {

    private final BlockPos pos;
    private final ResourceLocation texture;
    private final String[] lines = {"", "", "", ""};
    private int line;
    private int ticks;
    private GuiButton done;

    public GuiEditHangingSign(BlockPos pos, String wood) {
        this.pos = pos;
        this.texture = new ResourceLocation(SisyphusBackport.MODID, "textures/gui/hanging_signs/" + wood + ".png");
    }

    @Nullable
    private TileEntitySign sign() {
        TileEntity tile = this.mc.world == null ? null : this.mc.world.getTileEntity(this.pos);
        return tile instanceof TileEntitySign ? (TileEntitySign) tile : null;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        this.done = this.addButton(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 144, I18n.format("gui.done")));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        NetHandlerPlayClient connection = this.mc.getConnection();
        if (connection != null) {
            ITextComponent[] text = new ITextComponent[4];
            for (int i = 0; i < 4; i++) {
                text[i] = new TextComponentString(this.lines[i]);
            }
            connection.sendPacket(new CPacketUpdateSign(this.pos, text));
        }
    }

    @Override
    public void updateScreen() {
        this.ticks++;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled && button.id == 0) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_UP) {
            this.line = this.line - 1 & 3;
        }
        if (keyCode == Keyboard.KEY_DOWN || keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            this.line = this.line + 1 & 3;
        }
        String text = this.lines[this.line];
        if (keyCode == Keyboard.KEY_BACK && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
        }
        if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && this.fontRenderer.getStringWidth(text + typedChar) <= TileEntityHangingSign.MAX_LINE_WIDTH) {
            text = text + typedChar;
        }
        this.lines[this.line] = text;
        TileEntitySign sign = this.sign();
        if (sign != null) {
            sign.signText[this.line] = new TextComponentString(text);
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.actionPerformed(this.done);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("hanging_sign.edit"), this.width / 2, 40, 0xFFFFFF);

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2.0F, 125.0F, 50.0F);
        // The sign, four and a half times as large as its 16 pixels
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -13.0F, 0.0F);
        GlStateManager.scale(4.5F, 4.5F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.texture);
        drawModalRectWithCustomSizedTexture(-8, -8, 0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
        GlStateManager.popMatrix();

        GlStateManager.translate(0.0F, 0.0F, 4.0F);
        TileEntitySign sign = this.sign();
        int color = sign == null ? 0 : RenderColoredSign.getTextColor(sign);
        int height = TileEntityHangingSign.LINE_HEIGHT;
        int top = 4 * height / 2;
        boolean cursor = this.ticks / 6 % 2 == 0;
        for (int i = 0; i < 4; i++) {
            String text = this.lines[i];
            int width = this.fontRenderer.getStringWidth(text);
            int y = i * height - top;
            this.fontRenderer.drawString(text, -width / 2, y, color);
            if (i == this.line && cursor) {
                this.fontRenderer.drawString("_", width - width / 2, y, color);
            }
        }
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
