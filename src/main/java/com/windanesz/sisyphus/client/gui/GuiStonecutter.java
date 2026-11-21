package com.windanesz.sisyphus.client.gui;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.inventory.ContainerStonecutter;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.List;

public class GuiStonecutter extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/gui/container/stonecutter.png");
    private static final int COLUMNS = 4;
    private static final int ROWS = 3;
    private static final int BUTTON_WIDTH = 16;
    private static final int BUTTON_HEIGHT = 18;
    private static final int GRID_LEFT = 52;
    private static final int GRID_TOP = 14;

    private final ContainerStonecutter container;

    public GuiStonecutter(InventoryPlayer playerInventory, World world, BlockPos pos) {
        super(new ContainerStonecutter(playerInventory, world, pos));
        this.container = (ContainerStonecutter) this.inventorySlots;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.stonecutter"), 8, 4, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int left = this.guiLeft;
        int top = this.guiTop;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        // The scroll bar is only decoration, as there is never more than one page
        this.drawTexturedModalRect(left + 119, top + 15, 176 + 12, 0, 12, 15);

        List<ItemStack> results = this.container.getResults();
        int count = Math.min(results.size(), COLUMNS * ROWS);
        for (int i = 0; i < count; i++) {
            int x = left + GRID_LEFT + (i % COLUMNS) * BUTTON_WIDTH;
            int y = top + GRID_TOP + (i / COLUMNS) * BUTTON_HEIGHT + 2;
            int v = this.ySize;
            if (i == this.container.getSelected()) {
                v += BUTTON_HEIGHT;
            } else if (mouseX >= x && mouseY >= y && mouseX < x + BUTTON_WIDTH && mouseY < y + BUTTON_HEIGHT) {
                v += BUTTON_HEIGHT * 2;
            }
            this.mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(x, y - 1, 0, v, BUTTON_WIDTH, BUTTON_HEIGHT);
        }

        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < count; i++) {
            int x = left + GRID_LEFT + (i % COLUMNS) * BUTTON_WIDTH;
            int y = top + GRID_TOP + (i / COLUMNS) * BUTTON_HEIGHT + 2;
            this.itemRender.renderItemAndEffectIntoGUI(results.get(i), x, y + 1);
            this.itemRender.renderItemOverlays(this.fontRenderer, results.get(i), x, y + 1);
        }
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int count = Math.min(this.container.getResults().size(), COLUMNS * ROWS);
        for (int i = 0; i < count; i++) {
            int x = this.guiLeft + GRID_LEFT + (i % COLUMNS) * BUTTON_WIDTH;
            int y = this.guiTop + GRID_TOP + (i / COLUMNS) * BUTTON_HEIGHT + 2;
            if (mouseX >= x && mouseY >= y && mouseX < x + BUTTON_WIDTH && mouseY < y + BUTTON_HEIGHT) {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.mc.playerController.sendEnchantPacket(this.container.windowId, i);
                return;
            }
        }
    }
}
