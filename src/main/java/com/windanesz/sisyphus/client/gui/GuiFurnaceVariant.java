package com.windanesz.sisyphus.client.gui;

import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

public class GuiFurnaceVariant extends GuiFurnace {

    private final ResourceLocation texture;
    private final IInventory furnace;

    public GuiFurnaceVariant(InventoryPlayer playerInventory, IInventory furnace, ResourceLocation texture) {
        super(playerInventory, furnace);
        this.furnace = furnace;
        this.texture = texture;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.texture);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        if (TileEntityFurnace.isBurning(this.furnace)) {
            int total = this.furnace.getField(1);
            if (total == 0) {
                total = 200;
            }
            int burn = this.furnace.getField(0) * 13 / total;
            this.drawTexturedModalRect(left + 56, top + 36 + 12 - burn, 176, 12 - burn, 14, burn + 1);
        }

        int cook = this.furnace.getField(2);
        int cookTotal = this.furnace.getField(3);
        int progress = cookTotal != 0 && cook != 0 ? cook * 24 / cookTotal : 0;
        this.drawTexturedModalRect(left + 79, top + 34, 176, 14, progress + 1, 16);
    }
}
